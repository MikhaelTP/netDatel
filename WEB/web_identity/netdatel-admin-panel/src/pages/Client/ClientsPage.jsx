import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { clientsApi } from '../../services/adminApi';
import { 
  Building2, 
  Plus, 
  Search, 
  Filter, 
  MoreVertical, 
  Edit, 
  Trash2, 
  Eye, 
  Users,
  Package,
  Calendar,
  MapPin,
  Phone,
  Mail,
  RefreshCw,
  Download,
  AlertTriangle,
  CheckCircle,
  Clock,
  XCircle
} from 'lucide-react';

const ClientsPage = () => {
  const navigate = useNavigate();
  const { isSuperAdmin, hasRole } = useAuth();
  const [clients, setClients] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [filterStatus, setFilterStatus] = useState('ALL');
  const [showActionsMenu, setShowActionsMenu] = useState(null);
  const [selectedClients, setSelectedClients] = useState([]);

  const statusOptions = [
    { value: 'ALL', label: 'Todos los estados' },
    { value: 'ACTIVE', label: 'Activos' },
    { value: 'INACTIVE', label: 'Inactivos' },
    { value: 'SUSPENDED', label: 'Suspendidos' },
  ];

  useEffect(() => {
    loadClients();
  }, []);

  const loadClients = async () => {
    try {
      setLoading(true);
      const response = await clientsApi.getAll({ size: 100 }); // Cargar hasta 100 clientes
      const clientsData = response.data.content || response.data;
      setClients(Array.isArray(clientsData) ? clientsData : []);
    } catch (error) {
      console.error('Error loading clients:', error);
      setClients([]);
    } finally {
      setLoading(false);
    }
  };

  const filteredClients = clients.filter(client => {
    const matchesSearch = client.businessName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         client.commercialName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         client.ruc?.includes(searchTerm) ||
                         client.code?.toLowerCase().includes(searchTerm.toLowerCase());
    
    const matchesStatus = filterStatus === 'ALL' || client.status === filterStatus;
    
    return matchesSearch && matchesStatus;
  });

  const handleClientAction = async (action, clientId) => {
    try {
      switch (action) {
        case 'changeStatus':
          const newStatus = prompt('Ingrese el nuevo estado (ACTIVE, INACTIVE, SUSPENDED):');
          if (newStatus && ['ACTIVE', 'INACTIVE', 'SUSPENDED'].includes(newStatus)) {
            await clientsApi.changeStatus(clientId, newStatus);
          }
          break;
        case 'delete':
          if (window.confirm('¿Estás seguro de que quieres eliminar este cliente?')) {
            await clientsApi.delete(clientId);
          }
          break;
      }
      loadClients(); // Recargar la lista
      setShowActionsMenu(null);
    } catch (error) {
      console.error('Error performing client action:', error);
      alert('Error al realizar la acción');
    }
  };

  const handleBulkAction = async (action) => {
    if (selectedClients.length === 0) {
      alert('Selecciona al menos un cliente');
      return;
    }

    if (window.confirm(`¿Realizar ${action} en ${selectedClients.length} cliente(s)?`)) {
      try {
        for (const clientId of selectedClients) {
          await handleClientAction(action, clientId);
        }
        setSelectedClients([]);
      } catch (error) {
        console.error('Error in bulk action:', error);
      }
    }
  };

  const toggleSelectClient = (clientId) => {
    setSelectedClients(prev => 
      prev.includes(clientId) 
        ? prev.filter(id => id !== clientId)
        : [...prev, clientId]
    );
  };

  const selectAllClients = () => {
    if (selectedClients.length === filteredClients.length) {
      setSelectedClients([]);
    } else {
      setSelectedClients(filteredClients.map(client => client.id));
    }
  };

  const getStatusIcon = (status) => {
    switch (status) {
      case 'ACTIVE': return CheckCircle;
      case 'INACTIVE': return Clock;
      case 'SUSPENDED': return XCircle;
      default: return AlertTriangle;
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

  const ClientCard = ({ client }) => {
    const StatusIcon = getStatusIcon(client.status);
    const statusColor = getStatusColor(client.status);

    return (
      <div className="card hover:shadow-soft-lg transition-shadow duration-200">
        <div className="flex items-start justify-between">
          <div className="flex items-start space-x-4">
            <div className="flex items-center">
              <input
                type="checkbox"
                checked={selectedClients.includes(client.id)}
                onChange={() => toggleSelectClient(client.id)}
                className="text-primary-600 focus:ring-primary-500 rounded mr-3"
              />
              <div className="w-12 h-12 bg-primary-100 rounded-lg flex items-center justify-center">
                <Building2 className="w-6 h-6 text-primary-600" />
              </div>
            </div>
            
            <div className="flex-1 min-w-0">
              <div className="flex items-center space-x-2 mb-1">
                <h3 className="text-lg font-medium text-gray-900 truncate">
                  {client.businessName}
                </h3>
                <span className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${statusColor}`}>
                  <StatusIcon className="w-3 h-3 mr-1" />
                  {client.status}
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
                {client.modules && client.modules.length > 0 && (
                  <div className="flex items-center text-blue-600">
                    <Package className="w-4 h-4 mr-1" />
                    <span className="text-xs">{client.modules.length} módulos</span>
                  </div>
                )}
                
                {client.administrators && client.administrators.length > 0 && (
                  <div className="flex items-center text-green-600">
                    <Users className="w-4 h-4 mr-1" />
                    <span className="text-xs">{client.administrators.length} admins</span>
                  </div>
                )}
                
                <div className="flex items-center text-gray-500">
                  <Calendar className="w-4 h-4 mr-1" />
                  <span className="text-xs">
                    {new Date(client.registrationDate).toLocaleDateString('es-ES')}
                  </span>
                </div>
              </div>
              
              {client.contactNumber && (
                <div className="flex items-center text-gray-500 mt-2">
                  <Phone className="w-4 h-4 mr-1" />
                  <span className="text-xs">{client.contactNumber}</span>
                </div>
              )}
            </div>
          </div>
          
          <div className="relative">
            <button
              onClick={() => setShowActionsMenu(showActionsMenu === client.id ? null : client.id)}
              className="p-2 text-gray-400 hover:text-gray-600 rounded-lg hover:bg-gray-100"
            >
              <MoreVertical className="w-5 h-5" />
            </button>
            
            {showActionsMenu === client.id && (
              <div className="absolute right-0 top-10 w-48 bg-white rounded-lg shadow-soft-lg border border-gray-200 py-2 z-10">
                <button
                  onClick={() => navigate(`/clients/${client.id}`)}
                  className="w-full px-4 py-2 text-left text-sm text-gray-700 hover:bg-gray-100 flex items-center space-x-2"
                >
                  <Eye className="w-4 h-4" />
                  <span>Ver detalles</span>
                </button>
                
                <button
                  onClick={() => navigate(`/clients/${client.id}/edit`)}
                  className="w-full px-4 py-2 text-left text-sm text-gray-700 hover:bg-gray-100 flex items-center space-x-2"
                >
                  <Edit className="w-4 h-4" />
                  <span>Editar</span>
                </button>
                
                <hr className="my-1" />
                
                <button
                  onClick={() => handleClientAction('changeStatus', client.id)}
                  className="w-full px-4 py-2 text-left text-sm text-gray-700 hover:bg-gray-100 flex items-center space-x-2"
                >
                  <AlertTriangle className="w-4 h-4" />
                  <span>Cambiar estado</span>
                </button>
                
                {(isSuperAdmin() || hasRole('CLIENT_ADMIN')) && (
                  <>
                    <hr className="my-1" />
                    <button
                      onClick={() => handleClientAction('delete', client.id)}
                      className="w-full px-4 py-2 text-left text-sm text-red-600 hover:bg-red-50 flex items-center space-x-2"
                    >
                      <Trash2 className="w-4 h-4" />
                      <span>Eliminar</span>
                    </button>
                  </>
                )}
              </div>
            )}
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

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Gestión de Clientes</h1>
          <p className="mt-1 text-sm text-gray-600">
            Administra todos los clientes registrados ({filteredClients.length} clientes)
          </p>
        </div>
        
        <div className="mt-4 sm:mt-0 flex space-x-3">
          <button
            onClick={loadClients}
            className="btn-secondary flex items-center space-x-2"
          >
            <RefreshCw className="w-4 h-4" />
            <span>Actualizar</span>
          </button>
          
          <button
            onClick={() => {
              // Implementar exportación
              alert('Funcionalidad de exportación próximamente');
            }}
            className="btn-secondary flex items-center space-x-2"
          >
            <Download className="w-4 h-4" />
            <span>Exportar</span>
          </button>
          
          <button
            onClick={() => navigate('/clients/new')}
            className="btn-primary flex items-center space-x-2"
          >
            <Plus className="w-4 h-4" />
            <span>Nuevo Cliente</span>
          </button>
        </div>
      </div>

      {/* Filters */}
      <div className="card">
        <div className="flex flex-col lg:flex-row lg:items-center space-y-4 lg:space-y-0 lg:space-x-4">
          <div className="flex-1">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
              <input
                type="text"
                placeholder="Buscar clientes por nombre, RUC, código..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="input pl-10"
              />
            </div>
          </div>
          
          <div className="flex items-center space-x-4">
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
      </div>

      {/* Bulk Actions */}
      {selectedClients.length > 0 && (
        <div className="bg-primary-50 border border-primary-200 rounded-lg p-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-3">
              <span className="text-sm font-medium text-primary-900">
                {selectedClients.length} cliente(s) seleccionado(s)
              </span>
            </div>
            <div className="flex items-center space-x-2">
              <button
                onClick={() => handleBulkAction('changeStatus')}
                className="btn-secondary text-sm"
              >
                Cambiar Estado
              </button>
              {(isSuperAdmin() || hasRole('CLIENT_ADMIN')) && (
                <button
                  onClick={() => handleBulkAction('delete')}
                  className="bg-red-600 hover:bg-red-700 text-white font-medium py-2 px-4 rounded-lg text-sm"
                >
                  Eliminar Seleccionados
                </button>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Select All */}
      {filteredClients.length > 0 && (
        <div className="flex items-center justify-between">
          <label className="flex items-center space-x-2 text-sm text-gray-600">
            <input
              type="checkbox"
              checked={selectedClients.length === filteredClients.length}
              onChange={selectAllClients}
              className="text-primary-600 focus:ring-primary-500 rounded"
            />
            <span>Seleccionar todos ({filteredClients.length})</span>
          </label>
          
          <div className="text-sm text-gray-500">
            {selectedClients.length} de {filteredClients.length} seleccionados
          </div>
        </div>
      )}

      {/* Clients Grid */}
      {filteredClients.length === 0 ? (
        <div className="text-center py-12">
          <Building2 className="w-12 h-12 text-gray-300 mx-auto mb-4" />
          <h3 className="text-lg font-medium text-gray-900 mb-2">No se encontraron clientes</h3>
          <p className="text-gray-600 mb-6">
            {searchTerm || filterStatus !== 'ALL' 
              ? 'Intenta ajustar los filtros de búsqueda' 
              : 'Comienza registrando tu primer cliente'
            }
          </p>
          {!searchTerm && filterStatus === 'ALL' && (
            <button
              onClick={() => navigate('/clients/new')}
              className="btn-primary"
            >
              Registrar Primer Cliente
            </button>
          )}
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredClients.map(client => (
            <ClientCard key={client.id} client={client} />
          ))}
        </div>
      )}
      
      {/* Click outside to close menu */}
      {showActionsMenu && (
        <div 
          className="fixed inset-0 z-0" 
          onClick={() => setShowActionsMenu(null)}
        />
      )}
    </div>
  );
};

export default ClientsPage;