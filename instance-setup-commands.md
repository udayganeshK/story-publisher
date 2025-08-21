# Manual Setup Commands for EC2 Instance
# Run these commands after SSH'ing into your instance

# === STEP 1: Wait for instance to fully initialize ===
# Sometimes EC2 instances need 5-10 minutes to fully boot
# You can check by running: ssh -i story-publisher-key.pem ec2-user@18.208.224.81

# === STEP 2: Once connected, run these commands ===

# Update system
sudo yum update -y

# Install Docker
sudo yum install -y docker
sudo service docker start
sudo usermod -a -G docker ec2-user

# Install Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Install Git
sudo yum install -y git

# Get your public IP
PUBLIC_IP=$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4)
echo "Your public IP is: $PUBLIC_IP"

# Clone your repository
git clone https://github.com/udayganeshK/story-publisher.git
cd story-publisher

# Create environment file
cat > .env << EOF
POSTGRES_DB=storypublisher
POSTGRES_USER=storypublisher
POSTGRES_PASSWORD=securepassword123
JWT_SECRET=StoryPublisherSecretKeyForJWTTokenGenerationAndValidation2025
CORS_ORIGINS=http://${PUBLIC_IP}:3000,http://localhost:3000
NEXT_PUBLIC_API_URL=http://${PUBLIC_IP}:8080/api
EOF

# Logout and login again (to refresh Docker group)
exit

# === STEP 3: After logging back in ===
# ssh -i story-publisher-key.pem ec2-user@18.208.224.81
cd story-publisher

# Deploy the application
docker-compose -f docker-compose.simple.yml up -d

# Check status
docker-compose ps

# View logs if needed
docker-compose logs -f

# === STEP 4: Access your application ===
# Frontend: http://18.208.224.81:3000
# Backend: http://18.208.224.81:8080/api
# Health: http://18.208.224.81:8080/api/actuator/health

# === Troubleshooting Commands ===
# Check if containers are running: docker ps
# View all logs: docker-compose logs
# Restart application: docker-compose restart
# Stop application: docker-compose down
# Check disk space: df -h
# Check memory: free -h
