# Onboarding Platform Frontend

React TypeScript frontend for the Onboarding Platform that tests all backend functionality.

## 🚀 Features Implemented

### User Stories Testing
- **US 1.1**: User account creation with complete profile management
- **US 1.2**: Role assignment and management (ADMINISTRATEUR, MANAGER, COLLABORATEUR)
- **US 1.3**: User authentication with JWT tokens

### Key Features
- ✅ **Secure Authentication**: JWT-based login/logout (US 1.3)
- ✅ **User Creation**: Complete user creation with profile fields (US 1.1)
- ✅ **Role Assignment**: Change user roles dynamically (US 1.2)
- ✅ **User Listing**: View all users in table format
- ✅ **Role-based Access Control**: Admin-only user management
- ✅ **Responsive Design**: Works on desktop and mobile

## 🛠 Technology Stack

- **React 18** with TypeScript
- **React Router** for navigation
- **Axios** for API communication
- **CSS3** with modern flexbox/grid layouts
- **ESLint** for code quality

## 📁 Project Structure

```
frontend/
├── src/
│   ├── components/
│   │   ├── Login.tsx          # Authentication page
│   │   ├── Login.css          # Login page styles
│   │   ├── Dashboard.tsx      # Main user management interface
│   │   └── Dashboard.css      # Dashboard styles
│   ├── contexts/
│   │   └── AuthContext.tsx    # Authentication state management
│   ├── services/
│   │   └── api.ts             # API service layer
│   ├── App.tsx                # Main application component
│   └── App.css                # Global styles
├── package.json
└── README.md
```

## 🎯 Testing Scenarios

### Authentication Testing
1. **Login**: Use existing admin credentials
   - Email: `admin@test.com`
   - Password: `Admin123!`

2. **JWT Token Management**: Automatic token handling
3. **Protected Routes**: Redirect to login if not authenticated

### User Management Testing (Admin Only)
1. **Create Users**: Complete user creation form with all required fields
2. **Role Assignment**: Change user roles dynamically (ADMINISTRATEUR, MANAGER, COLLABORATEUR)
3. **User Listing**: View all users in a clean table format

### Role-based Testing
- **ADMINISTRATEUR**: Full access to all user management features
- **MANAGER**: View users (limited functionality in demo)
- **COLLABORATEUR**: Basic access (limited functionality in demo)

## 🔧 Setup Instructions

### Prerequisites
- Node.js 16+ installed
- Backend server running on `http://localhost:8082`

### Installation
```bash
cd frontend
npm install
```

### Running the Application
```bash
npm start
```

The app will open at `http://localhost:3000`

### Environment Configuration
The app is configured with a proxy to the backend in `package.json`:
```json
"proxy": "http://localhost:8082"
```

## 🧪 Testing Guide

### 1. Start Both Applications
```bash
# Backend (in one terminal)
cd backend
mvn spring-boot:run

# Frontend (in another terminal)
cd frontend
npm start
```

### 2. Test Authentication
1. Navigate to `http://localhost:3000`
2. Login with admin credentials
3. Verify JWT token is stored and used

### 3. Test User Management (Admin)
1. Click "Créer un utilisateur"
2. Fill in all user fields (prenom, nom, email, role, etc.)
3. Submit and verify user appears in table
4. Test role changes via dropdown
5. Verify role-based access control

### 4. Test API Integration
All frontend actions make real API calls to the SpringBoot backend:
- `POST /api/auth/login` - Authentication (US 1.3)
- `GET /api/users` - List users
- `POST /api/users` - Create user (US 1.1)
- `PUT /api/users/{id}/role` - Update role (US 1.2)

## 🎨 UI Features

### Login Page
- Clean, modern design with gradient background
- Form validation and error handling
- Responsive layout

### Dashboard
- Clean table layout with user data
- Modal form for user creation
- Role-based UI elements
- Role assignment dropdown
- Professional styling

### Responsive Design
- Mobile-friendly interface
- Adaptive layouts for different screen sizes
- Touch-friendly controls

## 🔐 Security Features

- JWT token-based authentication
- Automatic token refresh
- Protected routes with redirects
- Role-based access control
- Secure API communication

## 📊 Backend Integration

The frontend fully integrates with the SpringBoot backend:
- **Authentication**: JWT login/logout flow
- **User Management**: Complete CRUD operations
- **Role Management**: Dynamic role assignment
- **Status Management**: Account activation/deactivation
- **Error Handling**: Proper error display and user feedback

## 🚀 Next Steps

This frontend demonstrates full integration with your SpringBoot backend. You can:

1. **Extend Features**: Add password reset, user profiles, etc.
2. **Enhance UI**: Add animations, charts, better UX
3. **Add Testing**: Unit tests, integration tests
4. **Deploy**: Build for production deployment
5. **Mobile App**: Use same API for mobile development

## 📝 Notes

- The backend must be running for the frontend to work
- Admin user is pre-configured for testing
- Email functionality is configured but may need real SMTP settings
- All user stories (US 1.1, US 1.2, US 1.3) are fully testable
