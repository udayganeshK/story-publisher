#!/bin/bash

# Story Publisher Application Startup Script
# This script will start both backend and frontend and verify everything is working

set -e  # Exit on any error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
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

# Function to check if a port is in use
check_port() {
    local port=$1
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null ; then
        return 0  # Port is in use
    else
        return 1  # Port is free
    fi
}

# Function to wait for service to be ready
wait_for_service() {
    local url=$1
    local service_name=$2
    local max_attempts=15
    local attempt=1
    
    print_status "Waiting for $service_name to be ready..."
    
    while [ $attempt -le $max_attempts ]; do
        if curl -s -f "$url" > /dev/null 2>&1; then
            print_success "$service_name is ready!"
            return 0
        fi
        
        echo -n "."
        sleep 3
        attempt=$((attempt + 1))
    done
    
    print_error "$service_name failed to start within 45 seconds"
    return 1
}

# Function to kill processes on specific ports
cleanup_ports() {
    print_status "Cleaning up existing processes..."
    
    # Kill backend (port 8080)
    if check_port 8080; then
        print_warning "Killing existing backend process on port 8080"
        lsof -ti:8080 | xargs kill -9 2>/dev/null || true
        sleep 2
    fi
    
    # Kill frontend (port 3000)
    if check_port 3000; then
        print_warning "Killing existing frontend process on port 3000"
        lsof -ti:3000 | xargs kill -9 2>/dev/null || true
        sleep 2
    fi
}

# Function to check prerequisites
check_prerequisites() {
    print_header "CHECKING PREREQUISITES"
    
    # Check Java
    if command -v java &> /dev/null; then
        java_version=$(java -version 2>&1 | head -n 1)
        print_success "Java found: $java_version"
    else
        print_error "Java not found. Please install Java 17 or higher."
        exit 1
    fi
    
    # Check Maven
    if command -v mvn &> /dev/null; then
        mvn_version=$(mvn -version | head -n 1)
        print_success "Maven found: $mvn_version"
    else
        print_error "Maven not found. Please install Maven."
        exit 1
    fi
    
    # Check Node.js
    if command -v node &> /dev/null; then
        node_version=$(node --version)
        print_success "Node.js found: $node_version"
    else
        print_error "Node.js not found. Please install Node.js."
        exit 1
    fi
    
    # Check npm
    if command -v npm &> /dev/null; then
        npm_version=$(npm --version)
        print_success "npm found: v$npm_version"
    else
        print_error "npm not found. Please install npm."
        exit 1
    fi
    
    # Check PostgreSQL
    if command -v psql &> /dev/null; then
        print_success "PostgreSQL found"
    else
        print_warning "PostgreSQL CLI not found. Make sure PostgreSQL server is running."
    fi
}

# Function to setup backend
setup_backend() {
    print_header "SETTING UP BACKEND"
    
    cd backend
    
    print_status "Cleaning and building backend..."
    mvn clean compile -q
    
    if [ $? -eq 0 ]; then
        print_success "Backend build successful"
    else
        print_error "Backend build failed"
        exit 1
    fi
    
    cd ..
}

# Function to setup frontend
setup_frontend() {
    print_header "SETTING UP FRONTEND"
    
    cd frontend
    
    print_status "Installing frontend dependencies..."
    npm install --silent
    
    if [ $? -eq 0 ]; then
        print_success "Frontend dependencies installed"
    else
        print_error "Frontend dependency installation failed"
        exit 1
    fi
    
    cd ..
}

# Function to start backend
start_backend() {
    print_header "STARTING BACKEND SERVER"
    
    cd backend
    
    print_status "Starting Spring Boot application..."
    nohup mvn spring-boot:run > ../logs/backend.log 2>&1 &
    backend_pid=$!
    echo $backend_pid > ../logs/backend.pid
    
    cd ..
    
    # Wait for backend to be ready
    if wait_for_service "http://localhost:8080/api/stories" "Backend"; then
        print_success "Backend server started successfully (PID: $backend_pid)"
        return 0
    else
        print_error "Backend server failed to start"
        return 1
    fi
}

