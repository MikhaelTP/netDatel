import axios from 'axios';

// Configuración base de la API
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'https://localhost:8080/api';

// Crear instancia de axios
const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000, // 10 segundos
});

// Interceptor para agregar token a todas las requests
apiClient.interceptors.request.use(
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
apiClient.interceptors.response.use(
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
    console.error('API Error:', {
      status: error.response?.status,
      message: error.response?.data?.message || error.message,
      url: error.config?.url,
    });
    
    return Promise.reject(error);
  }
);

// Funciones de API genéricas
export const api = {
  // GET request
  get: (url, config = {}) => apiClient.get(url, config),
  
  // POST request
  post: (url, data, config = {}) => apiClient.post(url, data, config),
  
  // PUT request
  put: (url, data, config = {}) => apiClient.put(url, data, config),
  
  // DELETE request
  delete: (url, config = {}) => apiClient.delete(url, config),
  
  // PATCH request
  patch: (url, data, config = {}) => apiClient.patch(url, data, config),
};

// Funciones específicas de autenticación
export const authApi = {
  login: (credentials) => api.post('auth/login', credentials),
  refreshToken: (refreshToken) => api.post(`auth/refresh-token?refreshToken=${refreshToken}`),
  logout: (refreshToken) => api.post(`auth/logout?refreshToken=${refreshToken}`),
  validateToken: (token) => api.post(`auth/validate-token?token=${token}`),
};

// Funciones específicas de usuarios
export const usersApi = {
  getAll: (params = {}) => api.get('users', { params }),
  getById: (id) => api.get(`users/${id}`),
  getByType: (type) => api.get(`users/type/${type}`),
  create: (userData) => api.post('users', userData),
  autoRegister: (userData) => api.post('users/auto-register', userData),
  update: (id, userData) => api.put(`users/${id}`, userData),
  delete: (id) => api.delete(`users/${id}`),
  enable: (id) => api.put(`users/${id}/enable`),
  disable: (id) => api.put(`users/${id}/disable`),
  lock: (id) => api.put(`users/${id}/lock`),
  unlock: (id) => api.put(`users/${id}/unlock`),
  addRole: (userId, roleId) => api.post(`users/${userId}/roles/${roleId}`),
  removeRole: (userId, roleId) => api.delete(`users/${userId}/roles/${roleId}`),
};

// Funciones específicas de roles
export const rolesApi = {
  getAll: (params = {}) => api.get('roles', { params }),
  getById: (id) => api.get(`roles/${id}`),
  getByName: (name) => api.get(`roles/name/${name}`),
  getDefault: () => api.get('roles/default'),
  create: (roleData) => api.post('roles', roleData),
  update: (id, roleData) => api.put(`roles/${id}`, roleData),
  delete: (id) => api.delete(`roles/${id}`),
  addPermission: (roleId, permissionId) => api.post(`roles/${roleId}/permissions/${permissionId}`),
  removePermission: (roleId, permissionId) => api.delete(`roles/${roleId}/permissions/${permissionId}`),
  setAsDefault: (id) => api.put(`roles/${id}/default`),
};

// Funciones específicas de permisos
export const permissionsApi = {
  getAll: (params = {}) => api.get('permissions', { params }),
  getById: (id) => api.get(`permissions/${id}`),
  getByCode: (code) => api.get(`permissions/code/${code}`),
  getByCategory: (category) => api.get(`permissions/category/${category}`),
  getByService: (service) => api.get(`permissions/service/${service}`),
  create: (permissionData) => api.post('permissions', permissionData),
  update: (id, permissionData) => api.put(`permissions/${id}`, permissionData),
  delete: (id) => api.delete(`permissions/${id}`),
};

export default apiClient;