#!/bin/bash

# Story Publisher Application Shutdown Script

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

print_header() {
    echo -e "\n${BLUE}================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}================================${NC}\n"
}

# Function to kill process by PID file
kill_by_pid_file() {
    local pid_file=$1
    local service_name=$2
    
    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file")
        if kill -0 "$pid" 2>/dev/null; then
            print_status "Stopping $service_name (PID: $pid)..."
            kill "$pid"
            sleep 2
            
            # Force kill if still running
            if kill -0 "$pid" 2>/dev/null; then
                print_warning "Force killing $service_name..."
                kill -9 "$pid"
            fi
            
            print_success "$service_name stopped"
        else
            print_warning "$service_name was not running"
        fi
        rm -f "$pid_file"
    else
        print_warning "$service_name PID file not found"
    fi
}

# Function to kill processes on specific ports
kill_by_port() {
    local port=$1
    local service_name=$2
    
    local pids=$(lsof -ti:$port 2>/dev/null)
    if [ -n "$pids" ]; then
        print_status "Stopping $service_name on port $port..."
        echo "$pids" | xargs kill -9 2>/dev/null
        print_success "$service_name stopped"
    else
        print_status "$service_name was not running on port $port"
    fi
}

main() {
    print_header "SHUTTING DOWN STORY PUBLISHER"
    
    # Try to stop by PID files first
    kill_by_pid_file "logs/backend.pid" "Backend"
    kill_by_pid_file "logs/frontend.pid" "Frontend"
    
    # Also kill by ports as backup
    kill_by_port 8080 "Backend"
    kill_by_port 3000 "Frontend"
    
    print_success "Story Publisher application shutdown complete"
    
    echo ""
    echo "To start the application again, run: ./startup.sh"
}

main "$@"
