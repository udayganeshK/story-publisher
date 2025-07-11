# Copilot Instructions for Story Publisher

<!-- Use this file to provide workspace-specific custom instructions to Copilot. For more details, visit https://code.visualstudio.com/docs/copilot/copilot-customization#_use-a-githubcopilotinstructionsmd-file -->

## Project Overview
This is a Story Publishing Platform with a microservices architecture:
- **Frontend**: Next.js 14 with TypeScript and Tailwind CSS
- **Backend**: Spring Boot with Java 17+ (to be created)
- **Database**: PostgreSQL with Spring Data JPA/Hibernate
- **Authentication**: Spring Security with JWT tokens

## Code Style Guidelines

### Frontend (Next.js/TypeScript)
- Use TypeScript for all components and utilities
- Follow React functional component patterns with hooks
- Use Tailwind CSS for styling (avoid inline styles when possible)
- Implement proper error handling and loading states
- Use proper TypeScript interfaces for API responses
- Follow Next.js 13+ App Router conventions

### Backend (Spring Boot/Java)
- Use Java 17+ features where appropriate
- Follow Spring Boot best practices and conventions
- Implement proper REST API patterns
- Use Spring Data JPA for database operations
- Implement comprehensive error handling
- Follow clean architecture principles (Controller → Service → Repository)
- Use DTOs for API responses
- Implement proper validation using Bean Validation

### General Guidelines
- Write clean, self-documenting code
- Implement proper error handling
- Use meaningful variable and function names
- Follow SOLID principles
- Write unit tests for critical business logic
- Implement proper logging
- Use environment variables for configuration

## API Design
- Follow RESTful conventions
- Use proper HTTP status codes
- Implement consistent error response format
- Use pagination for list endpoints
- Implement proper CORS configuration
- Use JWT tokens for authentication

## Database Design
- Use proper entity relationships
- Implement soft deletes where appropriate
- Use proper indexing for performance
- Follow database naming conventions
- Implement proper constraints

## Security Considerations
- Implement proper authentication and authorization
- Validate all user inputs
- Use HTTPS in production
- Implement rate limiting
- Follow OWASP security guidelines
- Sanitize user-generated content

## Testing Strategy
- Write unit tests for business logic
- Implement integration tests for APIs
- Use TestContainers for database testing
- Test error scenarios and edge cases
- Implement proper test data setup
