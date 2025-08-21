#!/bin/bash

# Simple and fast deployment script
# Avoids hanging SSH sessions by using shorter commands

set -e

INSTANCE_IP="34.201.50.63"
KEY_FILE="story-publisher-key.pem"

echo "🚀 Simple Story Publisher Deployment"

# Step 1: Build locally (already done, skip if JAR exists)
if [ ! -f "backend/target/story-publisher-api-0.0.1-SNAPSHOT.jar" ]; then
    echo "📦 Building application..."
    cd backend && mvn clean package -DskipTests && cd ..
fi

echo "✅ Using existing JAR file"

# Step 2: Quick connectivity test
echo "🔗 Testing connection..."
ssh -i "$KEY_FILE" -o ConnectTimeout=10 -o StrictHostKeyChecking=no ubuntu@$INSTANCE_IP 'echo "Connected!"'

# Step 3: Simple cleanup (one command at a time)
echo "🧹 Quick cleanup..."
ssh -i "$KEY_FILE" -o ConnectTimeout=10 -o StrictHostKeyChecking=no ubuntu@$INSTANCE_IP 'docker stop $(docker ps -aq) || true'
ssh -i "$KEY_FILE" -o ConnectTimeout=10 -o StrictHostKeyChecking=no ubuntu@$INSTANCE_IP 'docker rm $(docker ps -aq) || true'
ssh -i "$KEY_FILE" -o ConnectTimeout=10 -o StrictHostKeyChecking=no ubuntu@$INSTANCE_IP 'rm -rf StoryPublisher || true'

# Step 4: Create directory
echo "📁 Creating project directory..."
ssh -i "$KEY_FILE" -o ConnectTimeout=10 -o StrictHostKeyChecking=no ubuntu@$INSTANCE_IP 'mkdir -p StoryPublisher/backend'

# Step 5: Copy JAR
echo "📤 Copying application..."
scp -i "$KEY_FILE" -o ConnectTimeout=10 -o StrictHostKeyChecking=no backend/target/story-publisher-api-0.0.1-SNAPSHOT.jar ubuntu@$INSTANCE_IP:~/StoryPublisher/backend/app.jar

# Step 6: Create files (simple approach)
echo "📝 Creating deployment files..."

# Create Dockerfile locally and copy
cat > temp-dockerfile << 'DOCKER_EOF'
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
DOCKER_EOF

scp -i "$KEY_FILE" -o StrictHostKeyChecking=no temp-dockerfile ubuntu@$INSTANCE_IP:~/StoryPublisher/backend/Dockerfile
rm temp-dockerfile

# Create docker-compose locally and copy
cat > temp-compose.yml << 'COMPOSE_EOF'
version: '3.8'
services:
  postgres:
    image: postgres:13-alpine
    container_name: story-db
    environment:
      POSTGRES_DB: storypublisher
      POSTGRES_USER: storypublisher
      POSTGRES_PASSWORD: storypublisher123
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  backend:
    build: ./backend
    container_name: story-backend
    environment:
      SPRING_PROFILES_ACTIVE: docker
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/storypublisher
      SPRING_DATASOURCE_USERNAME: storypublisher
      SPRING_DATASOURCE_PASSWORD: storypublisher123
      JWT_SECRET: mySecretKey123456789012345678901234567890
      JWT_EXPIRATION: 86400000
      SPRING_JPA_HIBERNATE_DDL_AUTO: update
    ports:
      - "8080:8080"
    depends_on:
      - postgres
    restart: unless-stopped

volumes:
  postgres_data:
COMPOSE_EOF

scp -i "$KEY_FILE" -o StrictHostKeyChecking=no temp-compose.yml ubuntu@$INSTANCE_IP:~/StoryPublisher/docker-compose.yml
rm temp-compose.yml

# Step 7: Start deployment
echo "🚀 Starting containers..."
ssh -i "$KEY_FILE" -o ConnectTimeout=30 -o StrictHostKeyChecking=no ubuntu@$INSTANCE_IP 'cd StoryPublisher && docker-compose up -d --build'

# Step 8: Wait and check
echo "⏳ Waiting for containers to start (30 seconds)..."
sleep 30

echo "📊 Checking status..."
ssh -i "$KEY_FILE" -o ConnectTimeout=10 -o StrictHostKeyChecking=no ubuntu@$INSTANCE_IP 'cd StoryPublisher && docker-compose ps'

echo ""
echo "✅ Deployment completed!"
echo "🌐 Your app should be available at:"
echo "   http://$INSTANCE_IP:8080"
echo "   http://$INSTANCE_IP:8080/actuator/health"
