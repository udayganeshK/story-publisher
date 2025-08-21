#!/bin/bash

# Fast deployment with pre-built JAR
set -e

INSTANCE_IP="18.208.224.81"
KEY_FILE="story-publisher-key.pem"

echo "🚀 Fast deployment with pre-built JAR..."

echo "📤 Copying JAR to EC2 instance..."
scp -i "$KEY_FILE" -o StrictHostKeyChecking=no backend/target/story-publisher-api-0.0.1-SNAPSHOT.jar ubuntu@$INSTANCE_IP:~/StoryPublisher/backend/app.jar

echo "🐳 Starting deployment on instance..."
ssh -i "$KEY_FILE" -o StrictHostKeyChecking=no ubuntu@$INSTANCE_IP << 'REMOTE_EOF'
cd StoryPublisher

# Stop any running containers
docker-compose down || true

# Create simplified backend Dockerfile
cat > backend/Dockerfile << 'DOCKER_EOF'
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
COPY app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
DOCKER_EOF

# Fix frontend package.json to include package-lock
cd frontend
npm install --package-lock-only
cd ..

echo "✅ Files prepared, starting containers..."
docker-compose up -d --build

# Wait for containers
sleep 30

echo "📊 Container status:"
docker-compose ps

echo "📝 Recent logs:"
docker-compose logs --tail=10
REMOTE_EOF

echo "✅ Deployment completed!"
echo "🌐 Frontend: http://$INSTANCE_IP:3000"
echo "🔧 Backend: http://$INSTANCE_IP:8080/api"
