// src/services/adminApi.js
import axios from 'axios';

// Configuración base de la API de Admin
const ADMIN_API_BASE_URL = import.meta.env.VITE_ADMIN_API_BASE_URL || 'https://localhost:8084/api';

// Crear instancia de axios para Admin API
const adminApiClient = axios.create({
  baseURL: ADMIN_API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 15000, // 15 segundos para operaciones más largas
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
    return response;
  },
  (error) => {
    // Si el token expira (401), redirigir al login
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('refreshToken');
      localStorage.removeItem('user');
      window.location.href = '/login';
    }
    
    // Log del error para debugging
    console.error('Admin API Error:', {
      status: error.response?.status,
      message: error.response?.data?.message || error.message,
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

// API de procesamiento RUC
export const rucApi = {
  processFile: (file) => {
    const formData = new FormData();
    formData.append('file', file);
    return adminApi.post('ruc/process', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  },
};

export default adminApiClient;