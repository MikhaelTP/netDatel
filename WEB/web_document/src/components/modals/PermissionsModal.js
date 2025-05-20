// src/components/Modals/PermissionsModal.js
import React from 'react';

const PermissionsModal = ({ 
  isOpen, 
  onClose, 
  onSave, 
  selectedResource,
  resourceType,
  users,
  selectedUser,
  setSelectedUser,
  permissions,
  setPermissions
}) => {
  if (!isOpen || !selectedResource) return null;
  
  return (
    <div className="fixed inset-0 overflow-y-auto bg-gray-500 bg-opacity-75 flex items-center justify-center">
      <div className="bg-white rounded-lg shadow-xl max-w-md w-full p-6">
        <h3 className="text-lg font-medium text-gray-900 mb-4">
          Administrar permisos - {resourceType === 'FOLDER' ? 'Carpeta' : 'Archivo'}: {selectedResource.name}
        </h3>
        
        <div className="mb-4">
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Seleccionar usuario
          </label>
          <select 
            className="w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
            value={selectedUser?.id || ''}
            onChange={(e) => {
              const selected = users.find(u => u.id === parseInt(e.target.value));
              setSelectedUser(selected);
            }}
          >
            <option value="">Seleccione un usuario</option>
            {users.map(user => (
              <option key={user.id} value={user.id}>
                {user.name} ({user.email})
              </option>
            ))}
          </select>
        </div>
        
        {selectedUser && (
          <div className="mb-4">
            <h4 className="text-sm font-medium text-gray-700 mb-2">Permisos para {selectedUser.name}</h4>
            
            <div className="space-y-2">
              <div className="flex items-center">
                <input 
                  type="checkbox" 
                  id="canRead"
                  checked={permissions.canRead}
                  onChange={(e) => setPermissions({...permissions, canRead: e.target.checked})}
                  className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                />
                <label htmlFor="canRead" className="ml-2 block text-sm text-gray-700">
                  Leer
                </label>
              </div>
              
              <div className="flex items-center">
                <input 
                  type="checkbox" 
                  id="canWrite"
                  checked={permissions.canWrite}
                  onChange={(e) => setPermissions({...permissions, canWrite: e.target.checked})}
                  className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                />
                <label htmlFor="canWrite" className="ml-2 block text-sm text-gray-700">
                  Escribir
                </label>
              </div>
              
              <div className="flex items-center">
                <input 
                  type="checkbox" 
                  id="canDelete"
                  checked={permissions.canDelete}
                  onChange={(e) => setPermissions({...permissions, canDelete: e.target.checked})}
                  className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                />
                <label htmlFor="canDelete" className="ml-2 block text-sm text-gray-700">
                  Eliminar
                </label>
              </div>
              
              <div className="flex items-center">
                <input 
                  type="checkbox" 
                  id="canDownload"
                  checked={permissions.canDownload}
                  onChange={(e) => setPermissions({...permissions, canDownload: e.target.checked})}
                  className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 rounded"
                />
                <label htmlFor="canDownload" className="ml-2 block text-sm text-gray-700">
                  Descargar
                </label>
              </div>
            </div>
          </div>
        )}
        
        <div className="flex justify-end space-x-2">
          <button 
            className="px-4 py-2 border border-gray-300 shadow-sm text-sm font-medium rounded-md text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
            onClick={onClose}
          >
            Cancelar
          </button>
          <button 
            className="px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
            onClick={onSave}
            disabled={!selectedUser}
          >
            Guardar
          </button>
        </div>
      </div>
    </div>
  );
};

export default PermissionsModal;