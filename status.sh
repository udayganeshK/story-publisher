#!/bin/bash

# Story Publisher Application Status Checker

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
    echo -e "${GREEN}[✅]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[⚠️ ]${NC} $1"
}

print_error() {
    echo -e "${RED}[❌]${NC} $1"
}

check_service() {
    local url=$1
    local service_name=$2
    
    if curl -s -f "$url" > /dev/null 2>&1; then
        print_success "$service_name is running"
        return 0
    else
        print_error "$service_name is not responding"
        return 1
    fi
}

check_port() {
    local port=$1
    local service_name=$2
    
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        local pid=$(lsof -ti:$port)
        print_success "$service_name is running on port $port (PID: $pid)"
        return 0
    else
        print_error "$service_name is not running on port $port"
        return 1
    fi
}

main() {
    echo -e "${BLUE}╔══════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║        STORY PUBLISHER STATUS           ║${NC}"
    echo -e "${BLUE}╚══════════════════════════════════════════╝${NC}"
    echo ""
    
    # Check ports
    backend_port_ok=0
    frontend_port_ok=0
    backend_service_ok=0
    frontend_service_ok=0
    
    if check_port 8080 "Backend Server"; then
        backend_port_ok=1
    fi
    
    if check_port 3000 "Frontend Server"; then
        frontend_port_ok=1
    fi
    
    echo ""
    
    # Check services
    if check_service "http://localhost:8080/api/stories" "Backend API"; then
        backend_service_ok=1
    fi
    
    if check_service "http://localhost:3000" "Frontend"; then
        frontend_service_ok=1
    fi
    
    echo ""
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    
    # Overall status
    if [ $backend_port_ok -eq 1 ] && [ $frontend_port_ok -eq 1 ] && [ $backend_service_ok -eq 1 ] && [ $frontend_service_ok -eq 1 ]; then
        echo -e "${GREEN}"
        echo "    🚀 ALL SYSTEMS GO! 🚀"
        echo ""
        echo "    Application is ready at: http://localhost:3000"
        echo -e "${NC}"
    else
        echo -e "${RED}"
        echo "    ⚠️  SOME ISSUES DETECTED"
        echo ""
        if [ $backend_port_ok -eq 0 ] || [ $backend_service_ok -eq 0 ]; then
            echo "    Backend issues detected"
        fi
        if [ $frontend_port_ok -eq 0 ] || [ $frontend_service_ok -eq 0 ]; then
            echo "    Frontend issues detected"
        fi
        echo ""
        echo "    Run './startup.sh' to start the application"
        echo -e "${NC}"
    fi
    
    # Show PIDs if available
    if [ -f "logs/backend.pid" ] || [ -f "logs/frontend.pid" ]; then
        echo ""
        echo "Process Information:"
        if [ -f "logs/backend.pid" ]; then
            echo "  Backend PID:  $(cat logs/backend.pid)"
        fi
        if [ -f "logs/frontend.pid" ]; then
            echo "  Frontend PID: $(cat logs/frontend.pid)"
        fi
    fi
}

main "$@"
