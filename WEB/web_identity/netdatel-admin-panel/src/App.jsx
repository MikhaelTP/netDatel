import React from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider, useAuth } from './context/AuthContext';
import MainLayout from './components/layout/MainLayout';
import Login from './pages/Login';
import Dashboard from './pages/DashBoard';
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

// Páginas temporales para las rutas que aún no hemos creado
const ComingSoon = ({ title }) => (
  <div className="text-center py-12">
    <h1 className="text-3xl font-bold text-gray-900 mb-4">{title}</h1>
    <p className="text-gray-600">Esta funcionalidad estará disponible pronto.</p>
  </div>
);

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
            <Route path="users" element={<ComingSoon title="Gestión de Usuarios" />} />
            <Route path="users/new" element={<ComingSoon title="Crear Usuario" />} />
            <Route path="users/:id" element={<ComingSoon title="Editar Usuario" />} />
            
            {/* Roles */}
            <Route path="roles" element={<ComingSoon title="Gestión de Roles" />} />
            <Route path="roles/new" element={<ComingSoon title="Crear Rol" />} />
            <Route path="roles/:id" element={<ComingSoon title="Editar Rol" />} />
            
            {/* Permisos */}
            <Route path="permissions" element={<ComingSoon title="Gestión de Permisos" />} />
            <Route path="permissions/new" element={<ComingSoon title="Crear Permiso" />} />
            
            {/* Configuración */}
            <Route path="settings" element={<ComingSoon title="Configuración" />} />
            <Route path="profile" element={<ComingSoon title="Mi Perfil" />} />
            <Route path="activity" element={<ComingSoon title="Actividad del Sistema" />} />
            
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