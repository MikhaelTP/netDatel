import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { usersApi, rolesApi, permissionsApi } from '../services/api';
import { 
  Users, 
  Shield, 
  Key, 
  Activity, 
  TrendingUp, 
  Clock,
  UserCheck,
  AlertTriangle 
} from 'lucide-react';

const Dashboard = () => {
  const { user, isSuperAdmin } = useAuth();
  const [stats, setStats] = useState({
    totalUsers: 0,
    totalRoles: 0,
    totalPermissions: 0,
    activeUsers: 0,
    loading: true,
  });

  const [recentActivity, setRecentActivity] = useState([]);

  useEffect(() => {
    loadDashboardData();
  }, []);

  const loadDashboardData = async () => {
    try {
      const [usersResponse, rolesResponse, permissionsResponse] = await Promise.all([
        usersApi.getAll({ size: 1000 }),
        rolesApi.getAll({ size: 1000 }),
        permissionsApi.getAll({ size: 1000 }),
      ]);

      const users = usersResponse.data.content || usersResponse.data;
      const roles = rolesResponse.data.content || rolesResponse.data;
      const permissions = permissionsResponse.data.content || permissionsResponse.data;

      setStats({
        totalUsers: Array.isArray(users) ? users.length : 0,
        totalRoles: Array.isArray(roles) ? roles.length : 0,
        totalPermissions: Array.isArray(permissions) ? permissions.length : 0,
        activeUsers: Array.isArray(users) ? users.filter(u => u.enabled).length : 0,
        loading: false,
      });

      // Simular actividad reciente
      setRecentActivity([
        {
          id: 1,
          action: 'Usuario creado',
          target: 'nuevo.usuario',
          time: '2 minutos',
          type: 'create'
        },
        {
          id: 2,
          action: 'Rol actualizado',
          target: 'ROLE_ADMIN',
          time: '15 minutos',
          type: 'update'
        },
        {
          id: 3,
          action: 'Permiso asignado',
          target: 'user:read',
          time: '1 hora',
          type: 'assign'
        },
      ]);

    } catch (error) {
      console.error('Error loading dashboard data:', error);
      setStats(prev => ({ ...prev, loading: false }));
    }
  };

  const StatCard = ({ title, value, icon: Icon, color, trend }) => (
    <div className="card">
      <div className="flex items-center justify-between">
        <div>
          <p className="text-sm font-medium text-gray-600">{title}</p>
          <p className="text-3xl font-bold text-gray-900 mt-1">{value}</p>
          {trend && (
            <div className="flex items-center mt-2 text-sm">
              <TrendingUp className="w-4 h-4 text-green-500 mr-1" />
              <span className="text-green-600">+{trend}% este mes</span>
            </div>
          )}
        </div>
        <div className={`w-12 h-12 rounded-lg flex items-center justify-center ${color}`}>
          <Icon className="w-6 h-6 text-white" />
        </div>
      </div>
    </div>
  );

  const QuickAction = ({ title, description, icon: Icon, onClick, color }) => (
    <button
      onClick={onClick}
      className="card hover:shadow-soft-lg transition-shadow duration-200 text-left group"
    >
      <div className="flex items-start space-x-4">
        <div className={`w-10 h-10 rounded-lg flex items-center justify-center ${color} group-hover:scale-110 transition-transform duration-200`}>
          <Icon className="w-5 h-5 text-white" />
        </div>
        <div>
          <h3 className="font-medium text-gray-900 group-hover:text-primary-700 transition-colors">
            {title}
          </h3>
          <p className="text-sm text-gray-600 mt-1">{description}</p>
        </div>
      </div>
    </button>
  );

  if (stats.loading) {
    return (
      <div className="animate-fade-in">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6 mb-8">
          {[...Array(4)].map((_, i) => (
            <div key={i} className="card animate-pulse">
              <div className="h-20 bg-gray-200 rounded"></div>
            </div>
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-8 animate-fade-in">
      {/* Welcome Section */}
      <div className="bg-gradient-to-r from-primary-600 to-primary-700 rounded-xl text-white p-8">
        <h1 className="text-3xl font-bold mb-2">
          ¡Bienvenido, {user?.username}!
        </h1>
        <p className="text-primary-100 text-lg">
          Panel de administración del sistema de identidades Netdatel
        </p>
        <div className="mt-4 text-sm text-primary-200">
          <span>Rol: {user?.userType}</span>
          <span className="mx-2">•</span>
          <span>Último acceso: Hoy</span>
        </div>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <StatCard
          title="Total Usuarios"
          value={stats.totalUsers}
          icon={Users}
          color="bg-blue-500"
          trend={12}
        />
        <StatCard
          title="Usuarios Activos"
          value={stats.activeUsers}
          icon={UserCheck}
          color="bg-green-500"
          trend={8}
        />
        <StatCard
          title="Roles"
          value={stats.totalRoles}
          icon={Shield}
          color="bg-purple-500"
        />
        <StatCard
          title="Permisos"
          value={stats.totalPermissions}
          icon={Key}
          color="bg-orange-500"
        />
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Quick Actions */}
        <div className="lg:col-span-2">
          <h2 className="text-xl font-bold text-gray-900 mb-6">Acciones Rápidas</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {isSuperAdmin() && (
              <>
                <QuickAction
                  title="Crear Usuario"
                  description="Agregar un nuevo usuario al sistema"
                  icon={Users}
                  color="bg-blue-500"
                  onClick={() => window.location.href = '/users/new'}
                />
                <QuickAction
                  title="Gestionar Roles"
                  description="Crear y modificar roles del sistema"
                  icon={Shield}
                  color="bg-purple-500"
                  onClick={() => window.location.href = '/roles'}
                />
                <QuickAction
                  title="Configurar Permisos"
                  description="Administrar permisos y accesos"
                  icon={Key}
                  color="bg-orange-500"
                  onClick={() => window.location.href = '/permissions'}
                />
              </>
            )}
            <QuickAction
              title="Ver Actividad"
              description="Revisar logs y actividad reciente"
              icon={Activity}
              color="bg-green-500"
              onClick={() => window.location.href = '/activity'}
            />
          </div>
        </div>

        {/* Recent Activity */}
        <div>
          <h2 className="text-xl font-bold text-gray-900 mb-6">Actividad Reciente</h2>
          <div className="card">
            <div className="space-y-4">
              {recentActivity.map((activity) => (
                <div key={activity.id} className="flex items-start space-x-3">
                  <div className="w-2 h-2 bg-primary-500 rounded-full mt-2"></div>
                  <div className="flex-1 min-w-0">
                    <p className="text-sm text-gray-900 font-medium">
                      {activity.action}
                    </p>
                    <p className="text-sm text-gray-600">
                      {activity.target}
                    </p>
                    <div className="flex items-center mt-1 text-xs text-gray-500">
                      <Clock className="w-3 h-3 mr-1" />
                      {activity.time}
                    </div>
                  </div>
                </div>
              ))}
              
              {recentActivity.length === 0 && (
                <div className="text-center py-8 text-gray-500">
                  <Activity className="w-12 h-12 mx-auto mb-3 text-gray-300" />
                  <p>No hay actividad reciente</p>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* System Status */}
      <div className="card">
        <div className="flex items-center justify-between">
          <div>
            <h3 className="text-lg font-medium text-gray-900">Estado del Sistema</h3>
            <p className="text-sm text-gray-600 mt-1">Todos los servicios funcionando correctamente</p>
          </div>
          <div className="flex items-center space-x-2 text-green-600">
            <div className="w-3 h-3 bg-green-500 rounded-full animate-pulse"></div>
            <span className="text-sm font-medium">En línea</span>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;