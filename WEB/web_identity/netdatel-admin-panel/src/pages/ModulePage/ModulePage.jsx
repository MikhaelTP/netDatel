import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { clientsApi, modulesApi } from '../../services/adminApi';
import { 
  Package, 
  Building2, 
  Search, 
  Filter, 
  Phone,
  Eye, 
  Users,
  Calendar,
  CheckCircle,
  Clock,
  XCircle,
  AlertTriangle,
  RefreshCw,
  FileText,
  BarChart3,
  Mail,
  Send,
  AlertCircle,
  Check
} from 'lucide-react';

const ModulePage = ({ moduleType, title }) => {
  const navigate = useNavigate();
  const [clients, setClients] = useState([]);
  const [moduleInfo, setModuleInfo] = useState(null);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [filterStatus, setFilterStatus] = useState('ALL');
  const [sendingNotifications, setSendingNotifications] = useState(new Set());
  const [stats, setStats] = useState({
    total: 0,
    active: 0,
    inactive: 0,
    expired: 0
  });

  const moduleDescriptions = {
    'MOD1': {
      name: 'Módulo 1 - Documentos Básicos',
      description: 'Gestión básica de documentos y archivos',
      features: ['Almacenamiento de documentos', 'Organización por carpetas', 'Búsqueda básica'],
      color: 'bg-blue-500'
    },
    'MOD2': {
      name: 'Módulo 2 - Documentos Avanzados',
      description: 'Gestión avanzada con funcionalidades adicionales',
      features: ['Flujos de trabajo', 'Versionado de documentos', 'Colaboración en tiempo real'],
      color: 'bg-green-500'
    },
    'MOD3': {
      name: 'Módulo 3 - Gestión de Proveedores',
      description: 'Sistema completo para gestión de proveedores y auditorías',
      features: ['Registro de proveedores', 'Auditorías', 'Evaluaciones de desempeño'],
      color: 'bg-purple-500'
    }
  };

  const statusOptions = [
    { value: 'ALL', label: 'Todos los estados' },
    { value: 'ACTIVE', label: 'Activos' },
    { value: 'INACTIVE', label: 'Inactivos' },
    { value: 'PENDING', label: 'Pendientes' },
    { value: 'EXPIRED', label: 'Expirados' },
  ];

  useEffect(() => {
    loadModuleClients();
    loadModuleInfo();
  }, [moduleType]);

  const loadModuleClients = async () => {
    try {
      setLoading(true);
      
      // Cargar todos los clientes y filtrar por módulo
      const response = await clientsApi.getAll({ size: 200 });
      const allClients = response.data.content || response.data;
      
      // Filtrar clientes que tengan el módulo específico
      const moduleClients = allClients.filter(client => 
        client.modules && client.modules.some(module => 
          module.moduleCode === moduleType || 
          (module.module && module.module.code === moduleType)
        )
      );
      
      setClients(moduleClients);
      
      // Calcular estadísticas
      const moduleStats = moduleClients.reduce((acc, client) => {
        const clientModule = client.modules.find(m => 
          m.moduleCode === moduleType || 
          (m.module && m.module.code === moduleType)
        );
        
        acc.total++;
        if (clientModule) {
          switch (clientModule.status) {
            case 'ACTIVE': acc.active++; break;
            case 'INACTIVE': acc.inactive++; break;
            case 'EXPIRED': acc.expired++; break;
            default: acc.inactive++;
          }
        }
        return acc;
      }, { total: 0, active: 0, inactive: 0, expired: 0 });
      
      setStats(moduleStats);
    } catch (error) {
      console.error('Error loading module clients:', error);
      setClients([]);
    } finally {
      setLoading(false);
    }
  };

  const loadModuleInfo = async () => {
    try {
      const response = await modulesApi.getByCode(moduleType);
      setModuleInfo(response.data);
    } catch (error) {
      console.error('Error loading module info:', error);
      setModuleInfo(moduleDescriptions[moduleType]);
    }
  };

  const handleSendNotification = async (clientId) => {
    try {
      setSendingNotifications(prev => new Set(prev).add(clientId));
      
      // Simular llamada a API para enviar notificación
      // await clientsApi.sendModuleNotification(clientId, moduleType);
      
      // Actualizar el estado del cliente localmente
      setClients(prev => prev.map(client => 
        client.id === clientId 
          ? { ...client, notificationSent: true, notificationDate: new Date().toISOString() }
          : client
      ));
      
      // Aquí iría la llamada real a la API
      console.log(`Enviando notificación a cliente ${clientId} para módulo ${moduleType}`);
      
    } catch (error) {
      console.error('Error sending notification:', error);
    } finally {
      setSendingNotifications(prev => {
        const newSet = new Set(prev);
        newSet.delete(clientId);
        return newSet;
      });
    }
  };

  const filteredClients = clients.filter(client => {
    const matchesSearch = client.businessName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         client.commercialName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         client.ruc?.includes(searchTerm) ||
                         client.code?.toLowerCase().includes(searchTerm.toLowerCase());
    
    if (filterStatus === 'ALL') return matchesSearch;
    
    const clientModule = client.modules?.find(m => 
      m.moduleCode === moduleType || 
      (m.module && m.module.code === moduleType)
    );
    
    return matchesSearch && clientModule?.status === filterStatus;
  });

  const getStatusIcon = (status) => {
    switch (status) {
      case 'ACTIVE': return CheckCircle;
      case 'INACTIVE': return Clock;
      case 'EXPIRED': return XCircle;
      case 'PENDING': return AlertTriangle;
      default: return AlertTriangle;
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'ACTIVE': return 'bg-green-100 text-green-800';
      case 'INACTIVE': return 'bg-gray-100 text-gray-800';
      case 'EXPIRED': return 'bg-red-100 text-red-800';
      case 'PENDING': return 'bg-yellow-100 text-yellow-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  const StatCard = ({ title, value, icon: Icon, color, description }) => (
    <div className="card">
      <div className="flex items-center justify-between">
        <div>
          <p className="text-sm font-medium text-gray-600">{title}</p>
          <p className="text-3xl font-bold text-gray-900 mt-1">{value}</p>
          {description && (
            <p className="text-xs text-gray-500 mt-1">{description}</p>
          )}
        </div>
        <div className={`w-12 h-12 rounded-lg flex items-center justify-center ${color}`}>
          <Icon className="w-6 h-6 text-white" />
        </div>
      </div>
    </div>
  );

  // ✅ Función para parsear features JSON de la API
  const parseFeatures = (featuresData) => {
    if (Array.isArray(featuresData)) {
      return featuresData;
    }
    
    if (typeof featuresData === 'string') {
      try {
        const parsed = JSON.parse(featuresData);
        const features = [];
        if (parsed.storage) {
          features.push(`Almacenamiento: ${parsed.storage}`);
        }
        if (parsed.permissions && Array.isArray(parsed.permissions)) {
          features.push(`Permisos: ${parsed.permissions.join(', ')}`);
        }
        return features.length > 0 ? features : null;
      } catch (error) {
        console.warn('Error parsing features JSON:', error);
        return null;
      }
    }
    
    return null;
  };

  if (loading) {
    return (
      <div className="space-y-6">
        <div className="flex justify-between items-center">
          <div className="h-8 bg-gray-200 rounded w-48 animate-pulse"></div>
          <div className="h-10 bg-gray-200 rounded w-32 animate-pulse"></div>
        </div>
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
          {[...Array(4)].map((_, i) => (
            <div key={i} className="card animate-pulse">
              <div className="h-20 bg-gray-200 rounded"></div>
            </div>
          ))}
        </div>
        <div className="card animate-pulse">
          <div className="h-96 bg-gray-200 rounded"></div>
        </div>
      </div>
    );
  }

  const currentModule = {
    ...moduleDescriptions[moduleType],
    ...(moduleInfo || {}),
    features: parseFeatures(moduleInfo?.features) || moduleDescriptions[moduleType]?.features || []
  };

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">{title}</h1>
          <p className="mt-1 text-sm text-gray-600">
            {currentModule?.description}
          </p>
          <p className="mt-1 text-sm text-gray-500">
            {filteredClients.length} clientes con este módulo
          </p>
        </div>
        
        <div className="mt-4 sm:mt-0 flex space-x-3">
          <button
            onClick={loadModuleClients}
            className="btn-secondary flex items-center space-x-2"
          >
            <RefreshCw className="w-4 h-4" />
            <span>Actualizar</span>
          </button>
        </div>
      </div>

      {/* Module Info */}
      <div className="card">
        <div className="flex items-start space-x-6">
          <div className={`w-16 h-16 rounded-xl flex items-center justify-center ${currentModule?.color || 'bg-gray-500'}`}>
            <Package className="w-8 h-8 text-white" />
          </div>
          
          <div className="flex-1">
            <h3 className="text-lg font-medium text-gray-900 mb-2">
              {currentModule?.name}
            </h3>
            
            {currentModule?.features && Array.isArray(currentModule.features) && currentModule.features.length > 0 && (
              <div>
                <p className="text-sm text-gray-600 mb-3">Características principales:</p>
                <div className="flex flex-wrap gap-2">
                  {currentModule.features.map((feature, index) => (
                    <span key={index} className="inline-flex items-center px-3 py-1 rounded-full text-sm bg-gray-100 text-gray-700">
                      {feature}
                    </span>
                  ))}
                </div>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
        <StatCard
          title="Total Clientes"
          value={stats.total}
          icon={Building2}
          color="bg-blue-500"
          description={`Con ${moduleType}`}
        />
        <StatCard
          title="Activos"
          value={stats.active}
          icon={CheckCircle}
          color="bg-green-500"
          description="Funcionando"
        />
        <StatCard
          title="Inactivos"
          value={stats.inactive}
          icon={Clock}
          color="bg-gray-500"
          description="Pausados"
        />
        <StatCard
          title="Expirados"
          value={stats.expired}
          icon={XCircle}
          color="bg-red-500"
          description="Vencidos"
        />
      </div>

      {/* Filters */}
      <div className="card">
        <div className="flex flex-col lg:flex-row lg:items-center space-y-4 lg:space-y-0 lg:space-x-4">
          <div className="flex-1">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
              <input
                type="text"
                placeholder="Buscar clientes..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="input pl-10"
              />
            </div>
          </div>
          
          <div className="flex items-center space-x-2">
            <Filter className="w-5 h-5 text-gray-400" />
            <select
              value={filterStatus}
              onChange={(e) => setFilterStatus(e.target.value)}
              className="input min-w-0 sm:w-48"
            >
              {statusOptions.map(option => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>
        </div>
      </div>

      {/* Clients Table */}
      {filteredClients.length === 0 ? (
        <div className="text-center py-12">
          <Package className="w-12 h-12 text-gray-300 mx-auto mb-4" />
          <h3 className="text-lg font-medium text-gray-900 mb-2">
            No se encontraron clientes con {moduleType}
          </h3>
          <p className="text-gray-600 mb-6">
            {searchTerm || filterStatus !== 'ALL' 
              ? 'Intenta ajustar los filtros de búsqueda' 
              : `No hay clientes registrados con el ${moduleType} aún`
            }
          </p>
          {!searchTerm && filterStatus === 'ALL' && (
            <button
              onClick={() => navigate('/clients/new')}
              className="btn-primary"
            >
              Registrar Cliente con {moduleType}
            </button>
          )}
        </div>
      ) : (
        <div className="card overflow-hidden">
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-gray-200">
              <thead className="bg-gray-50">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Estado
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Código
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Cliente
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Contacto
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Módulos
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Estado Módulo
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Usuarios
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Fecha Inicio
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Fecha Fin
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Notificación
                  </th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                    Acciones
                  </th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-200">
                {filteredClients.map((client) => {
                  const clientModule = client.modules?.find(m => 
                    m.moduleCode === moduleType || 
                    (m.module && m.module.code === moduleType)
                  );
                  
                  if (!clientModule) return null;

                  const StatusIcon = getStatusIcon(clientModule.status);
                  const statusColor = getStatusColor(clientModule.status);
                  const isNotificationSending = sendingNotifications.has(client.id);
                  
                  // Simular si se envió notificación (esto vendría de la API)
                  const wasNotified = client.notificationSent || Math.random() > 0.5;

                  return (
                    <tr key={client.id} className="hover:bg-gray-50">
                      {/* Estado de Notificación */}
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center">
                          {wasNotified ? (
                            <div className="flex items-center text-green-600">
                              <Check className="w-5 h-5" />
                            </div>
                          ) : (
                            <div className="flex items-center text-orange-500">
                              <AlertCircle className="w-5 h-5" />
                            </div>
                          )}
                        </div>
                      </td>

                      {/* Código del Cliente */}
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="text-sm font-mono text-gray-900 bg-gray-100 px-2 py-1 rounded">
                          {client.code}
                        </div>
                      </td>

                      {/* Información del Cliente */}
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex items-center">
                          <div className="w-10 h-10 bg-blue-100 rounded-lg flex items-center justify-center">
                            <Building2 className="w-5 h-5 text-blue-600" />
                          </div>
                          <div className="ml-4">
                            <div className="text-sm font-medium text-gray-900">
                              {client.businessName}
                            </div>
                            <div className="text-sm text-gray-500">
                              RUC: {client.ruc}
                            </div>
                          </div>
                        </div>
                      </td>

                      {/* Número de Contacto */}
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        <div className="flex items-center">
                          <Phone className="w-4 h-4 mr-1 text-gray-400" />
                          {client.contactNumber || 'N/A'}
                        </div>
                      </td>

                      {/* Módulos del Cliente */}
                      <td className="px-6 py-4 whitespace-nowrap">
                        <div className="flex flex-wrap gap-1">
                          {client.modules && client.modules.map((mod, index) => (
                            <span 
                              key={index}
                              className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${
                                mod.moduleCode === moduleType || (mod.module && mod.module.code === moduleType)
                                  ? 'bg-blue-100 text-blue-800' 
                                  : 'bg-gray-100 text-gray-600'
                              }`}
                            >
                              {mod.moduleCode || mod.module?.code || 'N/A'}
                            </span>
                          ))}
                        </div>
                      </td>

                      {/* Estado del Módulo */}
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${statusColor}`}>
                          <StatusIcon className="w-3 h-3 mr-1" />
                          {clientModule.status}
                        </span>
                      </td>

                      {/* Usuarios */}
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        <div className="flex items-center">
                          <Users className="w-4 h-4 mr-1 text-gray-400" />
                          {clientModule.maxUserAccounts || 0}
                        </div>
                      </td>

                      {/* Fecha de Inicio */}
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        <div className="flex items-center">
                          <Calendar className="w-4 h-4 mr-1 text-gray-400" />
                          {new Date(clientModule.startDate).toLocaleDateString('es-ES')}
                        </div>
                      </td>

                      {/* Fecha de Fin */}
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                        <div className="flex items-center">
                          <Calendar className="w-4 h-4 mr-1 text-gray-400" />
                          {clientModule.endDate 
                            ? new Date(clientModule.endDate).toLocaleDateString('es-ES')
                            : 'Indefinida'
                          }
                        </div>
                      </td>

                      {/* Notificación */}
                      <td className="px-6 py-4 whitespace-nowrap">
                        {wasNotified ? (
                          <div className="flex items-center">
                            <span className="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-green-100 text-green-800">
                              <Check className="w-3 h-3 mr-1" />
                              Notificado
                            </span>
                          </div>
                        ) : (
                          <button
                            onClick={() => handleSendNotification(client.id)}
                            disabled={isNotificationSending}
                            className="inline-flex items-center px-3 py-1 border border-transparent text-xs font-medium rounded-md text-blue-700 bg-blue-100 hover:bg-blue-200 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 transition-colors"
                          >
                            {isNotificationSending ? (
                              <>
                                <div className="w-3 h-3 border-2 border-blue-600 border-t-transparent rounded-full animate-spin mr-1"></div>
                                Enviando...
                              </>
                            ) : (
                              <>
                                <Send className="w-3 h-3 mr-1" />
                                Notificar
                              </>
                            )}
                          </button>
                        )}
                      </td>

                      {/* Acciones */}
                      <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                        <button
                          onClick={() => navigate(`/clients/${client.id}`)}
                          className="text-blue-600 hover:text-blue-900 transition-colors"
                        >
                          <Eye className="w-4 h-4" />
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        </div>
      )}
    </div>
  );
};

export default ModulePage;