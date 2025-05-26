import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { clientsApi, modulesApi } from '../../services/adminApi';
import { 
  Package, 
  Building2, 
  Search, 
  Filter, 
  Eye, 
  Users,
  Calendar,
  CheckCircle,
  Clock,
  XCircle,
  AlertTriangle,
  RefreshCw,
  FileText,
  BarChart3
} from 'lucide-react';

const ModulePage = ({ moduleType, title }) => {
  const navigate = useNavigate();
  const [clients, setClients] = useState([]);
  const [moduleInfo, setModuleInfo] = useState(null);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [filterStatus, setFilterStatus] = useState('ALL');
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
      // Usar información por defecto si no se puede cargar
      setModuleInfo(moduleDescriptions[moduleType]);
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

  const ClientModuleCard = ({ client }) => {
    const clientModule = client.modules?.find(m => 
      m.moduleCode === moduleType || 
      (m.module && m.module.code === moduleType)
    );
    
    if (!clientModule) return null;

    const StatusIcon = getStatusIcon(clientModule.status);
    const statusColor = getStatusColor(clientModule.status);

    return (
      <div className="card hover:shadow-soft-lg transition-shadow duration-200">
        <div className="flex items-start justify-between">
          <div className="flex items-start space-x-4">
            <div className="w-12 h-12 bg-primary-100 rounded-lg flex items-center justify-center">
              <Building2 className="w-6 h-6 text-primary-600" />
            </div>
            
            <div className="flex-1 min-w-0">
              <div className="flex items-center space-x-2 mb-1">
                <h3 className="text-lg font-medium text-gray-900 truncate">
                  {client.businessName}
                </h3>
                <span className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${statusColor}`}>
                  <StatusIcon className="w-3 h-3 mr-1" />
                  {clientModule.status}
                </span>
              </div>
              
              <div className="space-y-1 text-sm text-gray-600">
                <p>RUC: {client.ruc}</p>
                <p>Código: {client.code}</p>
                {client.commercialName && (
                  <p>Nombre Comercial: {client.commercialName}</p>
                )}
              </div>
              
              <div className="flex items-center space-x-4 mt-3">
                <div className="flex items-center text-blue-600">
                  <Calendar className="w-4 h-4 mr-1" />
                  <span className="text-xs">
                    Desde: {new Date(clientModule.startDate).toLocaleDateString('es-ES')}
                  </span>
                </div>
                
                {clientModule.endDate && (
                  <div className="flex items-center text-orange-600">
                    <Clock className="w-4 h-4 mr-1" />
                    <span className="text-xs">
                      Hasta: {new Date(clientModule.endDate).toLocaleDateString('es-ES')}
                    </span>
                  </div>
                )}
                
                {clientModule.maxUserAccounts && (
                  <div className="flex items-center text-green-600">
                    <Users className="w-4 h-4 mr-1" />
                    <span className="text-xs">{clientModule.maxUserAccounts} usuarios</span>
                  </div>
                )}
              </div>
              
              {clientModule.specificStorageLimit && (
                <div className="flex items-center text-purple-600 mt-2">
                  <FileText className="w-4 h-4 mr-1" />
                  <span className="text-xs">
                    {Math.round(clientModule.specificStorageLimit / 1024 / 1024)} MB asignados
                  </span>
                </div>
              )}
            </div>
          </div>
          
          <div>
            <button
              onClick={() => navigate(`/clients/${client.id}`)}
              className="p-2 text-gray-400 hover:text-gray-600 rounded-lg hover:bg-gray-100"
            >
              <Eye className="w-5 h-5" />
            </button>
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
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6 mb-8">
          {[...Array(4)].map((_, i) => (
            <div key={i} className="card animate-pulse">
              <div className="h-20 bg-gray-200 rounded"></div>
            </div>
          ))}
        </div>
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {[...Array(6)].map((_, i) => (
            <div key={i} className="card animate-pulse">
              <div className="h-48 bg-gray-200 rounded"></div>
            </div>
          ))}
        </div>
      </div>
    );
  }

  const currentModule = moduleInfo || moduleDescriptions[moduleType];

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
            
            {currentModule?.features && (
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

      {/* Clients Grid */}
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
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredClients.map(client => (
            <ClientModuleCard key={client.id} client={client} />
          ))}
        </div>
      )}
    </div>
  );
};

export default ModulePage;