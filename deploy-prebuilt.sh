#!/bin/bash

# Handle Maven download issues and provide alternatives
# For slow Maven builds on t3.nano instances

set -e

INSTANCE_IP="18.208.224.81"
KEY_FILE="story-publisher-key.pem"

echo "🚀 Handling Maven download issues..."

# Option 1: Build locally and copy JAR
echo "Option 1: Building locally and copying to instance..."
echo "This will be much faster than building on t3.nano"

# Build the backend locally
echo "📦 Building Spring Boot application locally..."
cd backend
mvn clean package -DskipTests
cd ..

echo "📤 Copying JAR to EC2 instance..."
scp -i "$KEY_FILE" -o StrictHostKeyChecking=no backend/target/*.jar ubuntu@$INSTANCE_IP:~/StoryPublisher/backend/

# Create a simplified Dockerfile that uses the pre-built JAR
echo "🐳 Creating simplified Dockerfile on instance..."
ssh -i "$KEY_FILE" -o StrictHostKeyChecking=no ubuntu@$INSTANCE_IP << 'EOF'
cd StoryPublisher

# Stop any running containers
docker-compose down || true

# Create simplified backend Dockerfile
cat > backend/Dockerfile << 'DOCKER_EOF'
FROM openjdk:17-jre-slim

WORKDIR /app

# Copy the pre-built JAR file
COPY *.jar app.jar

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
DOCKER_EOF

echo "✅ Simplified Dockerfile created!"
EOF

echo ""
echo "🔄 Now rebuilding with pre-built JAR..."
ssh -i "$KEY_FILE" -o StrictHostKeyChecking=no ubuntu@$INSTANCE_IP << 'EOF'
cd StoryPublisher

# Rebuild and start services
docker-compose up -d --build

echo ""
echo "🎯 Checking container status..."
docker-compose ps

echo ""
echo "📊 Checking logs..."
docker-compose logs --tail=10
EOF

echo ""
echo "✅ Deployment with pre-built JAR completed!"
echo "🌐 Frontend: http://$INSTANCE_IP:3000"
echo "🔧 Backend: http://$INSTANCE_IP:8080/api"
