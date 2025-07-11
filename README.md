# Story Publisher

A modern story publishing platform built with microservices architecture, allowing authors to write, publish, and share their stories.

## 🏗 Architecture

### Frontend
- **Framework**: Next.js 14 with TypeScript
- **Styling**: Tailwind CSS
- **Development**: App Router, ESLint, Prettier

### Backend (Coming Soon)
- **Framework**: Spring Boot with Java 17+
- **Database**: PostgreSQL with Spring Data JPA
- **Authentication**: Spring Security with JWT
- **Testing**: JUnit 5, Mockito, TestContainers

## 🚀 Getting Started

### Prerequisites
- Node.js 18+ 
- Java 17+ (for backend development)
- PostgreSQL (for database)

### Frontend Development

1. Navigate to the frontend directory:
```bash
cd frontend
```

2. Install dependencies:
```bash
npm install
```

3. Start the development server:
```bash
npm run dev
```

The frontend will be available at [http://localhost:3000](http://localhost:3000)

### Backend Development (Coming Soon)
Backend setup instructions will be added when the Spring Boot application is created.

## 📁 Project Structure

```
StoryPublisher/
├── frontend/              # Next.js Frontend Application
│   ├── src/app/          # Next.js 13+ app directory
│   ├── src/components/   # Reusable UI components
│   ├── src/lib/         # Utility functions and API clients
│   └── ...
├── backend/              # Spring Boot Backend (To be created)
├── docker/               # Docker configurations (To be created)
├── jenkins/              # CI/CD pipeline scripts (To be created)
├── aws/                  # AWS infrastructure configs (To be created)
└── docs/                 # Documentation
```

## 🎯 Development Phases

### Phase 1 - MVP
- [x] Frontend project setup
- [ ] Backend project setup  
- [ ] User authentication
- [ ] Basic story editor
- [ ] Story CRUD operations
- [ ] User profiles

### Phase 2 - Enhanced Features
- [ ] Advanced rich text editor
- [ ] Story categories and tags
- [ ] Privacy controls
- [ ] Search functionality

### Phase 3 - DevOps & CI/CD
- [ ] Docker containerization
- [ ] Jenkins CI/CD pipeline
- [ ] AWS deployment
- [ ] Monitoring and logging

## 🛠 Development Commands

### Frontend
- `npm run dev` - Start development server
- `npm run build` - Build for production
- `npm run start` - Start production server
- `npm run lint` - Run ESLint

## 📚 Resources

- [Next.js Documentation](https://nextjs.org/docs)
- [Tailwind CSS](https://tailwindcss.com)
- [Spring Boot](https://spring.io/projects/spring-boot)
- [Spring Security](https://spring.io/projects/spring-security)

## 🤝 Contributing

This is a learning project focused on modern web development and DevOps practices.

## 📄 License

This project is for educational purposes.
