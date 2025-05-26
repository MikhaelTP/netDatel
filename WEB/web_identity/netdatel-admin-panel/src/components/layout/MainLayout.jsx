import React, { useState } from 'react';
import { Outlet, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { 
  Users, 
  Shield, 
  Key, 
  Settings, 
  LogOut, 
  Menu, 
  X, 
  Home,
  User,
  ChevronDown,
  Bell,
  Building2,
  UserPlus,
  Package,
  Activity,
  FileText,
  BarChart3
} from 'lucide-react';

const MainLayout = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, logout, isSuperAdmin, hasRole } = useAuth();
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const [userMenuOpen, setUserMenuOpen] = useState(false);

  // Navegación del sidebar
  const navigationItems = [
    {
      name: 'Dashboard',
      href: '/dashboard',
      icon: Home,
      show: true,
    },
    // Sección de Identity Management (existente)
    {
      name: 'Usuarios',
      href: '/users',
      icon: Users,
      show: isSuperAdmin() || hasRole('CLIENT_ADMIN'),
    },
    {
      name: 'Roles',
      href: '/roles',
      icon: Shield,
      show: isSuperAdmin() || hasRole('CLIENT_ADMIN'),
    },
    {
      name: 'Permisos',
      href: '/permissions',
      icon: Key,
      show: isSuperAdmin(),
    },
    // Nueva sección de Admin Management
    {
      name: 'Dashboard Admin',
      href: '/admin-dashboard',
      icon: BarChart3,
      show: isSuperAdmin() || hasRole('CLIENT_ADMIN'),
      separator: true, // Agregar separador visual
    },
    {
      name: 'Registrar Cliente',
      href: '/clients/new',
      icon: UserPlus,
      show: isSuperAdmin() || hasRole('CLIENT_ADMIN'),
    },
    {
      name: 'Gestión Clientes',
      href: '/clients',
      icon: Building2,
      show: isSuperAdmin() || hasRole('CLIENT_ADMIN'),
    },
    {
      name: 'Módulo 1',
      href: '/modules/module-1',
      icon: Package,
      show: isSuperAdmin() || hasRole('CLIENT_ADMIN'),
    },
    {
      name: 'Módulo 2',
      href: '/modules/module-2',
      icon: Package,
      show: isSuperAdmin() || hasRole('CLIENT_ADMIN'),
    },
    {
      name: 'Módulo 3',
      href: '/modules/module-3',
      icon: Package,
      show: isSuperAdmin() || hasRole('CLIENT_ADMIN'),
    },
    {
      name: 'Notificaciones',
      href: '/notifications',
      icon: Bell,
      show: isSuperAdmin() || hasRole('CLIENT_ADMIN'),
    },
    // Configuración (existente)
    {
      name: 'Configuración',
      href: '/settings',
      icon: Settings,
      show: isSuperAdmin(),
      separator: true,
    },
  ];

  const handleLogout = async () => {
    await logout();
    navigate('/login');
  };

  const isActiveRoute = (href) => {
    return location.pathname === href || location.pathname.startsWith(href + '/');
  };

  return (
    <div className="h-screen flex bg-gray-50">
      {/* Sidebar Mobile Overlay */}
      {sidebarOpen && (
        <div 
          className="fixed inset-0 z-40 lg:hidden"
          onClick={() => setSidebarOpen(false)}
        >
          <div className="fixed inset-0 bg-gray-600 bg-opacity-75"></div>
        </div>
      )}

      {/* Sidebar */}
      <div className={`
        fixed inset-y-0 left-0 z-50 w-64 bg-white shadow-soft-lg transform transition-transform duration-300 ease-in-out lg:translate-x-0 lg:static lg:inset-0
        ${sidebarOpen ? 'translate-x-0' : '-translate-x-full'}
      `}>
        <div className="flex items-center justify-between h-16 px-6 border-b border-gray-200">
          <div className="flex items-center space-x-3">
            <div className="w-8 h-8 bg-primary-600 rounded-lg flex items-center justify-center">
              <Shield className="w-5 h-5 text-white" />
            </div>
            <span className="text-xl font-bold text-gray-900">Netdatel</span>
          </div>
          <button
            onClick={() => setSidebarOpen(false)}
            className="lg:hidden text-gray-500 hover:text-gray-700"
          >
            <X className="w-6 h-6" />
          </button>
        </div>

        {/* Navigation */}
        <nav className="mt-6 px-3 overflow-y-auto h-full pb-20">
          <div className="space-y-1">
            {navigationItems
              .filter(item => item.show)
              .map((item, index) => {
                const Icon = item.icon;
                const isActive = isActiveRoute(item.href);
                
                return (
                  <React.Fragment key={item.name}>
                    {/* Separador visual */}
                    {item.separator && index > 0 && (
                      <div className="pt-4 pb-2">
                        <div className="border-t border-gray-200"></div>
                      </div>
                    )}
                    
                    <button
                      onClick={() => {
                        navigate(item.href);
                        setSidebarOpen(false);
                      }}
                      className={`
                        w-full flex items-center px-3 py-3 text-sm font-medium rounded-lg transition-colors duration-200
                        ${isActive 
                          ? 'bg-primary-50 text-primary-700 border-r-2 border-primary-600' 
                          : 'text-gray-700 hover:bg-gray-100'
                        }
                      `}
                    >
                      <Icon className={`w-5 h-5 mr-3 ${isActive ? 'text-primary-700' : 'text-gray-500'}`} />
                      {item.name}
                    </button>
                  </React.Fragment>
                );
              })}
          </div>
        </nav>

        {/* User Info */}
        <div className="absolute bottom-0 left-0 right-0 p-4 border-t border-gray-200 bg-white">
          <div className="flex items-center space-x-3 text-sm">
            <div className="w-8 h-8 bg-primary-100 rounded-full flex items-center justify-center">
              <User className="w-4 h-4 text-primary-600" />
            </div>
            <div className="flex-1 min-w-0">
              <p className="font-medium text-gray-900 truncate">{user?.username}</p>
              <p className="text-gray-500 truncate">{user?.userType}</p>
            </div>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="flex-1 flex flex-col overflow-hidden">
        {/* Top Header */}
        <header className="bg-white shadow-sm border-b border-gray-200">
          <div className="flex items-center justify-between h-16 px-6">
            {/* Mobile menu button */}
            <button
              onClick={() => setSidebarOpen(true)}
              className="lg:hidden text-gray-500 hover:text-gray-700"
            >
              <Menu className="w-6 h-6" />
            </button>

            {/* Page Title */}
            <div className="flex-1 lg:flex-none">
              <h1 className="text-xl font-semibold text-gray-900">
                {(() => {
                  const currentPage = navigationItems.find(item => isActiveRoute(item.href));
                  return currentPage?.name || 'Panel de Administración';
                })()}
              </h1>
            </div>

            {/* Right side actions */}
            <div className="flex items-center space-x-4">
              {/* Notifications */}
              <button 
                className="text-gray-500 hover:text-gray-700 relative"
                onClick={() => navigate('/notifications')}
              >
                <Bell className="w-6 h-6" />
                <span className="absolute -top-1 -right-1 w-3 h-3 bg-red-500 rounded-full"></span>
              </button>

              {/* User Menu */}
              <div className="relative">
                <button
                  onClick={() => setUserMenuOpen(!userMenuOpen)}
                  className="flex items-center space-x-2 text-gray-700 hover:text-gray-900"
                >
                  <div className="w-8 h-8 bg-primary-100 rounded-full flex items-center justify-center">
                    <User className="w-4 h-4 text-primary-600" />
                  </div>
                  <ChevronDown className="w-4 h-4" />
                </button>

                {/* User Dropdown */}
                {userMenuOpen && (
                  <div className="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-soft-lg border border-gray-200 py-2 z-50">
                    <div className="px-4 py-2 border-b border-gray-200">
                      <p className="text-sm font-medium text-gray-900">{user?.username}</p>
                      <p className="text-xs text-gray-500">{user?.email}</p>
                    </div>
                    <button
                      onClick={() => {
                        navigate('/profile');
                        setUserMenuOpen(false);
                      }}
                      className="w-full px-4 py-2 text-left text-sm text-gray-700 hover:bg-gray-100"
                    >
                      Mi Perfil
                    </button>
                    <button
                      onClick={() => {
                        navigate('/settings');
                        setUserMenuOpen(false);
                      }}
                      className="w-full px-4 py-2 text-left text-sm text-gray-700 hover:bg-gray-100"
                    >
                      Configuración
                    </button>
                    <hr className="my-2" />
                    <button
                      onClick={handleLogout}
                      className="w-full px-4 py-2 text-left text-sm text-red-600 hover:bg-red-50 flex items-center space-x-2"
                    >
                      <LogOut className="w-4 h-4" />
                      <span>Cerrar Sesión</span>
                    </button>
                  </div>
                )}
              </div>
            </div>
          </div>
        </header>

        {/* Main Content Area */}
        <main className="flex-1 overflow-auto bg-gray-50 p-6">
          <div className="max-w-7xl mx-auto">
            <Outlet />
          </div>
        </main>
      </div>
    </div>
  );
};

export default MainLayout;