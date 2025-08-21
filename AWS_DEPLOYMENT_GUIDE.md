# 🚀 AWS Deployment Guide for Story Publisher

This guide will walk you through deploying the Story Publisher application to AWS using a production-ready architecture.

## 📋 Prerequisites

### 1. AWS Account Setup
- AWS Account with appropriate permissions
- AWS CLI installed and configured
- Docker installed locally
- Basic familiarity with AWS services

### 2. Required AWS Services
- **ECS Fargate**: Container orchestration
- **RDS PostgreSQL**: Database
- **ECR**: Container registry
- **Application Load Balancer**: Load balancing
- **VPC**: Networking
- **Systems Manager**: Parameter store
- **CloudWatch**: Logging and monitoring

## 🏗️ Architecture Overview

```
Internet → ALB → ECS Fargate → RDS PostgreSQL
           ↓
       CloudWatch Logs
```

### Components:
- **Frontend**: Next.js app running on ECS Fargate (Port 3000)
- **Backend**: Spring Boot API running on ECS Fargate (Port 8080)
- **Database**: PostgreSQL on RDS in private subnets
- **Load Balancer**: ALB distributing traffic to services
- **Networking**: VPC with public/private subnets across 2 AZs

## 🚀 Deployment Options

### Option 1: Quick Deployment (Recommended for Testing)

1. **Configure AWS Account ID**
   ```bash
   # Edit aws/deploy.sh and set your AWS Account ID
   vim aws/deploy.sh
   # Set: AWS_ACCOUNT_ID="123456789012"
   ```

2. **Deploy Infrastructure**
   ```bash
   # Deploy CloudFormation stack
   aws cloudformation create-stack \
     --stack-name storypublisher-infrastructure \
     --template-body file://aws/infrastructure.yaml \
     --capabilities CAPABILITY_IAM \
     --region us-east-1
   
   # Wait for completion (10-15 minutes)
   aws cloudformation wait stack-create-complete \
     --stack-name storypublisher-infrastructure \
     --region us-east-1
   ```

3. **Configure Parameters**
   ```bash
   # Set database URL (get from CloudFormation outputs)
   aws ssm put-parameter \
     --name "/storypublisher/database/url" \
     --value "jdbc:postgresql://DATABASE_ENDPOINT:5432/storypublisher" \
     --type "SecureString" \
     --region us-east-1
   
   # Set database credentials
   aws ssm put-parameter \
     --name "/storypublisher/database/username" \
     --value "storypublisher" \
     --type "SecureString" \
     --region us-east-1
   
   aws ssm put-parameter \
     --name "/storypublisher/database/password" \
     --value "your-secure-password" \
     --type "SecureString" \
     --region us-east-1
   
   # Set JWT secret
   aws ssm put-parameter \
     --name "/storypublisher/jwt/secret" \
     --value "StoryPublisherSecretKeyForJWTTokenGenerationAndValidation2025" \
     --type "SecureString" \
     --region us-east-1
   
   # Set CORS origins (ALB DNS from CloudFormation)
   aws ssm put-parameter \
     --name "/storypublisher/cors/origins" \
     --value "https://your-alb-dns-name.us-east-1.elb.amazonaws.com" \
     --type "SecureString" \
     --region us-east-1
   
   # Set backend URL for frontend
   aws ssm put-parameter \
     --name "/storypublisher/backend/url" \
     --value "https://your-alb-dns-name.us-east-1.elb.amazonaws.com/api" \
     --type "SecureString" \
     --region us-east-1
   ```

4. **Deploy Application**
   ```bash
   cd aws
   ./deploy.sh deploy
   ```

### Option 2: Step-by-Step Manual Deployment

#### Step 1: Build Infrastructure
```bash
# Validate CloudFormation template
aws cloudformation validate-template \
  --template-body file://aws/infrastructure.yaml

# Deploy infrastructure
aws cloudformation create-stack \
  --stack-name storypublisher-infrastructure \
  --template-body file://aws/infrastructure.yaml \
  --capabilities CAPABILITY_IAM \
  --parameters ParameterKey=Environment,ParameterValue=production \
  --region us-east-1
```

#### Step 2: Set Up ECS Task Execution Role
```bash
# Create ECS task execution role (if not exists)
aws iam create-role \
  --role-name ecsTaskExecutionRole \
  --assume-role-policy-document '{
    "Version": "2012-10-17",
    "Statement": [
      {
        "Effect": "Allow",
        "Principal": {
          "Service": "ecs-tasks.amazonaws.com"
        },
        "Action": "sts:AssumeRole"
      }
    ]
  }'

# Attach policies
aws iam attach-role-policy \
  --role-name ecsTaskExecutionRole \
  --policy-arn arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy

aws iam attach-role-policy \
  --role-name ecsTaskExecutionRole \
  --policy-arn arn:aws:iam::aws:policy/AmazonSSMReadOnlyAccess
```

