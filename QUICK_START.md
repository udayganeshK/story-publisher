# Story Publisher - Quick Start Guide

## 🚀 One-Command Startup

This application now includes automated startup scripts that handle everything for you!

### Prerequisites

Make sure you have installed:
- Java 17+ 
- Maven 3.6+
- Node.js 18+
- npm 
- PostgreSQL (running on localhost:5432)

### Quick Commands

#### Start Everything
```bash
./startup.sh
```
This single command will:
- ✅ Check all prerequisites
- ✅ Clean and build the backend
- ✅ Install frontend dependencies  
- ✅ Start backend server (port 8080)
- ✅ Start frontend server (port 3000)
- ✅ Run health checks
- ✅ Give you a "GO GREEN" status when ready

#### Check Status
```bash
./status.sh
```
Shows real-time status of all services.

#### Stop Everything
```bash
./shutdown.sh
```
Cleanly stops both backend and frontend servers.

### Expected Output

When everything is working, you'll see:

```
🚀 STORY PUBLISHER APPLICATION IS READY! 🚀

✅ Backend:  http://localhost:8080/api
✅ Frontend: http://localhost:3000

📊 Health:   http://localhost:8080/api/actuator/health  
📚 Stories:  http://localhost:8080/api/stories

🔗 Ready to use at: http://localhost:3000
```

### Application Features

- **Public Story Access**: Anyone can read all stories
- **Authenticated Writing**: Login required to create/edit stories
- **User Dashboard**: See only your own stories in dashboard
- **Story Management**: Edit/delete only your own stories
- **Real-time Updates**: Changes reflect immediately

### API Endpoints

#### Public (No Authentication)
- `GET /api/stories` - List all published stories
- `GET /api/stories/{id}` - Get any story by ID

#### Authenticated Required  
- `POST /api/stories` - Create new story
- `PUT /api/stories/{id}` - Update story (owner only)
- `DELETE /api/stories/{id}` - Delete story (owner only)
- `GET /api/stories/my` - Get current user's stories

#### Authentication
- `POST /api/auth/signup` - Create account
- `POST /api/auth/login` - Login
- `GET /api/auth/profile` - Get current user info

### Troubleshooting

#### If startup fails:
1. Check if PostgreSQL is running
2. Verify database `storypublisher` exists
3. Check logs: `tail -f logs/backend.log` or `tail -f logs/frontend.log`

#### If ports are in use:
The startup script automatically kills existing processes on ports 8080 and 3000.

#### If health checks fail:
Wait a few more seconds and run `./status.sh` - services might still be starting up.

### Development

#### View Logs
```bash
# Backend logs
tail -f logs/backend.log

# Frontend logs  
tail -f logs/frontend.log
```

#### Manual Development Mode
If you prefer to run services manually:

Backend:
```bash
cd backend
mvn spring-boot:run
```

Frontend:
```bash
cd frontend
npm run dev
```

### Database Setup

The application expects a PostgreSQL database named `storypublisher`. If it doesn't exist:

```bash
createdb storypublisher
```

The application will automatically create tables on first run.

---

**That's it! Just run `./startup.sh` and you're ready to go! 🎉**
