// src/App.js
import React, { useState, useEffect } from 'react';
import { ChevronRight, Users, Upload, Download } from 'lucide-react';
import api from './services/api';
import { Plus } from 'lucide-react';

// Componentes
import FolderTree from './components/FolderTree';
import FileList from './components/FileList';
import CreateFolderModal from './components/modals/CreateFolderModal';
import UploadFileModal from './components/modals/UploadFileModal';
import PermissionsModal from './components/modals/PermissionsModal';
import Notifications from './components/Notificacions';

function App() {
  // Estado para el usuario actual (simulado para este ejemplo)
  const [currentUser, setCurrentUser] = useState({ id: 1, name: 'Admin' });
  
  // Estado para espacios de cliente
  const [clientSpaces, setClientSpaces] = useState([]);
  const [selectedClientSpace, setSelectedClientSpace] = useState(null);
  
  // Estado para carpetas y archivos
  const [folders, setFolders] = useState([]);
  const [files, setFiles] = useState([]);
  const [selectedFolder, setSelectedFolder] = useState(null);
  const [expandedFolders, setExpandedFolders] = useState({});
  
  // Estado para breadcrumb
  const [breadcrumb, setBreadcrumb] = useState([{ id: null, name: 'Raíz', path: '/' }]);
  
  // Estados para modales
  const [showCreateFolderModal, setShowCreateFolderModal] = useState(false);
  const [newFolderName, setNewFolderName] = useState('');
  
  const [showUploadModal, setShowUploadModal] = useState(false);
  const [uploadFile, setUploadFile] = useState(null);
  
  const [showPermissionsModal, setShowPermissionsModal] = useState(false);
  const [selectedResource, setSelectedResource] = useState(null);
  const [resourceType, setResourceType] = useState(null);
  
  // Estado para usuarios y permisos
  const [users, setUsers] = useState([
    { id: 1, name: 'Admin', email: 'admin@example.com' },
    { id: 2, name: 'Usuario 1', email: 'user1@example.com' },
    { id: 3, name: 'Usuario 2', email: 'user2@example.com' }
  ]);
  const [selectedUser, setSelectedUser] = useState(null);
  const [permissions, setPermissions] = useState({
    canRead: false,
    canWrite: false,
    canDelete: false,
    canDownload: false
  });
  
  // Estado para notificaciones
  const [notification, setNotification] = useState({ 
    show: false, 
    message: '', 
    type: '' 
  });

  // Cargar espacios de cliente al iniciar
  useEffect(() => {
    const loadClientSpaces = async () => {
      try {
        // Descomentar para usar la API real:
        const spaces = await api.getClientSpaces();
        
        // Datos simulados para desarrollo:
        // const spaces = [
        //   { id: 1, clientId: 101, moduleId: 1, storagePath: '/clients/101/module_1', totalQuotaBytes: 1073741824, usedBytes: 52428800, isActive: true },
        //   { id: 2, clientId: 102, moduleId: 2, storagePath: '/clients/102/module_2', totalQuotaBytes: 2147483648, usedBytes: 104857600, isActive: true }
        // ];
        
        setClientSpaces(spaces);
        if (spaces.length > 0) {
          setSelectedClientSpace(spaces[0]);
        }
      } catch (error) {
        showNotification('Error al cargar espacios de cliente: ' + error.message, 'error');
      }
    };
    
    loadClientSpaces();
  }, []);

  // Cargar carpetas raíz cuando se selecciona un espacio de cliente
  useEffect(() => {
    if (selectedClientSpace) {
      loadRootFolders();
    }
  }, [selectedClientSpace]);

  // Funciones para mostrar notificaciones
  const showNotification = (message, type = 'info') => {
    setNotification({ show: true, message, type });
    
    // Auto-ocultar después de 5 segundos
    setTimeout(() => {
      setNotification(prev => ({ ...prev, show: false }));
    }, 5000);
  };
  
  const closeNotification = () => {
    setNotification(prev => ({ ...prev, show: false }));
  };

  // Funciones para carpetas
  const loadRootFolders = async () => {
    try {
      // Descomentar para usar la API real:
      const rootFolders = await api.getRootFolders(selectedClientSpace.id);
      
      // Datos simulados para desarrollo:
      // const rootFolders = [
      //   { id: 1, name: 'Documentos', path: '/Documentos', parentId: null, isActive: true },
      //   { id: 2, name: 'Contratos', path: '/Contratos', parentId: null, isActive: true },
      //   { id: 3, name: 'Facturas', path: '/Facturas', parentId: null, isActive: true }
      // ];
      
      setFolders(rootFolders);
      setBreadcrumb([{ id: null, name: 'Raíz', path: '/' }]);
      setSelectedFolder(null);
      setFiles([]);
    } catch (error) {
      showNotification('Error al cargar carpetas: ' + error.message, 'error');
    }
  };
  
  const loadSubfolders = async (folderId) => {
    try {
      // Descomentar para usar la API real:
      const subfolders = await api.getSubfolders(folderId);
      
      // Datos simulados para desarrollo:
      // const subfolders = [
      //   { id: 11, name: 'Legales', path: '/Documentos/Legales', parentId: 1, isActive: true },
      //   { id: 12, name: 'Marketing', path: '/Documentos/Marketing', parentId: 1, isActive: true }
      // ];
      
      // Actualizamos el estado para mostrar las subcarpetas
      setFolders(prevFolders => {
        // Creamos una copia de las carpetas actuales
        const updatedFolders = [...prevFolders];
        // Encontramos la carpeta padre
        const parentIndex = updatedFolders.findIndex(f => f.id === folderId);
        if (parentIndex !== -1) {
          // Insertamos las subcarpetas después de la carpeta padre
          updatedFolders.splice(parentIndex + 1, 0, ...subfolders);
        }
        return updatedFolders;
      });
      
      // Marcamos la carpeta como expandida
      setExpandedFolders(prev => ({
        ...prev,
        [folderId]: true
      }));
    } catch (error) {
      showNotification('Error al cargar subcarpetas: ' + error.message, 'error');
    }
  };
  
  const toggleFolder = (folder) => {
    if (expandedFolders[folder.id]) {
      // Si ya está expandida, la contraemos y eliminamos las subcarpetas
      setExpandedFolders(prev => {
        const updated = {...prev};
        delete updated[folder.id];
        return updated;
      });
      
      // Filtramos las subcarpetas
      setFolders(prevFolders => prevFolders.filter(f => f.parentId !== folder.id));
    } else {
      // Si no está expandida, cargamos las subcarpetas
      loadSubfolders(folder.id);
    }
  };
  
  const selectFolder = (folder) => {
    setSelectedFolder(folder);
    if (folder) {
      loadFiles(folder.id);
    } else {
      setFiles([]);
      setBreadcrumb([{ id: null, name: 'Raíz', path: '/' }]);
    }
  };
  
  const createFolder = async () => {
    if (!newFolderName.trim()) {
      showNotification('El nombre de la carpeta no puede estar vacío', 'error');
      return;
    }
    
    try {
      const data = {
        name: newFolderName,
        clientSpaceId: selectedClientSpace.id,
        parentId: selectedFolder ? selectedFolder.id : null
      };
      
      // Descomentar para usar la API real:
      const createdFolder = await api.createFolder(data);
      
      // Simulación para desarrollo:
      // const createdFolder = { 
      //   ...data, 
      //   id: Math.floor(Math.random() * 1000),
      //   path: selectedFolder 
      //     ? `${selectedFolder.path}/${newFolderName}` 
      //     : `/${newFolderName}`,
      //   isActive: true
      // };
      
      // Actualizamos la lista de carpetas
      if (selectedFolder) {
        // Si tenemos una carpeta seleccionada, refrescamos sus subcarpetas
        if (expandedFolders[selectedFolder.id]) {
          loadSubfolders(selectedFolder.id);
        } else {
          toggleFolder(selectedFolder);
        }
      } else {
        // Si estamos en la raíz, refrescamos las carpetas raíz
        loadRootFolders();
      }
      
      setShowCreateFolderModal(false);
      setNewFolderName('');
      
      showNotification('Carpeta creada exitosamente', 'success');
    } catch (error) {
      showNotification('Error al crear carpeta: ' + error.message, 'error');
    }
  };

  // Funciones para archivos
  const loadFiles = async (folderId) => {
    try {
      // Descomentar para usar la API real:
      const folderFiles = await api.getFolderFiles(folderId);
      
      // Datos simulados para desarrollo:
      // const folderFiles = [
      //   { 
      //     id: 101, 
      //     name: 'informe.pdf', 
      //     originalName: 'informe_2025.pdf', 
      //     fileSize: 2048576, 
      //     mimeType: 'application/pdf', 
      //     status: 'ACTIVE', 
      //     viewStatus: 'NEW',
      //     viewStatusColor: 'BLUE',
      //     uploadDate: '2025-05-10T14:30:00',
      //     version: 1
      //   },
      //   { 
      //     id: 102, 
      //     name: 'presentacion.pptx', 
      //     originalName: 'presentacion_mayo.pptx', 
      //     fileSize: 3145728, 
      //     mimeType: 'application/vnd.openxmlformats-officedocument.presentationml.presentation', 
      //     status: 'ACTIVE', 
      //     viewStatus: 'VIEWED',
      //     viewStatusColor: 'AMBER',
      //     uploadDate: '2025-05-11T09:15:00',
      //     version: 1
      //   },
      //   { 
      //     id: 103, 
      //     name: 'datos.xlsx', 
      //     originalName: 'datos_financieros.xlsx', 
      //     fileSize: 1048576, 
      //     mimeType: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet', 
      //     status: 'ACTIVE', 
      //     viewStatus: 'DOWNLOADED',
      //     viewStatusColor: 'GREEN',
      //     uploadDate: '2025-05-12T11:45:00',
      //     version: 2
      //   }
      // ];
      
      setFiles(folderFiles);
      
      // Actualizamos el breadcrumb
      if (selectedFolder) {
        const path = selectedFolder.path.split('/').filter(Boolean);
        const breadcrumbItems = [{ id: null, name: 'Raíz', path: '/' }];
        let currentPath = '';
        
        path.forEach((segment, index) => {
          currentPath += `/${segment}`;
          breadcrumbItems.push({
            id: index === path.length - 1 ? selectedFolder.id : null,
            name: segment,
            path: currentPath
          });
        });
        
        setBreadcrumb(breadcrumbItems);
      }
    } catch (error) {
      showNotification('Error al cargar archivos: ' + error.message, 'error');
    }
  };
  
  const uploadFileToFolder = async () => {
    if (!uploadFile || !selectedFolder) {
      showNotification('Seleccione un archivo y una carpeta destino', 'error');
      return;
    }
    
    try {
      // Descomentar para usar la API real:
      await api.uploadFileToFolder(selectedFolder.id, uploadFile);
      
      // Simulación para desarrollo:
      // await new Promise(resolve => setTimeout(resolve, 1000));
      
      // Recargamos los archivos
      loadFiles(selectedFolder.id);
      
      setShowUploadModal(false);
      setUploadFile(null);
      
      showNotification('Archivo subido exitosamente', 'success');
    } catch (error) {
      showNotification('Error al subir archivo: ' + error.message, 'error');
    }
  };
  
  const downloadFile = async (file) => {
    try {
      // Descomentar para usar la API real:
      const response = await api.downloadFile(file.id);
      const link = document.createElement('a');
      link.href = response.downloadUrl;
      link.download = file.originalName;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      
      // Simulación para desarrollo:
      showNotification(`Iniciando descarga de ${file.name}...`, 'info');
    } catch (error) {
      showNotification('Error al descargar archivo: ' + error.message, 'error');
    }
  };
  
  const startBatchDownload = async () => {
    if (!selectedFolder) {
      showNotification('Seleccione una carpeta para iniciar la descarga masiva', 'error');
      return;
    }
    
    try {
      // Descomentar para usar la API real:
      await api.startBatchDownload({
        folderId: selectedFolder.id,
        includeSubfolders: true
      });
      
      // Simulación para desarrollo:
      showNotification('Descarga masiva iniciada. Recibirá una notificación cuando esté lista.', 'success');
    } catch (error) {
      showNotification('Error al iniciar descarga masiva: ' + error.message, 'error');
    }
  };

  // Funciones para permisos
  const openPermissionsModal = (resource, type) => {
    setSelectedResource(resource);
    setResourceType(type);
    setSelectedUser(null);
    setPermissions({
      canRead: false,
      canWrite: false,
      canDelete: false,
      canDownload: false
    });
    setShowPermissionsModal(true);
    
    // Cargar permisos actuales (en implementación real)
    // loadPermissions(resource.id, type);
  };
  
  const savePermissions = async () => {
    if (!selectedResource || !resourceType || !selectedUser) {
      showNotification('Seleccione un recurso, tipo y usuario', 'error');
      return;
    }
    
    try {
      const data = {
        userId: selectedUser.id,
        ...permissions
      };
      
      if (resourceType === 'FOLDER') {
        // Descomentar para usar la API real:
        await api.assignFolderPermission(selectedResource.id, data);
      } else {
        // Descomentar para usar la API real:
        await api.assignFilePermission(selectedResource.id, data);
      }
      
      // Simulación para desarrollo:
      await new Promise(resolve => setTimeout(resolve, 500));
      
      setShowPermissionsModal(false);
      showNotification('Permisos actualizados exitosamente', 'success');
    } catch (error) {
      showNotification('Error al guardar permisos: ' + error.message, 'error');
    }
  };

  // Renderizado del componente
  return (
    <div className="flex flex-col h-screen bg-gray-50">
      {/* Cabecera */}
      <header className="bg-white shadow-sm p-4 border-b">
        <div className="max-w-7xl mx-auto flex justify-between items-center">
          <h1 className="text-xl font-semibold text-gray-800">Sistema de Gestión Documental</h1>
          
          <div className="flex items-center space-x-4">
            {selectedClientSpace && (
              <div className="text-sm text-gray-600">
                <span className="font-medium">Cliente:</span> {selectedClientSpace.clientId} | 
                <span className="font-medium"> Módulo:</span> {selectedClientSpace.moduleId}
              </div>
            )}
            
            <div className="text-sm text-gray-600">
              <span className="font-medium">Usuario:</span> {currentUser.name}
            </div>
          </div>
        </div>
      </header>
      
      {/* Contenido principal */}
      <main className="flex-1 flex overflow-hidden">
        {/* Panel lateral - Árbol de carpetas */}
        <div className="w-1/4 bg-white border-r overflow-auto p-4">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-lg font-medium text-gray-700">Carpetas</h2>
            <button 
              onClick={() => setShowCreateFolderModal(true)}
              className="p-1 rounded-full hover:bg-gray-100"
              title="Crear carpeta raíz"
            >
              <Plus size={18} />
            </button>
          </div>
          
          {/* Selector de espacios */}
          {clientSpaces.length > 0 && (
            <div className="mb-4">
              <label className="block text-sm font-medium text-gray-700 mb-1">Espacio de Cliente</label>
              <select 
                className="w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm"
                value={selectedClientSpace?.id || ''}
                onChange={(e) => {
                  const selected = clientSpaces.find(cs => cs.id === parseInt(e.target.value));
                  setSelectedClientSpace(selected);
                }}
              >
                {clientSpaces.map(space => (
                  <option key={space.id} value={space.id}>
                    Cliente {space.clientId} - Módulo {space.moduleId}
                  </option>
                ))}
              </select>
            </div>
          )}
          
          {/* Árbol de carpetas */}
          <FolderTree 
            folders={folders}
            selectedFolder={selectedFolder}
            expandedFolders={expandedFolders}
            onFolderSelect={selectFolder}
            onFolderToggle={toggleFolder}
            onCreateFolder={(folder) => {
              setSelectedFolder(folder);
              setShowCreateFolderModal(true);
            }}
            onManagePermissions={openPermissionsModal}
          />
        </div>
        
        {/* Panel principal - Contenido de carpeta */}
        <div className="flex-1 overflow-auto">
          {/* Barra de herramientas */}
          <div className="bg-white p-4 border-b flex justify-between items-center">
            <div className="flex items-center space-x-2">
              {/* Breadcrumb */}
              <div className="flex items-center text-sm text-gray-600">
                {breadcrumb.map((item, index) => (
                  <React.Fragment key={index}>
                    {index > 0 && <ChevronRight size={16} className="mx-1 text-gray-400" />}
                    <span 
                      className="hover:text-blue-500 cursor-pointer"
                      onClick={() => {
                        if (item.id) {
                          const folder = folders.find(f => f.id === item.id);
                          if (folder) selectFolder(folder);
                        } else {
                          // Es la carpeta raíz
                          selectFolder(null);
                        }
                      }}
                    >
                      {item.name}
                    </span>
                  </React.Fragment>
                ))}
              </div>
            </div>
            
            <div className="flex items-center space-x-2">
              <button 
                className="px-3 py-1 bg-blue-50 text-blue-700 rounded-md hover:bg-blue-100 flex items-center text-sm"
                title="Administrar usuarios"
              >
                <Users size={16} className="mr-1" />
                <span>Usuarios</span>
              </button>
              
              <button 
                className="px-3 py-1 bg-blue-50 text-blue-700 rounded-md hover:bg-blue-100 flex items-center text-sm"
                onClick={() => setShowUploadModal(true)}
                disabled={!selectedFolder}
                title="Subir archivo"
              >
                <Upload size={16} className="mr-1" />
                <span>Subir</span>
              </button>
              
              <button 
                className="px-3 py-1 bg-blue-50 text-blue-700 rounded-md hover:bg-blue-100 flex items-center text-sm"
                onClick={startBatchDownload}
                disabled={!selectedFolder}
                title="Descargar carpeta completa"
              >
                <Download size={16} className="mr-1" />
                <span>Descarga Masiva</span>
              </button>
            </div>
          </div>
          
          {/* Lista de archivos */}
          <div className="p-4">
            <FileList 
              files={files}
              selectedFolder={selectedFolder}
              onDownloadFile={downloadFile}
              onManagePermissions={openPermissionsModal}
              onUploadFile={() => setShowUploadModal(true)}
            />
          </div>
        </div>
      </main>
      
      {/* Modales */}
      <CreateFolderModal 
        isOpen={showCreateFolderModal}
        onClose={() => {
          setShowCreateFolderModal(false);
          setNewFolderName('');
        }}
        onSave={createFolder}
        folderName={newFolderName}
        setFolderName={setNewFolderName}
        selectedFolder={selectedFolder}
      />
      
      <UploadFileModal 
        isOpen={showUploadModal}
        onClose={() => {
          setShowUploadModal(false);
          setUploadFile(null);
        }}
        onUpload={uploadFileToFolder}
        selectedFile={uploadFile}
        setSelectedFile={setUploadFile}
        selectedFolder={selectedFolder}
      />
      
      <PermissionsModal 
        isOpen={showPermissionsModal}
        onClose={() => {
          setShowPermissionsModal(false);
          setSelectedResource(null);
          setResourceType(null);
          setSelectedUser(null);
          setPermissions({
            canRead: false,
            canWrite: false,
            canDelete: false,
            canDownload: false
          });
        }}
        onSave={savePermissions}
        selectedResource={selectedResource}
        resourceType={resourceType}
        users={users}
        selectedUser={selectedUser}
        setSelectedUser={setSelectedUser}
        permissions={permissions}
        setPermissions={setPermissions}
      />
      
      {/* Notificaciones */}
      <Notifications 
        notification={notification}
        onClose={closeNotification}
      />
    </div>
  );
}

export default App;