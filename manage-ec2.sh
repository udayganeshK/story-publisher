#!/bin/bash

# EC2 Instance Manager for Story Publisher
# Easily start/stop your instance to save costs

set -e

# Configuration
REGION="us-east-1"
INSTANCE_NAME="story-publisher"

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

print_status() { echo -e "${BLUE}[INFO]${NC} $1"; }
print_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
print_warning() { echo -e "${YELLOW}[WARNING]${NC} $1"; }
print_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# Get instance ID by name
get_instance_id() {
    aws ec2 describe-instances \
        --filters "Name=tag:Name,Values=$INSTANCE_NAME" "Name=instance-state-name,Values=running,stopped,stopping,pending" \
        --query 'Reservations[0].Instances[0].InstanceId' \
        --output text \
        --region $REGION 2>/dev/null
}

# Get instance state
get_instance_state() {
    local instance_id=$1
    aws ec2 describe-instances \
        --instance-ids $instance_id \
        --query 'Reservations[0].Instances[0].State.Name' \
        --output text \
        --region $REGION 2>/dev/null
}

# Get instance public IP
get_instance_ip() {
    local instance_id=$1
    aws ec2 describe-instances \
        --instance-ids $instance_id \
        --query 'Reservations[0].Instances[0].PublicIpAddress' \
        --output text \
        --region $REGION 2>/dev/null
}

# Start instance
start_instance() {
    local instance_id=$1
    print_status "Starting EC2 instance..."
    
    aws ec2 start-instances \
        --instance-ids $instance_id \
        --region $REGION > /dev/null
    
    print_status "Waiting for instance to be running..."
    aws ec2 wait instance-running \
        --instance-ids $instance_id \
        --region $REGION
    
    local public_ip=$(get_instance_ip $instance_id)
    
    print_success "✅ Instance started successfully!"
    echo ""
    echo "📋 Instance Information:"
    echo "  Instance ID: $instance_id"
    echo "  Public IP: $public_ip"
    echo "  Status: Running"
    echo ""
    echo "🌐 Access your app:"
    echo "  Frontend: http://$public_ip:3000"
    echo "  Backend API: http://$public_ip:8080/api"
    echo "  Health Check: http://$public_ip:8080/api/actuator/health"
    echo ""
    echo "📱 SSH Access:"
    echo "  ssh -i ~/.ssh/storypublisher-key.pem ec2-user@$public_ip"
}

# Stop instance
stop_instance() {
    local instance_id=$1
    print_warning "Stopping EC2 instance..."
    
    aws ec2 stop-instances \
        --instance-ids $instance_id \
        --region $REGION > /dev/null
    
    print_status "Waiting for instance to stop..."
    aws ec2 wait instance-stopped \
        --instance-ids $instance_id \
        --region $REGION
    
    print_success "✅ Instance stopped successfully!"
    echo ""
    echo "💰 Cost Savings:"
    echo "  • EC2 charges stopped (saves ~$3.50/month when off)"
    echo "  • EBS storage charges continue (~$1/month)"
    echo "  • Data and snapshots preserved"
    echo ""
    echo "🔄 To restart: ./manage-ec2.sh start"
}

# Show status
show_status() {
    local instance_id=$1
    local state=$(get_instance_state $instance_id)
    
    echo ""
    echo "📊 EC2 Instance Status:"
    echo "  Instance ID: $instance_id"
    echo "  Name: $INSTANCE_NAME"
    echo "  Region: $REGION"
    echo "  State: $state"
    
    if [ "$state" = "running" ]; then
        local public_ip=$(get_instance_ip $instance_id)
        echo "  Public IP: $public_ip"
        echo ""
        echo "🌐 Access URLs:"
        echo "  Frontend: http://$public_ip:3000"
        echo "  Backend: http://$public_ip:8080/api"
        echo ""
        echo "💰 Current Cost: ~$3.50/month (running)"
        echo "🔄 To stop: ./manage-ec2.sh stop"
    else
        echo "  Public IP: None (instance stopped)"
        echo ""
        echo "💰 Current Cost: ~$1/month (storage only)"
        echo "🔄 To start: ./manage-ec2.sh start"
    fi
    echo ""
}

# Main function
main() {
    local action=${1:-status}
    
    print_status "🔍 Looking for Story Publisher instance..."
    
    # Get instance ID
    INSTANCE_ID=$(get_instance_id)
    
    if [ "$INSTANCE_ID" = "None" ] || [ -z "$INSTANCE_ID" ]; then
        print_error "No instance found with name '$INSTANCE_NAME'"
        print_status "Make sure you have deployed the instance first with ./deploy-simple.sh"
        exit 1
    fi
    
    case $action in
        start)
            local state=$(get_instance_state $INSTANCE_ID)
            if [ "$state" = "running" ]; then
                print_warning "Instance is already running"
                show_status $INSTANCE_ID
            else
                start_instance $INSTANCE_ID
            fi
            ;;
        stop)
            local state=$(get_instance_state $INSTANCE_ID)
            if [ "$state" = "stopped" ]; then
                print_warning "Instance is already stopped"
                show_status $INSTANCE_ID
            else
                stop_instance $INSTANCE_ID
            fi
            ;;
        status)
            show_status $INSTANCE_ID
            ;;
        *)
            echo "Usage: $0 [start|stop|status]"
            echo ""
            echo "Commands:"
            echo "  start   - Start the EC2 instance"
            echo "  stop    - Stop the EC2 instance (save costs)"
            echo "  status  - Show current instance status"
            echo ""
            echo "Examples:"
            echo "  $0 start    # Start instance for development"
            echo "  $0 stop     # Stop instance to save money"
            echo "  $0 status   # Check current status"
            exit 1
            ;;
    esac
}

main "$@"
