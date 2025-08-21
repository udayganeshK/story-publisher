#!/bin/bash
apt-get update
apt-get install -y docker.io docker-compose git

# Add ubuntu user to docker group
usermod -aG docker ubuntu

# Install Docker Compose v2
curl -L "https://github.com/docker/compose/releases/download/v2.21.0/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose

# Start Docker
systemctl start docker
systemctl enable docker

# Create application directory
mkdir -p /opt/storypublisher
chown ubuntu:ubuntu /opt/storypublisher

# Clone repository (you'll need to do this manually after instance creation)
echo "Ready for Story Publisher deployment!" > /opt/storypublisher/README.txt
