import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { rolesApi, usersApi } from '../../services/api';
import { 
  Shield, 
  Plus, 
  Search, 
  MoreVertical, 
  Edit, 
  Trash2, 
  Users, 
  Key,
  Star,
  RefreshCw,
  Crown
} from 'lucide-react';

const RolesPage = () => {
  const navigate = useNavigate();
  const { isSuperAdmin } = useAuth();
  const [roles, setRoles] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [showActionsMenu, setShowActionsMenu] = useState(null);

  useEffect(() => {
    loadRoles();
  }, []);

  const loadRoles = async () => {
    try {
      setLoading(true);
      const response = await rolesApi.getAll();
      const rolesData = response.data.content || response.data;
      setRoles(Array.isArray(rolesData) ? rolesData : []);
    } catch (error) {
      console.error('Error loading roles:', error);
      setRoles([]);
    } finally {
      setLoading(false);
    }
  };

  const filteredRoles = roles.filter(role =>
    role.name?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    role.description?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const handleRoleAction = async (action, roleId) => {
    try {
      switch (action) {
        case 'setDefault':
          await rolesApi.setAsDefault(roleId);
          break;
        case 'delete':
          if (window.confirm('¿Estás seguro de que quieres eliminar este rol?')) {
            await rolesApi.delete(roleId);
          }
          break;
      }
      loadRoles(); // Recargar la lista
      setShowActionsMenu(null);
    } catch (error) {
      console.error('Error performing role action:', error);
      alert('Error al realizar la acción');
    }
  };

  const getRoleIcon = (roleName) => {
    if (roleName?.includes('SUPER_ADMIN')) return Crown;
    if (roleName?.includes('ADMIN')) return Shield;
    return Users;
  };

  const getRoleColor = (roleName) => {
    if (roleName?.includes('SUPER_ADMIN')) return 'bg-red-500';
    if (roleName?.includes('ADMIN')) return 'bg-purple-500';
    if (roleName?.includes('AUDITOR')) return 'bg-blue-500';
    return 'bg-gray-500';
  };

  const RoleCard = ({ role }) => {
    const IconComponent = getRoleIcon(role.name);
    const colorClass = getRoleColor(role.name);

    return (
      <div className="card hover:shadow-soft-lg transition-shadow duration-200">
        <div className="flex items-start justify-between">
          <div className="flex items-start space-x-4">
            <div className={`w-12 h-12 ${colorClass} rounded-lg flex items-center justify-center`}>
              <IconComponent className="w-6 h-6 text-white" />
            </div>
            <div className="flex-1 min-w-0">
              <div className="flex items-center space-x-2">
                <h3 className="text-lg font-medium text-gray-900 truncate">
                  {role.name?.replace('ROLE_', '')}
                </h3>
                {role.isDefault && (
                  <Star className="w-4 h-4 text-yellow-500 fill-current" />
                )}
              </div>
              
              {role.description && (
                <p className="text-sm text-gray-600 mt-1">{role.description}</p>
              )}
              
              <div className="flex items-center space-x-4 mt-3">
                <div className="flex items-center text-blue-600">
                  <Key className="w-4 h-4 mr-1" />
                  <span className="text-xs">
                    {role.permissions?.length || 0} permisos
                  </span>
                </div>
                
                <div className="flex items-center text-green-600">
                  <Users className="w-4 h-4 mr-1" />
                  <span className="text-xs">
                    {/* Aquí podrías mostrar el número de usuarios con este rol */}
                    Nivel {role.hierarchyLevel || 'N/A'}
                  </span>
                </div>
                
                {role.isDefault && (
                  <span className="inline-flex items-center px-2 py-1 rounded-full text-xs bg-yellow-100 text-yellow-800">
                    Por defecto
                  </span>
                )}
              </div>
              
              {role.permissions && role.permissions.length > 0 && (
                <div className="mt-3">
                  <div className="flex flex-wrap gap-1">
                    {role.permissions.slice(0, 3).map((permission, idx) => (
                      <span key={idx} className="inline-flex items-center px-2 py-1 rounded-md text-xs bg-blue-50 text-blue-700">
                        {permission.code}
                      </span>
                    ))}
                    {role.permissions.length > 3 && (
                      <span className="inline-flex items-center px-2 py-1 rounded-md text-xs bg-gray-50 text-gray-700">
                        +{role.permissions.length - 3} más
                      </span>
                    )}
                  </div>
                </div>
              )}
            </div>
          </div>
          
          {isSuperAdmin() && (
            <div className="relative">
              <button
                onClick={() => setShowActionsMenu(showActionsMenu === role.id ? null : role.id)}
                className="p-2 text-gray-400 hover:text-gray-600 rounded-lg hover:bg-gray-100"
              >
                <MoreVertical className="w-5 h-5" />
              </button>
              
              {showActionsMenu === role.id && (
                <div className="absolute right-0 top-10 w-48 bg-white rounded-lg shadow-soft-lg border border-gray-200 py-2 z-10">
                  <button
                    onClick={() => navigate(`/roles/${role.id}`)}
                    className="w-full px-4 py-2 text-left text-sm text-gray-700 hover:bg-gray-100 flex items-center space-x-2"
                  >
                    <Shield className="w-4 h-4" />
                    <span>Ver detalles</span>
                  </button>
                  <button
                    onClick={() => navigate(`/roles/${role.id}/edit`)}
                    className="w-full px-4 py-2 text-left text-sm text-gray-700 hover:bg-gray-100 flex items-center space-x-2"
                  >
                    <Edit className="w-4 h-4" />
                    <span>Editar</span>
                  </button>
                  {!role.isDefault && (
                    <button
                      onClick={() => handleRoleAction('setDefault', role.id)}
                      className="w-full px-4 py-2 text-left text-sm text-gray-700 hover:bg-gray-100 flex items-center space-x-2"
                    >
                      <Star className="w-4 h-4" />
                      <span>Marcar como defecto</span>
                    </button>
                  )}
                  <hr className="my-1" />
                  <button
                    onClick={() => handleRoleAction('delete', role.id)}
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
          <h1 className="text-2xl font-bold text-gray-900">Gestión de Roles</h1>
          <p className="mt-1 text-sm text-gray-600">
            Administra roles y permisos del sistema ({filteredRoles.length} roles)
          </p>
        </div>
        
        <div className="mt-4 sm:mt-0 flex space-x-3">
          <button
            onClick={loadRoles}
            className="btn-secondary flex items-center space-x-2"
          >
            <RefreshCw className="w-4 h-4" />
            <span>Actualizar</span>
          </button>
          
          {isSuperAdmin() && (
            <button
              onClick={() => navigate('/roles/new')}
              className="btn-primary flex items-center space-x-2"
            >
              <Plus className="w-4 h-4" />
              <span>Nuevo Rol</span>
            </button>
          )}
        </div>
      </div>

      {/* Search */}
      <div className="card">
        <div className="relative">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
          <input
            type="text"
            placeholder="Buscar roles..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="input pl-10"
          />
        </div>
      </div>

      {/* Roles Grid */}
      {filteredRoles.length === 0 ? (
        <div className="text-center py-12">
          <Shield className="w-12 h-12 text-gray-300 mx-auto mb-4" />
          <h3 className="text-lg font-medium text-gray-900 mb-2">No se encontraron roles</h3>
          <p className="text-gray-600 mb-6">
            {searchTerm 
              ? 'Intenta ajustar el término de búsqueda' 
              : 'Comienza creando tu primer rol'
            }
          </p>
          {isSuperAdmin() && !searchTerm && (
            <button
              onClick={() => navigate('/roles/new')}
              className="btn-primary"
            >
              Crear Primer Rol
            </button>
          )}
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredRoles.map(role => (
            <RoleCard key={role.id} role={role} />
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

export default RolesPage;