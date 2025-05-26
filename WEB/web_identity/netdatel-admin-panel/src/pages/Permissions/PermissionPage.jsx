import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { permissionsApi } from '../../services/api';
import { 
  Key, 
  Plus, 
  Search, 
  Filter, 
  MoreVertical, 
  Edit, 
  Trash2,
  RefreshCw,
  Code,
  Tags,
  Server
} from 'lucide-react';

const PermissionsPage = () => {
  const navigate = useNavigate();
  const { isSuperAdmin } = useAuth();
  const [permissions, setPermissions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [filterCategory, setFilterCategory] = useState('ALL');
  const [filterService, setFilterService] = useState('ALL');
  const [showActionsMenu, setShowActionsMenu] = useState(null);

  useEffect(() => {
    loadPermissions();
  }, []);

  const loadPermissions = async () => {
    try {
      setLoading(true);
      const response = await permissionsApi.getAll();
      const permissionsData = response.data.content || response.data;
      setPermissions(Array.isArray(permissionsData) ? permissionsData : []);
    } catch (error) {
      console.error('Error loading permissions:', error);
      setPermissions([]);
    } finally {
      setLoading(false);
    }
  };

  const getUniqueCategories = () => {
    const categories = ['ALL', ...new Set(permissions.map(p => p.category).filter(Boolean))];
    return categories;
  };

  const getUniqueServices = () => {
    const services = ['ALL', ...new Set(permissions.map(p => p.service).filter(Boolean))];
    return services;
  };

  const filteredPermissions = permissions.filter(permission => {
    const matchesSearch = permission.code?.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         permission.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         permission.description?.toLowerCase().includes(searchTerm.toLowerCase());
    
    const matchesCategory = filterCategory === 'ALL' || permission.category === filterCategory;
    const matchesService = filterService === 'ALL' || permission.service === filterService;
    
    return matchesSearch && matchesCategory && matchesService;
  });

  const handlePermissionAction = async (action, permissionId) => {
    try {
      switch (action) {
        case 'delete':
          if (window.confirm('¿Estás seguro de que quieres eliminar este permiso?')) {
            await permissionsApi.delete(permissionId);
          }
          break;
      }
      loadPermissions();
      setShowActionsMenu(null);
    } catch (error) {
      console.error('Error performing permission action:', error);
      alert('Error al realizar la acción');
    }
  };

  const getServiceIcon = (service) => {
    switch (service) {
      case 'admin-service': return Server;
      case 'document-service': return Key;
      case 'provider-service': return Tags;
      default: return Code;
    }
  };

  const getServiceColor = (service) => {
    switch (service) {
      case 'admin-service': return 'bg-purple-500';
      case 'document-service': return 'bg-blue-500';
      case 'provider-service': return 'bg-green-500';
      default: return 'bg-gray-500';
    }
  };

  const PermissionCard = ({ permission }) => {
    const IconComponent = getServiceIcon(permission.service);
    const colorClass = getServiceColor(permission.service);

    return (
      <div className="card hover:shadow-soft-lg transition-shadow duration-200">
        <div className="flex items-start justify-between">
          <div className="flex items-start space-x-4">
            <div className={`w-12 h-12 ${colorClass} rounded-lg flex items-center justify-center`}>
              <IconComponent className="w-6 h-6 text-white" />
            </div>
            <div className="flex-1 min-w-0">
              <h3 className="text-lg font-medium text-gray-900 truncate font-mono">
                {permission.code}
              </h3>
              
              {permission.name && (
                <p className="text-sm text-gray-600 mt-1">{permission.name}</p>
              )}
              
              {permission.description && (
                <p className="text-xs text-gray-500 mt-2 line-clamp-2">{permission.description}</p>
              )}
              
              <div className="flex items-center space-x-4 mt-3">
                {permission.category && (
                  <div className="flex items-center text-blue-600">
                    <Tags className="w-4 h-4 mr-1" />
                    <span className="text-xs">{permission.category}</span>
                  </div>
                )}
                
                <div className="flex items-center text-green-600">
                  <Server className="w-4 h-4 mr-1" />
                  <span className="text-xs">{permission.service}</span>
                </div>
                
                {permission.isActive !== undefined && (
                  <span className={`inline-flex items-center px-2 py-1 rounded-full text-xs ${
                    permission.isActive 
                      ? 'bg-green-100 text-green-800' 
                      : 'bg-red-100 text-red-800'
                  }`}>
                    {permission.isActive ? 'Activo' : 'Inactivo'}
                  </span>
                )}
              </div>
            </div>
          </div>
          
          {isSuperAdmin() && (
            <div className="relative">
              <button
                onClick={() => setShowActionsMenu(showActionsMenu === permission.id ? null : permission.id)}
                className="p-2 text-gray-400 hover:text-gray-600 rounded-lg hover:bg-gray-100"
              >
                <MoreVertical className="w-5 h-5" />
              </button>
              
              {showActionsMenu === permission.id && (
                <div className="absolute right-0 top-10 w-48 bg-white rounded-lg shadow-soft-lg border border-gray-200 py-2 z-10">
                  <button
                    onClick={() => navigate(`/permissions/${permission.id}/edit`)}
                    className="w-full px-4 py-2 text-left text-sm text-gray-700 hover:bg-gray-100 flex items-center space-x-2"
                  >
                    <Edit className="w-4 h-4" />
                    <span>Editar</span>
                  </button>
                  <hr className="my-1" />
                  <button
                    onClick={() => handlePermissionAction('delete', permission.id)}
                    className="w-full px-4 py-2 text-left text-sm text-red-600 hover:bg-red-50 flex items-center space-x-2"
                  >
                    <Trash2 className="w-4 h-4" />
                    <span>Eliminar</span>
                  </button>
                </div>
              )}
            </div>
          )}
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
              <div className="h-40 bg-gray-200 rounded"></div>
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
          <h1 className="text-2xl font-bold text-gray-900">Gestión de Permisos</h1>
          <p className="mt-1 text-sm text-gray-600">
            Administra permisos del sistema ({filteredPermissions.length} permisos)
          </p>
        </div>
        
        <div className="mt-4 sm:mt-0 flex space-x-3">
          <button
            onClick={loadPermissions}
            className="btn-secondary flex items-center space-x-2"
          >
            <RefreshCw className="w-4 h-4" />
            <span>Actualizar</span>
          </button>
          
          {isSuperAdmin() && (
            <button
              onClick={() => navigate('/permissions/new')}
              className="btn-primary flex items-center space-x-2"
            >
              <Plus className="w-4 h-4" />
              <span>Nuevo Permiso</span>
            </button>
          )}
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
                placeholder="Buscar permisos..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="input pl-10"
              />
            </div>
          </div>
          
          <div>
            <div className="flex items-center space-x-2">
              <Tags className="w-5 h-5 text-gray-400" />
              <select
                value={filterCategory}
                onChange={(e) => setFilterCategory(e.target.value)}
                className="input"
              >
                {getUniqueCategories().map(category => (
                  <option key={category} value={category}>
                    {category === 'ALL' ? 'Todas las categorías' : category}
                  </option>
                ))}
              </select>
            </div>
          </div>
          
          <div>
            <div className="flex items-center space-x-2">
              <Server className="w-5 h-5 text-gray-400" />
              <select
                value={filterService}
                onChange={(e) => setFilterService(e.target.value)}
                className="input"
              >
                {getUniqueServices().map(service => (
                  <option key={service} value={service}>
                    {service === 'ALL' ? 'Todos los servicios' : service}
                  </option>
                ))}
              </select>
            </div>
          </div>
        </div>
      </div>

      {/* Permissions Grid */}
      {filteredPermissions.length === 0 ? (
        <div className="text-center py-12">
          <Key className="w-12 h-12 text-gray-300 mx-auto mb-4" />
          <h3 className="text-lg font-medium text-gray-900 mb-2">No se encontraron permisos</h3>
          <p className="text-gray-600 mb-6">
            {searchTerm || filterCategory !== 'ALL' || filterService !== 'ALL'
              ? 'Intenta ajustar los filtros de búsqueda' 
              : 'Los permisos se cargan automáticamente desde el sistema'
            }
          </p>
          {isSuperAdmin() && !searchTerm && filterCategory === 'ALL' && filterService === 'ALL' && (
            <button
              onClick={() => navigate('/permissions/new')}
              className="btn-primary"
            >
              Crear Primer Permiso
            </button>
          )}
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredPermissions.map(permission => (
            <PermissionCard key={permission.id} permission={permission} />
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

export default PermissionsPage;