// src/services/api.js

// URL base de la API
const API_BASE_URL = 'https://localhost:8443/api';

// Función para realizar llamadas a la API
const apiCall = async (endpoint, method = 'GET', data = null) => {
  try {
    // Comentar o remover la parte de autenticación por ahora
    // const token = localStorage.getItem('token');
    
    const options = {
      method,
      headers: {
        'Content-Type': 'application/json',
        // 'Authorization': token ? `Bearer ${token}` : ''
      },
      body: data ? JSON.stringify(data) : undefined
    };
    
    const response = await fetch(`${API_BASE_URL}${endpoint}`, options);
    
    if (!response.ok) {
      throw new Error(`Error en la petición: ${response.statusText}`);
    }
    
    return await response.json();
  } catch (error) {
    console.error('API call error:', error);
    throw error;
  }
};

// Función para subir archivos (maneja FormData)
const uploadFile = async (endpoint, formData) => {
  try {
    // const token = localStorage.getItem('token');
    
    const options = {
      method: 'POST',
    //   headers: {
    //     'Authorization': token ? `Bearer ${token}` : ''
    //   },
      body: formData
    };
    
    const response = await fetch(`${API_BASE_URL}${endpoint}`, options);
    
    if (!response.ok) {
      throw new Error(`Error en la subida: ${response.statusText}`);
    }
    
    return await response.json();
  } catch (error) {
    console.error('Upload error:', error);
    throw error;
  }
};

// APIs específicas
const api = {
  // Espacios de Cliente
  getClientSpaces: () => apiCall('/client-spaces'),
  
  // Carpetas
  getRootFolders: (clientSpaceId) => apiCall(`/folders/client-space/${clientSpaceId}`),
  getSubfolders: (folderId) => apiCall(`/folders/parent/${folderId}`),
  createFolder: (data) => apiCall('/folders', 'POST', data),
  
  // Archivos
  getFolderFiles: (folderId) => apiCall(`/files/folder/${folderId}`),
  getFileDetails: (fileId) => apiCall(`/files/${fileId}`),
  uploadFileToFolder: (folderId, file, metadata) => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('folderId', folderId);
    if (metadata) {
      formData.append('metadata', JSON.stringify(metadata));
    }
    return uploadFile('/files', formData);
  },
  downloadFile: (fileId) => apiCall(`/files/${fileId}/download`),
  
  // Permisos
  getFolderPermissions: (folderId) => apiCall(`/permissions/folder/${folderId}`),
  getFilePermissions: (fileId) => apiCall(`/permissions/file/${fileId}`),
  assignFolderPermission: (folderId, permissionData) => apiCall(`/permissions/folder/${folderId}`, 'POST', permissionData),
  assignFilePermission: (fileId, permissionData) => apiCall(`/permissions/file/${fileId}`, 'POST', permissionData),
  
  // Descargas Masivas
  startBatchDownload: (data) => apiCall('/batch-downloads', 'POST', data)
};

export default api;