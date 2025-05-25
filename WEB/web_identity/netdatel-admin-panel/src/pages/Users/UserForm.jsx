import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { usersApi, rolesApi } from '../../services/api';
import { 
  ArrowLeft, 
  Save, 
  Eye, 
  EyeOff, 
  User, 
  Mail, 
  Shield, 
  AlertCircle,
  Check,
  X
} from 'lucide-react';

const UserForm = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const isEditing = Boolean(id);
  
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [availableRoles, setAvailableRoles] = useState([]);
  const [showPassword, setShowPassword] = useState(false);
  const [errors, setErrors] = useState({});
  
  const [formData, setFormData] = useState({
    username: '',
    email: '',
    password: '',
    confirmPassword: '',
    firstName: '',
    lastName: '',
    userType: 'WORKER',
    enabled: true,
    accountNonLocked: true,
    selectedRoles: []
  });

  const userTypes = [
    { value: 'WORKER', label: 'Trabajador', description: 'Usuario estándar del sistema' },
    { value: 'CLIENT_ADMIN', label: 'Administrador Cliente', description: 'Administrador con permisos limitados' },
    { value: 'AUDITOR', label: 'Auditor', description: 'Usuario con permisos de auditoría' },
    { value: 'PROVIDER', label: 'Proveedor', description: 'Usuario proveedor externo' },
    { value: 'SUPER_ADMIN', label: 'Super Administrador', description: 'Control total del sistema' },
  ];

  useEffect(() => {
    loadAvailableRoles();
    if (isEditing) {
      loadUserData();
    }
  }, [id, isEditing]);

  const loadAvailableRoles = async () => {
    try {
      const response = await rolesApi.getAll();
      const rolesData = response.data.content || response.data;
      setAvailableRoles(Array.isArray(rolesData) ? rolesData : []);
    } catch (error) {
      console.error('Error loading roles:', error);
    }
  };

  const loadUserData = async () => {
    try {
      setLoading(true);
      const response = await usersApi.getById(id);
      const userData = response.data;
      
      setFormData({
        username: userData.username || '',
        email: userData.email || '',
        password: '',
        confirmPassword: '',
        firstName: userData.firstName || '',
        lastName: userData.lastName || '',
        userType: userData.userType || 'WORKER',
        enabled: userData.enabled ?? true,
        accountNonLocked: userData.accountNonLocked ?? true,
        selectedRoles: userData.roles?.map(role => role.id) || []
      });
    } catch (error) {
      console.error('Error loading user:', error);
      alert('Error al cargar los datos del usuario');
      navigate('/users');
    } finally {
      setLoading(false);
    }
  };

  const validateForm = () => {
    const newErrors = {};

    if (!formData.username.trim()) {
      newErrors.username = 'El nombre de usuario es requerido';
    } else if (formData.username.length < 3) {
      newErrors.username = 'El nombre de usuario debe tener al menos 3 caracteres';
    }

    if (!formData.email.trim()) {
      newErrors.email = 'El email es requerido';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = 'El email no tiene un formato válido';
    }

    if (!isEditing || formData.password) {
      if (!formData.password) {
        newErrors.password = 'La contraseña es requerida';
      } else if (formData.password.length < 8) {
        newErrors.password = 'La contraseña debe tener al menos 8 caracteres';
      }

      if (formData.password !== formData.confirmPassword) {
        newErrors.confirmPassword = 'Las contraseñas no coinciden';
      }
    }

    if (!formData.firstName.trim()) {
      newErrors.firstName = 'El nombre es requerido';
    }

    if (!formData.lastName.trim()) {
      newErrors.lastName = 'El apellido es requerido';
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
      
      const userData = {
        username: formData.username,
        email: formData.email,
        userType: formData.userType,
        firstName: formData.firstName,
        lastName: formData.lastName,
        enabled: formData.enabled,
        accountNonLocked: formData.accountNonLocked,
      };

      // Solo incluir password si se está creando o si se cambió
      if (!isEditing || formData.password) {
        userData.password = formData.password;
      }

      if (isEditing) {
        await usersApi.update(id, userData);
      } else {
        await usersApi.create(userData);
      }

      navigate('/users');
    } catch (error) {
      console.error('Error saving user:', error);
      const errorMessage = error.response?.data?.message || 'Error al guardar el usuario';
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

  const handleRoleToggle = (roleId) => {
    setFormData(prev => ({
      ...prev,
      selectedRoles: prev.selectedRoles.includes(roleId)
        ? prev.selectedRoles.filter(id => id !== roleId)
        : [...prev.selectedRoles, roleId]
    }));
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
          onClick={() => navigate('/users')}
          className="p-2 text-gray-600 hover:text-gray-900 rounded-lg hover:bg-gray-100"
        >
          <ArrowLeft className="w-5 h-5" />
        </button>
        <div>
          <h1 className="text-2xl font-bold text-gray-900">
            {isEditing ? 'Editar Usuario' : 'Crear Usuario'}
          </h1>
          <p className="text-sm text-gray-600">
            {isEditing ? 'Modifica la información del usuario' : 'Completa los datos para crear un nuevo usuario'}
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
                <User className="w-5 h-5 mr-2" />
                Información Personal
              </h3>
              
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label htmlFor="firstName" className="block text-sm font-medium text-gray-700 mb-2">
                    Nombre *
                  </label>
                  <input
                    id="firstName"
                    name="firstName"
                    type="text"
                    value={formData.firstName}
                    onChange={handleChange}
                    className={`input ${errors.firstName ? 'border-red-300 focus:border-red-500 focus:ring-red-500' : ''}`}
                    placeholder="Ingresa el nombre"
                  />
                  {errors.firstName && (
                    <p className="mt-1 text-sm text-red-600 flex items-center">
                      <AlertCircle className="w-4 h-4 mr-1" />
                      {errors.firstName}
                    </p>
                  )}
                </div>

                <div>
                  <label htmlFor="lastName" className="block text-sm font-medium text-gray-700 mb-2">
                    Apellido *
                  </label>
                  <input
                    id="lastName"
                    name="lastName"
                    type="text"
                    value={formData.lastName}
                    onChange={handleChange}
                    className={`input ${errors.lastName ? 'border-red-300 focus:border-red-500 focus:ring-red-500' : ''}`}
                    placeholder="Ingresa el apellido"
                  />
                  {errors.lastName && (
                    <p className="mt-1 text-sm text-red-600 flex items-center">
                      <AlertCircle className="w-4 h-4 mr-1" />
                      {errors.lastName}
                    </p>
                  )}
                </div>

                <div>
                  <label htmlFor="username" className="block text-sm font-medium text-gray-700 mb-2">
                    Nombre de Usuario *
                  </label>
                  <input
                    id="username"
                    name="username"
                    type="text"
                    value={formData.username}
                    onChange={handleChange}
                    className={`input ${errors.username ? 'border-red-300 focus:border-red-500 focus:ring-red-500' : ''}`}
                    placeholder="usuario.ejemplo"
                  />
                  {errors.username && (
                    <p className="mt-1 text-sm text-red-600 flex items-center">
                      <AlertCircle className="w-4 h-4 mr-1" />
                      {errors.username}
                    </p>
                  )}
                </div>

                <div>
                  <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-2">
                    Email *
                  </label>
                  <input
                    id="email"
                    name="email"
                    type="email"
                    value={formData.email}
                    onChange={handleChange}
                    className={`input ${errors.email ? 'border-red-300 focus:border-red-500 focus:ring-red-500' : ''}`}
                    placeholder="usuario@ejemplo.com"
                  />
                  {errors.email && (
                    <p className="mt-1 text-sm text-red-600 flex items-center">
                      <AlertCircle className="w-4 h-4 mr-1" />
                      {errors.email}
                    </p>
                  )}
                </div>
              </div>
            </div>

            {/* Security */}
            <div className="card">
              <h3 className="text-lg font-medium text-gray-900 mb-6 flex items-center">
                <Shield className="w-5 h-5 mr-2" />
                Seguridad
              </h3>
              
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label htmlFor="password" className="block text-sm font-medium text-gray-700 mb-2">
                    {isEditing ? 'Nueva Contraseña (opcional)' : 'Contraseña *'}
                  </label>
                  <div className="relative">
                    <input
                      id="password"
                      name="password"
                      type={showPassword ? 'text' : 'password'}
                      value={formData.password}
                      onChange={handleChange}
                      className={`input pr-10 ${errors.password ? 'border-red-300 focus:border-red-500 focus:ring-red-500' : ''}`}
                      placeholder={isEditing ? 'Dejar vacío para mantener actual' : 'Mínimo 8 caracteres'}
                    />
                    <button
                      type="button"
                      onClick={() => setShowPassword(!showPassword)}
                      className="absolute inset-y-0 right-0 pr-3 flex items-center text-gray-500 hover:text-gray-700"
                    >
                      {showPassword ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
                    </button>
                  </div>
                  {errors.password && (
                    <p className="mt-1 text-sm text-red-600 flex items-center">
                      <AlertCircle className="w-4 h-4 mr-1" />
                      {errors.password}
                    </p>
                  )}
                </div>

                <div>
                  <label htmlFor="confirmPassword" className="block text-sm font-medium text-gray-700 mb-2">
                    Confirmar Contraseña {!isEditing && '*'}
                  </label>
                  <input
                    id="confirmPassword"
                    name="confirmPassword"
                    type="password"
                    value={formData.confirmPassword}
                    onChange={handleChange}
                    className={`input ${errors.confirmPassword ? 'border-red-300 focus:border-red-500 focus:ring-red-500' : ''}`}
                    placeholder="Confirma la contraseña"
                  />
                  {errors.confirmPassword && (
                    <p className="mt-1 text-sm text-red-600 flex items-center">
                      <AlertCircle className="w-4 h-4 mr-1" />
                      {errors.confirmPassword}
                    </p>
                  )}
                </div>
              </div>
            </div>
          </div>

          {/* Sidebar */}
          <div className="space-y-6">
            {/* User Type */}
            <div className="card">
              <h3 className="text-lg font-medium text-gray-900 mb-4">Tipo de Usuario</h3>
              <div className="space-y-3">
                {userTypes.map(type => (
                  <label key={type.value} className="flex items-start space-x-3 cursor-pointer">
                    <input
                      type="radio"
                      name="userType"
                      value={type.value}
                      checked={formData.userType === type.value}
                      onChange={handleChange}
                      className="mt-1 text-primary-600 focus:ring-primary-500"
                    />
                    <div className="flex-1 min-w-0">
                      <div className="text-sm font-medium text-gray-900">{type.label}</div>
                      <div className="text-xs text-gray-500">{type.description}</div>
                    </div>
                  </label>
                ))}
              </div>
            </div>

            {/* Status */}
            <div className="card">
              <h3 className="text-lg font-medium text-gray-900 mb-4">Estado</h3>
              <div className="space-y-4">
                <label className="flex items-center justify-between">
                  <div>
                    <div className="text-sm font-medium text-gray-900">Usuario Activo</div>
                    <div className="text-xs text-gray-500">El usuario puede iniciar sesión</div>
                  </div>
                  <input
                    type="checkbox"
                    name="enabled"
                    checked={formData.enabled}
                    onChange={handleChange}
                    className="text-primary-600 focus:ring-primary-500 rounded"
                  />
                </label>
                
                <label className="flex items-center justify-between">
                  <div>
                    <div className="text-sm font-medium text-gray-900">Cuenta Desbloqueada</div>
                    <div className="text-xs text-gray-500">La cuenta no está bloqueada</div>
                  </div>
                  <input
                    type="checkbox"
                    name="accountNonLocked"
                    checked={formData.accountNonLocked}
                    onChange={handleChange}
                    className="text-primary-600 focus:ring-primary-500 rounded"
                  />
                </label>
              </div>
            </div>

            {/* Roles */}
            {availableRoles.length > 0 && (
              <div className="card">
                <h3 className="text-lg font-medium text-gray-900 mb-4">Roles</h3>
                <div className="space-y-2 max-h-48 overflow-y-auto">
                  {availableRoles.map(role => (
                    <label key={role.id} className="flex items-center space-x-2 cursor-pointer">
                      <input
                        type="checkbox"
                        checked={formData.selectedRoles.includes(role.id)}
                        onChange={() => handleRoleToggle(role.id)}
                        className="text-primary-600 focus:ring-primary-500 rounded"
                      />
                      <div className="flex-1 min-w-0">
                        <div className="text-sm font-medium text-gray-900">
                          {role.name?.replace('ROLE_', '')}
                        </div>
                        {role.description && (
                          <div className="text-xs text-gray-500">{role.description}</div>
                        )}
                      </div>
                    </label>
                  ))}
                </div>
              </div>
            )}
          </div>
        </div>

        {/* Form Actions */}
        <div className="flex justify-end space-x-4 pt-6 border-t border-gray-200">
          <button
            type="button"
            onClick={() => navigate('/users')}
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
                <span>{isEditing ? 'Actualizar' : 'Crear'} Usuario</span>
              </>
            )}
          </button>
        </div>
      </form>
    </div>
  );
};

export default UserForm;