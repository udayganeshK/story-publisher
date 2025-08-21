#!/bin/bash

# Fix SSM Agent on Ubuntu EC2 instance
# This script fixes common SSM Agent issues

set -e

INSTANCE_IP="18.208.224.81"
KEY_FILE="story-publisher-key.pem"

echo "🔧 Fixing SSM Agent on EC2 instance..."

# Connect to instance and fix SSM Agent
ssh -i "$KEY_FILE" -o StrictHostKeyChecking=no ubuntu@$INSTANCE_IP << 'EOF'
echo "📋 Checking current SSM Agent status..."
sudo systemctl status amazon-ssm-agent --no-pager || true

echo ""
echo "🛠️ Fixing SSM Agent..."

# Update package list
sudo apt-get update -y

# Install/reinstall SSM Agent
echo "Installing SSM Agent..."
sudo snap install amazon-ssm-agent --classic || {
    echo "Snap install failed, trying manual installation..."
    
    # Download and install SSM Agent manually
    cd /tmp
    wget https://s3.amazonaws.com/ec2-downloads-windows/SSMAgent/latest/debian_amd64/amazon-ssm-agent.deb
    sudo dpkg -i amazon-ssm-agent.deb
    sudo apt-get install -f -y
}

# Enable and start the service
echo "Enabling and starting SSM Agent..."
sudo systemctl enable amazon-ssm-agent
sudo systemctl start amazon-ssm-agent

# Wait a moment for the service to start
sleep 5

# Check status
echo ""
echo "✅ Checking SSM Agent status after fix..."
sudo systemctl status amazon-ssm-agent --no-pager

# Check if agent is registered
echo ""
echo "🔍 Checking SSM Agent registration..."
sudo /snap/amazon-ssm-agent/current/amazon-ssm-agent -register -code "$(curl -s http://169.254.169.254/latest/meta-data/instance-id)" -id "$(curl -s http://169.254.169.254/latest/meta-data/instance-id)" -region "$(curl -s http://169.254.169.254/latest/meta-data/placement/region)" || true

echo ""
echo "🎯 SSM Agent fix complete!"
echo "It may take a few minutes for the instance to appear in Systems Manager console."
EOF

echo ""
echo "✅ SSM Agent fix completed!"
echo "💡 Alternative: You can also continue using SSH for deployment:"
echo "   ssh -i $KEY_FILE ubuntu@$INSTANCE_IP"
