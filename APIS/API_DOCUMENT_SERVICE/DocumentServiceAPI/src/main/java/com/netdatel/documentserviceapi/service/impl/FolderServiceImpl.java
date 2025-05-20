package com.netdatel.documentserviceapi.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netdatel.documentserviceapi.exception.InvalidRequestException;
import com.netdatel.documentserviceapi.exception.ResourceNotFoundException;
import com.netdatel.documentserviceapi.model.dto.request.FolderRequest;
import com.netdatel.documentserviceapi.model.entity.ClientSpace;
import com.netdatel.documentserviceapi.model.entity.File;
import com.netdatel.documentserviceapi.model.entity.Folder;
import com.netdatel.documentserviceapi.model.enums.FileStatus;
import com.netdatel.documentserviceapi.repository.ClientSpaceRepository;
import com.netdatel.documentserviceapi.repository.FileRepository;
import com.netdatel.documentserviceapi.repository.FolderRepository;
import com.netdatel.documentserviceapi.service.FolderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class FolderServiceImpl implements FolderService {
    private final FolderRepository folderRepository;
    private final ClientSpaceRepository clientSpaceRepository;
    private final FileRepository fileRepository;

    @Override
    public Folder createFolder(FolderRequest request, Integer userId) {
        log.info("Creating folder: {}", request.getName());

        // Verificar campos obligatorios
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            throw new InvalidRequestException("El nombre de la carpeta es obligatorio");
        }

        // Si es una subcarpeta, verificar que la carpeta padre existe
        Folder parent = null;
        if (request.getParentId() != null) {
            parent = folderRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Carpeta padre no encontrada"));
        }

        // Determinar el espacio del cliente
        ClientSpace clientSpace;
        if (parent != null) {
            clientSpace = parent.getClientSpace();

            // Verificar si ya existe una carpeta con el mismo nombre en el mismo nivel
            if (folderRepository.existsByClientSpaceIdAndParentIdAndName(
                    clientSpace.getId(), parent.getId(), request.getName())) {
                throw new InvalidRequestException("Ya existe una carpeta con ese nombre en este nivel");
            }
        } else {
            // Es una carpeta raíz, debe especificar el espacio del cliente
            if (request.getClientSpaceId() == null) {
                throw new InvalidRequestException("Para carpetas raíz debe especificar el ID del espacio del cliente");
            }

            clientSpace = clientSpaceRepository.findById(request.getClientSpaceId())
                    .orElseThrow(() -> new ResourceNotFoundException("Espacio de cliente no encontrado"));

            // Verificar si ya existe una carpeta raíz con el mismo nombre
            if (folderRepository.existsByClientSpaceIdAndParentIdAndName(
                    clientSpace.getId(), null, request.getName())) {
                throw new InvalidRequestException("Ya existe una carpeta raíz con ese nombre");
            }
        }

        // Determinar la ruta completa
        String path;
        if (parent != null) {
            path = parent.getPath() + "/" + request.getName();
        } else {
            path = "/" + request.getName();
        }

        // Convertir atributos a JSON si existen
        String attributesJson = null;
        if (request.getAttributes() != null && !request.getAttributes().isEmpty()) {
            try {
                attributesJson = new ObjectMapper().writeValueAsString(request.getAttributes());
            } catch (Exception e) {
                log.warn("Error converting attributes to JSON", e);
                attributesJson = "{}";
            }
        }

        // Crear y guardar la carpeta
        Folder folder = Folder.builder()
                .clientSpace(clientSpace)
                .name(request.getName())
                .description(request.getDescription())
                .parent(parent)
                .path(path)
                .createdBy(userId)
                .isActive(true)
                .attributes(attributesJson)
                .build();

        return folderRepository.save(folder);
    }

    @Override
    public Folder getFolder(Integer id) {
        return folderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Carpeta no encontrada"));
    }

    @Override
    public List<Folder> getRootFolders(Integer clientSpaceId) {
        if (!clientSpaceRepository.existsById(clientSpaceId)) {
            throw new ResourceNotFoundException("Espacio de cliente no encontrado");
        }

        return folderRepository.findByClientSpaceIdAndParentIsNull(clientSpaceId);
    }

    @Override
    public List<Folder> getSubfolders(Integer parentId) {
        if (!folderRepository.existsById(parentId)) {
            throw new ResourceNotFoundException("Carpeta padre no encontrada");
        }

        return folderRepository.findByParentId(parentId);
    }

    @Override
    public Folder updateFolder(Integer id, FolderRequest request, Integer userId) {
        log.info("Updating folder: {}", id);

        Folder folder = getFolder(id);

        // Actualizar nombre si se proporciona y es diferente
        if (request.getName() != null && !request.getName().isEmpty() && !request.getName().equals(folder.getName())) {
            // Verificar si el nuevo nombre ya existe en el mismo nivel
            if (folderRepository.existsByClientSpaceIdAndParentIdAndName(
                    folder.getClientSpace().getId(),
                    folder.getParent() != null ? folder.getParent().getId() : null,
                    request.getName())) {
                throw new InvalidRequestException("Ya existe una carpeta con ese nombre en este nivel");
            }

            // Actualizar nombre y ruta
            String oldPath = folder.getPath();
            String newPath = oldPath.substring(0, oldPath.lastIndexOf('/') + 1) + request.getName();

            folder.setName(request.getName());
            folder.setPath(newPath);

            // También actualizar rutas de subcarpetas
            updateSubfolderPaths(folder, oldPath, newPath);
        }

        // Actualizar descripción si se proporciona
        if (request.getDescription() != null) {
            folder.setDescription(request.getDescription());
        }

        // Actualizar atributos si se proporcionan
        if (request.getAttributes() != null) {
            try {
                String attributesJson = new ObjectMapper().writeValueAsString(request.getAttributes());
                folder.setAttributes(attributesJson);
            } catch (Exception e) {
                log.warn("Error converting attributes to JSON", e);
            }
        }

        folder.setUpdatedBy(userId);

        return folderRepository.save(folder);
    }

    @Override
    public void deleteFolder(Integer id) {
        log.info("Deleting folder: {}", id);

        Folder folder = getFolder(id);

        // Eliminar subcarpetas primero (recursivo)
        List<Folder> subfolders = folderRepository.findByParentId(id);
        for (Folder subfolder : subfolders) {
            deleteFolder(subfolder.getId());
        }

        // Eliminar archivos en la carpeta
        List<File> files = fileRepository.findByFolderId(id);
        for (File file : files) {
            file.setStatus(FileStatus.DELETED);
            fileRepository.save(file);
        }

        // Eliminar la carpeta
        folderRepository.delete(folder);
    }

    @Override
    public List<Folder> searchFolders(Integer clientSpaceId, String query) {
        return folderRepository.findByClientSpaceIdAndPathContaining(clientSpaceId, query);
    }

    // Helper methods

    private void updateSubfolderPaths(Folder folder, String oldBasePath, String newBasePath) {
        List<Folder> subfolders = folderRepository.findByParentId(folder.getId());

        for (Folder subfolder : subfolders) {
            String currentPath = subfolder.getPath();
            String newPath = currentPath.replace(oldBasePath, newBasePath);

            subfolder.setPath(newPath);
            folderRepository.save(subfolder);

            // Procesamiento recursivo para subcarpetas
            updateSubfolderPaths(subfolder, currentPath, newPath);
        }
    }
}