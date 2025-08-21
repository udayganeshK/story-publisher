#!/bin/bash

# Complete Story Publisher Deployment with Frontend
# Deploy the entire application (backend + frontend + database)

set -e

INSTANCE_IP="54.221.140.224"
KEY_FILE="story-publisher-key.pem"

echo "🚀 Deploying Complete Story Publisher Application..."

# Step 1: Build backend locally
echo "📦 Building backend locally..."
cd backend
mvn clean package -DskipTests
echo "✅ Backend build completed"
cd ..

# Step 2: Clean up EC2 instance
echo "🧹 Cleaning up EC2 instance..."
ssh -i "$KEY_FILE" -o StrictHostKeyChecking=no ubuntu@$INSTANCE_IP << 'EOF'
# Stop containers and clean up
docker stop $(docker ps -aq) 2>/dev/null || true
docker rm $(docker ps -aq) 2>/dev/null || true
docker system prune -af 2>/dev/null || true

# Remove old files
rm -rf StoryPublisher
echo "✅ Cleanup completed"
EOF

# Step 3: Create project structure and copy files
echo "📁 Creating project structure..."
ssh -i "$KEY_FILE" -o StrictHostKeyChecking=no ubuntu@$INSTANCE_IP 'mkdir -p StoryPublisher/{backend,frontend}'

echo "📤 Copying application files..."

# Copy backend JAR
echo "  - Copying backend JAR..."
scp -i "$KEY_FILE" -o StrictHostKeyChecking=no backend/target/story-publisher-api-0.0.1-SNAPSHOT.jar ubuntu@$INSTANCE_IP:~/StoryPublisher/backend/app.jar

# Copy frontend files (excluding node_modules and .next)
echo "  - Copying frontend source..."
rsync -avz --progress -e "ssh -i $KEY_FILE -o StrictHostKeyChecking=no" \
  --exclude 'node_modules' \
  --exclude '.next' \
  --exclude '.env.local' \
  frontend/ ubuntu@$INSTANCE_IP:~/StoryPublisher/frontend/

# Step 4: Create deployment configuration
echo "📝 Creating deployment configuration..."
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

# Frontend Configuration
NEXT_PUBLIC_API_URL=http://54.221.140.224:8080/api
ENV_EOF

# Create backend Dockerfile
cat > backend/Dockerfile << 'BACKEND_DOCKERFILE'
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
COPY app.jar app.jar
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
BACKEND_DOCKERFILE

# Create frontend Dockerfile
cat > frontend/Dockerfile << 'FRONTEND_DOCKERFILE'
FROM node:18-alpine AS deps
WORKDIR /app
COPY package*.json ./
RUN npm ci

FROM node:18-alpine AS builder
WORKDIR /app
COPY --from=deps /app/node_modules ./node_modules
COPY . .
RUN npm run build

FROM node:18-alpine AS runner
WORKDIR /app
ENV NODE_ENV production

RUN addgroup --system --gid 1001 nodejs
RUN adduser --system --uid 1001 nextjs

COPY --from=builder /app/next.config.* ./
COPY --from=builder /app/public ./public
COPY --from=builder /app/package.json ./package.json
COPY --from=builder --chown=nextjs:nodejs /app/.next/standalone ./
COPY --from=builder --chown=nextjs:nodejs /app/.next/static ./.next/static

USER nextjs
EXPOSE 3000
ENV PORT 3000

CMD ["node", "server.js"]
FRONTEND_DOCKERFILE

# Create docker-compose.yml for the complete application
cat > docker-compose.yml << 'COMPOSE_EOF'
version: '3.8'

services:
  postgres:
    image: postgres:13-alpine
    container_name: story-db
    environment:
      POSTGRES_DB: ${POSTGRES_DB}
      POSTGRES_USER: ${POSTGRES_USER}
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
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
    container_name: story-backend
    environment:
      SPRING_PROFILES_ACTIVE: docker
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/${POSTGRES_DB}
      SPRING_DATASOURCE_USERNAME: ${POSTGRES_USER}
      SPRING_DATASOURCE_PASSWORD: ${POSTGRES_PASSWORD}
      JWT_SECRET: ${JWT_SECRET}
      JWT_EXPIRATION: ${JWT_EXPIRATION}
      SPRING_JPA_HIBERNATE_DDL_AUTO: update
    ports:
      - "8080:8080"
    depends_on:
      postgres:
        condition: service_healthy
    networks:
      - story-network
    restart: unless-stopped

  frontend:
    build: ./frontend
    container_name: story-frontend
    environment:
      NEXT_PUBLIC_API_URL: ${NEXT_PUBLIC_API_URL}
    ports:
      - "3000:3000"
    depends_on:
      - backend
    networks:
      - story-network
    restart: unless-stopped

volumes:
  postgres_data:

networks:
  story-network:
    driver: bridge
COMPOSE_EOF

echo "✅ Configuration files created"
ls -la
EOF

# Step 5: Deploy the complete application
echo "🚀 Starting complete application deployment..."
ssh -i "$KEY_FILE" -o StrictHostKeyChecking=no ubuntu@$INSTANCE_IP << 'EOF'
cd StoryPublisher

echo "Building and starting all services..."
docker-compose up -d --build

echo "Waiting for services to start..."
sleep 45

echo ""
echo "📊 Container Status:"
docker-compose ps

echo ""
echo "📋 Service Health:"
echo "=== DATABASE ==="
docker-compose logs postgres | tail -3

echo ""
echo "=== BACKEND ==="
docker-compose logs backend | tail -5

echo ""
echo "=== FRONTEND ==="
docker-compose logs frontend | tail -5

echo ""
echo "🧪 Testing Services..."
sleep 10

# Test backend
echo "Testing backend API..."
curl -s http://localhost:8080/api/stories/public > /dev/null && echo "✅ Backend API working" || echo "❌ Backend API not ready"

# Test frontend
echo "Testing frontend..."
curl -s http://localhost:3000 > /dev/null && echo "✅ Frontend working" || echo "❌ Frontend not ready"

echo ""
echo "🎯 Deployment completed!"
EOF

echo ""
echo "🎉 Complete Story Publisher Deployment Finished!"
echo ""
echo "🌐 Your full application is now live:"
echo "   📱 Frontend (Next.js): http://$INSTANCE_IP:3000"
echo "   🔧 Backend API: http://$INSTANCE_IP:8080/api"
echo "   📚 Stories API: http://$INSTANCE_IP:8080/api/stories/public"
echo "   🔐 Auth API: http://$INSTANCE_IP:8080/api/auth/login"
echo ""
echo "🎯 Features Available:"
echo "   • User Registration & Login"
echo "   • Story Creation & Management"
echo "   • Story Publishing & Sharing"
echo "   • Public Story Discovery"
echo "   • Personal Dashboard"
echo ""
echo "📊 Management Commands:"
echo "   Status: ssh -i $KEY_FILE ubuntu@$INSTANCE_IP 'cd StoryPublisher && docker-compose ps'"
echo "   Logs: ssh -i $KEY_FILE ubuntu@$INSTANCE_IP 'cd StoryPublisher && docker-compose logs'"
echo "   Restart: ssh -i $KEY_FILE ubuntu@$INSTANCE_IP 'cd StoryPublisher && docker-compose restart'"
