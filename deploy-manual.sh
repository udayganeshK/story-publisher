#!/bin/bash

# Simple AWS Deployment - Manual Steps
# This is much simpler and more reliable

set -e

echo "🚀 Story Publisher - Simple AWS Deployment Guide"
echo ""
echo "We'll deploy a t3.nano instance (~$4.50/month) with your application."
echo ""

# Get VPC ID
VPC_ID=$(aws ec2 describe-vpcs --filters "Name=is-default,Values=true" --query 'Vpcs[0].VpcId' --output text --region us-east-1)
echo "✅ Using VPC: $VPC_ID"

# Create security group
echo "📋 Creating security group..."
SG_ID=$(aws ec2 create-security-group \
    --group-name story-publisher-sg \
    --description "Security group for Story Publisher" \
    --vpc-id $VPC_ID \
    --region us-east-1 \
    --output text --query 'GroupId')

echo "✅ Security group created: $SG_ID"

# Add security group rules
echo "🔐 Adding security group rules..."
aws ec2 authorize-security-group-ingress --group-id $SG_ID --protocol tcp --port 22 --cidr 0.0.0.0/0 --region us-east-1
aws ec2 authorize-security-group-ingress --group-id $SG_ID --protocol tcp --port 80 --cidr 0.0.0.0/0 --region us-east-1
aws ec2 authorize-security-group-ingress --group-id $SG_ID --protocol tcp --port 3000 --cidr 0.0.0.0/0 --region us-east-1
aws ec2 authorize-security-group-ingress --group-id $SG_ID --protocol tcp --port 8080 --cidr 0.0.0.0/0 --region us-east-1

# Create key pair
echo "🔑 Creating key pair..."
aws ec2 create-key-pair --key-name story-publisher-key --query 'KeyMaterial' --output text --region us-east-1 > story-publisher-key.pem
chmod 400 story-publisher-key.pem
echo "✅ Key pair saved: story-publisher-key.pem"

# Get latest Ubuntu AMI
AMI_ID=$(aws ec2 describe-images \
    --owners 099720109477 \
    --filters "Name=name,Values=ubuntu/images/hvm-ssd/ubuntu-jammy-22.04-amd64-server-*" \
    --query 'Images|sort_by(@, &CreationDate)[-1].ImageId' \
    --output text --region us-east-1)

echo "💿 Using Ubuntu AMI: $AMI_ID"

# Launch instance
echo "🚀 Launching EC2 instance (t3.nano)..."
INSTANCE_ID=$(aws ec2 run-instances \
    --image-id $AMI_ID \
    --count 1 \
    --instance-type t3.nano \
    --key-name story-publisher-key \
    --security-group-ids $SG_ID \
    --tag-specifications 'ResourceType=instance,Tags=[{Key=Name,Value=story-publisher}]' \
    --region us-east-1 \
    --query 'Instances[0].InstanceId' --output text)

echo "✅ Instance launched: $INSTANCE_ID"

# Wait for instance to be running
echo "⏳ Waiting for instance to be running..."
aws ec2 wait instance-running --instance-ids $INSTANCE_ID --region us-east-1

# Get public IP
PUBLIC_IP=$(aws ec2 describe-instances \
    --instance-ids $INSTANCE_ID \
    --region us-east-1 \
    --query 'Reservations[0].Instances[0].PublicIpAddress' \
    --output text)

echo ""
echo "🎉 SUCCESS! Your EC2 instance is ready!"
echo ""
echo "📋 Instance Details:"
echo "   Instance ID: $INSTANCE_ID"
echo "   Public IP: $PUBLIC_IP"
echo "   Instance Type: t3.nano"
echo "   Monthly Cost: ~$4.50"
echo ""
echo "🔗 SSH Command:"
echo "   ssh -i story-publisher-key.pem ubuntu@$PUBLIC_IP"
echo ""
echo "📝 Next Steps:"
echo "1. SSH into your instance: ssh -i story-publisher-key.pem ubuntu@$PUBLIC_IP"
echo "2. Update the system: sudo apt update && sudo apt upgrade -y"
echo "3. Install Docker: sudo apt install -y docker.io docker-compose"
echo "4. Add user to docker group: sudo usermod -aG docker ubuntu"
echo "5. Clone your repo: git clone https://github.com/udayganeshK/story-publisher.git"
echo "6. Deploy: cd story-publisher && docker-compose -f docker-compose.simple.yml up -d"
echo ""
echo "🌐 Once deployed, your app will be available at:"
echo "   Frontend: http://$PUBLIC_IP:3000"
echo "   Backend API: http://$PUBLIC_IP:8080/api"
echo ""
echo "💰 Monthly cost breakdown:"
echo "   - EC2 t3.nano: $3.50"
echo "   - EBS 8GB storage: $1.00"
echo "   - Total: $4.50/month"
echo ""
echo "🎯 Perfect for your portfolio project with 25 users and 150 stories!"
