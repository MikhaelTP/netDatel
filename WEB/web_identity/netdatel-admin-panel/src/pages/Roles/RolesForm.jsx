import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { rolesApi, permissionsApi } from '../../services/api';
import { 
  ArrowLeft, 
  Save, 
  Shield, 
  Key, 
  AlertCircle,
  Search,
  Check,
  X
} from 'lucide-react';

const RoleForm = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const isEditing = Boolean(id);
  
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [availablePermissions, setAvailablePermissions] = useState([]);
  const [permissionSearch, setPermissionSearch] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('ALL');
  const [errors, setErrors] = useState({});
  
  const [formData, setFormData] = useState({
    name: '',
    description: '',
    hierarchyLevel: 5,
    isDefault: false,
    selectedPermissions: []
  });

  useEffect(() => {
    loadAvailablePermissions();
    if (isEditing) {
      loadRoleData();
    }
  }, [id, isEditing]);

  const loadAvailablePermissions = async () => {
    try {
      const response = await permissionsApi.getAll();
      const permissionsData = response.data.content || response.data;
      setAvailablePermissions(Array.isArray(permissionsData) ? permissionsData : []);
    } catch (error) {
      console.error('Error loading permissions:', error);
    }
  };

  const loadRoleData = async () => {
    try {
      setLoading(true);
      const response = await rolesApi.getById(id);
      const roleData = response.data;
      
      setFormData({
        name: roleData.name || '',
        description: roleData.description || '',
        hierarchyLevel: roleData.hierarchyLevel || 5,
        isDefault: roleData.isDefault || false,
        selectedPermissions: roleData.permissions?.map(p => p.id) || []
      });
    } catch (error) {
      console.error('Error loading role:', error);
      alert('Error al cargar los datos del rol');
      navigate('/roles');
    } finally {
      setLoading(false);
    }
  };

  const validateForm = () => {
    const newErrors = {};

    if (!formData.name.trim()) {
      newErrors.name = 'El nombre del rol es requerido';
    } else if (formData.name.length < 3) {
      newErrors.name = 'El nombre debe tener al menos 3 caracteres';
    }

    if (formData.hierarchyLevel < 1 || formData.hierarchyLevel > 10) {
      newErrors.hierarchyLevel = 'El nivel de jerarquía debe estar entre 1 y 10';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }

    try {
      setSaving(true);
      
      const roleData = {
        name: formData.name.startsWith('ROLE_') ? formData.name : `ROLE_${formData.name}`,
        description: formData.description,
        hierarchyLevel: parseInt(formData.hierarchyLevel),
        isDefault: formData.isDefault,
      };

      let savedRole;
      if (isEditing) {
        savedRole = await rolesApi.update(id, roleData);
      } else {
        savedRole = await rolesApi.create(roleData);
      }

      navigate('/roles');
    } catch (error) {
      console.error('Error saving role:', error);
      const errorMessage = error.response?.data?.message || 'Error al guardar el rol';
      alert(errorMessage);
    } finally {
      setSaving(false);
    }
  };

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
    
    // Limpiar error del campo cuando el usuario escribe
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: ''
      }));
    }
  };

  const handlePermissionToggle = (permissionId) => {
    setFormData(prev => ({
      ...prev,
      selectedPermissions: prev.selectedPermissions.includes(permissionId)
        ? prev.selectedPermissions.filter(id => id !== permissionId)
        : [...prev.selectedPermissions, permissionId]
    }));
  };

  const getUniqueCategories = () => {
    const categories = ['ALL', ...new Set(availablePermissions.map(p => p.category).filter(Boolean))];
    return categories;
  };

  const filteredPermissions = availablePermissions.filter(permission => {
    const matchesSearch = permission.code?.toLowerCase().includes(permissionSearch.toLowerCase()) ||
                         permission.name?.toLowerCase().includes(permissionSearch.toLowerCase());
    
    const matchesCategory = selectedCategory === 'ALL' || permission.category === selectedCategory;
    
    return matchesSearch && matchesCategory;
  });

  const groupedPermissions = filteredPermissions.reduce((groups, permission) => {
    const category = permission.category || 'Sin categoría';
    if (!groups[category]) {
      groups[category] = [];
    }
    groups[category].push(permission);
    return groups;
  }, {});

  if (loading) {
    return (
      <div className="space-y-6">
        <div className="flex items-center space-x-4">
          <div className="h-8 bg-gray-200 rounded w-8 animate-pulse"></div>
          <div className="h-8 bg-gray-200 rounded w-48 animate-pulse"></div>
        </div>
        <div className="card animate-pulse">
          <div className="h-96 bg-gray-200 rounded"></div>
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Header */}
      <div className="flex items-center space-x-4">
        <button
          onClick={() => navigate('/roles')}
          className="p-2 text-gray-600 hover:text-gray-900 rounded-lg hover:bg-gray-100"
        >
          <ArrowLeft className="w-5 h-5" />
        </button>
        <div>
          <h1 className="text-2xl font-bold text-gray-900">
            {isEditing ? 'Editar Rol' : 'Crear Rol'}
          </h1>
          <p className="text-sm text-gray-600">
            {isEditing ? 'Modifica la información del rol' : 'Completa los datos para crear un nuevo rol'}
          </p>
        </div>
      </div>

      {/* Form */}
      <form onSubmit={handleSubmit} className="space-y-6">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Main Information */}
          <div className="lg:col-span-2 space-y-6">
            <div className="card">
              <h3 className="text-lg font-medium text-gray-900 mb-6 flex items-center">
                <Shield className="w-5 h-5 mr-2" />
                Información del Rol
              </h3>
              
              <div className="space-y-6">
                <div>
                  <label htmlFor="name" className="block text-sm font-medium text-gray-700 mb-2">
                    Nombre del Rol *
                  </label>
                  <div className="flex">
                    <span className="inline-flex items-center px-3 rounded-l-lg border border-r-0 border-gray-300 bg-gray-50 text-gray-500 text-sm">
                      ROLE_
                    </span>
                    <input
                      id="name"
                      name="name"
                      type="text"
                      value={formData.name.replace('ROLE_', '')}
                      onChange={(e) => setFormData(prev => ({ ...prev, name: e.target.value }))}
                      className={`input rounded-l-none ${errors.name ? 'border-red-300 focus:border-red-500 focus:ring-red-500' : ''}`}
                      placeholder="ADMIN, USER, MANAGER..."
                    />
                  </div>
                  {errors.name && (
                    <p className="mt-1 text-sm text-red-600 flex items-center">
                      <AlertCircle className="w-4 h-4 mr-1" />
                      {errors.name}
                    </p>
                  )}
                </div>

                <div>
                  <label htmlFor="description" className="block text-sm font-medium text-gray-700 mb-2">
                    Descripción
                  </label>
                  <textarea
                    id="description"
                    name="description"
                    rows={3}
                    value={formData.description}
                    onChange={handleChange}
                    className="input"
                    placeholder="Describe las responsabilidades de este rol..."
                  />
                </div>

                <div>
                  <label htmlFor="hierarchyLevel" className="block text-sm font-medium text-gray-700 mb-2">
                    Nivel de Jerarquía *
                  </label>
                  <input
                    id="hierarchyLevel"
                    name="hierarchyLevel"
                    type="number"
                    min="1"
                    max="10"
                    value={formData.hierarchyLevel}
                    onChange={handleChange}
                    className={`input ${errors.hierarchyLevel ? 'border-red-300 focus:border-red-500 focus:ring-red-500' : ''}`}
                  />
                  <p className="mt-1 text-sm text-gray-500">
                    Nivel 1 = Máxima autoridad, Nivel 10 = Mínima autoridad
                  </p>
                  {errors.hierarchyLevel && (
                    <p className="mt-1 text-sm text-red-600 flex items-center">
                      <AlertCircle className="w-4 h-4 mr-1" />
                      {errors.hierarchyLevel}
                    </p>
                  )}
                </div>

                <div>
                  <label className="flex items-center space-x-2">
                    <input
                      type="checkbox"
                      name="isDefault"
                      checked={formData.isDefault}
                      onChange={handleChange}
                      className="text-primary-600 focus:ring-primary-500 rounded"
                    />
                    <span className="text-sm font-medium text-gray-700">Rol por defecto</span>
                  </label>
                  <p className="mt-1 text-sm text-gray-500">
                    Los nuevos usuarios recibirán automáticamente este rol
                  </p>
                </div>
              </div>
            </div>

            {/* Permissions */}
            <div className="card">
              <h3 className="text-lg font-medium text-gray-900 mb-6 flex items-center">
                <Key className="w-5 h-5 mr-2" />
                Permisos ({formData.selectedPermissions.length} seleccionados)
              </h3>
              
              {/* Permission Filters */}
              <div className="space-y-4 mb-6">
                <div className="relative">
                  <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
                  <input
                    type="text"
                    placeholder="Buscar permisos..."
                    value={permissionSearch}
                    onChange={(e) => setPermissionSearch(e.target.value)}
                    className="input pl-10"
                  />
                </div>
                
                <div className="flex flex-wrap gap-2">
                  {getUniqueCategories().map(category => (
                    <button
                      key={category}
                      type="button"
                      onClick={() => setSelectedCategory(category)}
                      className={`px-3 py-1 rounded-full text-sm transition-colors ${
                        selectedCategory === category
                          ? 'bg-primary-100 text-primary-700 border border-primary-200'
                          : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                      }`}
                    >
                      {category === 'ALL' ? 'Todas las categorías' : category}
                    </button>
                  ))}
                </div>
              </div>

              {/* Permissions List */}
              <div className="max-h-96 overflow-y-auto border border-gray-200 rounded-lg">
                {Object.keys(groupedPermissions).length === 0 ? (
                  <div className="p-6 text-center text-gray-500">
                    <Key className="w-8 h-8 mx-auto mb-2 text-gray-300" />
                    <p>No se encontraron permisos</p>
                  </div>
                ) : (
                  Object.entries(groupedPermissions).map(([category, permissions]) => (
                    <div key={category} className="border-b border-gray-200 last:border-b-0">
                      <div className="bg-gray-50 px-4 py-2 font-medium text-sm text-gray-700">
                        {category}
                      </div>
                      <div className="p-4 space-y-2">
                        {permissions.map(permission => (
                          <label
                            key={permission.id}
                            className="flex items-start space-x-3 cursor-pointer hover:bg-gray-50 p-2 rounded"
                          >
                            <input
                              type="checkbox"
                              checked={formData.selectedPermissions.includes(permission.id)}
                              onChange={() => handlePermissionToggle(permission.id)}
                              className="mt-1 text-primary-600 focus:ring-primary-500 rounded"
                            />
                            <div className="flex-1 min-w-0">
                              <div className="text-sm font-medium text-gray-900">
                                {permission.code}
                              </div>
                              <div className="text-sm text-gray-600">
                                {permission.name}
                              </div>
                              {permission.description && (
                                <div className="text-xs text-gray-500 mt-1">
                                  {permission.description}
                                </div>
                              )}
                            </div>
                          </label>
                        ))}
                      </div>
                    </div>
                  ))
                )}
              </div>
            </div>
          </div>

          {/* Sidebar - Selected Permissions Summary */}
          <div className="space-y-6">
            <div className="card">
              <h3 className="text-lg font-medium text-gray-900 mb-4">Resumen</h3>
              <div className="space-y-4">
                <div className="flex justify-between">
                  <span className="text-sm text-gray-600">Permisos seleccionados:</span>
                  <span className="text-sm font-medium text-gray-900">
                    {formData.selectedPermissions.length}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-sm text-gray-600">Nivel de jerarquía:</span>
                  <span className="text-sm font-medium text-gray-900">
                    {formData.hierarchyLevel}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-sm text-gray-600">Rol por defecto:</span>
                  <span className="text-sm font-medium text-gray-900">
                    {formData.isDefault ? (
                      <Check className="w-4 h-4 text-green-500" />
                    ) : (
                      <X className="w-4 h-4 text-gray-400" />
                    )}
                  </span>
                </div>
              </div>
            </div>

            {/* Quick Actions */}
            <div className="card">
              <h3 className="text-lg font-medium text-gray-900 mb-4">Acciones Rápidas</h3>
              <div className="space-y-2">
                <button
                  type="button"
                  onClick={() => {
                    const allPermissionIds = availablePermissions.map(p => p.id);
                    setFormData(prev => ({ ...prev, selectedPermissions: allPermissionIds }));
                  }}
                  className="w-full btn-secondary text-sm"
                >
                  Seleccionar Todos
                </button>
                <button
                  type="button"
                  onClick={() => setFormData(prev => ({ ...prev, selectedPermissions: [] }))}
                  className="w-full btn-secondary text-sm"
                >
                  Deseleccionar Todos
                </button>
              </div>
            </div>
          </div>
        </div>

        {/* Form Actions */}
        <div className="flex justify-end space-x-4 pt-6 border-t border-gray-200">
          <button
            type="button"
            onClick={() => navigate('/roles')}
            className="btn-secondary"
          >
            Cancelar
          </button>
          <button
            type="submit"
            disabled={saving}
            className="btn-primary flex items-center space-x-2 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {saving ? (
              <>
                <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                <span>Guardando...</span>
              </>
            ) : (
              <>
                <Save className="w-4 h-4" />
                <span>{isEditing ? 'Actualizar' : 'Crear'} Rol</span>
              </>
            )}
          </button>
        </div>
      </form>
    </div>
  );
};

export default RoleForm;