#### Step 3: Build and Push Images
```bash
# Get your account ID
AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
AWS_REGION="us-east-1"

# Login to ECR
aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com

# Build and push backend
cd backend
docker build -t storypublisher-backend .
docker tag storypublisher-backend:latest $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/storypublisher-backend:latest
docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/storypublisher-backend:latest

# Build and push frontend
cd ../frontend
docker build -t storypublisher-frontend .
docker tag storypublisher-frontend:latest $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/storypublisher-frontend:latest
docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/storypublisher-frontend:latest
```

#### Step 4: Create ECS Services
```bash
# Update task definitions with your account ID
sed -i "s/ACCOUNT_ID/$AWS_ACCOUNT_ID/g; s/REGION/$AWS_REGION/g" aws/backend-task-definition.json
sed -i "s/ACCOUNT_ID/$AWS_ACCOUNT_ID/g; s/REGION/$AWS_REGION/g" aws/frontend-task-definition.json

# Register task definitions
aws ecs register-task-definition --cli-input-json file://aws/backend-task-definition.json
aws ecs register-task-definition --cli-input-json file://aws/frontend-task-definition.json

# Create services (requires additional configuration)
# See the ECS console for service creation with load balancer integration
```

## 🔧 Post-Deployment Configuration

### 1. Database Setup
```bash
# Connect to RDS and create initial schema
# Use the database endpoint from CloudFormation outputs
```

### 2. Load Balancer Configuration
- Configure target groups for backend (port 8080) and frontend (port 3000)
- Set up health checks using `/api/actuator/health` and `/api/health`
- Configure routing rules

### 3. Domain Setup (Optional)
```bash
# Create Route 53 hosted zone
aws route53 create-hosted-zone --name yourdomain.com --caller-reference $(date +%s)

# Point domain to ALB
# Create CNAME record pointing to ALB DNS name
```

### 4. SSL Certificate (Optional)
```bash
# Request SSL certificate
aws acm request-certificate \
  --domain-name yourdomain.com \
  --validation-method DNS \
  --region us-east-1
```

## 📊 Monitoring and Logging

### CloudWatch Logs
- Backend logs: `/ecs/storypublisher-backend`
- Frontend logs: `/ecs/storypublisher-frontend`

### Health Checks
- Backend: `https://your-alb-dns/api/actuator/health`
- Frontend: `https://your-alb-dns/api/health`

### Monitoring Commands
```bash
# View service status
aws ecs describe-services \
  --cluster storypublisher-cluster \
  --services storypublisher-backend-service storypublisher-frontend-service

# View logs
aws logs tail /ecs/storypublisher-backend --follow
aws logs tail /ecs/storypublisher-frontend --follow
```

## 🔄 Continuous Deployment

### GitHub Actions (Optional)
Create `.github/workflows/deploy.yml` for automated deployments on push to main branch.

### Manual Updates
```bash
# Rebuild and redeploy
cd aws
./deploy.sh deploy
```

## 💰 Cost Estimation

### Monthly Costs (us-east-1):
- **ECS Fargate**: ~$30-50 (2 services, small tasks)
- **RDS t3.micro**: ~$15-20
- **ALB**: ~$20-25
- **NAT Gateway**: ~$45
- **Data Transfer**: ~$5-10
- **Total**: ~$115-150/month

### Cost Optimization:
- Use FARGATE_SPOT for non-critical workloads
- Schedule ECS services to scale down during off-hours
- Use reserved instances for predictable workloads

## 🔐 Security Considerations

1. **Network Security**
   - RDS in private subnets
   - Security groups with minimal access
   - NACLs for additional protection

2. **Application Security**
   - Secrets stored in Parameter Store/Secrets Manager
   - Container images scanned for vulnerabilities
   - HTTPS-only communication

3. **Monitoring**
   - CloudWatch alarms for critical metrics
   - AWS Config for compliance
   - CloudTrail for audit logging

## 🆘 Troubleshooting

### Common Issues:

1. **ECS Tasks Not Starting**
   ```bash
   # Check task logs
   aws ecs describe-tasks --cluster storypublisher-cluster --tasks TASK_ARN
   ```

2. **Database Connection Issues**
   ```bash
   # Verify security groups allow connection from ECS to RDS
   # Check parameter store values
   aws ssm get-parameter --name "/storypublisher/database/url"
   ```

3. **Load Balancer Health Check Failures**
   ```bash
   # Check target group health
   aws elbv2 describe-target-health --target-group-arn TARGET_GROUP_ARN
   ```

## 📞 Support

For deployment issues:
1. Check CloudWatch logs
2. Verify security groups and networking
3. Ensure parameter store values are correct
4. Review ECS service events

## 🧹 Cleanup

To destroy all resources:
```bash
# Delete ECS services first
aws ecs update-service --cluster storypublisher-cluster --service storypublisher-backend-service --desired-count 0
aws ecs update-service --cluster storypublisher-cluster --service storypublisher-frontend-service --desired-count 0

# Delete CloudFormation stack
aws cloudformation delete-stack --stack-name storypublisher-infrastructure
```

This will remove all AWS resources and stop billing.
