import React, { useState, useEffect } from 'react';
import { useAuth } from '../../context/AuthContext';
import { 
  Settings, 
  Shield, 
  Database, 
  Mail, 
  Server, 
  Save, 
  RefreshCw,
  AlertCircle,
  CheckCircle,
  Eye,
  EyeOff,
  Globe,
  Clock
} from 'lucide-react';

const SettingsPage = () => {
  const { user } = useAuth();
  const [activeTab, setActiveTab] = useState('general');
  const [saving, setSaving] = useState(false);
  const [showPasswords, setShowPasswords] = useState({});
  const [notification, setNotification] = useState(null);

  const [settings, setSettings] = useState({
    general: {
      appName: 'Netdatel Admin Panel',
      appDescription: 'Sistema de Gestión de Identidades',
      defaultLanguage: 'es',
      timezone: 'America/Lima',
      sessionTimeout: 30,
    },
    security: {
      passwordMinLength: 8,
      passwordRequireUppercase: true,
      passwordRequireNumbers: true,
      passwordRequireSpecialChars: true,
      maxLoginAttempts: 5,
      lockoutDuration: 15,
      jwtExpirationMinutes: 60,
      refreshTokenExpirationDays: 7,
    },
    email: {
      smtpHost: 'smtp.mailersend.net',
      smtpPort: 587,
      smtpUser: '',
      smtpPassword: '',
      fromName: 'Netdatel Admin',
      fromEmail: 'admin@netdatel.com',
      enableTLS: true,
    },
    system: {
      enableAuditLog: true,
      enableDebugMode: false,
      maxFileUploadSize: 50,
      allowedFileTypes: 'pdf,doc,docx,xls,xlsx,png,jpg,jpeg',
      enableMaintenanceMode: false,
      maintenanceMessage: 'Sistema en mantenimiento. Intente más tarde.',
    }
  });

  const tabs = [
    { id: 'general', label: 'General', icon: Settings },
    { id: 'security', label: 'Seguridad', icon: Shield },
    { id: 'email', label: 'Email', icon: Mail },
    { id: 'system', label: 'Sistema', icon: Server },
  ];

  const showNotification = (message, type = 'success') => {
    setNotification({ message, type });
    setTimeout(() => setNotification(null), 3000);
  };

  const handleSettingChange = (category, key, value) => {
    setSettings(prev => ({
      ...prev,
      [category]: {
        ...prev[category],
        [key]: value
      }
    }));
  };

  const handleSave = async (category) => {
    try {
      setSaving(true);
      // Aquí harías la llamada a la API para guardar las configuraciones
      // await api.post(`/settings/${category}`, settings[category]);
      
      // Simulamos una llamada exitosa
      await new Promise(resolve => setTimeout(resolve, 1000));
      
      showNotification(`Configuración de ${tabs.find(t => t.id === category)?.label} guardada exitosamente`);
    } catch (error) {
      console.error('Error saving settings:', error);
      showNotification('Error al guardar la configuración', 'error');
    } finally {
      setSaving(false);
    }
  };

  const togglePasswordVisibility = (field) => {
    setShowPasswords(prev => ({
      ...prev,
      [field]: !prev[field]
    }));
  };

  const SettingField = ({ label, type = 'text', value, onChange, description, options, min, max, step, placeholder }) => (
    <div className="space-y-2">
      <label className="block text-sm font-medium text-gray-700">
        {label}
      </label>
      
      {type === 'select' ? (
        <select
          value={value}
          onChange={(e) => onChange(e.target.value)}
          className="input"
        >
          {options.map(option => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      ) : type === 'checkbox' ? (
        <label className="flex items-center space-x-2">
          <input
            type="checkbox"
            checked={value}
            onChange={(e) => onChange(e.target.checked)}
            className="text-primary-600 focus:ring-primary-500 rounded"
          />
          <span className="text-sm text-gray-700">{description}</span>
        </label>
      ) : type === 'password' ? (
        <div className="relative">
          <input
            type={showPasswords[label] ? 'text' : 'password'}
            value={value}
            onChange={(e) => onChange(e.target.value)}
            placeholder={placeholder}
            className="input pr-10"
          />
          <button
            type="button"
            onClick={() => togglePasswordVisibility(label)}
            className="absolute inset-y-0 right-0 pr-3 flex items-center text-gray-500 hover:text-gray-700"
          >
            {showPasswords[label] ? <EyeOff className="w-5 h-5" /> : <Eye className="w-5 h-5" />}
          </button>
        </div>
      ) : type === 'textarea' ? (
        <textarea
          value={value}
          onChange={(e) => onChange(e.target.value)}
          placeholder={placeholder}
          rows={3}
          className="input"
        />
      ) : (
        <input
          type={type}
          value={value}
          onChange={(e) => onChange(type === 'number' ? Number(e.target.value) : e.target.value)}
          placeholder={placeholder}
          min={min}
          max={max}
          step={step}
          className="input"
        />
      )}
      
      {description && type !== 'checkbox' && (
        <p className="text-sm text-gray-500">{description}</p>
      )}
    </div>
  );

  const GeneralSettings = () => (
    <div className="space-y-6">
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <SettingField
          label="Nombre de la Aplicación"
          value={settings.general.appName}
          onChange={(value) => handleSettingChange('general', 'appName', value)}
          description="Nombre que aparece en el header y título"
          placeholder="Netdatel Admin Panel"
        />
        
        <SettingField
          label="Descripción"
          value={settings.general.appDescription}
          onChange={(value) => handleSettingChange('general', 'appDescription', value)}
          description="Descripción que aparece en el login"
          placeholder="Sistema de Gestión de Identidades"
        />
        
        <SettingField
          label="Idioma por Defecto"
          type="select"
          value={settings.general.defaultLanguage}
          onChange={(value) => handleSettingChange('general', 'defaultLanguage', value)}
          options={[
            { value: 'es', label: 'Español' },
            { value: 'en', label: 'English' },
            { value: 'pt', label: 'Português' }
          ]}
          description="Idioma predeterminado para nuevos usuarios"
        />
        
        <SettingField
          label="Zona Horaria"
          type="select"
          value={settings.general.timezone}
          onChange={(value) => handleSettingChange('general', 'timezone', value)}
          options={[
            { value: 'America/Lima', label: 'Lima (UTC-5)' },
            { value: 'America/New_York', label: 'Nueva York (UTC-5)' },
            { value: 'Europe/Madrid', label: 'Madrid (UTC+1)' },
            { value: 'UTC', label: 'UTC' }
          ]}
          description="Zona horaria para timestamps y logs"
        />
        
        <SettingField
          label="Timeout de Sesión (minutos)"
          type="number"
          value={settings.general.sessionTimeout}
          onChange={(value) => handleSettingChange('general', 'sessionTimeout', value)}
          min="5"
          max="480"
          description="Tiempo antes de cerrar sesión automáticamente"
        />
      </div>
    </div>
  );

  const SecuritySettings = () => (
    <div className="space-y-6">
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <SettingField
          label="Longitud Mínima de Contraseña"
          type="number"
          value={settings.security.passwordMinLength}
          onChange={(value) => handleSettingChange('security', 'passwordMinLength', value)}
          min="6"
          max="32"
          description="Número mínimo de caracteres"
        />
        
        <SettingField
          label="Máximo Intentos de Login"
          type="number"
          value={settings.security.maxLoginAttempts}
          onChange={(value) => handleSettingChange('security', 'maxLoginAttempts', value)}
          min="3"
          max="10"
          description="Intentos antes de bloquear cuenta"
        />
        
        <SettingField
          label="Duración de Bloqueo (minutos)"
          type="number"
          value={settings.security.lockoutDuration}
          onChange={(value) => handleSettingChange('security', 'lockoutDuration', value)}
          min="5"
          max="1440"
          description="Tiempo de bloqueo tras intentos fallidos"
        />
        
        <SettingField
          label="Expiración JWT (minutos)"
          type="number"
          value={settings.security.jwtExpirationMinutes}
          onChange={(value) => handleSettingChange('security', 'jwtExpirationMinutes', value)}
          min="15"
          max="480"
          description="Tiempo de vida del token de acceso"
        />
      </div>
      
      <div className="space-y-4">
        <h4 className="text-md font-medium text-gray-900">Requisitos de Contraseña</h4>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <SettingField
            label="Requiere Mayúsculas"
            type="checkbox"
            value={settings.security.passwordRequireUppercase}
            onChange={(value) => handleSettingChange('security', 'passwordRequireUppercase', value)}
            description="Al menos una letra mayúscula"
          />
          
          <SettingField
            label="Requiere Números"
            type="checkbox"
            value={settings.security.passwordRequireNumbers}
            onChange={(value) => handleSettingChange('security', 'passwordRequireNumbers', value)}
            description="Al menos un número"
          />
          
          <SettingField
            label="Requiere Caracteres Especiales"
            type="checkbox"
            value={settings.security.passwordRequireSpecialChars}
            onChange={(value) => handleSettingChange('security', 'passwordRequireSpecialChars', value)}
            description="Al menos un símbolo (!@#$%^&*)"
          />
        </div>
      </div>
    </div>
  );

  const EmailSettings = () => (
    <div className="space-y-6">
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <SettingField
          label="Servidor SMTP"
          value={settings.email.smtpHost}
          onChange={(value) => handleSettingChange('email', 'smtpHost', value)}
          placeholder="smtp.mailersend.net"
          description="Servidor de correo saliente"
        />
        
        <SettingField
          label="Puerto SMTP"
          type="number"
          value={settings.email.smtpPort}
          onChange={(value) => handleSettingChange('email', 'smtpPort', value)}
          min="25"
          max="65535"
          description="Puerto del servidor SMTP"
        />
        
        <SettingField
          label="Usuario SMTP"
          value={settings.email.smtpUser}
          onChange={(value) => handleSettingChange('email', 'smtpUser', value)}
          placeholder="usuario@dominio.com"
          description="Usuario para autenticación"
        />
        
        <SettingField
          label="Contraseña SMTP"
          type="password"
          value={settings.email.smtpPassword}
          onChange={(value) => handleSettingChange('email', 'smtpPassword', value)}
          placeholder="••••••••"
          description="Contraseña del servidor SMTP"
        />
        
        <SettingField
          label="Nombre del Remitente"
          value={settings.email.fromName}
          onChange={(value) => handleSettingChange('email', 'fromName', value)}
          placeholder="Netdatel Admin"
          description="Nombre que aparece en los emails"
        />
        
        <SettingField
          label="Email del Remitente"
          type="email"
          value={settings.email.fromEmail}
          onChange={(value) => handleSettingChange('email', 'fromEmail', value)}
          placeholder="admin@netdatel.com"
          description="Dirección de email del remitente"
        />
      </div>
      
      <SettingField
        label="Habilitar TLS"
        type="checkbox"
        value={settings.email.enableTLS}
        onChange={(value) => handleSettingChange('email', 'enableTLS', value)}
        description="Usar conexión segura TLS/SSL"
      />
    </div>
  );

  const SystemSettings = () => (
    <div className="space-y-6">
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        <SettingField
          label="Tamaño Max. de Archivo (MB)"
          type="number"
          value={settings.system.maxFileUploadSize}
          onChange={(value) => handleSettingChange('system', 'maxFileUploadSize', value)}
          min="1"
          max="1000"
          description="Tamaño máximo para subida de archivos"
        />
        
        <SettingField
          label="Tipos de Archivo Permitidos"
          value={settings.system.allowedFileTypes}
          onChange={(value) => handleSettingChange('system', 'allowedFileTypes', value)}
          placeholder="pdf,doc,docx,xls,xlsx"
          description="Extensiones separadas por comas"
        />
      </div>
      
      <div className="space-y-4">
        <SettingField
          label="Habilitar Log de Auditoría"
          type="checkbox"
          value={settings.system.enableAuditLog}
          onChange={(value) => handleSettingChange('system', 'enableAuditLog', value)}
          description="Registrar todas las acciones del sistema"
        />
        
        <SettingField
          label="Modo de Depuración"
          type="checkbox"
          value={settings.system.enableDebugMode}
          onChange={(value) => handleSettingChange('system', 'enableDebugMode', value)}
          description="Mostrar información detallada de errores (solo desarrollo)"
        />
        
        <SettingField
          label="Modo de Mantenimiento"
          type="checkbox"
          value={settings.system.enableMaintenanceMode}
          onChange={(value) => handleSettingChange('system', 'enableMaintenanceMode', value)}
          description="Bloquear acceso al sistema para mantenimiento"
        />
        
        {settings.system.enableMaintenanceMode && (
          <SettingField
            label="Mensaje de Mantenimiento"
            type="textarea"
            value={settings.system.maintenanceMessage}
            onChange={(value) => handleSettingChange('system', 'maintenanceMessage', value)}
            placeholder="Sistema en mantenimiento..."
            description="Mensaje mostrado durante el mantenimiento"
          />
        )}
      </div>
    </div>
  );

  const renderTabContent = () => {
    switch (activeTab) {
      case 'general': return <GeneralSettings />;
      case 'security': return <SecuritySettings />;
      case 'email': return <EmailSettings />;
      case 'system': return <SystemSettings />;
      default: return <GeneralSettings />;
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
        <h1 className="text-2xl font-bold text-gray-900">Configuración del Sistema</h1>
        <p className="mt-1 text-sm text-gray-600">
          Administra la configuración global del sistema
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
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-xl font-semibold text-gray-900">
            {tabs.find(t => t.id === activeTab)?.label}
          </h2>
          <button
            onClick={() => handleSave(activeTab)}
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
        
        {renderTabContent()}
      </div>
    </div>
  );
};

export default SettingsPage;