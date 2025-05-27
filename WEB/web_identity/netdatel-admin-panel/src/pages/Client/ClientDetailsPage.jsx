// src/pages/Client/ClientDetailsPage.jsx
import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { clientsApi} from '../../services/adminApi';
import { 
  ArrowLeft, 
  Edit, 
  Building2, 
  MapPin, 
  Phone, 
  Mail,
  Calendar,
  Package,
  Users,
  User,
  FileText,
  CheckCircle,
  Clock,
  XCircle,
  AlertTriangle,
  MoreVertical,
  Plus
} from 'lucide-react';

const ClientDetailsPage = () => {
  const { id } = useParams();
  const navigate = useNavigate();
  const [client, setClient] = useState(null);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('general');

  useEffect(() => {
    loadClientDetails();
  }, [id]);

  const loadClientDetails = async () => {
    try {
      setLoading(true);
      const response = await clientsApi.getById(id);
      setClient(response.data);
    } catch (error) {
      console.error('Error loading client details:', error);
      // Redirigir si no se encuentra el cliente
      navigate('/clients');
    } finally {
      setLoading(false);
    }
  };

  const getStatusColor = (status) => {
    switch (status) {
      case 'ACTIVE': return 'bg-green-100 text-green-800';
      case 'INACTIVE': return 'bg-gray-100 text-gray-800';
      case 'SUSPENDED': return 'bg-red-100 text-red-800';
      default: return 'bg-yellow-100 text-yellow-800';
    }
  };

  const getModuleStatusIcon = (status) => {
    switch (status) {
      case 'ACTIVE': return CheckCircle;
      case 'INACTIVE': return Clock;
      case 'EXPIRED': return XCircle;
      case 'PENDING': return AlertTriangle;
      default: return AlertTriangle;
    }
  };

  const getModuleStatusColor = (status) => {
    switch (status) {
      case 'ACTIVE': return 'text-green-500 bg-green-50';
      case 'INACTIVE': return 'text-gray-500 bg-gray-50';
      case 'EXPIRED': return 'text-red-500 bg-red-50';
      case 'PENDING': return 'text-yellow-500 bg-yellow-50';
      default: return 'text-gray-500 bg-gray-50';
    }
  };

  const formatDate = (dateString) => {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleDateString('es-ES', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
  };

  const formatDateTime = (dateString) => {
    if (!dateString) return 'N/A';
    return new Date(dateString).toLocaleString('es-ES', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

 const tabs = [
  { id: 'general', label: 'Información General', icon: Building2 },
  { id: 'modules', label: 'Módulos', icon: Package },
  { id: 'representatives', label: 'Representantes', icon: User },
  { id: 'administrators', label: 'Administradores', icon: Users },
];

const GeneralTab = () => (
  <div className="space-y-6">
    {/* Información básica */}
    <div className="card">
      <h3 className="text-lg font-medium text-gray-900 mb-4">Datos Básicos</h3>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Código</label>
          <p className="text-sm text-gray-900 font-mono">{client.code}</p>
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">RUC</label>
          <p className="text-sm text-gray-900">{client.ruc}</p>
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Estado</label>
          <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${getStatusColor(client.status)}`}>
            {client.status}
          </span>
        </div>
        <div className="md:col-span-2">
          <label className="block text-sm font-medium text-gray-700 mb-1">Razón Social</label>
          <p className="text-sm text-gray-900">{client.businessName}</p>
        </div>
        {client.commercialName && (
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Nombre Comercial</label>
            <p className="text-sm text-gray-900">{client.commercialName}</p>
          </div>
        )}
      </div>
    </div>

    {/* Fechas importantes */}
    <div className="card">
      <h3 className="text-lg font-medium text-gray-900 mb-4">Fechas Importantes</h3>
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Fecha de Registro</label>
          <p className="text-sm text-gray-900">{formatDateTime(client.registrationDate)}</p>
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Última Actualización</label>
          <p className="text-sm text-gray-900">{formatDateTime(client.lastUpdateDate)}</p>
        </div>
        <div>
          <label className="block text-sm font-medium text-gray-700 mb-1">Almacenamiento Asignado</label>
          <p className="text-sm text-gray-900">
            {client.allocatedStorage ? `${Math.round(client.allocatedStorage / 1024 / 1024)} MB` : 'N/A'}
          </p>
        </div>
      </div>
    </div>
  </div>
);

const ModulesTab = () => (
  <div className="space-y-6">
    {client.modules && client.modules.length > 0 ? (
      client.modules.map((module) => {
        const StatusIcon = getModuleStatusIcon(module.status);
        const statusColor = getModuleStatusColor(module.status);
        
        return (
          <div key={module.id} className="card">
            <div className="flex items-start space-x-4">
              <div className={`w-12 h-12 rounded-lg flex items-center justify-center ${statusColor}`}>
                <StatusIcon className="w-6 h-6" />
              </div>
              <div className="flex-1">
                <h3 className="text-lg font-medium text-gray-900">{module.moduleName}</h3>
                <div className="grid grid-cols-2 gap-4 mt-2 text-sm">
                  <div>Estado: <span className="font-medium">{module.status}</span></div>
                  <div>Usuarios: <span className="font-medium">{module.maxUserAccounts}</span></div>
                </div>
              </div>
            </div>
          </div>
        );
      })
    ) : (
      <div className="text-center py-12">
        <Package className="w-12 h-12 text-gray-300 mx-auto mb-4" />
        <h3 className="text-lg font-medium text-gray-900 mb-2">No hay módulos asignados</h3>
      </div>
    )}
  </div>
);

const RepresentativesTab = () => (
  <div className="space-y-4">
    {client.legalRepresentatives && client.legalRepresentatives.length > 0 ? (
      client.legalRepresentatives.map((representative) => (
        <div key={representative.id} className="card">
          <div className="flex items-start space-x-4">
            <div className="w-10 h-10 rounded-full bg-blue-100 text-blue-600 flex items-center justify-center">
              <User className="w-5 h-5" />
            </div>
            <div className="flex-1">
              <h3 className="text-lg font-medium text-gray-900">{representative.fullName}</h3>
              <p className="text-sm text-gray-600">{representative.position}</p>
              <p className="text-sm text-gray-500 mt-1">
                {representative.documentType}: {representative.documentNumber}
              </p>
            </div>
          </div>
        </div>
      ))
    ) : (
      <div className="text-center py-12">
        <User className="w-12 h-12 text-gray-300 mx-auto mb-4" />
        <h3 className="text-lg font-medium text-gray-900 mb-2">No hay representantes legales</h3>
      </div>
    )}
  </div>
);

if (loading) {
  return (
    <div className="space-y-6">
      <div className="h-8 bg-gray-200 rounded w-48 animate-pulse"></div>
      <div className="card animate-pulse">
        <div className="h-64 bg-gray-200 rounded"></div>
      </div>
    </div>
  );
}

if (!client) {
  return (
    <div className="text-center py-12">
      <Building2 className="w-12 h-12 text-gray-300 mx-auto mb-4" />
      <h3 className="text-lg font-medium text-gray-900 mb-2">Cliente no encontrado</h3>
      <button onClick={() => navigate('/clients')} className="btn-primary">
        Volver a clientes
      </button>
    </div>
  );
}

return (
  <div className="space-y-6">
    {/* Header */}
    <div className="flex items-center justify-between">
      <div className="flex items-center space-x-4">
        <button
          onClick={() => navigate('/clients')}
          className="p-2 text-gray-600 hover:text-gray-900 rounded-lg hover:bg-gray-100"
        >
          <ArrowLeft className="w-5 h-5" />
        </button>
        <div>
          <h1 className="text-2xl font-bold text-gray-900">{client.businessName}</h1>
          <p className="text-sm text-gray-600">RUC: {client.ruc} • Código: {client.code}</p>
        </div>
      </div>
      
      <button
        onClick={() => navigate(`/clients/${client.id}/edit`)}
        className="btn-primary flex items-center space-x-2"
      >
        <Edit className="w-4 h-4" />
        <span>Editar</span>
      </button>
    </div>

    {/* Tabs */}
    <div className="border-b border-gray-200">
      <nav className="-mb-px flex space-x-8">
        {tabs.map((tab) => (
          <button
            key={tab.id}
            onClick={() => setActiveTab(tab.id)}
            className={`py-2 px-1 border-b-2 font-medium text-sm flex items-center space-x-2 ${
              activeTab === tab.id
                ? 'border-blue-500 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`}
          >
            <tab.icon className="w-4 h-4" />
            <span>{tab.label}</span>
          </button>
        ))}
      </nav>
    </div>

    {/* Tab Content */}
    <div>
      {activeTab === 'general' && <GeneralTab />}
      {activeTab === 'modules' && <ModulesTab />}
      {activeTab === 'representatives' && <RepresentativesTab />}
      {activeTab === 'administrators' && (
        <div className="text-center py-12">
          <Users className="w-12 h-12 text-gray-300 mx-auto mb-4" />
          <h3 className="text-lg font-medium text-gray-900 mb-2">Administradores</h3>
          <p className="text-gray-600">Funcionalidad en desarrollo</p>
        </div>
      )}
    </div>
  </div>
);

};

export default ClientDetailsPage;