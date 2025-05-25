import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { usersApi } from '../../services/api';
import { 
  Users, 
  Plus, 
  Search, 
  Filter, 
  MoreVertical, 
  Edit, 
  Trash2, 
  Shield, 
  ShieldOff,
  Lock,
  Unlock,
  Eye,
  RefreshCw,
  Download
} from 'lucide-react';

const UsersPage = () => {
  const navigate = useNavigate();
  const { isSuperAdmin } = useAuth();
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [filterType, setFilterType] = useState('ALL');
  const [selectedUser, setSelectedUser] = useState(null);
  const [showActionsMenu, setShowActionsMenu] = useState(null);

  const userTypes = [
    { value: 'ALL', label: 'Todos los usuarios' },
    { value: 'SUPER_ADMIN', label: 'Super Administradores' },
    { value: 'CLIENT_ADMIN', label: 'Administradores' },
    { value: 'WORKER', label: 'Trabajadores' },
    { value: 'AUDITOR', label: 'Auditores' },
    { value: 'PROVIDER', label: 'Proveedores' },
  ];

  useEffect(() => {
    loadUsers();
  }, []);

  const loadUsers = async () => {
    try {
      setLoading(true);
      const response = await usersApi.getAll();
      const userData = response.data.content || response.data;
      setUsers(Array.isArray(userData) ? userData : []);
    } catch (error) {
      console.error('Error loading users:', error);
      setUsers([]);
    } finally {
      setLoading(false);
    }
  };

  const filteredUsers = users.filter(user => {
    const matchesSearch = user.username.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         user.email?.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         `${user.firstName} ${user.lastName}`.toLowerCase().includes(searchTerm.toLowerCase());
    
    const matchesFilter = filterType === 'ALL' || user.userType === filterType;
    
    return matchesSearch && matchesFilter;
  });

  const handleUserAction = async (action, userId) => {
    try {
      switch (action) {
        case 'enable':
          await usersApi.enable(userId);
          break;
        case 'disable':
          await usersApi.disable(userId);
          break;
        case 'lock':
          await usersApi.lock(userId);
          break;
        case 'unlock':
          await usersApi.unlock(userId);
          break;
        case 'delete':
          if (window.confirm('¿Estás seguro de que quieres eliminar este usuario?')) {
            await usersApi.delete(userId);
          }
          break;
      }
      loadUsers(); // Recargar la lista
      setShowActionsMenu(null);
    } catch (error) {
      console.error('Error performing user action:', error);
      alert('Error al realizar la acción');
    }
  };

  const UserCard = ({ user }) => (
    <div className="card hover:shadow-soft-lg transition-shadow duration-200">
      <div className="flex items-start justify-between">
        <div className="flex items-start space-x-4">
          <div className="w-12 h-12 bg-primary-100 rounded-full flex items-center justify-center">
            <Users className="w-6 h-6 text-primary-600" />
          </div>
          <div className="flex-1 min-w-0">
            <h3 className="text-lg font-medium text-gray-900 truncate">
              {user.firstName} {user.lastName}
            </h3>
            <p className="text-sm text-gray-600">@{user.username}</p>
            <p className="text-sm text-gray-500">{user.email}</p>
            
            <div className="flex items-center space-x-4 mt-3">
              <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                user.userType === 'SUPER_ADMIN' ? 'bg-red-100 text-red-800' :
                user.userType === 'CLIENT_ADMIN' ? 'bg-purple-100 text-purple-800' :
                user.userType === 'AUDITOR' ? 'bg-blue-100 text-blue-800' :
                'bg-gray-100 text-gray-800'
              }`}>
                {user.userType}
              </span>
              
              <div className="flex items-center space-x-2">
                {user.enabled ? (
                  <div className="flex items-center text-green-600">
                    <Shield className="w-4 h-4 mr-1" />
                    <span className="text-xs">Activo</span>
                  </div>
                ) : (
                  <div className="flex items-center text-red-600">
                    <ShieldOff className="w-4 h-4 mr-1" />
                    <span className="text-xs">Inactivo</span>
                  </div>
                )}
                
                {!user.accountNonLocked && (
                  <div className="flex items-center text-orange-600">
                    <Lock className="w-4 h-4 mr-1" />
                    <span className="text-xs">Bloqueado</span>
                  </div>
                )}
              </div>
            </div>
            
            {user.roles && user.roles.length > 0 && (
              <div className="mt-2">
                <div className="flex flex-wrap gap-1">
                  {user.roles.slice(0, 2).map((role, idx) => (
                    <span key={idx} className="inline-flex items-center px-2 py-1 rounded-md text-xs bg-blue-50 text-blue-700">
                      {role.name?.replace('ROLE_', '')}
                    </span>
                  ))}
                  {user.roles.length > 2 && (
                    <span className="inline-flex items-center px-2 py-1 rounded-md text-xs bg-gray-50 text-gray-700">
                      +{user.roles.length - 2} más
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
              onClick={() => setShowActionsMenu(showActionsMenu === user.id ? null : user.id)}
              className="p-2 text-gray-400 hover:text-gray-600 rounded-lg hover:bg-gray-100"
            >
              <MoreVertical className="w-5 h-5" />
            </button>
            
            {showActionsMenu === user.id && (
              <div className="absolute right-0 top-10 w-48 bg-white rounded-lg shadow-soft-lg border border-gray-200 py-2 z-10">
                <button
                  onClick={() => navigate(`/users/${user.id}`)}
                  className="w-full px-4 py-2 text-left text-sm text-gray-700 hover:bg-gray-100 flex items-center space-x-2"
                >
                  <Eye className="w-4 h-4" />
                  <span>Ver detalles</span>
                </button>
                <button
                  onClick={() => navigate(`/users/${user.id}/edit`)}
                  className="w-full px-4 py-2 text-left text-sm text-gray-700 hover:bg-gray-100 flex items-center space-x-2"
                >
                  <Edit className="w-4 h-4" />
                  <span>Editar</span>
                </button>
                <hr className="my-1" />
                <button
                  onClick={() => handleUserAction(user.enabled ? 'disable' : 'enable', user.id)}
                  className="w-full px-4 py-2 text-left text-sm text-gray-700 hover:bg-gray-100 flex items-center space-x-2"
                >
                  {user.enabled ? <ShieldOff className="w-4 h-4" /> : <Shield className="w-4 h-4" />}
                  <span>{user.enabled ? 'Desactivar' : 'Activar'}</span>
                </button>
                <button
                  onClick={() => handleUserAction(user.accountNonLocked ? 'lock' : 'unlock', user.id)}
                  className="w-full px-4 py-2 text-left text-sm text-gray-700 hover:bg-gray-100 flex items-center space-x-2"
                >
                  {user.accountNonLocked ? <Lock className="w-4 h-4" /> : <Unlock className="w-4 h-4" />}
                  <span>{user.accountNonLocked ? 'Bloquear' : 'Desbloquear'}</span>
                </button>
                <hr className="my-1" />
                <button
                  onClick={() => handleUserAction('delete', user.id)}
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
              <div className="h-32 bg-gray-200 rounded"></div>
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
          <h1 className="text-2xl font-bold text-gray-900">Gestión de Usuarios</h1>
          <p className="mt-1 text-sm text-gray-600">
            Administra usuarios del sistema ({filteredUsers.length} usuarios)
          </p>
        </div>
        
        <div className="mt-4 sm:mt-0 flex space-x-3">
          <button
            onClick={loadUsers}
            className="btn-secondary flex items-center space-x-2"
          >
            <RefreshCw className="w-4 h-4" />
            <span>Actualizar</span>
          </button>
          
          {isSuperAdmin() && (
            <button
              onClick={() => navigate('/users/new')}
              className="btn-primary flex items-center space-x-2"
            >
              <Plus className="w-4 h-4" />
              <span>Nuevo Usuario</span>
            </button>
          )}
        </div>
      </div>

      {/* Filters */}
      <div className="card">
        <div className="flex flex-col sm:flex-row sm:items-center space-y-4 sm:space-y-0 sm:space-x-4">
          <div className="flex-1">
            <div className="relative">
              <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
              <input
                type="text"
                placeholder="Buscar usuarios..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="input pl-10"
              />
            </div>
          </div>
          
          <div className="flex items-center space-x-2">
            <Filter className="w-5 h-5 text-gray-400" />
            <select
              value={filterType}
              onChange={(e) => setFilterType(e.target.value)}
              className="input min-w-0 sm:w-48"
            >
              {userTypes.map(type => (
                <option key={type.value} value={type.value}>
                  {type.label}
                </option>
              ))}
            </select>
          </div>
        </div>
      </div>

      {/* Users Grid */}
      {filteredUsers.length === 0 ? (
        <div className="text-center py-12">
          <Users className="w-12 h-12 text-gray-300 mx-auto mb-4" />
          <h3 className="text-lg font-medium text-gray-900 mb-2">No se encontraron usuarios</h3>
          <p className="text-gray-600 mb-6">
            {searchTerm || filterType !== 'ALL' 
              ? 'Intenta ajustar los filtros de búsqueda' 
              : 'Comienza creando tu primer usuario'
            }
          </p>
          {isSuperAdmin() && !searchTerm && filterType === 'ALL' && (
            <button
              onClick={() => navigate('/users/new')}
              className="btn-primary"
            >
              Crear Primer Usuario
            </button>
          )}
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredUsers.map(user => (
            <UserCard key={user.id} user={user} />
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

export default UsersPage;