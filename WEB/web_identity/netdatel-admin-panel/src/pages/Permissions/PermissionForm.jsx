import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { permissionsApi } from '../../services/api';
import { 
  ArrowLeft, 
  Save, 
  Key, 
  Code, 
  Tags, 
  Server,
  AlertCircle,
  Check,
  X
} from 'lucide-react';

const PermissionForm = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const isEditing = Boolean(id);
  
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [errors, setErrors] = useState({});
  
  const [formData, setFormData] = useState({
    code: '',
    name: '',
    description: '',
    category: '',
    service: 'admin-service',
    isActive: true
  });

  const services = [
    { value: 'admin-service', label: 'Admin Service', description: 'Gestión de clientes y administración' },
    { value: 'document-service', label: 'Document Service', description: 'Gestión de documentos y archivos' },
    { value: 'provider-service', label: 'Provider Service', description: 'Gestión de proveedores y auditorías' },
    { value: 'auth-service', label: 'Auth Service', description: 'Autenticación y autorización' },
  ];

  const commonCategories = [
    'user-management',
    'client-management', 
    'document-management',
    'provider-management',
    'audit-management',
    'system-configuration',
    'security',
    'reporting'
  ];

  useEffect(() => {
    if (isEditing) {
      loadPermissionData();
    }
  }, [id, isEditing]);

  const loadPermissionData = async () => {
    try {
      setLoading(true);
      const response = await permissionsApi.getById(id);
      const permissionData = response.data;
      
      setFormData({
        code: permissionData.code || '',
        name: permissionData.name || '',
        description: permissionData.description || '',
        category: permissionData.category || '',
        service: permissionData.service || 'admin-service',
        isActive: permissionData.isActive ?? true
      });
    } catch (error) {
      console.error('Error loading permission:', error);
      alert('Error al cargar los datos del permiso');
      navigate('/permissions');
    } finally {
      setLoading(false);
    }
  };

  const validateForm = () => {
    const newErrors = {};

    if (!formData.code.trim()) {
      newErrors.code = 'El código del permiso es requerido';
    } else if (formData.code.length < 3) {
      newErrors.code = 'El código debe tener al menos 3 caracteres';
    } else if (!/^[a-z][a-z0-9]*:[a-z][a-z0-9]*:[a-z][a-z0-9]*$/.test(formData.code)) {
      newErrors.code = 'El código debe seguir el formato: servicio:entidad:accion (ej: admin:client:create)';
    }

    if (!formData.name.trim()) {
      newErrors.name = 'El nombre del permiso es requerido';
    } else if (formData.name.length < 3) {
      newErrors.name = 'El nombre debe tener al menos 3 caracteres';
    }

    if (!formData.service) {
      newErrors.service = 'El servicio es requerido';
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
      
      const permissionData = {
        code: formData.code.toLowerCase(),
        name: formData.name,
        description: formData.description,
        category: formData.category || null,
        service: formData.service,
        isActive: formData.isActive,
      };

      if (isEditing) {
        await permissionsApi.update(id, permissionData);
      } else {
        await permissionsApi.create(permissionData);
      }

      navigate('/permissions');
    } catch (error) {
      console.error('Error saving permission:', error);
      const errorMessage = error.response?.data?.message || 'Error al guardar el permiso';
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

  const generateCodeFromName = () => {
    if (formData.name && formData.service) {
      // Generar un código sugerido basado en el nombre y servicio
      const servicePrefix = formData.service.replace('-service', '');
      const nameSlug = formData.name
        .toLowerCase()
        .replace(/[^a-z0-9\s]/g, '')
        .replace(/\s+/g, '_');
      
      // Formato: servicio:entidad:accion
      const suggestedCode = `${servicePrefix}:${nameSlug}:action`;
      setFormData(prev => ({ ...prev, code: suggestedCode }));
    }
  };

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
          onClick={() => navigate('/permissions')}
          className="p-2 text-gray-600 hover:text-gray-900 rounded-lg hover:bg-gray-100"
        >
          <ArrowLeft className="w-5 h-5" />
        </button>
        <div>
          <h1 className="text-2xl font-bold text-gray-900">
            {isEditing ? 'Editar Permiso' : 'Crear Permiso'}
          </h1>
          <p className="text-sm text-gray-600">
            {isEditing ? 'Modifica la información del permiso' : 'Completa los datos para crear un nuevo permiso'}
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
                <Key className="w-5 h-5 mr-2" />
                Información del Permiso
              </h3>
              
              <div className="space-y-6">
                <div>
                  <label htmlFor="name" className="block text-sm font-medium text-gray-700 mb-2">
                    Nombre del Permiso *
                  </label>
                  <input
                    id="name"
                    name="name"
                    type="text"
                    value={formData.name}
                    onChange={handleChange}
                    onBlur={generateCodeFromName}
                    className={`input ${errors.name ? 'border-red-300 focus:border-red-500 focus:ring-red-500' : ''}`}
                    placeholder="Crear cliente, Ver documentos, Editar proveedor..."
                  />
                  {errors.name && (
                    <p className="mt-1 text-sm text-red-600 flex items-center">
                      <AlertCircle className="w-4 h-4 mr-1" />
                      {errors.name}
                    </p>
                  )}
                </div>

                <div>
                  <label htmlFor="code" className="block text-sm font-medium text-gray-700 mb-2">
                    Código del Permiso *
                  </label>
                  <div className="flex items-center space-x-2">
                    <input
                      id="code"
                      name="code"
                      type="text"
                      value={formData.code}
                      onChange={handleChange}
                      className={`input ${errors.code ? 'border-red-300 focus:border-red-500 focus:ring-red-500' : ''}`}
                      placeholder="admin:client:create"
                    />
                    <button
                      type="button"
                      onClick={generateCodeFromName}
                      className="btn-secondary whitespace-nowrap"
                      disabled={!formData.name || !formData.service}
                    >
                      <Code className="w-4 h-4 mr-1" />
                      Generar
                    </button>
                  </div>
                  <p className="mt-1 text-sm text-gray-500">
                    Formato: servicio:entidad:accion (ej: admin:client:create)
                  </p>
                  {errors.code && (
                    <p className="mt-1 text-sm text-red-600 flex items-center">
                      <AlertCircle className="w-4 h-4 mr-1" />
                      {errors.code}
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
                    placeholder="Describe qué permite hacer este permiso..."
                  />
                </div>

                <div>
                  <label htmlFor="category" className="block text-sm font-medium text-gray-700 mb-2">
                    Categoría
                  </label>
                  <div className="flex space-x-2">
                    <input
                      id="category"
                      name="category"
                      type="text"
                      value={formData.category}
                      onChange={handleChange}
                      className="input"
                      placeholder="user-management, document-management..."
                      list="categories"
                    />
                    <datalist id="categories">
                      {commonCategories.map(cat => (
                        <option key={cat} value={cat} />
                      ))}
                    </datalist>
                  </div>
                  <p className="mt-1 text-sm text-gray-500">
                    Opcional: agrupa permisos relacionados
                  </p>
                </div>
              </div>
            </div>
          </div>

          {/* Sidebar */}
          <div className="space-y-6">
            {/* Service */}
            <div className="card">
              <h3 className="text-lg font-medium text-gray-900 mb-4 flex items-center">
                <Server className="w-5 h-5 mr-2" />
                Servicio
              </h3>
              <div className="space-y-3">
                {services.map(service => (
                  <label key={service.value} className="flex items-start space-x-3 cursor-pointer">
                    <input
                      type="radio"
                      name="service"
                      value={service.value}
                      checked={formData.service === service.value}
                      onChange={handleChange}
                      className="mt-1 text-primary-600 focus:ring-primary-500"
                    />
                    <div className="flex-1 min-w-0">
                      <div className="text-sm font-medium text-gray-900">{service.label}</div>
                      <div className="text-xs text-gray-500">{service.description}</div>
                    </div>
                  </label>
                ))}
              </div>
              {errors.service && (
                <p className="mt-2 text-sm text-red-600 flex items-center">
                  <AlertCircle className="w-4 h-4 mr-1" />
                  {errors.service}
                </p>
              )}
            </div>

            {/* Status */}
            <div className="card">
              <h3 className="text-lg font-medium text-gray-900 mb-4">Estado</h3>
              <label className="flex items-center justify-between">
                <div>
                  <div className="text-sm font-medium text-gray-900">Permiso Activo</div>
                  <div className="text-xs text-gray-500">El permiso puede ser asignado</div>
                </div>
                <input
                  type="checkbox"
                  name="isActive"
                  checked={formData.isActive}
                  onChange={handleChange}
                  className="text-primary-600 focus:ring-primary-500 rounded"
                />
              </label>
            </div>

            {/* Code Preview */}
            <div className="card">
              <h3 className="text-lg font-medium text-gray-900 mb-4">Vista Previa</h3>
              <div className="space-y-3">
                <div className="bg-gray-50 rounded-lg p-3">
                  <div className="text-xs text-gray-500 mb-1">Código:</div>
                  <div className="font-mono text-sm text-gray-900 break-all">
                    {formData.code || 'servicio:entidad:accion'}
                  </div>
                </div>
                
                <div className="bg-gray-50 rounded-lg p-3">
                  <div className="text-xs text-gray-500 mb-1">Servicio:</div>
                  <div className="text-sm text-gray-900">
                    {services.find(s => s.value === formData.service)?.label || 'No seleccionado'}
                  </div>
                </div>
                
                {formData.category && (
                  <div className="bg-gray-50 rounded-lg p-3">
                    <div className="text-xs text-gray-500 mb-1">Categoría:</div>
                    <div className="text-sm text-gray-900">{formData.category}</div>
                  </div>
                )}
                
                <div className="flex items-center justify-between">
                  <span className="text-sm text-gray-600">Estado:</span>
                  <span className="text-sm font-medium text-gray-900">
                    {formData.isActive ? (
                      <Check className="w-4 h-4 text-green-500" />
                    ) : (
                      <X className="w-4 h-4 text-red-500" />
                    )}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Form Actions */}
        <div className="flex justify-end space-x-4 pt-6 border-t border-gray-200">
          <button
            type="button"
            onClick={() => navigate('/permissions')}
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
                <span>{isEditing ? 'Actualizar' : 'Crear'} Permiso</span>
              </>
            )}
          </button>
        </div>
      </form>
      </div>
  );
};

export default PermissionForm;