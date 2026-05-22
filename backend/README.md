# Onboarding Platform Backend

## Description

Backend SpringBoot application for an onboarding platform that manages user accounts, roles, and authentication.

## Architecture

### Technology Stack
- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Database**: MySQL
- **Security**: Spring Security with JWT
- **Build Tool**: Maven

### Project Structure
```
backend/
├── src/main/java/com/onboarding/
│   ├── controller/          # REST API controllers
│   ├── service/            # Business logic services
│   ├── repository/         # JPA repositories
│   ├── entity/             # JPA entities
│   ├── dto/                # Data Transfer Objects
│   ├── config/             # Configuration classes
│   └── OnboardingPlatformApplication.java
├── src/main/resources/
│   └── application.properties
└── pom.xml
```

## User Stories Implementation

### US 1.1: Création de compte utilisateur ✅
- **Features**:
  - Admin can create user accounts with all required fields
  - Email validation (RFC format + uniqueness)
  - Role assignment (ADMINISTRATEUR, MANAGER, COLLABORATEUR)
  - Password policy enforcement (8 chars, 1 uppercase, 1 digit)
  - Secure password setup via email link
  - Account status management (active/inactive)
  - Audit logging for all changes

### US 1.2: Attribution des rôles utilisateur ✅
- **Features**:
  - Role management by administrators
  - Three roles: ADMINISTRATEUR, MANAGER, COLLABORATEUR
  - Automatic permission adaptation
  - Immediate role changes
  - Audit logging for role modifications

### US 1.3: Authentification utilisateur ✅
- **Features**:
  - Email + password authentication
  - JWT-based secure sessions
  - Error messages for invalid credentials
  - Account status validation
  - Password recovery functionality
  - Logout capability

## API Endpoints

### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/forgot-password` - Password reset request
- `POST /api/auth/reset-password` - Set new password
- `POST /api/auth/logout` - User logout

### User Management (Admin/Manager only)
- `POST /api/users` - Create new user
- `GET /api/users` - Get all users
- `GET /api/users/{id}` - Get user by ID
- `PUT /api/users/{id}/role` - Update user role
- `PUT /api/users/{id}/activate` - Activate user
- `PUT /api/users/{id}/deactivate` - Deactivate user
- `DELETE /api/users/{id}` - Delete user
- `GET /api/users/role/{role}` - Get users by role
- `GET /api/users/status/{status}` - Get users by status
- `GET /api/users/search?keyword=` - Search users

## Database Schema

### Users Table
- **id**: Primary key
- **prenom**: First name (required)
- **nom**: Last name (required)
- **email**: Email (required, unique, RFC format)
- **role**: User role (ADMINISTRATEUR, MANAGER, COLLABORATEUR)
- **poste**: Job position
- **departement**: Department
- **manager**: Manager name
- **date_embauche**: Hire date
- **type_contrat**: Contract type (CDI, CDD, CVIP)
- **adresse**: Address
- **telephone**: Phone number
- **cin**: ID card number
- **diplome**: Degree
- **password**: Encrypted password
- **reset_token**: Password reset token
- **reset_token_expiry**: Token expiry date
- **statut**: Account status (active/inactive)
- **date_creation**: Creation date
- **date_modification**: Last modification date

### AuditLogs Table
- **id**: Primary key
- **user_id**: Foreign key to users
- **action**: Action performed
- **ancienne_valeur**: Old value
- **nouvelle_valeur**: New value
- **date_action**: Action date
- **effectue_par**: Who performed the action

## Security Configuration

### JWT Authentication
- Token-based authentication
- 24-hour token expiration
- Role-based authorization
- Secure password encoding with BCrypt

### Role Permissions
- **ADMINISTRATEUR**: Full system access (super-user)
- **MANAGER**: User management and viewing
- **COLLABORATEUR**: Basic access

## Configuration

### Database
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/onboarding_db
spring.datasource.username=root
spring.datasource.password=
```

### Email
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

### JWT
```properties
jwt.secret=mySecretKey
jwt.expiration=86400000
```

## Password Policy
- Minimum 8 characters
- At least 1 uppercase letter
- At least 1 digit

## Getting Started

1. **Prerequisites**:
   - Java 17+
   - MySQL Server
   - Maven

2. **Database Setup**:
   ```sql
   CREATE DATABASE onboarding_db;
   ```

3. **Configuration**:
   - Update `application.properties` with your database credentials
   - Configure email settings

4. **Run Application**:
   ```bash
   mvn spring-boot:run
   ```

## Features Summary

✅ **User Management**: Complete CRUD operations with validation
✅ **Authentication**: Secure JWT-based authentication
✅ **Authorization**: Role-based access control
✅ **Audit Trail**: Complete logging of all user actions
✅ **Email Integration**: Password reset and welcome emails
✅ **Password Security**: Policy enforcement and encryption
✅ **API Documentation**: RESTful API with proper error handling

## Next Steps

- Frontend React application development
- Additional user stories implementation
- Performance optimization
- Unit and integration testing
- Production deployment configuration
