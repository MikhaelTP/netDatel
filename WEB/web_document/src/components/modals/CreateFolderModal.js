// src/components/Modals/CreateFolderModal.js
import React from 'react';

const CreateFolderModal = ({ 
  isOpen, 
  onClose, 
  onSave, 
  folderName, 
  setFolderName, 
  selectedFolder 
}) => {
  if (!isOpen) return null;
  
  return (
    <div className="fixed inset-0 overflow-y-auto bg-gray-500 bg-opacity-75 flex items-center justify-center">
      <div className="bg-white rounded-lg shadow-xl max-w-md w-full p-6">
        <h3 className="text-lg font-medium text-gray-900 mb-4">
          {selectedFolder ? `Crear subcarpeta en ${selectedFolder.name}` : 'Crear carpeta raíz'}
        </h3>
        
        <div className="mb-4">
          <label className="block text-sm font-medium text-gray-700 mb-1">
            Nombre de la carpeta
          </label>
          <input 
            type="text"
            className="w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
            value={folderName}
            onChange={(e) => setFolderName(e.target.value)}
            placeholder="Ingrese el nombre de la carpeta"
            autoFocus
          />
        </div>
        
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
          >
            Crear
          </button>
        </div>
      </div>
    </div>
  );
};

export default CreateFolderModal;