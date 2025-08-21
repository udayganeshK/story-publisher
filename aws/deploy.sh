#!/bin/bash

# Story Publisher AWS Deployment Script
# This script deploys the application to AWS using ECS Fargate

set -e

# Configuration
AWS_REGION="us-east-1"
AWS_ACCOUNT_ID=""  # Replace with your AWS Account ID
CLUSTER_NAME="storypublisher-cluster"
BACKEND_SERVICE="storypublisher-backend-service"
FRONTEND_SERVICE="storypublisher-frontend-service"
ECR_BACKEND_REPO="storypublisher-backend"
ECR_FRONTEND_REPO="storypublisher-frontend"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if AWS Account ID is set
check_account_id() {
    if [ -z "$AWS_ACCOUNT_ID" ]; then
        print_error "Please set your AWS_ACCOUNT_ID in this script"
        exit 1
    fi
}

# Check prerequisites
check_prerequisites() {
    print_status "Checking prerequisites..."
    
    # Check AWS CLI
    if ! command -v aws &> /dev/null; then
        print_error "AWS CLI not found. Please install AWS CLI and configure it."
        exit 1
    fi
    
    # Check Docker
    if ! command -v docker &> /dev/null; then
        print_error "Docker not found. Please install Docker."
        exit 1
    fi
    
    # Check if AWS credentials are configured
    if ! aws sts get-caller-identity &> /dev/null; then
        print_error "AWS credentials not configured. Run 'aws configure' first."
        exit 1
    fi
    
    print_success "All prerequisites met"
}

# Create ECR repositories
create_ecr_repos() {
    print_status "Creating ECR repositories..."
    
    # Backend repository
    aws ecr describe-repositories --repository-names $ECR_BACKEND_REPO --region $AWS_REGION 2>/dev/null || {
        print_status "Creating backend ECR repository..."
        aws ecr create-repository --repository-name $ECR_BACKEND_REPO --region $AWS_REGION
        print_success "Backend ECR repository created"
    }
    
    # Frontend repository
    aws ecr describe-repositories --repository-names $ECR_FRONTEND_REPO --region $AWS_REGION 2>/dev/null || {
        print_status "Creating frontend ECR repository..."
        aws ecr create-repository --repository-name $ECR_FRONTEND_REPO --region $AWS_REGION
        print_success "Frontend ECR repository created"
    }
}

# Build and push Docker images
build_and_push() {
    print_status "Building and pushing Docker images..."
    
    # Get ECR login token
    aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com
    
    # Build and push backend
    print_status "Building backend image..."
    cd backend
    docker build -t $ECR_BACKEND_REPO .
    docker tag $ECR_BACKEND_REPO:latest $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$ECR_BACKEND_REPO:latest
    docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$ECR_BACKEND_REPO:latest
    cd ..
    print_success "Backend image pushed"
    
    # Build and push frontend
    print_status "Building frontend image..."
    cd frontend
    docker build -t $ECR_FRONTEND_REPO .
    docker tag $ECR_FRONTEND_REPO:latest $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$ECR_FRONTEND_REPO:latest
    docker push $AWS_ACCOUNT_ID.dkr.ecr.$AWS_REGION.amazonaws.com/$ECR_FRONTEND_REPO:latest
    cd ..
    print_success "Frontend image pushed"
}

# Update task definitions
update_task_definitions() {
    print_status "Updating task definitions..."
    
    # Update backend task definition
    sed "s/ACCOUNT_ID/$AWS_ACCOUNT_ID/g; s/REGION/$AWS_REGION/g" aws/backend-task-definition.json > /tmp/backend-task-def.json
    aws ecs register-task-definition --cli-input-json file:///tmp/backend-task-def.json --region $AWS_REGION
    
    # Update frontend task definition
    sed "s/ACCOUNT_ID/$AWS_ACCOUNT_ID/g; s/REGION/$AWS_REGION/g" aws/frontend-task-definition.json > /tmp/frontend-task-def.json
    aws ecs register-task-definition --cli-input-json file:///tmp/frontend-task-def.json --region $AWS_REGION
    
    print_success "Task definitions updated"
}

# Update ECS services
update_services() {
    print_status "Updating ECS services..."
    
    # Update backend service
    aws ecs update-service \
        --cluster $CLUSTER_NAME \
        --service $BACKEND_SERVICE \
        --task-definition storypublisher-backend \
        --region $AWS_REGION
    
    # Update frontend service
    aws ecs update-service \
        --cluster $CLUSTER_NAME \
        --service $FRONTEND_SERVICE \
        --task-definition storypublisher-frontend \
        --region $AWS_REGION
    
    print_success "Services updated"
}

# Wait for deployment
wait_for_deployment() {
    print_status "Waiting for deployment to complete..."
    
    # Wait for backend service
    aws ecs wait services-stable \
        --cluster $CLUSTER_NAME \
        --services $BACKEND_SERVICE \
        --region $AWS_REGION
    
    # Wait for frontend service
    aws ecs wait services-stable \
        --cluster $CLUSTER_NAME \
        --services $FRONTEND_SERVICE \
        --region $AWS_REGION
    
    print_success "Deployment completed successfully!"
}

# Main deployment function
deploy() {
    print_status "Starting AWS deployment for Story Publisher..."
    
    check_account_id
    check_prerequisites
    create_ecr_repos
    build_and_push
    update_task_definitions
    update_services
    wait_for_deployment
    
    print_success "🚀 Story Publisher successfully deployed to AWS!"
    print_status "Check the AWS ECS console for service status and logs."
}

# Show usage
usage() {
    echo "Usage: $0 [deploy|build|push]"
    echo "  deploy  - Full deployment (build, push, update services)"
    echo "  build   - Build Docker images only"
    echo "  push    - Push images to ECR (assumes images are built)"
    exit 1
}

# Main script execution
case "${1:-deploy}" in
    deploy)
        deploy
        ;;
    build)
        check_prerequisites
        print_status "Building Docker images..."
        cd backend && docker build -t $ECR_BACKEND_REPO . && cd ..
        cd frontend && docker build -t $ECR_FRONTEND_REPO . && cd ..
        print_success "Images built successfully"
        ;;
    push)
        check_account_id
        check_prerequisites
        create_ecr_repos
        build_and_push
        ;;
    *)
        usage
        ;;
esac
