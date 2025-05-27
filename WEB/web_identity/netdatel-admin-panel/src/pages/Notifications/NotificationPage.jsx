// src/pages/Notifications/NotificationsPage.jsx
import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { notificationsApi } from '../../services/adminApi';
import { 
  Bell, 
  Search, 
  Filter, 
  RefreshCw, 
  Send, 
  Plus,
  AlertTriangle,
  CheckCircle,
  Clock,
  XCircle,
  Eye,
  RotateCcw,
  Building2,
  Users,
  User
} from 'lucide-react';

const NotificationsPage = () => {
  const { user } = useAuth();
  const [notifications, setNotifications] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [filterStatus, setFilterStatus] = useState('ALL');
  const [filterType, setFilterType] = useState('ALL');
  const [selectedNotification, setSelectedNotification] = useState(null);
  const [showCreateModal, setShowCreateModal] = useState(false);

  const statusOptions = [
    { value: 'ALL', label: 'Todos los estados' },
    { value: 'PENDING', label: 'Pendientes' },
    { value: 'SENT', label: 'Enviadas' },
    { value: 'FAILED', label: 'Fallidas' },
    { value: 'DELIVERED', label: 'Entregadas' },
  ];

  const typeOptions = [
    { value: 'ALL', label: 'Todos los tipos' },
    { value: 'CLIENT', label: 'Clientes' },
    { value: 'ADMINISTRATOR', label: 'Administradores' },
    { value: 'WORKERS', label: 'Trabajadores' },
  ];

  useEffect(() => {
    loadNotifications();
  }, []);

  const loadNotifications = async () => {
    try {
      setLoading(true);
      const params = {};
      
      if (filterStatus !== 'ALL') {
        params.status = filterStatus;
      }
      
      if (filterType !== 'ALL') {
        params.targetType = filterType;
      }

      const response = await notificationsApi.getAll({ params });
      const notificationsData = response.data.content || response.data;
      
      setNotifications(Array.isArray(notificationsData) ? notificationsData : []);
    } catch (error) {
      console.error('Error loading notifications:', error);
      setNotifications([]);
    } finally {
      setLoading(false);
    }
  };

  const handleRetryNotification = async (notificationId) => {
    try {
      await notificationsApi.retry(notificationId);
      loadNotifications(); // Recargar la lista
    } catch (error) {
      console.error('Error retrying notification:', error);
      alert('Error al reintentar la notificación');
    }
  };

  const handleRetryFailedAll = async () => {
    try {
      const response = await notificationsApi.retryFailed();
      alert(`${response.data} notificaciones reintentadas`);
      loadNotifications();
    } catch (error) {
      console.error('Error retrying failed notifications:', error);
      alert('Error al reintentar notificaciones fallidas');
    }
  };

  const filteredNotifications = notifications.filter(notification => {
    const matchesSearch = 
      notification.subject?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      notification.notificationType?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      notification.content?.toLowerCase().includes(searchTerm.toLowerCase());
    
    return matchesSearch;
  });

  const getStatusIcon = (status) => {
    switch (status) {
      case 'SENT': case 'DELIVERED': return CheckCircle;
      case 'PENDING': return Clock;
      case 'FAILED': return XCircle;
      default: return AlertTriangle;
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'SENT': case 'DELIVERED': return 'text-green-500 bg-green-50';
      case 'PENDING': return 'text-yellow-500 bg-yellow-50';
      case 'FAILED': return 'text-red-500 bg-red-50';
      default: return 'text-gray-500 bg-gray-50';
    }
  };

  const getTargetTypeIcon = (targetType) => {
    switch (targetType) {
      case 'CLIENT': return Building2;
      case 'ADMINISTRATOR': return User;
      case 'WORKERS': return Users;
      default: return Bell;
    }
  };

  const formatTimestamp = (timestamp) => {
    return new Date(timestamp).toLocaleString('es-ES', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  const NotificationCard = ({ notification }) => {
    const StatusIcon = getStatusIcon(notification.status);
    const TargetIcon = getTargetTypeIcon(notification.targetType);
    const statusColor = getStatusColor(notification.status);

    return (
      <div className="card hover:shadow-soft-lg transition-shadow duration-200">
        <div className="flex items-start justify-between">
          <div className="flex items-start space-x-4">
            <div className={`w-12 h-12 rounded-full flex items-center justify-center ${statusColor}`}>
              <StatusIcon className="w-6 h-6" />
            </div>
            
            <div className="flex-1 min-w-0">
              <div className="flex items-center space-x-2 mb-1">
                <h3 className="text-lg font-medium text-gray-900 truncate">
                  {notification.subject}
                </h3>
                <div className="flex items-center text-blue-600">
                  <TargetIcon className="w-4 h-4 mr-1" />
                  <span className="text-xs">{notification.targetType}</span>
                </div>
              </div>
              
              <p className="text-sm text-gray-600 mb-2 line-clamp-2">
                {notification.content?.replace(/<[^>]*>/g, '')}
              </p>
              
              <div className="flex items-center space-x-4 text-xs text-gray-500">
                <span>Tipo: {notification.notificationType}</span>
                <span>Enviado: {formatTimestamp(notification.sendDate)}</span>
                {notification.retryCount > 0 && (
                  <span>Reintentos: {notification.retryCount}</span>
                )}
              </div>
              
              {notification.errorMessage && (
                <div className="mt-2 p-2 bg-red-50 border border-red-200 rounded text-xs text-red-700">
                  Error: {notification.errorMessage}
                </div>
              )}
            </div>
          </div>
          
          <div className="flex items-center space-x-2">
            <button
              onClick={() => setSelectedNotification(notification)}
              className="p-2 text-gray-400 hover:text-gray-600 rounded-lg hover:bg-gray-100"
            >
              <Eye className="w-4 h-4" />
            </button>
            
            {notification.status === 'FAILED' && (
              <button
                onClick={() => handleRetryNotification(notification.id)}
                className="p-2 text-orange-400 hover:text-orange-600 rounded-lg hover:bg-orange-50"
              >
                <RotateCcw className="w-4 h-4" />
              </button>
            )}
          </div>
        </div>
      </div>
    );
  };

  const NotificationDetailModal = ({ notification, onClose }) => {
    if (!notification) return null;

    return (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
        <div className="bg-white rounded-lg shadow-xl max-w-2xl w-full max-h-screen overflow-y-auto">
          <div className="p-6">
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-xl font-semibold text-gray-900">
                Detalles de Notificación
              </h2>
              <button
                onClick={onClose}
                className="text-gray-400 hover:text-gray-600"
              >
                <XCircle className="w-6 h-6" />
              </button>
            </div>
            
            <div className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Estado
                  </label>
                  <span className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${getStatusColor(notification.status)}`}>
                    {notification.status}
                  </span>
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Tipo de Destinatario
                  </label>
                  <span className="text-sm text-gray-900">{notification.targetType}</span>
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Tipo de Notificación
                  </label>
                  <span className="text-sm text-gray-900">{notification.notificationType}</span>
                </div>
                
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Fecha de Envío
                  </label>
                  <span className="text-sm text-gray-900">
                    {formatTimestamp(notification.sendDate)}
                  </span>
                </div>
              </div>
              
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Asunto
                </label>
                <p className="text-sm text-gray-900">{notification.subject}</p>
              </div>
              
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Contenido
                </label>
                <div 
                  className="text-sm text-gray-900 bg-gray-50 rounded-lg p-3"
                  dangerouslySetInnerHTML={{ __html: notification.content }}
                />
              </div>
              
              {notification.errorMessage && (
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">
                    Error
                  </label>
                  <p className="text-sm text-red-700 bg-red-50 rounded-lg p-3">
                    {notification.errorMessage}
                  </p>
                </div>
              )}
              
              {notification.retryCount > 0 && (
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Número de Reintentos
                    </label>
                    <span className="text-sm text-gray-900">{notification.retryCount}</span>
                  </div>
                  
                  {notification.lastRetry && (
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Último Reintento
                      </label>
                      <span className="text-sm text-gray-900">
                        {formatTimestamp(notification.lastRetry)}
                      </span>
                    </div>
                  )}
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
              <div className="h-24 bg-gray-200 rounded"></div>
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
          <h1 className="text-2xl font-bold text-gray-900">Gestión de Notificaciones</h1>
          <p className="mt-1 text-sm text-gray-600">
            Administra y supervisa las notificaciones del sistema ({filteredNotifications.length} notificaciones)
          </p>
        </div>
        
        <div className="mt-4 sm:mt-0 flex space-x-3">
          <button
            onClick={handleRetryFailedAll}
            className="btn-secondary flex items-center space-x-2"
          >
            <RotateCcw className="w-4 h-4" />
            <span>Reintentar Fallidas</span>
          </button>
          
          <button
            onClick={loadNotifications}
            className="btn-secondary flex items-center space-x-2"
          >
            <RefreshCw className="w-4 h-4" />
            <span>Actualizar</span>
          </button>
          
          <button
            onClick={() => setShowCreateModal(true)}
            className="btn-primary flex items-center space-x-2"
          >
            <Plus className="w-4 h-4" />
            <span>Nueva Notificación</span>
          </button>
        </div>
      </div>

      {/* Filters */}
      <div className="card">
        <div className="grid grid-cols-1 lg:grid-cols-4 gap-4">
          <div className="lg:col-span-2">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
              <input
                type="text"
                placeholder="Buscar notificaciones..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="input pl-10"
              />
            </div>
          </div>
          
          <div>
            <select
              value={filterStatus}
              onChange={(e) => setFilterStatus(e.target.value)}
              className="input"
            >
              {statusOptions.map(option => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>
          
          <div>
            <select
              value={filterType}
              onChange={(e) => setFilterType(e.target.value)}
              className="input"
            >
              {typeOptions.map(option => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>
        </div>
      </div>

      {/* Notifications List */}
      {filteredNotifications.length === 0 ? (
        <div className="text-center py-12">
          <Bell className="w-12 h-12 text-gray-300 mx-auto mb-4" />
          <h3 className="text-lg font-medium text-gray-900 mb-2">No se encontraron notificaciones</h3>
          <p className="text-gray-600">
            {searchTerm || filterStatus !== 'ALL' || filterType !== 'ALL'
              ? 'Intenta ajustar los filtros de búsqueda' 
              : 'No hay notificaciones registradas aún'
            }
          </p>
        </div>
      ) : (
        <div className="space-y-4">
          {filteredNotifications.map(notification => (
            <NotificationCard key={notification.id} notification={notification} />
          ))}
        </div>
      )}

      {/* Notification Detail Modal */}
      <NotificationDetailModal 
        notification={selectedNotification} 
        onClose={() => setSelectedNotification(null)} 
      />
    </div>
  );
};

export default NotificationsPage;