#!/bin/bash

# Simple AWS EC2 Deployment Script for Story Publisher
# Cost: ~$10-15/month with t3.micro instance

set -e

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

print_status() { echo -e "${YELLOW}[INFO]${NC} $1"; }
print_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
print_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# Configuration
INSTANCE_TYPE="t3.nano"  # $3.50/month - perfect for portfolio
REGION="us-east-1"
KEY_NAME="storypublisher-key"
SECURITY_GROUP="storypublisher-sg"

print_status "🚀 Starting budget-friendly AWS deployment..."

# Check AWS CLI
if ! command -v aws &> /dev/null; then
    print_error "AWS CLI not found. Please install it first."
    exit 1
fi

# Get default VPC
VPC_ID=$(aws ec2 describe-vpcs --filters "Name=is-default,Values=true" --query 'Vpcs[0].VpcId' --output text --region $REGION)
if [ "$VPC_ID" = "None" ]; then
    print_error "No default VPC found. Please create one first."
    exit 1
fi

print_success "Using VPC: $VPC_ID"

# Create key pair if it doesn't exist
if ! aws ec2 describe-key-pairs --key-names $KEY_NAME --region $REGION &>/dev/null; then
    print_status "Creating EC2 key pair..."
    aws ec2 create-key-pair --key-name $KEY_NAME --query 'KeyMaterial' --output text --region $REGION > ${KEY_NAME}.pem
    chmod 400 ${KEY_NAME}.pem
    print_success "Key pair created: ${KEY_NAME}.pem"
else
    print_status "Key pair $KEY_NAME already exists"
fi

# Create security group if it doesn't exist
if ! aws ec2 describe-security-groups --filters "Name=group-name,Values=$SECURITY_GROUP" --region $REGION &>/dev/null; then
    print_status "Creating security group..."
    SECURITY_GROUP_ID=$(aws ec2 create-security-group \
        --group-name $SECURITY_GROUP \
        --description "Security group for Story Publisher" \
        --vpc-id $VPC_ID \
        --region $REGION \
        --query 'GroupId' --output text)
    
    # Add rules
    aws ec2 authorize-security-group-ingress \
        --group-id $SECURITY_GROUP_ID \
        --protocol tcp --port 22 --cidr 0.0.0.0/0 \
        --region $REGION
    
    aws ec2 authorize-security-group-ingress \
        --group-id $SECURITY_GROUP_ID \
        --protocol tcp --port 80 --cidr 0.0.0.0/0 \
        --region $REGION
    
    aws ec2 authorize-security-group-ingress \
        --group-id $SECURITY_GROUP_ID \
        --protocol tcp --port 443 --cidr 0.0.0.0/0 \
        --region $REGION
    
    print_success "Security group created: $SECURITY_GROUP_ID"
else
    SECURITY_GROUP_ID=$(aws ec2 describe-security-groups --filters "Name=group-name,Values=$SECURITY_GROUP" --query 'SecurityGroups[0].GroupId' --output text --region $REGION)
    print_status "Security group $SECURITY_GROUP already exists: $SECURITY_GROUP_ID"
fi

# Get latest Ubuntu AMI
AMI_ID=$(aws ec2 describe-images \
    --owners 099720109477 \
    --filters "Name=name,Values=ubuntu/images/hvm-ssd/ubuntu-jammy-22.04-amd64-server-*" \
    --query 'Images|sort_by(@, &CreationDate)[-1].ImageId' \
    --output text --region $REGION)

print_status "Using Ubuntu AMI: $AMI_ID"

# Create user data script
cat > user-data.sh << 'EOF'
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
EOF

# Launch EC2 instance
print_status "Launching EC2 instance..."
INSTANCE_ID=$(aws ec2 run-instances \
    --image-id $AMI_ID \
    --count 1 \
    --instance-type $INSTANCE_TYPE \
    --key-name $KEY_NAME \
    --security-group-ids $SECURITY_GROUP_ID \
    --user-data file://user-data.sh \
    --tag-specifications "ResourceType=instance,Tags=[{Key=Name,Value=StoryPublisher}]" \
    --region $REGION \
    --query 'Instances[0].InstanceId' --output text)

print_success "Instance launched: $INSTANCE_ID"

# Wait for instance to be running
print_status "Waiting for instance to be running..."
aws ec2 wait instance-running --instance-ids $INSTANCE_ID --region $REGION

# Get public IP
PUBLIC_IP=$(aws ec2 describe-instances \
    --instance-ids $INSTANCE_ID \
    --query 'Reservations[0].Instances[0].PublicIpAddress' \
    --output text --region $REGION)

print_success "Instance is running!"
echo ""
echo "📋 Deployment Information:"
echo "  Instance ID: $INSTANCE_ID"
echo "  Public IP: $PUBLIC_IP"
echo "  SSH Command: ssh -i ${KEY_NAME}.pem ubuntu@$PUBLIC_IP"
echo ""
echo "🚀 Next Steps:"
echo "1. SSH into the instance: ssh -i ${KEY_NAME}.pem ubuntu@$PUBLIC_IP"
echo "2. Clone your repository: git clone https://github.com/udayganeshK/story-publisher.git"
echo "3. Navigate to the project: cd story-publisher"
echo "4. Create environment file with your settings"
echo "5. Run: docker-compose -f docker-compose.simple.yml up -d"
echo ""
echo "💰 Monthly Cost: ~\$10-15 (t3.micro + storage + data transfer)"
echo "🌐 Your app will be available at: http://$PUBLIC_IP"

# Cleanup
rm -f user-data.sh

print_success "✅ Budget deployment setup complete!"
