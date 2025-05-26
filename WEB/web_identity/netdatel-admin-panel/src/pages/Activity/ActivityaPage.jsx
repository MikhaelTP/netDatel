import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { 
  Activity, 
  Search, 
  Filter, 
  Download, 
  RefreshCw, 
  User, 
  Shield, 
  Key, 
  Users, 
  FileText, 
  AlertTriangle,
  CheckCircle,
  XCircle,
  Clock,
  Eye
} from 'lucide-react';

const ActivityPage = () => {
  const { user } = useAuth();
  const [activities, setActivities] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [filterAction, setFilterAction] = useState('ALL');
  const [filterUser, setFilterUser] = useState('ALL');
  const [dateRange, setDateRange] = useState('today');
  const [selectedActivity, setSelectedActivity] = useState(null);

  const actionTypes = [
    { value: 'ALL', label: 'Todas las acciones' },
    { value: 'LOGIN', label: 'Inicios de sesión' },
    { value: 'LOGOUT', label: 'Cierres de sesión' },
    { value: 'CREATE', label: 'Creaciones' },
    { value: 'UPDATE', label: 'Actualizaciones' },
    { value: 'DELETE', label: 'Eliminaciones' },
    { value: 'PERMISSION_GRANTED', label: 'Permisos otorgados' },
    { value: 'PERMISSION_REVOKED', label: 'Permisos revocados' },
  ];

  const dateRanges = [
    { value: 'today', label: 'Hoy' },
    { value: 'yesterday', label: 'Ayer' },
    { value: 'week', label: 'Esta semana' },
    { value: 'month', label: 'Este mes' },
    { value: 'all', label: 'Todo el tiempo' },
  ];

  // Datos simulados de actividad
  useEffect(() => {
    loadActivities();
  }, [filterAction, filterUser, dateRange]);

  const loadActivities = async () => {
    try {
      setLoading(true);
      
      // Simulamos datos de actividad
      const mockActivities = [
        {
          id: 1,
          action: 'LOGIN',
          user: { id: 1, username: 'super.admin', firstName: 'Super', lastName: 'Admin' },
          entityType: 'USER',
          entityId: '1',
          timestamp: new Date(Date.now() - 5 * 60 * 1000),
          ipAddress: '192.168.1.100',
          userAgent: 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36',
          success: true,
          details: { loginMethod: 'password' }
        },
        {
          id: 2,
          action: 'CREATE',
          user: { id: 1, username: 'super.admin', firstName: 'Super', lastName: 'Admin' },
          entityType: 'USER',
          entityId: '15',
          timestamp: new Date(Date.now() - 15 * 60 * 1000),
          ipAddress: '192.168.1.100',
          success: true,
          oldValues: null,
          newValues: { username: 'nuevo.usuario', email: 'nuevo@ejemplo.com', userType: 'WORKER' }
        },
        {
          id: 3,
          action: 'UPDATE',
          user: { id: 2, username: 'marlon.admin', firstName: 'Marlon', lastName: 'Admin' },
          entityType: 'ROLE',
          entityId: '5',
          timestamp: new Date(Date.now() - 30 * 60 * 1000),
          ipAddress: '192.168.1.105',
          success: true,
          oldValues: { name: 'ROLE_USER', description: 'Usuario básico' },
          newValues: { name: 'ROLE_USER', description: 'Usuario básico del sistema' }
        },
        {
          id: 4,
          action: 'DELETE',
          user: { id: 1, username: 'super.admin', firstName: 'Super', lastName: 'Admin' },
          entityType: 'PERMISSION',
          entityId: '20',
          timestamp: new Date(Date.now() - 45 * 60 * 1000),
          ipAddress: '192.168.1.100',
          success: true,
          oldValues: { code: 'test:permission:delete', name: 'Permiso de prueba' },
          newValues: null
        },
        {
          id: 5,
          action: 'LOGIN',
          user: { id: 3, username: 'usuario.test', firstName: 'Usuario', lastName: 'Test' },
          entityType: 'USER',
          entityId: '3',
          timestamp: new Date(Date.now() - 60 * 60 * 1000),
          ipAddress: '192.168.1.110',
          success: false,
          details: { reason: 'invalid_password', attempts: 3 }
        },
        {
          id: 6,
          action: 'PERMISSION_GRANTED',
          user: { id: 1, username: 'super.admin', firstName: 'Super', lastName: 'Admin' },
          entityType: 'USER_ROLE',
          entityId: '12',
          timestamp: new Date(Date.now() - 2 * 60 * 60 * 1000),
          ipAddress: '192.168.1.100',
          success: true,
          details: { userId: 5, roleId: 2, roleName: 'ROLE_ADMIN' }
        }
      ];

      setActivities(mockActivities);
    } catch (error) {
      console.error('Error loading activities:', error);
    } finally {
      setLoading(false);
    }
  };

  const filteredActivities = activities.filter(activity => {
    const matchesSearch = 
      activity.user.username.toLowerCase().includes(searchTerm.toLowerCase()) ||
      activity.action.toLowerCase().includes(searchTerm.toLowerCase()) ||
      activity.entityType.toLowerCase().includes(searchTerm.toLowerCase());
    
    const matchesAction = filterAction === 'ALL' || activity.action === filterAction;
    const matchesUser = filterUser === 'ALL' || activity.user.id.toString() === filterUser;
    
    return matchesSearch && matchesAction && matchesUser;
  });

  const getActionIcon = (action) => {
    switch (action) {
      case 'LOGIN': case 'LOGOUT': return User;
      case 'CREATE': case 'UPDATE': case 'DELETE': return FileText;
      case 'PERMISSION_GRANTED': case 'PERMISSION_REVOKED': return Key;
      default: return Activity;
    }
  };

  const getActionColor = (action, success) => {
    if (!success) return 'text-red-500 bg-red-50';
    
    switch (action) {
      case 'LOGIN': return 'text-green-500 bg-green-50';
      case 'LOGOUT': return 'text-gray-500 bg-gray-50';
      case 'CREATE': return 'text-blue-500 bg-blue-50';
      case 'UPDATE': return 'text-yellow-500 bg-yellow-50';
      case 'DELETE': return 'text-red-500 bg-red-50';
      case 'PERMISSION_GRANTED': return 'text-purple-500 bg-purple-50';
      case 'PERMISSION_REVOKED': return 'text-orange-500 bg-orange-50';
      default: return 'text-gray-500 bg-gray-50';
    }
  };

  const formatAction = (action) => {
    const actions = {
      'LOGIN': 'Inicio de sesión',
      'LOGOUT': 'Cierre de sesión',
      'CREATE': 'Creación',
      'UPDATE': 'Actualización',
      'DELETE': 'Eliminación',
      'PERMISSION_GRANTED': 'Permiso otorgado',
      'PERMISSION_REVOKED': 'Permiso revocado'
    };
    return actions[action] || action;
  };

  const formatEntityType = (entityType) => {
    const entities = {
      'USER': 'Usuario',
      'ROLE': 'Rol',
      'PERMISSION': 'Permiso',
      'USER_ROLE': 'Asignación de rol',
      'CLIENT': 'Cliente'
    };
    return entities[entityType] || entityType;
  };

  const formatTimestamp = (timestamp) => {
    const now = new Date();
    const diff = now - timestamp;
    const minutes = Math.floor(diff / 60000);
    const hours = Math.floor(diff / 3600000);
    const days = Math.floor(diff / 86400000);

    if (minutes < 1) return 'Hace un momento';
    if (minutes < 60) return `Hace ${minutes} min`;
    if (hours < 24) return `Hace ${hours} h`;
    if (days < 7) return `Hace ${days} d`;
    
    return timestamp.toLocaleDateString('es-ES', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const ActivityCard = ({ activity }) => {
    const IconComponent = getActionIcon(activity.action);
    const colorClasses = getActionColor(activity.action, activity.success);

    return (
      <div className="card hover:shadow-soft-lg transition-shadow duration-200 cursor-pointer"
           onClick={() => setSelectedActivity(activity)}>
        <div className="flex items-start space-x-4">
          <div className={`w-10 h-10 rounded-full flex items-center justify-center ${colorClasses}`}>
            <IconComponent className="w-5 h-5" />
          </div>
          
          <div className="flex-1 min-w-0">
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-2">
                <h3 className="text-sm font-medium text-gray-900">
                  {formatAction(activity.action)}
                </h3>
                {!activity.success && (
                  <XCircle className="w-4 h-4 text-red-500" />
                )}
              </div>
              <span className="text-xs text-gray-500">
                {formatTimestamp(activity.timestamp)}
              </span>
            </div>
            
            <p className="text-sm text-gray-600 mt-1">
              <span className="font-medium">{activity.user.firstName} {activity.user.lastName}</span>
              {' '}realizó {formatAction(activity.action).toLowerCase()} en{' '}
              <span className="font-medium">{formatEntityType(activity.entityType)}</span>
              {activity.entityId && ` #${activity.entityId}`}
            </p>
            
            <div className="flex items-center space-x-4 mt-2 text-xs text-gray-500">
              <span>IP: {activity.ipAddress}</span>
              {activity.userAgent && (
                <span className="truncate max-w-xs">
                  {activity.userAgent.split(' ')[0]}
                </span>
              )}
            </div>
          </div>
        </div>
      </div>
    );
  };

  const ActivityDetailModal = ({ activity, onClose }) => {
    if (!activity) return null;

    return (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
        <div className="bg-white rounded-lg shadow-xl max-w-2xl w-full max-h-screen overflow-y-auto">
          <div className="p-6">
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-xl font-semibold text-gray-900">
                Detalles de Actividad
              </h2>
              <button
                onClick={onClose}
                className="text-gray-400 hover:text-gray-600"
              >
                <XCircle className="w-6 h-6" />
              </button>
            </div>
            
            <div className="space-y-6">
              {/* Basic Info */}
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Acción
                  </label>
                  <div className="flex items-center space-x-2">
                    <span className="text-sm text-gray-900">{formatAction(activity.action)}</span>
                    {activity.success ? (
                      <CheckCircle className="w-4 h-4 text-green-500" />
                    ) : (
                      <XCircle className="w-4 h-4 text-red-500" />
                    )}
                  </div>
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Usuario
                  </label>
                  <span className="text-sm text-gray-900">
                    {activity.user.firstName} {activity.user.lastName} (@{activity.user.username})
                  </span>
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Tipo de Entidad
                  </label>
                  <span className="text-sm text-gray-900">{formatEntityType(activity.entityType)}</span>
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    ID de Entidad
                  </label>
                  <span className="text-sm text-gray-900">{activity.entityId || 'N/A'}</span>
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Fecha y Hora
                  </label>
                  <span className="text-sm text-gray-900">
                    {activity.timestamp.toLocaleString('es-ES')}
                  </span>
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Dirección IP
                  </label>
                  <span className="text-sm text-gray-900">{activity.ipAddress}</span>
                </div>
              </div>
              
              {/* User Agent */}
              {activity.userAgent && (
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Navegador/Cliente
                  </label>
                  <span className="text-sm text-gray-900 break-all">{activity.userAgent}</span>
                </div>
              )}
              
              {/* Old Values */}
              {activity.oldValues && (
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Valores Anteriores
                  </label>
                  <pre className="bg-gray-50 rounded-lg p-3 text-xs text-gray-700 overflow-x-auto">
                    {JSON.stringify(activity.oldValues, null, 2)}
                  </pre>
                </div>
              )}
              
              {/* New Values */}
              {activity.newValues && (
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Valores Nuevos
                  </label>
                  <pre className="bg-gray-50 rounded-lg p-3 text-xs text-gray-700 overflow-x-auto">
                    {JSON.stringify(activity.newValues, null, 2)}
                  </pre>
                </div>
              )}
              
              {/* Additional Details */}
              {activity.details && (
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-2">
                    Detalles Adicionales
                  </label>
                  <pre className="bg-gray-50 rounded-lg p-3 text-xs text-gray-700 overflow-x-auto">
                    {JSON.stringify(activity.details, null, 2)}
                  </pre>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    );
  };

  if (loading) {
    return (
      <div className="space-y-6">
        <div className="flex justify-between items-center">
          <div className="h-8 bg-gray-200 rounded w-48 animate-pulse"></div>
          <div className="h-10 bg-gray-200 rounded w-32 animate-pulse"></div>
        </div>
        <div className="space-y-4">
          {[...Array(6)].map((_, i) => (
            <div key={i} className="card animate-pulse">
              <div className="h-16 bg-gray-200 rounded"></div>
            </div>
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Actividad del Sistema</h1>
          <p className="mt-1 text-sm text-gray-600">
            Registro de todas las acciones realizadas en el sistema ({filteredActivities.length} actividades)
          </p>
        </div>
        
        <div className="mt-4 sm:mt-0 flex space-x-3">
          <button
            onClick={loadActivities}
            className="btn-secondary flex items-center space-x-2"
          >
            <RefreshCw className="w-4 h-4" />
            <span>Actualizar</span>
          </button>
          
          <button
            className="btn-primary flex items-center space-x-2"
            onClick={() => {
              // Aquí implementarías la exportación
              alert('Funcionalidad de exportación próximamente');
            }}
          >
            <Download className="w-4 h-4" />
            <span>Exportar</span>
          </button>
        </div>
      </div>

      {/* Filters */}
      <div className="card">
        <div className="grid grid-cols-1 lg:grid-cols-5 gap-4">
          <div className="lg:col-span-2">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
              <input
                type="text"
                placeholder="Buscar actividades..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="input pl-10"
              />
            </div>
          </div>
          
          <div>
            <select
              value={filterAction}
              onChange={(e) => setFilterAction(e.target.value)}
              className="input"
            >
              {actionTypes.map(type => (
                <option key={type.value} value={type.value}>
                  {type.label}
                </option>
              ))}
            </select>
          </div>
          
          <div>
            <select
              value={dateRange}
              onChange={(e) => setDateRange(e.target.value)}
              className="input"
            >
              {dateRanges.map(range => (
                <option key={range.value} value={range.value}>
                  {range.label}
                </option>
              ))}
            </select>
          </div>
          
          <div>
            <select
              value={filterUser}
              onChange={(e) => setFilterUser(e.target.value)}
              className="input"
            >
              <option value="ALL">Todos los usuarios</option>
              <option value="1">Super Admin</option>
              <option value="2">Marlon Admin</option>
            </select>
          </div>
        </div>
      </div>

      {/* Activities List */}
      {filteredActivities.length === 0 ? (
        <div className="text-center py-12">
          <Activity className="w-12 h-12 text-gray-300 mx-auto mb-4" />
          <h3 className="text-lg font-medium text-gray-900 mb-2">No se encontraron actividades</h3>
          <p className="text-gray-600">
            {searchTerm || filterAction !== 'ALL' || filterUser !== 'ALL'
              ? 'Intenta ajustar los filtros de búsqueda' 
              : 'No hay actividades registradas aún'
            }
          </p>
        </div>
      ) : (
        <div className="space-y-4">
          {filteredActivities.map(activity => (
            <ActivityCard key={activity.id} activity={activity} />
          ))}
        </div>
      )}

      {/* Activity Detail Modal */}
      <ActivityDetailModal 
        activity={selectedActivity} 
        onClose={() => setSelectedActivity(null)} 
      />
    </div>
  );
};

export default ActivityPage;