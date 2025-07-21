# 📖 Story Publisher Platform

A modern, full-stack story publishing platform that enables users to create, share, and discover engaging stories. Built with **Spring Boot** (backend) and **Next.js** (frontend), featuring a clean, responsive design and robust content management capabilities.

![Story Publisher](https://img.shields.io/badge/Platform-Story%20Publisher-blue)
![Spring Boot](https://img.shields.io/badge/Backend-Spring%20Boot%203.4-green)
![Next.js](https://img.shields.io/badge/Frontend-Next.js%2014-black)
![PostgreSQL](https://img.shields.io/badge/Database-PostgreSQL-336791)
![License](https://img.shields.io/badge/License-MIT-yellow)

## � Features

### ✨ Core Functionality
- **📝 Story Creation & Editing** - Rich text editor with auto-save functionality
- **👀 Public Story Discovery** - Browse all published stories without authentication
- **👤 Personal Dashboard** - Manage your own stories with full CRUD operations
- **🔐 Secure Authentication** - JWT-based user authentication and authorization
- **📱 Responsive Design** - Seamless experience across desktop and mobile devices
- **🚀 Auto-Generated Slugs** - SEO-friendly URLs for all stories

### 🛡️ Security & Access Control
- **Public Reading** - Anyone can read all stories
- **Authenticated Writing** - Login required to create and edit stories
- **Owner-Only Editing** - Users can only edit/delete their own stories
- **Privacy Settings** - Stories can be set as public or private
- **CORS Protection** - Secure cross-origin resource sharing

### 🎨 User Experience
- **Clean, Modern UI** - Built with Tailwind CSS
- **Intuitive Navigation** - Easy-to-use interface for all skill levels
- **Real-time Updates** - Changes reflect immediately across the platform
- **Error Handling** - Comprehensive error messages and validation

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                     STORY PUBLISHER                        │
├─────────────────────────────────────────────────────────────┤
│  Frontend (Next.js 14)          │  Backend (Spring Boot 3)  │
│  ├── React Components           │  ├── REST API Controllers │
│  ├── TypeScript                 │  ├── JWT Authentication   │
│  ├── Tailwind CSS              │  ├── JPA/Hibernate ORM    │
│  ├── Context API               │  ├── PostgreSQL Database  │
│  └── API Services              │  └── Spring Security      │
├─────────────────────────────────────────────────────────────┤
│                    PostgreSQL Database                     │
│  ├── Users (Authentication)    │  ├── Stories (Content)    │
│  ├── Categories (Organization) │  └── Tags (Metadata)      │
└─────────────────────────────────────────────────────────────┘
```

## 🚀 Quick Start

### Prerequisites
- **Java 17+** ☕
- **Maven 3.6+** 📦
- **Node.js 18+** 🟢
- **npm** 📋
- **PostgreSQL** 🐘

### One-Command Startup 🎯

```bash
# Clone the repository
git clone https://github.com/yourusername/story-publisher.git
cd story-publisher

# Start everything with one command
./startup.sh
```

This will automatically:
- ✅ Check all prerequisites
- ✅ Build the backend
- ✅ Install frontend dependencies
- ✅ Start both servers
- ✅ Run health checks
- ✅ Display "GO GREEN" status when ready

### 🔍 Verify Installation

```bash
# Check status of all services
./status.sh

# Expected output when everything is working:
# 🚀 ALL SYSTEMS GO! 🚀
# Application is ready at: http://localhost:3000
```

## 📚 API Documentation

### Public Endpoints (No Authentication Required)
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/stories` | List all published stories |
| `GET` | `/api/stories/{id}` | Get specific story by ID |
| `POST` | `/api/auth/signup` | Create new user account |
| `POST` | `/api/auth/login` | User authentication |

### Protected Endpoints (Authentication Required)
| Method | Endpoint | Description |
|--------|----------|-------------|
| `GET` | `/api/stories/my` | Get current user's stories |
| `POST` | `/api/stories` | Create new story |
| `PUT` | `/api/stories/{id}` | Update story (owner only) |
| `DELETE` | `/api/stories/{id}` | Delete story (owner only) |
| `GET` | `/api/auth/profile` | Get current user profile |

## �️ Development

### Project Structure
```
story-publisher/
├── backend/                 # Spring Boot API
│   ├── src/main/java/
│   │   └── com/storypublisher/
│   │       ├── controller/  # REST Controllers
│   │       ├── service/     # Business Logic
│   │       ├── model/       # JPA Entities
│   │       ├── repository/  # Data Access
│   │       ├── dto/         # Data Transfer Objects
│   │       ├── security/    # Authentication & Authorization
│   │       └── config/      # Configuration Classes
│   └── src/main/resources/
│       └── application.properties
├── frontend/                # Next.js Application
│   ├── src/
│   │   ├── app/            # Next.js App Router
│   │   ├── components/     # Reusable React Components
│   │   ├── contexts/       # React Context Providers
│   │   ├── services/       # API Services
│   │   ├── types/          # TypeScript Type Definitions
│   │   └── lib/            # Utility Functions
│   └── public/             # Static Assets
├── logs/                   # Application Logs
├── startup.sh             # One-command startup script
├── shutdown.sh            # Application shutdown script
└── status.sh              # Service status checker
```

### 🔧 Development Commands

```bash
# Start development environment
./startup.sh

# Check service status
./status.sh

# Stop all services
./shutdown.sh

# View logs
tail -f logs/backend.log
tail -f logs/frontend.log

# Build for production
cd backend && mvn clean package
cd frontend && npm run build
```

## 🤝 Contributing

We welcome contributions! Please follow these steps:

1. **Fork the repository**
2. **Create a feature branch**: `git checkout -b feature/amazing-feature`
3. **Commit your changes**: `git commit -m 'Add amazing feature'`
4. **Push to the branch**: `git push origin feature/amazing-feature`
5. **Open a Pull Request**

## 📄 License

This project is licensed under the MIT License.

## 🆘 Support & Troubleshooting

### Common Issues

**Database Connection Issues**: Make sure PostgreSQL is running and the database exists:
```bash
createdb storypublisher
```

**Port Already in Use**: The startup script automatically handles port conflicts.

**Authentication Issues**: Ensure you're using correct field names (`usernameOrEmail` not `username`).

### Getting Help
- **Issues**: Open an issue on GitHub
- **Questions**: Check the [QUICK_START.md](QUICK_START.md) guide

## 🎯 Roadmap

- [ ] **Docker Support** - Containerized deployment
- [ ] **Story Categories** - Organize stories by category
- [ ] **Comments System** - User comments on stories
- [ ] **Search & Filters** - Advanced story search
- [ ] **Rich Text Editor** - Enhanced story editing
- [ ] **User Profiles** - Enhanced user profile pages

---

<div align="center">

**Built with ❤️ using Spring Boot and Next.js**

[🌟 Star this project](https://github.com/yourusername/story-publisher) if you find it useful!

</div>