# Function to start frontend
start_frontend() {
    print_header "STARTING FRONTEND SERVER"
    
    cd frontend
    
    print_status "Starting Next.js development server..."
    nohup npm run dev > ../logs/frontend.log 2>&1 &
    frontend_pid=$!
    echo $frontend_pid > ../logs/frontend.pid
    
    cd ..
    
    # Wait for frontend to be ready
    if wait_for_service "http://localhost:3000" "Frontend"; then
        print_success "Frontend server started successfully (PID: $frontend_pid)"
        return 0
    else
        print_error "Frontend server failed to start"
        return 1
    fi
}

# Function to run health checks
run_health_checks() {
    print_header "RUNNING HEALTH CHECKS"
    
    local all_good=true
    
    # Test backend stories endpoint
    print_status "Testing backend API..."
    if curl -s -f "http://localhost:8080/api/stories" > /dev/null; then
        print_success "✅ Backend API working"
    else
        print_error "❌ Backend API failed"
        all_good=false
    fi
    
    # Test frontend
    print_status "Testing frontend..."
    if curl -s -f "http://localhost:3000" > /dev/null; then
        print_success "✅ Frontend working"
    else
        print_error "❌ Frontend failed"
        all_good=false
    fi
    
    # Test auth endpoints with a quick test
    print_status "Testing authentication system..."
    local test_response=$(curl -s -X POST http://localhost:8080/api/auth/signup \
        -H "Content-Type: application/json" \
        -d "{\"username\":\"quicktest$(date +%s)\",\"email\":\"quicktest$(date +%s)@example.com\",\"password\":\"password123\"}" 2>/dev/null)
    
    if echo "$test_response" | grep -q "accessToken\|already taken"; then
        print_success "✅ Authentication system working"
    else
        print_warning "⚠️  Authentication test inconclusive"
    fi
    
    if [ "$all_good" = true ]; then
        return 0
    else
        return 1
    fi
}

# Function to display final status
show_final_status() {
    print_header "APPLICATION STATUS"
    
    echo -e "${GREEN}"
    echo "    🚀 STORY PUBLISHER APPLICATION IS READY! 🚀"
    echo ""
    echo "    ✅ Backend:  http://localhost:8080/api"
    echo "    ✅ Frontend: http://localhost:3000"
    echo ""
    echo "    📊 Health:   http://localhost:8080/api/stories"
    echo "    📚 Stories:  http://localhost:8080/api/stories"
    echo ""
    echo "    🔗 Ready to use at: http://localhost:3000"
    echo -e "${NC}"
    
    print_header "PROCESS INFORMATION"
    echo "Backend PID:  $(cat logs/backend.pid 2>/dev/null || echo 'Not found')"
    echo "Frontend PID: $(cat logs/frontend.pid 2>/dev/null || echo 'Not found')"
    echo ""
    echo "Logs:"
    echo "  Backend:  tail -f logs/backend.log"
    echo "  Frontend: tail -f logs/frontend.log"
    echo ""
    echo "To stop the application, run: ./shutdown.sh"
}

# Function to create logs directory
setup_logs() {
    mkdir -p logs
    touch logs/backend.log logs/frontend.log
}

# Main execution
main() {
    clear
    echo -e "${GREEN}"
    echo "╔════════════════════════════════════════════════════════════════╗"
    echo "║                    STORY PUBLISHER STARTUP                    ║"
    echo "║                                                                ║"
    echo "║  This script will start the complete Story Publisher          ║"
    echo "║  application stack (Backend + Frontend)                       ║"
    echo "╚════════════════════════════════════════════════════════════════╝"
    echo -e "${NC}\n"
    
    setup_logs
    cleanup_ports
    check_prerequisites
    setup_backend
    setup_frontend
    
    if start_backend && start_frontend; then
        sleep 5  # Give services a moment to fully initialize
        
        if run_health_checks; then
            show_final_status
        else
            print_error "Health checks failed. Check logs for details."
            exit 1
        fi
    else
        print_error "Failed to start services. Check logs for details."
        exit 1
    fi
}

# Run main function
main "$@"
