#!/bin/bash

# Clean EC2 deployment script for Story Publisher
# This script will clean up and redeploy everything from scratch

set -e

INSTANCE_IP="54.221.140.224"
KEY_FILE="story-publisher-key.pem"

echo "🧹 Starting clean deployment to EC2..."

# Step 1: Build the application locally
echo "📦 Building Spring Boot application locally..."
cd backend
mvn clean package -DskipTests
echo "✅ Local build completed"
cd ..

# Step 2: Clean up EC2 instance
echo "🗑️ Cleaning up EC2 instance..."
ssh -i "$KEY_FILE" -o StrictHostKeyChecking=no ubuntu@$INSTANCE_IP << 'EOF'
echo "Stopping all Docker containers..."
docker stop $(docker ps -aq) 2>/dev/null || true
docker rm $(docker ps -aq) 2>/dev/null || true
docker system prune -af 2>/dev/null || true

echo "Removing old project files..."
rm -rf StoryPublisher story-publisher
rm -rf .env docker-compose.yml

echo "✅ EC2 cleanup completed"
EOF

# Step 3: Copy application files
echo "📤 Copying application files to EC2..."

# Create project structure on EC2
ssh -i "$KEY_FILE" -o StrictHostKeyChecking=no ubuntu@$INSTANCE_IP 'mkdir -p StoryPublisher/backend'

# Copy the built JAR
echo "Copying JAR file..."
scp -i "$KEY_FILE" -o StrictHostKeyChecking=no backend/target/story-publisher-api-0.0.1-SNAPSHOT.jar ubuntu@$INSTANCE_IP:~/StoryPublisher/backend/app.jar

# Step 4: Create deployment files on EC2
echo "📝 Creating deployment files on EC2..."
ssh -i "$KEY_FILE" -o StrictHostKeyChecking=no ubuntu@$INSTANCE_IP << 'EOF'
cd StoryPublisher

# Create .env file
cat > .env << 'ENV_EOF'
# Database Configuration
POSTGRES_DB=storypublisher
POSTGRES_USER=storypublisher
POSTGRES_PASSWORD=storypublisher123

# JWT Configuration
JWT_SECRET=mySecretKey123456789012345678901234567890
JWT_EXPIRATION=86400000

# Application Configuration
SERVER_PORT=8080
ENV_EOF

# Create backend Dockerfile
cat > backend/Dockerfile << 'DOCKER_EOF'
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Copy the pre-built JAR file
COPY app.jar app.jar

# Expose port
EXPOSE 8080

# Add health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Run the application
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
DOCKER_EOF

# Create docker-compose.yml
cat > docker-compose.yml << 'COMPOSE_EOF'
version: '3.8'

services:
  postgres:
    image: postgres:13-alpine
    container_name: story-publisher-db
    environment:
      POSTGRES_DB: ${POSTGRES_DB}
      POSTGRES_USER: ${POSTGRES_USER}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./init.sql:/docker-entrypoint-initdb.d/init.sql
    networks:
      - story-network
    restart: unless-stopped
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U ${POSTGRES_USER} -d ${POSTGRES_DB}"]
      interval: 30s
      timeout: 10s
      retries: 3

  backend:
    build: ./backend
    container_name: story-publisher-backend
    environment:
      SPRING_PROFILES_ACTIVE: docker
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/${POSTGRES_DB}
      SPRING_DATASOURCE_USERNAME: ${POSTGRES_USER}
      SPRING_DATASOURCE_PASSWORD: ${POSTGRES_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
      JWT_EXPIRATION: ${JWT_EXPIRATION}
      SPRING_JPA_HIBERNATE_DDL_AUTO: update
      SPRING_JPA_SHOW_SQL: false
      LOGGING_LEVEL_COM_STORYPUBLISHER: INFO
    ports:
      - "8080:8080"
    depends_on:
      postgres:
        condition: service_healthy
    networks:
      - story-network
    restart: unless-stopped

volumes:
  postgres_data:

networks:
  story-network:
    driver: bridge
COMPOSE_EOF

# Create a simple database initialization script
cat > init.sql << 'SQL_EOF'
-- Create database if not exists
SELECT 'CREATE DATABASE storypublisher' WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'storypublisher');
SQL_EOF

echo "✅ All deployment files created!"
ls -la
EOF

# Step 5: Deploy the application
echo "🚀 Starting application deployment..."
ssh -i "$KEY_FILE" -o StrictHostKeyChecking=no ubuntu@$INSTANCE_IP << 'EOF'
cd StoryPublisher

echo "Building and starting services..."
docker-compose up -d --build

echo "Waiting for services to start..."
sleep 30

echo ""
echo "📊 Container status:"
docker-compose ps

echo ""
echo "📋 Service logs:"
echo "=== DATABASE LOGS ==="
docker-compose logs postgres | tail -5

echo ""
echo "=== BACKEND LOGS ==="
docker-compose logs backend | tail -10

echo ""
echo "🧪 Testing application..."
sleep 10

# Test database connection
echo "Testing database connection..."
docker-compose exec -T postgres psql -U storypublisher -d storypublisher -c "\dt" || echo "Database not ready yet"

# Test backend health
echo "Testing backend API..."
curl -s http://localhost:8080/api/stories/public || echo "Backend API not ready yet"

echo ""
echo "🎯 Deployment completed!"
EOF

echo ""
echo "✅ Clean deployment finished!"
echo ""
echo "🌐 Your application should be available at:"
echo "   Backend API: http://$INSTANCE_IP:8080/api"
echo "   Stories API: http://$INSTANCE_IP:8080/api/stories/public"
echo "   Auth API: http://$INSTANCE_IP:8080/api/auth/login"
echo ""
echo "📊 To check status: ssh -i $KEY_FILE ubuntu@$INSTANCE_IP 'cd StoryPublisher && docker-compose ps'"
echo "📝 To view logs: ssh -i $KEY_FILE ubuntu@$INSTANCE_IP 'cd StoryPublisher && docker-compose logs'"
echo "🔄 To restart: ssh -i $KEY_FILE ubuntu@$INSTANCE_IP 'cd StoryPublisher && docker-compose restart'"
