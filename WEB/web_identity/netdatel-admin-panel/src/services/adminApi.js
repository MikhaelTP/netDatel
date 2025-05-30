// src/services/adminApi.js
import axios from 'axios';

// Configuración base de la API de Admin
const ADMIN_API_BASE_URL = import.meta.env.VITE_ADMIN_API_BASE_URL || 'https://localhost:8084/api';
// URL del servicio de procesamiento RUC (nuevo)
const RUC_PROCESSOR_BASE_URL = import.meta.env.VITE_RUC_PROCESSOR_URL || 'http://localhost:5000/api';

// Crear instancia de axios para Admin API
const adminApiClient = axios.create({
  baseURL: ADMIN_API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 15000, // 15 segundos para operaciones más largas
});

// Crear instancia separada para el procesador RUC
const rucProcessorClient = axios.create({
  baseURL: RUC_PROCESSOR_BASE_URL,
  timeout: 60000, // 60 segundos para procesamiento de PDFs
});

// Interceptor para agregar token a todas las requests
adminApiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Interceptor para manejar respuestas y errores
adminApiClient.interceptors.response.use(
  (response) => {
    console.log('✅ Admin API Response:', {
      status: response.status,
      url: response.config.url,
      data: response.data
    });
    return response;
  },
  (error) => {
    console.error('❌ Admin API Error:', {
      status: error.response?.status,
      message: error.response?.data?.message || error.message,
      url: error.config?.url,
    });

    // Si el token expira (401), redirigir al login
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    
    return Promise.reject(error);
  }
);

// Interceptor para el procesador RUC (sin autenticación por ahora)
rucProcessorClient.interceptors.response.use(
  (response) => {
    console.log('✅ RUC Processor Response:', {
      status: response.status,
      url: response.config.url,
      success: response.data?.success
    });
    return response;
  },
  (error) => {
    console.error('❌ RUC Processor Error:', {
      status: error.response?.status,
      message: error.response?.data?.error || error.message,
      url: error.config?.url,
    });
    return Promise.reject(error);
  }
);

// Funciones de API genéricas
export const adminApi = {
  get: (url, config = {}) => adminApiClient.get(url, config),
  post: (url, data, config = {}) => adminApiClient.post(url, data, config),
  put: (url, data, config = {}) => adminApiClient.put(url, data, config),
  delete: (url, config = {}) => adminApiClient.delete(url, config),
  patch: (url, data, config = {}) => adminApiClient.patch(url, data, config),
};

// API de Clientes
export const clientsApi = {
  getAll: (params = {}) => adminApi.get('clients', { params }),
  getById: (id) => adminApi.get(`clients/${id}`),
  getByCode: (code) => adminApi.get(`clients/code/${code}`),
  search: (term) => adminApi.get(`clients/search?term=${encodeURIComponent(term)}`),
  create: (clientData) => adminApi.post('clients', clientData),
  update: (id, clientData) => adminApi.put(`clients/${id}`, clientData),
  delete: (id) => adminApi.delete(`clients/${id}`),
  changeStatus: (id, status) => adminApi.patch(`clients/${id}/status?status=${status}`),
  getHistory: (id) => adminApi.get(`clients/${id}/history`),
};

// API de Módulos
export const modulesApi = {
  getAll: () => adminApi.get('modules'),
  getById: (id) => adminApi.get(`modules/${id}`),
  getByCode: (code) => adminApi.get(`modules/code/${code}`),
  getActive: () => adminApi.get('modules/active'),
  create: (moduleData) => adminApi.post('modules', moduleData),
  update: (id, moduleData) => adminApi.put(`modules/${id}`, moduleData),
  toggleActive: (id, active) => adminApi.patch(`modules/${id}/active?active=${active}`),
};

// API de Cliente-Módulos
export const clientModulesApi = {
  getClientModules: (clientId) => adminApi.get(`clients/${clientId}/modules`),
  assignModule: (clientId, moduleData) => adminApi.post(`clients/${clientId}/modules`, moduleData),
  updateModule: (clientId, moduleId, moduleData) => adminApi.put(`clients/${clientId}/modules/${moduleId}`, moduleData),
  changeModuleStatus: (clientId, moduleId, status) => adminApi.patch(`clients/${clientId}/modules/${moduleId}/status?status=${status}`),
  removeModule: (clientId, moduleId) => adminApi.delete(`clients/${clientId}/modules/${moduleId}`),
};

// API de Dashboard
export const dashboardApi = {
  getClientSummary: () => adminApi.get('dashboard/clients/summary'),
  getModuleDistribution: () => adminApi.get('dashboard/modules/distribution'),
  getRecentClients: (limit = 5) => adminApi.get(`dashboard/clients/recent?limit=${limit}`),
};

