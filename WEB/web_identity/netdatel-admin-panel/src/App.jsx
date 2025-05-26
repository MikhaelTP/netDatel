import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import MainLayout from './components/layout/MainLayout';
import Login from './pages/Login';
import Dashboard from './pages/DashBoard';
import UsersPage from './pages/Users/UserPages';
import UserForm from './pages/Users/UserForm';
import RolesPage from './pages/Roles/RolesPage';
import RoleForm from './pages/Roles/RolesForm';
import PermissionsPage from './pages/Permissions/PermissionPage';
import PermissionForm from './pages/Permissions/PermissionForm';
import SettingsPage from './pages/Settings/SettingsPage';
import ActivityPage from './pages/Activity/ActivityaPage';
import ProfilePage from './pages/Profile/ProfilePage';
import './styles/global.css';

// Componente para proteger rutas
const ProtectedRoute = ({ children }) => {
  const { status } = useAuth();
  
  if (status === 'loading') {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-primary-600"></div>
      </div>
    );
  }
  
  if (status === 'unauthenticated') {
    return <Navigate to="/login" replace />;
  }
  
  return children;
};

// Componente para rutas públicas (como login)
const PublicRoute = ({ children }) => {
  const { status } = useAuth();
  
  if (status === 'loading') {
    return (
      <div className="min-h-screen flex items-center justify-center">
        <div className="animate-spin rounded-full h-32 w-32 border-b-2 border-primary-600"></div>
      </div>
    );
  }
  
  if (status === 'authenticated') {
    return <Navigate to="/dashboard" replace />;
  }
  
  return children;
};

// Componente para proteger rutas de super admin
const SuperAdminRoute = ({ children }) => {
  const { isSuperAdmin } = useAuth();
  
  if (!isSuperAdmin()) {
    return (
      <div className="text-center py-12">
        <div className="text-red-500 text-6xl mb-4">🚫</div>
        <h1 className="text-2xl font-bold text-gray-900 mb-4">Acceso Denegado</h1>
        <p className="text-gray-600">No tienes permisos para acceder a esta sección.</p>
      </div>
    );
  }
  
  return children;
};


function App() {
  return (
    <AuthProvider>
      <Router>
        <Routes>
          {/* Rutas públicas */}
          <Route 
            path="/login" 
            element={
              <PublicRoute>
                <Login />
              </PublicRoute>
            } 
          />
          
          {/* Rutas protegidas */}
          <Route 
            path="/" 
            element={
              <ProtectedRoute>
                <MainLayout />
              </ProtectedRoute>
            }
          >
            {/* Dashboard */}
            <Route path="dashboard" element={<Dashboard />} />
            
            {/* Usuarios */}
            <Route path="users" element={<UsersPage />} />
            <Route path="users/new" element={
              <SuperAdminRoute>
                <UserForm />
              </SuperAdminRoute>
            } />
            <Route path="users/:id" element={<UserForm />} />
            <Route path="users/:id/edit" element={
              <SuperAdminRoute>
                <UserForm />
              </SuperAdminRoute>
            } />
            
            {/* Roles */}
            <Route path="roles" element={<RolesPage />} />
            <Route path="roles/new" element={
              <SuperAdminRoute>
                <RoleForm />
              </SuperAdminRoute>
            } />
            <Route path="roles/:id" element={<RoleForm />} />
            <Route path="roles/:id/edit" element={
              <SuperAdminRoute>
                <RoleForm />
              </SuperAdminRoute>
            } />
            
            {/* Permisos - Solo Super Admin */}
            <Route path="permissions" element={
              <SuperAdminRoute>
                <PermissionsPage />
              </SuperAdminRoute>
            } />
            <Route path="permissions/new" element={
              <SuperAdminRoute>
                <PermissionForm />
              </SuperAdminRoute>
            } />
            <Route path="permissions/:id/edit" element={
              <SuperAdminRoute>
                <PermissionForm />
              </SuperAdminRoute>
            } />
            
            {/* Configuración - Solo Super Admin */}
            <Route path="settings" element={
              <SuperAdminRoute>
                <SettingsPage />
              </SuperAdminRoute>
            } />
            
            {/* Perfil de usuario - Todos los usuarios autenticados */}
            <Route path="profile" element={<ProfilePage />} />
            
            {/* Actividad del sistema - Todos los usuarios autenticados */}
            <Route path="activity" element={<ActivityPage />} />
            
            {/* Redirect root to dashboard */}
            <Route path="" element={<Navigate to="/dashboard" replace />} />
          </Route>
          
          {/* Catch all - redirect to dashboard */}
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </Router>
    </AuthProvider>
  );
}
export default App;