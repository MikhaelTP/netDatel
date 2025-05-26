import React, { useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import { usersApi } from '../../services/api';
import { 
  User, 
  Save, 
  Eye, 
  EyeOff, 
  Shield, 
  Mail, 
  Calendar,
  MapPin,
  Phone,
  AlertCircle,
  CheckCircle,
  Edit,
  Camera
} from 'lucide-react';

const ProfilePage = () => {
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState('personal');
  const [saving, setSaving] = useState(false);
  const [showPassword, setShowPassword] = useState({});
  const [notification, setNotification] = useState(null);
  const [errors, setErrors] = useState({});

  const [personalData, setPersonalData] = useState({
    firstName: user?.firstName || '',
    lastName: user?.lastName || '',
    email: user?.email || '',
    phone: '',
    bio: '',
    location: 'Lima, Perú',
    timezone: 'America/Lima',
    language: 'es',
  });

  const [passwordData, setPasswordData] = useState({
    currentPassword: '',
    newPassword: '',
    confirmPassword: '',
  });

  const [preferences, setPreferences] = useState({
    emailNotifications: true,
    systemNotifications: true,
    darkMode: false,
    compactView: false,
    autoSave: true,
  });

  const tabs = [
    { id: 'personal', label: 'Información Personal', icon: User },
    { id: 'security', label: 'Seguridad', icon: Shield },
    { id: 'preferences', label: 'Preferencias', icon: Edit },
  ];

  const showNotification = (message, type = 'success') => {
    setNotification({ message, type });
    setTimeout(() => setNotification(null), 3000);
  };

  const validatePersonalData = () => {
    const newErrors = {};

    if (!personalData.firstName.trim()) {
      newErrors.firstName = 'El nombre es requerido';
    }

    if (!personalData.lastName.trim()) {
      newErrors.lastName = 'El apellido es requerido';
    }

    if (!personalData.email.trim()) {
      newErrors.email = 'El email es requerido';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(personalData.email)) {
      newErrors.email = 'El email no tiene un formato válido';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const validatePasswordData = () => {
    const newErrors = {};

    if (!passwordData.currentPassword) {
      newErrors.currentPassword = 'La contraseña actual es requerida';
    }

    if (!passwordData.newPassword) {
      newErrors.newPassword = 'La nueva contraseña es requerida';
    } else if (passwordData.newPassword.length < 8) {
      newErrors.newPassword = 'La contraseña debe tener al menos 8 caracteres';
    }

    if (passwordData.newPassword !== passwordData.confirmPassword) {
      newErrors.confirmPassword = 'Las contraseñas no coinciden';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSavePersonal = async () => {
    if (!validatePersonalData()) return;

    try {
      setSaving(true);
      
      // Aquí harías la llamada a la API
      // await usersApi.updateProfile(user.id, personalData);
      
      // Simulamos una llamada exitosa
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      showNotification('Información personal actualizada exitosamente');
    } catch (error) {
      console.error('Error updating personal data:', error);
      showNotification('Error al actualizar la información', 'error');
    } finally {
      setSaving(false);
    }
  };

  const handleSavePassword = async () => {
    if (!validatePasswordData()) return;

    try {
      setSaving(true);
      
      // Aquí harías la llamada a la API
      // await usersApi.changePassword(user.id, {
      //   currentPassword: passwordData.currentPassword,
      //   newPassword: passwordData.newPassword
      // });
      
      // Simulamos una llamada exitosa
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      setPasswordData({
        currentPassword: '',
        newPassword: '',
        confirmPassword: '',
      });
      
      showNotification('Contraseña actualizada exitosamente');
    } catch (error) {
      console.error('Error updating password:', error);
      showNotification('Error al cambiar la contraseña', 'error');
    } finally {
      setSaving(false);
    }
  };

  const handleSavePreferences = async () => {
    try {
      setSaving(true);
      
      // Aquí harías la llamada a la API
      // await usersApi.updatePreferences(user.id, preferences);
      
      // Simulamos una llamada exitosa
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      showNotification('Preferencias guardadas exitosamente');
    } catch (error) {
      console.error('Error updating preferences:', error);
      showNotification('Error al guardar las preferencias', 'error');
    } finally {
      setSaving(false);
    }
  };

  const togglePasswordVisibility = (field) => {
    setShowPassword(prev => ({
      ...prev,
      [field]: !prev[field]
    }));
  };

  const PersonalInfoTab = () => (
    <div className="space-y-6">
      {/* Profile Picture */}
      <div className="flex items-center space-x-6">
        <div className="relative">
          <div className="w-24 h-24 bg-primary-100 rounded-full flex items-center justify-center">
            <User className="w-12 h-12 text-primary-600" />
          </div>
          <button className="absolute bottom-0 right-0 bg-white rounded-full p-2 shadow-lg border border-gray-200 hover:bg-gray-50">
            <Camera className="w-4 h-4 text-gray-600" />
          </button>
        </div>
        <div>
          <h3 className="text-lg font-medium text-gray-900">Foto de Perfil</h3>
          <p className="text-sm text-gray-600">Cambia tu foto de perfil</p>
          <button className="mt-2 text-sm text-primary-600 hover:text-primary-700">
            Subir nueva foto
          </button>
        </div>
      </div>

      {/* Personal Information */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <div>
          <label htmlFor="firstName" className="block text-sm font-medium text-gray-700 mb-2">
            Nombre *
          </label>
          <input
            id="firstName"
            type="text"
            value={personalData.firstName}
            onChange={(e) => setPersonalData(prev => ({ ...prev, firstName: e.target.value }))}
            className={`input ${errors.firstName ? 'border-red-300 focus:border-red-500 focus:ring-red-500' : ''}`}
            placeholder="Tu nombre"
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
            type="text"
            value={personalData.lastName}
            onChange={(e) => setPersonalData(prev => ({ ...prev, lastName: e.target.value }))}
            className={`input ${errors.lastName ? 'border-red-300 focus:border-red-500 focus:ring-red-500' : ''}`}
            placeholder="Tu apellido"
          />
          {errors.lastName && (
            <p className="mt-1 text-sm text-red-600 flex items-center">
              <AlertCircle className="w-4 h-4 mr-1" />
              {errors.lastName}
            </p>
          )}
        </div>

        <div>
          <label htmlFor="email" className="block text-sm font-medium text-gray-700 mb-2">
            Email *
          </label>
          <input
            id="email"
            type="email"
            value={personalData.email}
            onChange={(e) => setPersonalData(prev => ({ ...prev, email: e.target.value }))}
            className={`input ${errors.email ? 'border-red-300 focus:border-red-500 focus:ring-red-500' : ''}`}
            placeholder="tu@email.com"
          />
          {errors.email && (
            <p className="mt-1 text-sm text-red-600 flex items-center">
              <AlertCircle className="w-4 h-4 mr-1" />
              {errors.email}
            </p>
          )}
        </div>

        <div>
          <label htmlFor="phone" className="block text-sm font-medium text-gray-700 mb-2">
            Teléfono
          </label>
          <input
            id="phone"
            type="tel"
            value={personalData.phone}
            onChange={(e) => setPersonalData(prev => ({ ...prev, phone: e.target.value }))}
            className="input"
            placeholder="+51 999 999 999"
          />
        </div>

        <div>
          <label htmlFor="location" className="block text-sm font-medium text-gray-700 mb-2">
            Ubicación
          </label>
          <input
            id="location"
            type="text"
            value={personalData.location}
            onChange={(e) => setPersonalData(prev => ({ ...prev, location: e.target.value }))}
            className="input"
            placeholder="Ciudad, País"
          />
        </div>

        <div>
          <label htmlFor="timezone" className="block text-sm font-medium text-gray-700 mb-2">
            Zona Horaria
          </label>
          <select
            id="timezone"
            value={personalData.timezone}
            onChange={(e) => setPersonalData(prev => ({ ...prev, timezone: e.target.value }))}
            className="input"
          >
            <option value="America/Lima">Lima (UTC-5)</option>
            <option value="America/New_York">Nueva York (UTC-5)</option>
            <option value="Europe/Madrid">Madrid (UTC+1)</option>
            <option value="UTC">UTC</option>
          </select>
        </div>
      </div>

      <div>
        <label htmlFor="bio" className="block text-sm font-medium text-gray-700 mb-2">
          Biografía
        </label>
        <textarea
          id="bio"
          rows={4}
          value={personalData.bio}
          onChange={(e) => setPersonalData(prev => ({ ...prev, bio: e.target.value }))}
          className="input"
          placeholder="Cuéntanos un poco sobre ti..."
        />
      </div>

      <div className="flex justify-end">
        <button
          onClick={handleSavePersonal}
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
              <span>Guardar Cambios</span>
            </>
          )}
        </button>
      </div>
    </div>
  );

  const SecurityTab = () => (
    <div className="space-y-6">
      {/* Account Info */}
      <div className="bg-gray-50 rounded-lg p-4">
        <h3 className="text-lg font-medium text-gray-900 mb-4">Información de la Cuenta</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Nombre de Usuario
            </label>
            <p className="text-sm text-gray-900">{user?.username}</p>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Tipo de Usuario
            </label>
            <p className="text-sm text-gray-900">{user?.userType}</p>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Último Acceso
            </label>
            <p className="text-sm text-gray-900">Hoy a las 10:30 AM</p>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              Estado de la Cuenta
            </label>
            <span className="inline-flex items-center px-2 py-1 rounded-full text-xs bg-green-100 text-green-800">
              Activa
            </span>
          </div>
        </div>
      </div>

      {/* Change Password */}
      <div>
        <h3 className="text-lg font-medium text-gray-900 mb-4">Cambiar Contraseña</h3>
        <div className="space-y-4">
          <div>
            <label htmlFor="currentPassword" className="block text-sm font-medium text-gray-700 mb-2">
              Contraseña Actual *
            </label>
            <div className="relative">
              <input
                id="currentPassword"
                type={showPassword.current ? 'text' : 'password'}
                value={passwordData.currentPassword}
                onChange={(e) => setPasswordData(prev => ({ ...prev, currentPassword: e.target.value }))}
                className={`input pr-10 ${errors.currentPassword ? 'border-red-300 focus:border-red-500 focus:ring-red-500' : ''}`}
                placeholder="Tu contraseña actual"
              />
              <button
                type="button"
                onClick={() => togglePasswordVisibility('current')}
                className="absolute inset-y-0 right-0 pr-3 flex items-center text-gray-500 hover:text-gray-700"
              >
                {showPassword.current ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
              </button>
            </div>
            {errors.currentPassword && (
              <p className="mt-1 text-sm text-red-600 flex items-center">
                <AlertCircle className="w-4 h-4 mr-1" />
                {errors.currentPassword}
              </p>
            )}
          </div>

          <div>
            <label htmlFor="newPassword" className="block text-sm font-medium text-gray-700 mb-2">
              Nueva Contraseña *
            </label>
            <div className="relative">
              <input
                id="newPassword"
                type={showPassword.new ? 'text' : 'password'}
                value={passwordData.newPassword}
                onChange={(e) => setPasswordData(prev => ({ ...prev, newPassword: e.target.value }))}
                className={`input pr-10 ${errors.newPassword ? 'border-red-300 focus:border-red-500 focus:ring-red-500' : ''}`}
                placeholder="Mínimo 8 caracteres"
              />
              <button
                type="button"
                onClick={() => togglePasswordVisibility('new')}
                className="absolute inset-y-0 right-0 pr-3 flex items-center text-gray-500 hover:text-gray-700"
              >
                {showPassword.new ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
              </button>
            </div>
            {errors.newPassword && (
              <p className="mt-1 text-sm text-red-600 flex items-center">
                <AlertCircle className="w-4 h-4 mr-1" />
                {errors.newPassword}
              </p>
            )}
          </div>

          <div>
            <label htmlFor="confirmPassword" className="block text-sm font-medium text-gray-700 mb-2">
              Confirmar Nueva Contraseña *
            </label>
            <div className="relative">
              <input
                id="confirmPassword"
                type={showPassword.confirm ? 'text' : 'password'}
                value={passwordData.confirmPassword}
                onChange={(e) => setPasswordData(prev => ({ ...prev, confirmPassword: e.target.value }))}
                className={`input pr-10 ${errors.confirmPassword ? 'border-red-300 focus:border-red-500 focus:ring-red-500' : ''}`}
                placeholder="Confirma tu nueva contraseña"
              />
              <button
                type="button"
                onClick={() => togglePasswordVisibility('confirm')}
                className="absolute inset-y-0 right-0 pr-3 flex items-center text-gray-500 hover:text-gray-700"
              >
                {showPassword.confirm ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
              </button>
            </div>
            {errors.confirmPassword && (
              <p className="mt-1 text-sm text-red-600 flex items-center">
                <AlertCircle className="w-4 h-4 mr-1" />
                {errors.confirmPassword}
              </p>
            )}
          </div>
        </div>

        <div className="flex justify-end mt-6">
          <button
            onClick={handleSavePassword}
            disabled={saving}
            className="btn-primary flex items-center space-x-2 disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {saving ? (
              <>
                <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                <span>Cambiando...</span>
              </>
            ) : (
              <>
                <Shield className="w-4 h-4" />
                <span>Cambiar Contraseña</span>
              </>
            )}
          </button>
        </div>
      </div>
    </div>
  );

  const PreferencesTab = () => (
    <div className="space-y-6">
      {/* Notifications */}
      <div>
        <h3 className="text-lg font-medium text-gray-900 mb-4">Notificaciones</h3>
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <div className="text-sm font-medium text-gray-900">Notificaciones por Email</div>
              <div className="text-sm text-gray-500">Recibir notificaciones importantes por correo</div>
            </div>
            <input
              type="checkbox"
              checked={preferences.emailNotifications}
              onChange={(e) => setPreferences(prev => ({ ...prev, emailNotifications: e.target.checked }))}
              className="text-primary-600 focus:ring-primary-500 rounded"
            />
          </div>

          <div className="flex items-center justify-between">
            <div>
              <div className="text-sm font-medium text-gray-900">Notificaciones del Sistema</div>
              <div className="text-sm text-gray-500">Mostrar notificaciones en tiempo real</div>
            </div>
            <input
              type="checkbox"
              checked={preferences.systemNotifications}
              onChange={(e) => setPreferences(prev => ({ ...prev, systemNotifications: e.target.checked }))}
              className="text-primary-600 focus:ring-primary-500 rounded"
            />
          </div>
        </div>
      </div>

      {/* Interface */}
      <div>
        <h3 className="text-lg font-medium text-gray-900 mb-4">Interfaz</h3>
        <div className="space-y-4">
          <div className="flex items-center justify-between">
            <div>
              <div className="text-sm font-medium text-gray-900">Modo Oscuro</div>
              <div className="text-sm text-gray-500">Usar tema oscuro en la interfaz</div>
            </div>
            <input
              type="checkbox"
              checked={preferences.darkMode}
              onChange={(e) => setPreferences(prev => ({ ...prev, darkMode: e.target.checked }))}
              className="text-primary-600 focus:ring-primary-500 rounded"
            />
          </div>

          <div className="flex items-center justify-between">
            <div>
              <div className="text-sm font-medium text-gray-900">Vista Compacta</div>
              <div className="text-sm text-gray-500">Mostrar más información en menos espacio</div>
            </div>
            <input
              type="checkbox"
              checked={preferences.compactView}
              onChange={(e) => setPreferences(prev => ({ ...prev, compactView: e.target.checked }))}
              className="text-primary-600 focus:ring-primary-500 rounded"
            />
          </div>

          <div className="flex items-center justify-between">
            <div>
              <div className="text-sm font-medium text-gray-900">Guardado Automático</div>
              <div className="text-sm text-gray-500">Guardar cambios automáticamente</div>
            </div>
            <input
              type="checkbox"
              checked={preferences.autoSave}
              onChange={(e) => setPreferences(prev => ({ ...prev, autoSave: e.target.checked }))}
              className="text-primary-600 focus:ring-primary-500 rounded"
            />
          </div>
        </div>
      </div>

      {/* Language */}
      <div>
        <h3 className="text-lg font-medium text-gray-900 mb-4">Idioma</h3>
        <select
          value={personalData.language}
          onChange={(e) => setPersonalData(prev => ({ ...prev, language: e.target.value }))}
          className="input max-w-xs"
        >
          <option value="es">Español</option>
          <option value="en">English</option>
          <option value="pt">Português</option>
        </select>
      </div>

      <div className="flex justify-end">
        <button
          onClick={handleSavePreferences}
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
              <span>Guardar Preferencias</span>
            </>
          )}
        </button>
      </div>
    </div>
  );

  const renderTabContent = () => {
    switch (activeTab) {
      case 'personal': return <PersonalInfoTab />;
      case 'security': return <SecurityTab />;
      case 'preferences': return <PreferencesTab />;
      default: return <PersonalInfoTab />;
    }
  };

  return (
    <div className="space-y-6 animate-fade-in">
      {/* Notification */}
      {notification && (
        <div className={`p-4 rounded-lg flex items-center space-x-3 ${
          notification.type === 'success' 
            ? 'bg-green-50 text-green-800 border border-green-200' 
            : 'bg-red-50 text-red-800 border border-red-200'
        }`}>
          {notification.type === 'success' ? (
            <CheckCircle className="w-5 h-5" />
          ) : (
            <AlertCircle className="w-5 h-5" />
          )}
          <span>{notification.message}</span>
        </div>
      )}

      {/* Header */}
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Mi Perfil</h1>
        <p className="mt-1 text-sm text-gray-600">
          Administra tu información personal y configuración de cuenta
        </p>
      </div>

      {/* Tabs */}
      <div className="card">
        <nav className="flex space-x-8 border-b border-gray-200 -mb-6 pb-4">
          {tabs.map((tab) => {
            const Icon = tab.icon;
            return (
              <button
                key={tab.id}
                onClick={() => setActiveTab(tab.id)}
                className={`flex items-center space-x-2 py-2 px-1 border-b-2 font-medium text-sm transition-colors ${
                  activeTab === tab.id
                    ? 'border-primary-500 text-primary-600'
                    : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
                }`}
              >
                <Icon className="w-5 h-5" />
                <span>{tab.label}</span>
              </button>
            );
          })}
        </nav>
      </div>

      {/* Tab Content */}
      <div className="card">
        {renderTabContent()}
      </div>
    </div>
  );
};

export default ProfilePage;