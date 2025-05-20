// src/components/FolderTree.js
import React from 'react';
import { Folder, ChevronRight, ChevronDown, Plus, Users } from 'lucide-react';

const FolderTree = ({ 
  folders, 
  selectedFolder, 
  expandedFolders, 
  onFolderSelect, 
  onFolderToggle, 
  onCreateFolder, 
  onManagePermissions 
}) => {
  return (
    <div className="mb-4">
      <div 
        className={`flex items-center p-2 cursor-pointer hover:bg-gray-100 rounded-md ${selectedFolder === null ? 'bg-blue-50' : ''}`}
        onClick={() => onFolderSelect(null)}
      >
        <Folder size={18} className="mr-2 text-blue-500" />
        <span>Carpeta Raíz</span>
      </div>
      
      {folders.map(folder => {
        // Calculamos el nivel de indentación basado en la ruta
        const level = folder.path.split('/').filter(Boolean).length;
        const indent = level > 1 ? (level - 1) * 16 : 0;
        
        return (
          <div 
            key={folder.id}
            className={`flex items-center p-2 cursor-pointer hover:bg-gray-100 rounded-md ${selectedFolder?.id === folder.id ? 'bg-blue-50' : ''}`}
            style={{ marginLeft: `${indent}px` }}
          >
            <button 
              className="mr-1 focus:outline-none"
              onClick={(e) => {
                e.stopPropagation();
                onFolderToggle(folder);
              }}
            >
              {expandedFolders[folder.id] ? 
                <ChevronDown size={16} /> : 
                <ChevronRight size={16} />
              }
            </button>
            
            <Folder size={18} className="mr-2 text-blue-500" />
            
            <span 
              className="flex-1 truncate"
              onClick={() => onFolderSelect(folder)}
              title={folder.name}
            >
              {folder.name}
            </span>
            
            <div className="flex space-x-1">
              <button 
                className="p-1 rounded-full hover:bg-gray-200"
                onClick={(e) => {
                  e.stopPropagation();
                  onCreateFolder(folder);
                }}
                title="Crear subcarpeta"
              >
                <Plus size={14} />
              </button>
              
              <button 
                className="p-1 rounded-full hover:bg-gray-200"
                onClick={(e) => {
                  e.stopPropagation();
                  onManagePermissions(folder, 'FOLDER');
                }}
                title="Administrar permisos"
              >
                <Users size={14} />
              </button>
            </div>
          </div>
        );
      })}
    </div>
  );
};

export default FolderTree;