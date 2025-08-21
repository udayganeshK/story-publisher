#!/bin/bash

# Setup script to run on EC2 instance
# This installs all dependencies and deploys the Story Publisher app

set -e

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

print_status() { echo -e "${BLUE}[INFO]${NC} $1"; }
print_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }

echo "🚀 Setting up Story Publisher on EC2 instance"
echo "=============================================="

# Step 1: Update system
print_status "Step 1: Updating system packages..."
sudo yum update -y

# Step 2: Install Docker
print_status "Step 2: Installing Docker..."
sudo yum install -y docker
sudo service docker start
sudo usermod -a -G docker ec2-user

# Step 3: Install Docker Compose
print_status "Step 3: Installing Docker Compose..."
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Step 4: Install Git
print_status "Step 4: Installing Git..."
sudo yum install -y git

# Step 5: Clone repository
print_status "Step 5: Cloning Story Publisher repository..."
cd /home/ec2-user
git clone https://github.com/udayganeshK/story-publisher.git
cd story-publisher

# Step 6: Create environment file
print_status "Step 6: Creating environment configuration..."
PUBLIC_IP=$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4)

cat > .env << EOF
# Story Publisher Environment Configuration
POSTGRES_DB=storypublisher
POSTGRES_USER=storypublisher
POSTGRES_PASSWORD=securepassword123
JWT_SECRET=StoryPublisherSecretKeyForJWTTokenGenerationAndValidation2025
CORS_ORIGINS=http://${PUBLIC_IP}:3000,http://localhost:3000
NEXT_PUBLIC_API_URL=http://${PUBLIC_IP}:8080/api
EOF

print_success "Environment file created with IP: $PUBLIC_IP"

# Step 7: Start Docker service and add user to group
print_status "Step 7: Configuring Docker permissions..."
sudo systemctl enable docker
sudo systemctl start docker

print_success "✅ Setup complete!"

echo ""
echo "📋 Next Steps:"
echo "1. Log out and log back in (to refresh Docker group membership)"
echo "2. Run: cd story-publisher"
echo "3. Run: docker-compose -f docker-compose.simple.yml up -d"
echo ""
echo "🌐 Your app will be available at:"
echo "   Frontend: http://$PUBLIC_IP:3000"
echo "   Backend: http://$PUBLIC_IP:8080/api"
echo ""
echo "💡 Commands to remember:"
echo "   View logs: docker-compose logs -f"
echo "   Stop app: docker-compose down"
echo "   Restart: docker-compose restart"
