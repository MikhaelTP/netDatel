import React, { useState, useEffect } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { clientsApi, modulesApi, rucApi } from '../../services/adminApi';
import { 
  ArrowLeft, 
  Save, 
  Building2, 
  FileText, 
  Users, 
  Package,
  AlertCircle,
  Upload,
  Plus,
  Trash2,
  Mail,
  User,
  Calendar,
  MapPin,
  Phone,
  FileX,
  CheckCircle,
  UserCheck,
  Settings
} from 'lucide-react';

const ClientRegistrationForm = () => {
  const navigate = useNavigate();
  const { id } = useParams();
  const isEditing = Boolean(id);
  
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [availableModules, setAvailableModules] = useState([]);
  const [rucProcessing, setRucProcessing] = useState(false);
  const [errors, setErrors] = useState({});
  const [notification, setNotification] = useState(null);
  
  const [formData, setFormData] = useState({
    // Datos básicos
    ruc: '',
    businessName: '',
    commercialName: '',
    taxpayerType: '',
    activityStartDate: '',
    fiscalAddress: '',
    economicActivity: '',
    contactNumber: '',
    allocatedStorage: 1024, // MB por defecto
    notes: '',
    
    // Representantes legales
    legalRepresentatives: [{
      documentType: 'DNI',
      documentNumber: '',
      fullName: '',
      position: '',
      startDate: '',
      endDate: '',
      isActive: true
    }],
    
    // Módulos
    modules: [],
    
    // Administradores
    administrators: [{
      email: '',
      dni: '',
      sendNotification: true
    }],
    
    // Trabajadores
    workers: []
  });

  useEffect(() => {
    loadAvailableModules();
    if (isEditing) {
      loadClientData();
    }
  }, [id, isEditing]);

  const loadAvailableModules = async () => {
    try {
      const response = await modulesApi.getActive();
      setAvailableModules(response.data);
    } catch (error) {
      console.error('Error loading modules:', error);
    }
  };

  const loadClientData = async () => {
    try {
      setLoading(true);
      const response = await clientsApi.getById(id);
      const clientData = response.data;
      
      setFormData({
        ruc: clientData.ruc || '',
        businessName: clientData.businessName || '',
        commercialName: clientData.commercialName || '',
        taxpayerType: clientData.taxpayerType || '',
        activityStartDate: clientData.activityStartDate || '',
        fiscalAddress: clientData.fiscalAddress || '',
        economicActivity: clientData.economicActivity || '',
        contactNumber: clientData.contactNumber || '',
        allocatedStorage: clientData.allocatedStorage || 1024,
        notes: clientData.notes || '',
        legalRepresentatives: clientData.legalRepresentatives || [formData.legalRepresentatives[0]],
        modules: clientData.modules || [],
        administrators: clientData.administrators || [formData.administrators[0]],
        workers: clientData.workers || []
      });
    } catch (error) {
      console.error('Error loading client:', error);
      showNotification('Error al cargar los datos del cliente', 'error');
      navigate('/clients');
    } finally {
      setLoading(false);
    }
  };

  const showNotification = (message, type = 'success') => {
    setNotification({ message, type });
    setTimeout(() => setNotification(null), 5000);
  };

  const validateForm = () => {
    const newErrors = {};

    // Validaciones básicas
    if (!formData.ruc.trim()) {
      newErrors.ruc = 'El RUC es requerido';
    } else if (!/^\d{11}$/.test(formData.ruc)) {
      newErrors.ruc = 'El RUC debe tener 11 dígitos';
    }

    if (!formData.businessName.trim()) {
      newErrors.businessName = 'La razón social es requerida';
    }

    // Validar representantes legales
    formData.legalRepresentatives.forEach((rep, index) => {
      if (!rep.fullName.trim()) {
        newErrors[`representative_${index}_name`] = 'El nombre es requerido';
      }
      if (!rep.documentNumber.trim()) {
        newErrors[`representative_${index}_doc`] = 'El documento es requerido';
      }
    });

    // Validar administradores
    formData.administrators.forEach((admin, index) => {
      if (!admin.email.trim()) {
        newErrors[`admin_${index}_email`] = 'El email es requerido';
      } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(admin.email)) {
        newErrors[`admin_${index}_email`] = 'Email inválido';
      }
      if (!admin.dni.trim()) {
        newErrors[`admin_${index}_dni`] = 'El DNI es requerido';
      }
    });

    // Validar trabajadores
    formData.workers.forEach((worker, index) => {
      if (!worker.email.trim()) {
        newErrors[`worker_${index}_email`] = 'El email es requerido';
      } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(worker.email)) {
        newErrors[`worker_${index}_email`] = 'Email inválido';
      }
      if (!worker.dni.trim()) {
        newErrors[`worker_${index}_dni`] = 'El DNI es requerido';
      }
    });

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) {
      showNotification('Por favor corrige los errores en el formulario', 'error');
      return;
    }

    try {
      setSaving(true);
      
      if (isEditing) {
        await clientsApi.update(id, formData);
        showNotification('Cliente actualizado exitosamente');
      } else {
        await clientsApi.create(formData);
        showNotification('Cliente registrado exitosamente');
      }
      
      navigate('/clients');
    } catch (error) {
      console.error('Error saving client:', error);
      const errorMessage = error.response?.data?.message || 'Error al guardar el cliente';
      showNotification(errorMessage, 'error');
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
    
    // Limpiar error del campo
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: ''
      }));
    }
  };

  const handleRucFileUpload = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    if (file.type !== 'application/pdf') {
      showNotification('Solo se permiten archivos PDF', 'error');
      return;
    }

    try {
      setRucProcessing(true);
      const response = await rucApi.processFile(file);
      const extractedData = response.data;
      
      if (extractedData.ruc) {
        setFormData(prev => ({
          ...prev,
          ruc: extractedData.ruc,
          businessName: extractedData.businessName || prev.businessName,
          commercialName: extractedData.commercialName || prev.commercialName,
          taxpayerType: extractedData.taxpayerType || prev.taxpayerType,
          activityStartDate: extractedData.activityStartDate || prev.activityStartDate,
          fiscalAddress: extractedData.fiscalAddress || prev.fiscalAddress,
          economicActivity: extractedData.economicActivity || prev.economicActivity,
        }));
        
        showNotification('Datos extraídos exitosamente del archivo RUC');
      }
    } catch (error) {
      console.error('Error processing RUC file:', error);
      showNotification('Error al procesar el archivo RUC', 'error');
    } finally {
      setRucProcessing(false);
    }
  };

  const addLegalRepresentative = () => {
    setFormData(prev => ({
      ...prev,
      legalRepresentatives: [...prev.legalRepresentatives, {
        documentType: 'DNI',
        documentNumber: '',
        fullName: '',
        position: '',
        startDate: '',
        endDate: '',
        isActive: true
      }]
    }));
  };

  const removeLegalRepresentative = (index) => {
    setFormData(prev => ({
      ...prev,
      legalRepresentatives: prev.legalRepresentatives.filter((_, i) => i !== index)
    }));
  };

  const addAdministrator = () => {
    setFormData(prev => ({
      ...prev,
      administrators: [...prev.administrators, {
        email: '',
        dni: '',
        sendNotification: true
      }]
    }));
  };

  const removeAdministrator = (index) => {
    setFormData(prev => ({
      ...prev,
      administrators: prev.administrators.filter((_, i) => i !== index)
    }));
  };

  const addWorker = () => {
    setFormData(prev => ({
      ...prev,
      workers: [...prev.workers, {
        email: '',
        dni: '',
        sendNotification: false
      }]
    }));
  };

  const removeWorker = (index) => {
    setFormData(prev => ({
      ...prev,
      workers: prev.workers.filter((_, i) => i !== index)
    }));
  };

  const toggleModule = (moduleId) => {
    setFormData(prev => {
      const isSelected = prev.modules.some(m => m.moduleId === moduleId);
      
      if (isSelected) {
        return {
          ...prev,
          modules: prev.modules.filter(m => m.moduleId !== moduleId)
        };
      } else {
        const today = new Date().toISOString().split('T')[0];
        return {
          ...prev,
          modules: [...prev.modules, {
            moduleId,
            startDate: today,
            endDate: '',
            maxUserAccounts: 10,
            specificStorageLimit: null,
            configuration: ''
          }]
        };
      }
    });
  };

  const updateModuleConfig = (moduleId, field, value) => {
    setFormData(prev => ({
      ...prev,
      modules: prev.modules.map(m => 
        m.moduleId === moduleId 
          ? { ...m, [field]: value }
          : m
      )
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
      <div className="flex items-center space-x-4">
        <button
          onClick={() => navigate('/admin-dashboard')}
          className="p-2 text-gray-600 hover:text-gray-900 rounded-lg hover:bg-gray-100 transition-colors"
        >
          <ArrowLeft className="w-5 h-5" />
        </button>
        <div>
          <h1 className="text-2xl font-bold text-gray-900">
            {isEditing ? 'Editar Cliente' : 'Registrar Cliente'}
          </h1>
          <p className="text-sm text-gray-600">
            {isEditing ? 'Modifica la información del cliente' : 'Completa los datos para registrar un nuevo cliente'}
          </p>
        </div>
      </div>

      {/* Form */}
      <form onSubmit={handleSubmit} className="space-y-6">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
          {/* Main Information */}
          <div className="lg:col-span-2 space-y-6">
            {/* Datos Básicos */}
            <div className="card">
              <div className="flex items-center justify-between mb-6">
                <h3 className="text-lg font-medium text-gray-900 flex items-center">
                  <Building2 className="w-5 h-5 mr-2" />
                  Información Básica
                </h3>
                
                {/* Upload RUC File */}
                <div className="relative">
                  <input
                    type="file"
                    accept=".pdf"
                    onChange={handleRucFileUpload}
                    className="absolute inset-0 w-full h-full opacity-0 cursor-pointer"
                    disabled={rucProcessing}
                  />
                  <button
                    type="button"
                    className="btn-secondary flex items-center space-x-2"
                    disabled={rucProcessing}
                  >
                    {rucProcessing ? (
                      <>
                        <div className="w-4 h-4 border-2 border-gray-400 border-t-transparent rounded-full animate-spin"></div>
                        <span>Procesando...</span>
                      </>
                    ) : (
                      <>
                        <Upload className="w-4 h-4" />
                        <span>Subir Ficha RUC</span>
                      </>
                    )}
                  </button>
                </div>
              </div>
              
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label htmlFor="ruc" className="block text-sm font-medium text-gray-700 mb-2">
                    RUC *
                  </label>
                  <input
                    id="ruc"
                    name="ruc"
                    type="text"
                    value={formData.ruc}
                    onChange={handleChange}
                    className={`input ${errors.ruc ? 'border-red-300 focus:border-red-500 focus:ring-red-500' : ''}`}
                    placeholder="12345678901"
                    maxLength="11"
                  />
                  {errors.ruc && (
                    <p className="mt-1 text-sm text-red-600 flex items-center">
                      <AlertCircle className="w-4 h-4 mr-1" />
                      {errors.ruc}
                    </p>
                  )}
                </div>

                <div>
                  <label htmlFor="businessName" className="block text-sm font-medium text-gray-700 mb-2">
                    Razón Social *
                  </label>
                  <input
                    id="businessName"
                    name="businessName"
                    type="text"
                    value={formData.businessName}
                    onChange={handleChange}
                    className={`input ${errors.businessName ? 'border-red-300 focus:border-red-500 focus:ring-red-500' : ''}`}
                    placeholder="EMPRESA EJEMPLO S.A.C."
                  />
                  {errors.businessName && (
                    <p className="mt-1 text-sm text-red-600 flex items-center">
                      <AlertCircle className="w-4 h-4 mr-1" />
                      {errors.businessName}
                    </p>
                  )}
                </div>

                <div>
                  <label htmlFor="commercialName" className="block text-sm font-medium text-gray-700 mb-2">
                    Nombre Comercial
                  </label>
                  <input
                    id="commercialName"
                    name="commercialName"
                    type="text"
                    value={formData.commercialName}
                    onChange={handleChange}
                    className="input"
                    placeholder="Empresa Ejemplo"
                  />
                </div>

                <div>
                  <label htmlFor="taxpayerType" className="block text-sm font-medium text-gray-700 mb-2">
                    Tipo de Contribuyente
                  </label>
                  <select
                    id="taxpayerType"
                    name="taxpayerType"
                    value={formData.taxpayerType}
                    onChange={handleChange}
                    className="input"
                  >
                    <option value="">Seleccionar...</option>
                    <option value="PERSONA_JURIDICA">Persona Jurídica</option>
                    <option value="PERSONA_NATURAL">Persona Natural</option>
                    <option value="EMPRESA_UNIPERSONAL">Empresa Unipersonal</option>
                  </select>
                </div>

                <div>
                  <label htmlFor="activityStartDate" className="block text-sm font-medium text-gray-700 mb-2">
                    Fecha de Inicio de Actividades
                  </label>
                  <input
                    id="activityStartDate"
                    name="activityStartDate"
                    type="date"
                    value={formData.activityStartDate}
                    onChange={handleChange}
                    className="input"
                  />
                </div>

                <div>
                  <label htmlFor="contactNumber" className="block text-sm font-medium text-gray-700 mb-2">
                    Teléfono de Contacto
                  </label>
                  <input
                    id="contactNumber"
                    name="contactNumber"
                    type="tel"
                    value={formData.contactNumber}
                    onChange={handleChange}
                    className="input"
                    placeholder="+51 999 999 999"
                  />
                </div>

                <div>
                  <label htmlFor="allocatedStorage" className="block text-sm font-medium text-gray-700 mb-2">
                    Almacenamiento Asignado (MB)
                  </label>
                  <input
                    id="allocatedStorage"
                    name="allocatedStorage"
                    type="number"
                    value={formData.allocatedStorage}
                    onChange={handleChange}
                    className="input"
                    min="0"
                    placeholder="1024"
                  />
                </div>
              </div>

              <div className="mt-6">
                <label htmlFor="fiscalAddress" className="block text-sm font-medium text-gray-700 mb-2">
                  Dirección Fiscal
                </label>
                <input
                  id="fiscalAddress"
                  name="fiscalAddress"
                  type="text"
                  value={formData.fiscalAddress}
                  onChange={handleChange}
                  className="input"
                  placeholder="Av. Ejemplo 123, Lima, Perú"
                />
              </div>

              <div className="mt-6">
                <label htmlFor="economicActivity" className="block text-sm font-medium text-gray-700 mb-2">
                  Actividad Económica
                </label>
                <textarea
                  id="economicActivity"
                  name="economicActivity"
                  rows={3}
                  value={formData.economicActivity}
                  onChange={handleChange}
                  className="input"
                  placeholder="Descripción de la actividad económica principal..."
                />
              </div>

              <div className="mt-6">
                <label htmlFor="notes" className="block text-sm font-medium text-gray-700 mb-2">
                  Notas Adicionales
                </label>
                <textarea
                  id="notes"
                  name="notes"
                  rows={3}
                  value={formData.notes}
                  onChange={handleChange}
                  className="input"
                  placeholder="Observaciones, comentarios especiales..."
                />
              </div>
            </div>

            {/* Representantes Legales */}
            <div className="card">
              <div className="flex items-center justify-between mb-6">
                <h3 className="text-lg font-medium text-gray-900 flex items-center">
                  <User className="w-5 h-5 mr-2" />
                  Representantes Legales
                </h3>
                <button
                  type="button"
                  onClick={addLegalRepresentative}
                  className="btn-secondary flex items-center space-x-2"
                >
                  <Plus className="w-4 h-4" />
                  <span>Agregar</span>
                </button>
              </div>
              
              {formData.legalRepresentatives.map((rep, index) => (
                <div key={index} className="border border-gray-200 rounded-lg p-4 mb-4">
                  <div className="flex items-center justify-between mb-4">
                    <h4 className="text-sm font-medium text-gray-900">
                      Representante {index + 1}
                    </h4>
                    {formData.legalRepresentatives.length > 1 && (
                      <button
                        type="button"
                        onClick={() => removeLegalRepresentative(index)}
                        className="text-red-600 hover:text-red-800 transition-colors"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    )}
                  </div>
                  
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Tipo de Documento
                      </label>
                      <select
                        value={rep.documentType}
                        onChange={(e) => {
                          const newReps = [...formData.legalRepresentatives];
                          newReps[index].documentType = e.target.value;
                          setFormData(prev => ({ ...prev, legalRepresentatives: newReps }));
                        }}
                        className="input"
                      >
                        <option value="DNI">DNI</option>
                        <option value="CEX">Carné de Extranjería</option>
                        <option value="PASAPORTE">Pasaporte</option>
                      </select>
                    </div>
                    
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Número de Documento *
                      </label>
                      <input
                        type="text"
                        value={rep.documentNumber}
                        onChange={(e) => {
                          const newReps = [...formData.legalRepresentatives];
                          newReps[index].documentNumber = e.target.value;
                          setFormData(prev => ({ ...prev, legalRepresentatives: newReps }));
                        }}
                        className={`input ${errors[`representative_${index}_doc`] ? 'border-red-300' : ''}`}
                        placeholder="12345678"
                      />
                      {errors[`representative_${index}_doc`] && (
                        <p className="mt-1 text-sm text-red-600">{errors[`representative_${index}_doc`]}</p>
                      )}
                    </div>
                    
                    <div className="md:col-span-2">
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Nombre Completo *
                      </label>
                      <input
                        type="text"
                        value={rep.fullName}
                        onChange={(e) => {
                          const newReps = [...formData.legalRepresentatives];
                          newReps[index].fullName = e.target.value;
                          setFormData(prev => ({ ...prev, legalRepresentatives: newReps }));
                        }}
                        className={`input ${errors[`representative_${index}_name`] ? 'border-red-300' : ''}`}
                        placeholder="Juan Pérez Ramírez"
                      />
                      {errors[`representative_${index}_name`] && (
                        <p className="mt-1 text-sm text-red-600">{errors[`representative_${index}_name`]}</p>
                      )}
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Cargo
                      </label>
                      <input
                        type="text"
                        value={rep.position}
                        onChange={(e) => {
                          const newReps = [...formData.legalRepresentatives];
                          newReps[index].position = e.target.value;
                          setFormData(prev => ({ ...prev, legalRepresentatives: newReps }));
                        }}
                        className="input"
                        placeholder="Gerente General"
                      />
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Fecha de Inicio
                      </label>
                      <input
                        type="date"
                        value={rep.startDate}
                        onChange={(e) => {
                          const newReps = [...formData.legalRepresentatives];
                          newReps[index].startDate = e.target.value;
                          setFormData(prev => ({ ...prev, legalRepresentatives: newReps }));
                        }}
                        className="input"
                      />
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Fecha de Fin
                      </label>
                      <input
                        type="date"
                        value={rep.endDate}
                        onChange={(e) => {
                          const newReps = [...formData.legalRepresentatives];
                          newReps[index].endDate = e.target.value;
                          setFormData(prev => ({ ...prev, legalRepresentatives: newReps }));
                        }}
                        className="input"
                      />
                    </div>

                    <div className="flex items-center mt-2 space-x-2">
                      <input
                        type="checkbox"
                        checked={rep.isActive}
                        onChange={(e) => {
                          const newReps = [...formData.legalRepresentatives];
                          newReps[index].isActive = e.target.checked;
                          setFormData(prev => ({ ...prev, legalRepresentatives: newReps }));
                        }}
                        className="text-blue-600 focus:ring-blue-500 rounded"
                        id={`active_${index}`}
                      />
                      <label htmlFor={`active_${index}`} className="text-sm text-gray-700">
                        Está activo
                      </label>
                    </div>
                  </div>
                </div>
              ))}
            </div>

            {/* Administradores */}
            <div className="card">
              <div className="flex items-center justify-between mb-6">
                <h3 className="text-lg font-medium text-gray-900 flex items-center">
                  <UserCheck className="w-5 h-5 mr-2" />
                  Administradores
                </h3>
                <button
                  type="button"
                  onClick={addAdministrator}
                  className="btn-secondary flex items-center space-x-2"
                >
                  <Plus className="w-4 h-4" />
                  <span>Agregar</span>
                </button>
              </div>
              
              {formData.administrators.map((admin, index) => (
                <div key={index} className="border border-gray-200 rounded-lg p-4 mb-4">
                  <div className="flex items-center justify-between mb-4">
                    <h4 className="text-sm font-medium text-gray-900">
                      Administrador {index + 1}
                    </h4>
                    {formData.administrators.length > 1 && (
                      <button
                        type="button"
                        onClick={() => removeAdministrator(index)}
                        className="text-red-600 hover:text-red-800 transition-colors"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    )}
                  </div>
                  
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Email *
                      </label>
                      <input
                        type="email"
                        value={admin.email}
                        onChange={(e) => {
                          const newAdmins = [...formData.administrators];
                          newAdmins[index].email = e.target.value;
                          setFormData(prev => ({ ...prev, administrators: newAdmins }));
                        }}
                        className={`input ${errors[`admin_${index}_email`] ? 'border-red-300' : ''}`}
                        placeholder="admin@empresa.com"
                      />
                      {errors[`admin_${index}_email`] && (
                        <p className="mt-1 text-sm text-red-600">{errors[`admin_${index}_email`]}</p>
                      )}
                    </div>

                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        DNI *
                      </label>
                      <input
                        type="text"
                        value={admin.dni}
                        onChange={(e) => {
                          const newAdmins = [...formData.administrators];
                          newAdmins[index].dni = e.target.value;
                          setFormData(prev => ({ ...prev, administrators: newAdmins }));
                        }}
                        className={`input ${errors[`admin_${index}_dni`] ? 'border-red-300' : ''}`}
                        placeholder="12345678"
                      />
                      {errors[`admin_${index}_dni`] && (
                        <p className="mt-1 text-sm text-red-600">{errors[`admin_${index}_dni`]}</p>
                      )}
                    </div>

                    <div className="md:col-span-2 flex items-center space-x-2">
                      <input
                        type="checkbox"
                        checked={admin.sendNotification}
                        onChange={(e) => {
                          const newAdmins = [...formData.administrators];
                          newAdmins[index].sendNotification = e.target.checked;
                          setFormData(prev => ({ ...prev, administrators: newAdmins }));
                        }}
                        className="text-blue-600 focus:ring-blue-500 rounded"
                        id={`notify_admin_${index}`}
                      />
                      <label htmlFor={`notify_admin_${index}`} className="text-sm text-gray-700">
                        Enviar notificación de registro por email
                      </label>
                    </div>
                  </div>
                </div>
              ))}
            </div>

            {/* Trabajadores */}
            <div className="card">
              <div className="flex items-center justify-between mb-6">
                <h3 className="text-lg font-medium text-gray-900 flex items-center">
                  <Users className="w-5 h-5 mr-2" />
                  Trabajadores
                </h3>
                <button
                  type="button"
                  onClick={addWorker}
                  className="btn-secondary flex items-center space-x-2"
                >
                  <Plus className="w-4 h-4" />
                  <span>Agregar</span>
                </button>
              </div>

              {formData.workers.length === 0 ? (
                <div className="text-center py-8 text-gray-500">
                  <Users className="w-12 h-12 mx-auto mb-3 text-gray-300" />
                  <p>No hay trabajadores registrados</p>
                  <p className="text-sm">Los trabajadores son opcionales y se pueden agregar más tarde</p>
                </div>
              ) : (
                formData.workers.map((worker, index) => (
                  <div key={index} className="border border-gray-200 rounded-lg p-4 mb-4">
                    <div className="flex items-center justify-between mb-4">
                      <h4 className="text-sm font-medium text-gray-900">
                        Trabajador {index + 1}
                      </h4>
                      <button
                        type="button"
                        onClick={() => removeWorker(index)}
                        className="text-red-600 hover:text-red-800 transition-colors"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </div>
                    
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                          Email *
                        </label>
                        <input
                          type="email"
                          value={worker.email}
                          onChange={(e) => {
                            const newWorkers = [...formData.workers];
                            newWorkers[index].email = e.target.value;
                            setFormData(prev => ({ ...prev, workers: newWorkers }));
                          }}
                          className={`input ${errors[`worker_${index}_email`] ? 'border-red-300' : ''}`}
                          placeholder="trabajador@empresa.com"
                        />
                        {errors[`worker_${index}_email`] && (
                          <p className="mt-1 text-sm text-red-600">{errors[`worker_${index}_email`]}</p>
                        )}
                      </div>

                      <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">
                          DNI *
                        </label>
                        <input
                          type="text"
                          value={worker.dni}
                          onChange={(e) => {
                            const newWorkers = [...formData.workers];
                            newWorkers[index].dni = e.target.value;
                            setFormData(prev => ({ ...prev, workers: newWorkers }));
                          }}
                          className={`input ${errors[`worker_${index}_dni`] ? 'border-red-300' : ''}`}
                          placeholder="12345678"
                        />
                        {errors[`worker_${index}_dni`] && (
                          <p className="mt-1 text-sm text-red-600">{errors[`worker_${index}_dni`]}</p>
                        )}
                      </div>

                      <div className="md:col-span-2 flex items-center space-x-2">
                        <input
                          type="checkbox"
                          checked={worker.sendNotification}
                          onChange={(e) => {
                            const newWorkers = [...formData.workers];
                            newWorkers[index].sendNotification = e.target.checked;
                            setFormData(prev => ({ ...prev, workers: newWorkers }));
                          }}
                          className="text-blue-600 focus:ring-blue-500 rounded"
                          id={`notify_worker_${index}`}
                        />
                        <label htmlFor={`notify_worker_${index}`} className="text-sm text-gray-700">
                          Enviar notificación de registro por email
                        </label>
                      </div>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>

          {/* Sidebar */}
          <div className="space-y-6">
            {/* Módulos Disponibles */}
            <div className="card">
              <h3 className="text-lg font-medium text-gray-900 flex items-center mb-6">
                <Package className="w-5 h-5 mr-2" />
                Módulos de Servicio
              </h3>

              {availableModules.length === 0 ? (
                <div className="text-center py-8 text-gray-500">
                  <Package className="w-12 h-12 mx-auto mb-3 text-gray-300" />
                  <p>No hay módulos disponibles</p>
                </div>
              ) : (
                <div className="space-y-4">
                  {availableModules.map((module) => {
                    const isSelected = formData.modules.some(m => m.moduleId === module.id);
                    const selectedModule = formData.modules.find(m => m.moduleId === module.id);
                    
                    return (
                      <div key={module.id} className="border border-gray-200 rounded-lg p-4">
                        <div className="flex items-start space-x-3">
                          <input
                            type="checkbox"
                            checked={isSelected}
                            onChange={() => toggleModule(module.id)}
                            className="mt-1 text-blue-600 focus:ring-blue-500 rounded"
                            id={`module_${module.id}`}
                          />
                          <div className="flex-1">
                            <label 
                              htmlFor={`module_${module.id}`}
                              className="font-medium text-gray-900 cursor-pointer"
                            >
                              {module.name}
                            </label>
                            <p className="text-sm text-gray-600 mt-1">
                              {module.description}
                            </p>

                            {/* Configuración del módulo */}
                            {isSelected && (
                              <div className="mt-4 space-y-3 bg-gray-50 rounded-lg p-3">
                                <div className="grid grid-cols-2 gap-3">
                                  <div>
                                    <label className="block text-xs font-medium text-gray-700 mb-1">
                                      Fecha de Inicio
                                    </label>
                                    <input
                                      type="date"
                                      value={selectedModule?.startDate || ''}
                                      onChange={(e) => updateModuleConfig(module.id, 'startDate', e.target.value)}
                                      className="w-full px-2 py-1 text-sm border border-gray-300 rounded focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
                                    />
                                  </div>
                                  <div>
                                    <label className="block text-xs font-medium text-gray-700 mb-1">
                                      Fecha de Fin
                                    </label>
                                    <input
                                      type="date"
                                      value={selectedModule?.endDate || ''}
                                      onChange={(e) => updateModuleConfig(module.id, 'endDate', e.target.value)}
                                      className="w-full px-2 py-1 text-sm border border-gray-300 rounded focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
                                    />
                                  </div>
                                </div>
                                
                                <div>
                                  <label className="block text-xs font-medium text-gray-700 mb-1">
                                    Máximo de Usuarios
                                  </label>
                                  <input
                                    type="number"
                                    min="1"
                                    value={selectedModule?.maxUserAccounts || 10}
                                    onChange={(e) => updateModuleConfig(module.id, 'maxUserAccounts', parseInt(e.target.value))}
                                    className="w-full px-2 py-1 text-sm border border-gray-300 rounded focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
                                  />
                                </div>

                                <div>
                                  <label className="block text-xs font-medium text-gray-700 mb-1">
                                    Límite de Almacenamiento Específico (MB)
                                  </label>
                                  <input
                                    type="number"
                                    min="0"
                                    value={selectedModule?.specificStorageLimit || ''}
                                    onChange={(e) => updateModuleConfig(module.id, 'specificStorageLimit', e.target.value ? parseInt(e.target.value) : null)}
                                    className="w-full px-2 py-1 text-sm border border-gray-300 rounded focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
                                    placeholder="Opcional - usar límite general"
                                  />
                                </div>

                                <div>
                                  <label className="block text-xs font-medium text-gray-700 mb-1">
                                    Configuración Adicional
                                  </label>
                                  <textarea
                                    rows={2}
                                    value={selectedModule?.configuration || ''}
                                    onChange={(e) => updateModuleConfig(module.id, 'configuration', e.target.value)}
                                    className="w-full px-2 py-1 text-sm border border-gray-300 rounded focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
                                    placeholder="Configuración JSON o notas adicionales..."
                                  />
                                </div>
                              </div>
                            )}
                          </div>
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}
            </div>

            {/* Resumen */}
            <div className="card">
              <h3 className="text-lg font-medium text-gray-900 flex items-center mb-4">
                <FileText className="w-5 h-5 mr-2" />
                Resumen
              </h3>
              
              <div className="space-y-3 text-sm">
                <div className="flex justify-between">
                  <span className="text-gray-600">Representantes legales:</span>
                  <span className="font-medium">{formData.legalRepresentatives.length}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">Administradores:</span>
                  <span className="font-medium">{formData.administrators.length}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">Trabajadores:</span>
                  <span className="font-medium">{formData.workers.length}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">Módulos seleccionados:</span>
                  <span className="font-medium">{formData.modules.length}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">Almacenamiento:</span>
                  <span className="font-medium">{formData.allocatedStorage} MB</span>
                </div>
              </div>

              {formData.modules.length > 0 && (
                <div className="mt-4 pt-4 border-t border-gray-200">
                  <h4 className="text-sm font-medium text-gray-900 mb-2">Módulos:</h4>
                  <div className="space-y-2">
                    {formData.modules.map((module) => {
                      const moduleInfo = availableModules.find(m => m.id === module.moduleId);
                      return (
                        <div key={module.moduleId} className="flex items-center space-x-2">
                          <div className="w-2 h-2 bg-blue-500 rounded-full"></div>
                          <span className="text-sm text-gray-700">
                            {moduleInfo?.name || `Módulo ${module.moduleId}`}
                          </span>
                        </div>
                      );
                    })}
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>

        {/* Action Buttons */}
        <div className="flex items-center justify-end space-x-4 pt-6 border-t border-gray-200">
          <button
            type="button"
            onClick={() => navigate('/clients')}
            className="btn-secondary"
            disabled={saving}
          >
            Cancelar
          </button>
          <button
            type="submit"
            className="btn-primary flex items-center space-x-2"
            disabled={saving}
          >
            {saving ? (
              <>
                <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                <span>Guardando...</span>
              </>
            ) : (
              <>
                <Save className="w-4 h-4" />
                <span>{isEditing ? 'Actualizar Cliente' : 'Registrar Cliente'}</span>
              </>
            )}
          </button>
        </div>
      </form>
    </div>
  );
};

export default ClientRegistrationForm;