// API de Trabajadores
export const workersApi = {
  getClientWorkers: (clientId) => adminApi.get(`clients/${clientId}/workers`),
  getWorkerById: (clientId, workerId) => adminApi.get(`clients/${clientId}/workers/${workerId}`),
  registerWorker: (clientId, workerData) => adminApi.post(`clients/${clientId}/workers`, workerData),
  registerWorkersBatch: (clientId, file) => {
    const formData = new FormData();
    formData.append('file', file);
    return adminApi.post(`clients/${clientId}/workers/batch`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  },
  deleteWorker: (clientId, workerId) => adminApi.delete(`clients/${clientId}/workers/${workerId}`),
  getBatchStatus: (batchId) => adminApi.get(`clients/workers/batch/${batchId}/status`),
};

// API de Notificaciones
export const notificationsApi = {
  getAll: (params = {}) => adminApi.get('notifications', { params }),
  getById: (id) => adminApi.get(`notifications/${id}`),
  create: (notificationData) => adminApi.post('notifications', notificationData),
  retry: (id) => adminApi.post(`notifications/${id}/retry`),
  retryFailed: () => adminApi.post('notifications/retry-failed'),
};

// API de procesamiento RUC (ACTUALIZADA CON SOPORTE MULTI-ARCHIVO)
export const rucApi = {
  // Procesar un único archivo RUC (mantiene compatibilidad)
  processFile: async (file) => {
    try {
      const formData = new FormData();
      formData.append('file', file);
      
      console.log('📄 Enviando archivo RUC para procesamiento:', file.name);
      
      const response = await rucProcessorClient.post('/ruc/process', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
        timeout: 60000, // 60 segundos para archivos grandes
      });
      
      if (response.data.success) {
        console.log('✅ Procesamiento exitoso:', response.data.data);
        return response;
      } else {
        throw new Error(response.data.error || 'Error procesando archivo RUC');
      }
    } catch (error) {
      console.error('❌ Error procesando RUC:', error);
      
      // Mejorar el mensaje de error
      if (error.code === 'NETWORK_ERROR' || error.message.includes('Network Error')) {
        throw new Error('No se pudo conectar al servicio de procesamiento RUC. Verifica que el servicio esté ejecutándose.');
      }
      
      if (error.response?.data?.error) {
        throw new Error(error.response.data.error);
      }
      
      throw new Error('Error procesando archivo RUC: ' + error.message);
    }
  },
  
  // NUEVO: Procesar múltiples archivos RUC
  processMultipleFiles: async (files) => {
    try {
      if (!files || files.length === 0) {
        throw new Error('No se proporcionaron archivos para procesar');
      }
      
      const formData = new FormData();
      
      // Agregar cada archivo al FormData con el nombre 'files' (importante!)
      Array.from(files).forEach(file => {
        formData.append('files', file);
      });
      
      console.log(`📄 Enviando ${files.length} archivos RUC para procesamiento en lote`);
      
      const response = await rucProcessorClient.post('/ruc/process-multiple', formData, {
        headers: {
          'Content-Type': 'multipart/form-data',
        },
        // Aumentar timeout para procesamiento de múltiples archivos
        timeout: 120000, // 2 minutos para lotes de archivos
      });
      
      if (response.data.success) {
        console.log(`✅ Procesamiento en lote exitoso: ${response.data.successful_files} de ${response.data.total_files} archivos procesados`);
        return response;
      } else {
        throw new Error(response.data.error || 'Error procesando archivos RUC');
      }
    } catch (error) {
      console.error('❌ Error procesando lote de RUCs:', error);
      
      if (error.code === 'NETWORK_ERROR' || error.message.includes('Network Error')) {
        throw new Error('No se pudo conectar al servicio de procesamiento RUC. Verifica que el servicio esté ejecutándose.');
      }
      
      if (error.response?.data?.error) {
        throw new Error(error.response.data.error);
      }
      
      throw new Error('Error procesando archivos RUC: ' + error.message);
    }
  },
  // Nuevo endpoint para verificar el estado del servicio
   healthCheck: async () => {
    try {
      const response = await rucProcessorClient.get('/ruc/health');
      return response.data;
    } catch (error) {
      throw new Error('Servicio de procesamiento RUC no disponible');
    }
  },
  
  // Obtener información del servicio
  getServiceInfo: async () => {
    try {
      const response = await rucProcessorClient.get('/ruc/status');
      return response.data;
    } catch (error) {
      throw new Error('No se pudo obtener información del servicio RUC');
    }
  }
};
export default adminApiClient;