// src/utils/helpers.js

// Formatear tamaño de archivo
export const formatFileSize = (bytes) => {
  if (bytes < 1024) return `${bytes} B`;
  if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(2)} KB`;
  if (bytes < 1024 * 1024 * 1024) return `${(bytes / (1024 * 1024)).toFixed(2)} MB`;
  return `${(bytes / (1024 * 1024 * 1024)).toFixed(2)} GB`;
};

// Mapear estado visual a nombres descriptivos
export const getStatusName = (viewStatus) => {
  const statusMap = {
    NEW: 'Nuevo',
    VIEWED: 'Visto',
    DOWNLOADED: 'Descargado',
    NOT_DOWNLOADED: 'No descargado'
  };
  return statusMap[viewStatus] || viewStatus;
};

// Mapear colores de estado visual
export const getStatusColorClass = (viewStatusColor) => {
  const colorMap = {
    BLUE: 'bg-blue-500',
    AMBER: 'bg-amber-500',
    GREEN: 'bg-green-500',
    RED: 'bg-red-500'
  };
  return colorMap[viewStatusColor] || 'bg-gray-500';
};

// Obtener extensión de archivo
export const getFileExtension = (filename) => {
  return filename.slice((filename.lastIndexOf(".") - 1 >>> 0) + 2);
};

// Obtener icono para tipo de archivo (puedes expandir según necesites)
export const getFileIconClass = (mimeType) => {
  if (mimeType.startsWith('image/')) return 'file-image';
  if (mimeType === 'application/pdf') return 'file-text';
  if (mimeType.includes('spreadsheet') || mimeType.includes('excel')) return 'file-spreadsheet';
  if (mimeType.includes('presentation') || mimeType.includes('powerpoint')) return 'file-presentation';
  if (mimeType.includes('document') || mimeType.includes('word')) return 'file-document';
  return 'file';
};