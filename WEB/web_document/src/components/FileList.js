// src/components/FileList.js
import { Folder } from 'lucide-react';


import { File, Download, Users, MessageSquare, FileEdit, Trash2, Upload } from 'lucide-react';

const FileList = ({ 
  files, 
  selectedFolder, 
  onDownloadFile,
  onManagePermissions,
  onUploadFile 
}) => {
  // Función para formatear tamaño de archivo
  const formatFileSize = (bytes) => {
    if (bytes < 1024) return `${bytes} B`;
    if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(2)} KB`;
    if (bytes < 1024 * 1024 * 1024) return `${(bytes / (1024 * 1024)).toFixed(2)} MB`;
    return `${(bytes / (1024 * 1024 * 1024)).toFixed(2)} GB`;
  };

  // Función para renderizar el indicador de estado visual (bolita de color)
  const renderStatusIndicator = (file) => {
    const colorMap = {
      BLUE: 'bg-blue-500',
      AMBER: 'bg-amber-500',
      GREEN: 'bg-green-500',
      RED: 'bg-red-500'
    };
    
    const statusMap = {
      NEW: 'Nuevo',
      VIEWED: 'Visto',
      DOWNLOADED: 'Descargado',
      NOT_DOWNLOADED: 'No descargado'
    };
    
    return (
      <div className="flex items-center">
        <div 
          className={`h-3 w-3 rounded-full mr-2 ${colorMap[file.viewStatusColor]}`} 
          title={statusMap[file.viewStatus]}
        />
        <span className="text-xs text-gray-500">{statusMap[file.viewStatus]}</span>
      </div>
    );
  };

  return (
    <div>
      <h2 className="text-lg font-medium text-gray-700 mb-4">
        {selectedFolder ? `Contenido de: ${selectedFolder.name}` : 'Contenido de Carpeta Raíz'}
        {selectedFolder && (
          <span className="text-sm font-normal text-gray-500 ml-2">
            ({files.length} {files.length === 1 ? 'archivo' : 'archivos'})
          </span>
        )}
      </h2>
      
      {files.length > 0 ? (
        <div className="border rounded-lg overflow-hidden">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Nombre
                </th>
                <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Estado
                </th>
                <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Tamaño
                </th>
                <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Fecha
                </th>
                <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Acciones
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {files.map(file => (
                <tr key={file.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex items-center">
                      <File size={18} className="mr-2 text-gray-500" />
                      <div className="text-sm font-medium text-gray-900">{file.name}</div>
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    {renderStatusIndicator(file)}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {formatFileSize(file.fileSize)}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {new Date(file.uploadDate).toLocaleDateString()}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium">
                    <div className="flex space-x-2">
                      <button 
                        className="text-blue-600 hover:text-blue-900"
                        onClick={() => onDownloadFile(file)}
                        title="Descargar"
                      >
                        <Download size={16} />
                      </button>
                      <button 
                        className="text-gray-600 hover:text-gray-900"
                        onClick={() => onManagePermissions(file, 'FILE')}
                        title="Permisos"
                      >
                        <Users size={16} />
                      </button>
                      <button 
                        className="text-green-600 hover:text-green-900"
                        title="Comentarios"
                      >
                        <MessageSquare size={16} />
                      </button>
                      <button 
                        className="text-gray-600 hover:text-gray-900"
                        title="Editar"
                      >
                        <FileEdit size={16} />
                      </button>
                      <button 
                        className="text-red-600 hover:text-red-900"
                        title="Eliminar"
                      >
                        <Trash2 size={16} />
                      </button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        selectedFolder ? (
          <div className="text-center py-10 bg-gray-50 rounded-lg border border-gray-200">
            <File size={40} className="mx-auto text-gray-400 mb-2" />
            <p className="text-gray-500">Esta carpeta está vacía</p>
            <button 
              className="mt-2 inline-flex items-center px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
              onClick={onUploadFile}
            >
              <Upload size={16} className="mr-2" />
              Subir archivo
            </button>
          </div>
        ) : (
          <div className="text-center py-10 bg-gray-50 rounded-lg border border-gray-200">
            <Folder size={40} className="mx-auto text-gray-400 mb-2" />
            <p className="text-gray-500">Seleccione una carpeta para ver su contenido</p>
          </div>
        )
      )}
    </div>
  );
};

export default FileList;