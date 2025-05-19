package com.netdatel.adminserviceapi.service.impl;

import com.netdatel.adminserviceapi.dto.event.ModuleAssignedEvent;
import com.netdatel.adminserviceapi.dto.external.StorageInitializeRequest;
import com.netdatel.adminserviceapi.dto.external.StorageInitializeResponse;
import com.netdatel.adminserviceapi.dto.external.StorageLimitUpdateRequest;
import com.netdatel.adminserviceapi.dto.request.ClientModuleRequest;
import com.netdatel.adminserviceapi.dto.request.NotificationRequest;
import com.netdatel.adminserviceapi.dto.response.ClientModuleResponse;
import com.netdatel.adminserviceapi.entity.Client;
import com.netdatel.adminserviceapi.entity.ClientModule;
import com.netdatel.adminserviceapi.entity.Module;
import com.netdatel.adminserviceapi.entity.enums.ModuleStatus;
import com.netdatel.adminserviceapi.entity.enums.TargetType;
import com.netdatel.adminserviceapi.exception.DuplicateResourceException;
import com.netdatel.adminserviceapi.exception.ExternalServiceException;
import com.netdatel.adminserviceapi.exception.ResourceNotFoundException;
import com.netdatel.adminserviceapi.mapper.ClientModuleMapper;
import com.netdatel.adminserviceapi.repository.ClientModuleRepository;
import com.netdatel.adminserviceapi.repository.ClientRepository;
import com.netdatel.adminserviceapi.repository.ModuleRepository;
import com.netdatel.adminserviceapi.service.ClientModuleService;
import com.netdatel.adminserviceapi.service.NotificationService;
import com.netdatel.adminserviceapi.service.integration.DocumentServiceClient;
import com.netdatel.adminserviceapi.service.integration.ProviderInitializeRequest;
import com.netdatel.adminserviceapi.service.integration.ProviderServiceClient;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientModuleServiceImpl implements ClientModuleService {

    private final ClientRepository clientRepository;
    private final ModuleRepository moduleRepository;
    private final ClientModuleRepository clientModuleRepository;
    private final ClientModuleMapper clientModuleMapper;
    private final DocumentServiceClient documentServiceClient;
    private final ProviderServiceClient providerServiceClient;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public ClientModuleResponse assignModuleToClient(Integer clientId, ClientModuleRequest request, Integer userId) {
        // Validar cliente
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", clientId));

        // Validar módulo
        Module module = moduleRepository.findById(request.getModuleId())
                .orElseThrow(() -> new ResourceNotFoundException("Módulo", "id", request.getModuleId()));

        if (!module.getIsActive()) {
            throw new IllegalArgumentException("El módulo solicitado no está activo");
        }

        // Verificar si ya existe la asignación
        if (clientModuleRepository.findByClientIdAndModuleId(clientId, request.getModuleId()).isPresent()) {
            throw new DuplicateResourceException("Asignación de módulo", "combinación cliente-módulo",
                    clientId + "-" + request.getModuleId());
        }

        // Crear la asignación
        ClientModule clientModule = clientModuleMapper.toEntity(request);
        clientModule.setClient(client);
        clientModule.setModule(module);
        clientModule.setCreatedBy(userId);
        clientModule.setStatus(ModuleStatus.ACTIVE);

        // Si no se especifica fecha de fin, es indefinida
        if (clientModule.getEndDate() == null) {
            log.info("Asignando módulo {} al cliente {} sin fecha de fin", module.getCode(), client.getCode());
        }

        ClientModule savedClientModule = clientModuleRepository.save(clientModule);

        // Inicializar servicios según el módulo
        try {
            if ("MOD1".equals(module.getCode()) || "MOD2".equals(module.getCode())) {
                // Módulo 1 o 2: Inicializar almacenamiento
                initializeStorage(client, module, savedClientModule);
            } else if ("MOD3".equals(module.getCode())) {
                // Módulo 3: Inicializar servicio de proveedores
                initializeProviderService(client, savedClientModule);
            }
        } catch (Exception e) {
            log.error("Error inicializando servicios para el módulo {}: {}", module.getCode(), e.getMessage(), e);
            // No revertimos la transacción, pero dejamos registro del error
        }

        // Enviar notificación
        sendModuleAssignmentNotification(client, module);

        // Publicar evento
        eventPublisher.publishEvent(new ModuleAssignedEvent(
                client.getId(),
                module.getId(),
                module.getCode(),
                clientModule.getStartDate(),
                clientModule.getEndDate(),
                request.getSpecificStorageLimit(),
                clientModule.getMaxUserAccounts(),
                LocalDateTime.now()
        ));

        return clientModuleMapper.toDto(savedClientModule);
    }

    @Override
    public List<ClientModuleResponse> getClientModules(Integer clientId) {
        // Validar cliente
        if (!clientRepository.existsById(clientId)) {
            throw new ResourceNotFoundException("Cliente", "id", clientId);
        }

        List<ClientModule> modules = clientModuleRepository.findByClientId(clientId);
        return clientModuleMapper.toDtoList(modules);
    }

    @Override
    @Transactional
    public ClientModuleResponse updateClientModule(Integer clientId, Integer moduleId, ClientModuleRequest request, Integer userId) {
        // Buscar la asignación existente
        ClientModule clientModule = clientModuleRepository.findByClientIdAndModuleId(clientId, moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Asignación de módulo", "clientId/moduleId",
                        clientId + "/" + moduleId));

        // Guardar valores originales para comparación
        Long originalStorageLimit = clientModule.getSpecificStorageLimit();

        // Actualizar datos
        clientModuleMapper.updateClientModuleFromDto(request, clientModule);
        clientModule.setLastUpdate(LocalDateTime.now());
        clientModule.setUpdatedBy(userId);

        ClientModule updatedClientModule = clientModuleRepository.save(clientModule);

        // Si cambió el límite de almacenamiento, actualizar en servicio externo
        if (originalStorageLimit != null && clientModule.getSpecificStorageLimit() != null &&
                !originalStorageLimit.equals(clientModule.getSpecificStorageLimit())) {

            Module module = clientModule.getModule();

            if ("MOD1".equals(module.getCode()) || "MOD2".equals(module.getCode())) {
                try {
                    updateStorageLimit(clientModule.getClient().getId(), module.getId(),
                            clientModule.getSpecificStorageLimit());
                } catch (Exception e) {
                    log.error("Error actualizando límite de almacenamiento: {}", e.getMessage(), e);
                    // No revertimos la transacción, pero dejamos registro del error
                }
            }
        }

        return clientModuleMapper.toDto(updatedClientModule);
    }

    @Override
    @Transactional
    public void changeModuleStatus(Integer clientId, Integer moduleId, ModuleStatus status, Integer userId) {
        // Buscar la asignación existente
        ClientModule clientModule = clientModuleRepository.findByClientIdAndModuleId(clientId, moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Asignación de módulo", "clientId/moduleId",
                        clientId + "/" + moduleId));

        // Si el estado es el mismo, no hacer nada
        if (clientModule.getStatus() == status) {
            return;
        }

        // Actualizar estado
        clientModule.setStatus(status);
        clientModule.setLastUpdate(LocalDateTime.now());
        clientModule.setUpdatedBy(userId);

        // Si se está suspendiendo o desactivando, registrar fecha
        if (status == ModuleStatus.INACTIVE || status == ModuleStatus.EXPIRED) {
            clientModule.setDeactivationDate(LocalDateTime.now());
        } else if (status == ModuleStatus.ACTIVE && clientModule.getDeactivationDate() != null) {
            // Si se está reactivando, limpiar fecha de desactivación
            clientModule.setActivationDate(LocalDateTime.now());
            clientModule.setDeactivationDate(null);
        }

        clientModuleRepository.save(clientModule);

        // Si se está desactivando el módulo 3, notificar al servicio de proveedores
        if ((status == ModuleStatus.INACTIVE || status == ModuleStatus.EXPIRED) &&
                "MOD3".equals(clientModule.getModule().getCode())) {
            try {
                providerServiceClient.disableClientInProviderService(clientId);
            } catch (Exception e) {
                log.error("Error desactivando cliente en servicio de proveedores: {}", e.getMessage(), e);
                // No revertimos la transacción, pero dejamos registro del error
            }
        }
    }

    @Override
    @Transactional
    public void removeClientModule(Integer clientId, Integer moduleId, Integer userId) {
        // Buscar la asignación existente
        ClientModule clientModule = clientModuleRepository.findByClientIdAndModuleId(clientId, moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Asignación de módulo", "clientId/moduleId",
                        clientId + "/" + moduleId));

        // Antes de eliminar, desactivar servicios externos
        try {
            Module module = clientModule.getModule();

            if ((module.getCode().equals("MOD1") || module.getCode().equals("MOD2")) &&
                    clientModule.getStatus() == ModuleStatus.ACTIVE) {
                // Para módulos 1 y 2, actualizar límite a 0
                updateStorageLimit(clientId, moduleId, 0L);
            } else if (module.getCode().equals("MOD3") &&
                    clientModule.getStatus() == ModuleStatus.ACTIVE) {
                // Para módulo 3, desactivar en servicio de proveedores
                providerServiceClient.disableClientInProviderService(clientId);
            }
        } catch (Exception e) {
            log.error("Error desactivando servicios externos: {}", e.getMessage(), e);
            // No revertimos la transacción, pero dejamos registro del error
        }

        // Eliminar la asignación
        clientModuleRepository.delete(clientModule);
    }

    @Override
    @Transactional
    public int updateExpiredModules() {
        List<ClientModule> expiredModules = clientModuleRepository.findExpiredModules(LocalDate.now());

        if (expiredModules.isEmpty()) {
            return 0;
        }

        for (ClientModule module : expiredModules) {
            module.setStatus(ModuleStatus.EXPIRED);
            module.setDeactivationDate(LocalDateTime.now());
            module.setLastUpdate(LocalDateTime.now());

            // Enviar notificación
            try {
                NotificationRequest notification = new NotificationRequest();
                notification.setClientId(module.getClient().getId());
                notification.setTargetType(TargetType.CLIENT);
                notification.setNotificationType("MODULE_EXPIRED");
                notification.setSubject("Expiración de módulo");
                notification.setContent("El módulo " + module.getModule().getName() +
                        " ha expirado el día " + module.getEndDate());

                notificationService.sendNotification(notification, (int) -1L); // Sistema
            } catch (Exception e) {
                log.error("Error enviando notificación de expiración: {}", e.getMessage(), e);
            }
        }

        clientModuleRepository.saveAll(expiredModules);

        return expiredModules.size();
    }

    @Override
    @Transactional
    public void expireModule(Integer moduleId, Integer userId) {
        ClientModule module = clientModuleRepository.findById(moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Módulo asignado", "id", moduleId));

        module.setStatus(ModuleStatus.EXPIRED);
        module.setDeactivationDate(LocalDateTime.now());
        module.setLastUpdate(LocalDateTime.now());
        module.setUpdatedBy(userId);

        clientModuleRepository.save(module);

        // Enviar notificación
        try {
            NotificationRequest notification = new NotificationRequest();
            notification.setClientId(module.getClient().getId());
            notification.setTargetType(TargetType.CLIENT);
            notification.setNotificationType("MODULE_EXPIRED");
            notification.setSubject("Expiración de módulo");
            notification.setContent("El módulo " + module.getModule().getName() + " ha expirado");

            notificationService.sendNotification(notification, userId);
        } catch (Exception e) {
            log.error("Error enviando notificación de expiración: {}", e.getMessage(), e);
        }
    }

    /**
     * Inicializa el almacenamiento para módulos 1 y 2
     */
    private void initializeStorage(Client client, Module module, ClientModule clientModule) {
        try {
            // Preparar solicitud
            StorageInitializeRequest request = new StorageInitializeRequest();
            request.setClientId(client.getId());
            request.setModuleId(module.getId());

            // Determinar límite de almacenamiento
            Long storageLimit = clientModule.getSpecificStorageLimit();
            if (storageLimit == null && client.getAllocatedStorage() != null) {
                storageLimit = client.getAllocatedStorage();
            }

            if (storageLimit == null || storageLimit <= 0) {
                storageLimit = 1_073_741_824L; // 1GB por defecto
            }

            request.setStorageLimitBytes(storageLimit);

            // Buscar email del administrador (si existe)
            String adminEmail = null;
            if (!client.getAdministrators().isEmpty()) {
                adminEmail = client.getAdministrators().get(0).getEmail();
            }
            request.setAdminEmail(adminEmail);

            // Enviar solicitud
            StorageInitializeResponse response = documentServiceClient.initializeStorage(request);

            if (response == null || !response.getSuccess()) {
                throw new ExternalServiceException("Document Service",
                        "Error inicializando almacenamiento: " +
                                (response != null ? response.getMessage() : "Sin respuesta"));
            }

            log.info("Almacenamiento inicializado para el cliente {} y módulo {}: {}",
                    client.getCode(), module.getCode(), response.getStoragePath());

        } catch (Exception e) {
            if (e instanceof ExternalServiceException) {
                throw e;
            }
            throw new ExternalServiceException("Document Service",
                    "Error inicializando almacenamiento: " + e.getMessage(), e);
        }
    }

    /**
     * Inicializa el servicio de proveedores para el módulo 3
     */
    private void initializeProviderService(Client client, ClientModule clientModule) {
        try {
            // Preparar solicitud
            ProviderInitializeRequest request = new ProviderInitializeRequest();
            request.setClientId(client.getId());
            request.setBusinessName(client.getBusinessName());

            // Determinar email del administrador (si existe)
            String adminEmail = null;
            if (!client.getAdministrators().isEmpty()) {
                adminEmail = client.getAdministrators().get(0).getEmail();
            }
            request.setAdminEmail(adminEmail);

            // Configurar límites
            int maxProviders = 50; // Valor por defecto
            int maxAuditors = 10;  // Valor por defecto

            // Si hay configuración específica, usarla
            if (clientModule.getConfiguration() != null && !clientModule.getConfiguration().isBlank()) {
                // La configuración es un JSON, pero por simplicidad dejamos los valores por defecto
                // En una implementación real, se extraerían del JSON
            }

            request.setMaxProviders(maxProviders);
            request.setMaxAuditors(maxAuditors);

            // Enviar solicitud
            providerServiceClient.initializeProviderService(request);

            log.info("Servicio de proveedores inicializado para el cliente {}", client.getCode());

        } catch (Exception e) {
            throw new ExternalServiceException("Provider Service",
                    "Error inicializando servicio de proveedores: " + e.getMessage(), e);
        }
    }

    /**
     * Actualiza el límite de almacenamiento para módulos 1 y 2
     */
    private void updateStorageLimit(Integer clientId, Integer moduleId, Long newLimit) {
        try {
            StorageLimitUpdateRequest request = new StorageLimitUpdateRequest();
            request.setClientId(clientId);
            request.setModuleId(moduleId);
            request.setNewLimitBytes(newLimit);

            documentServiceClient.updateStorageLimit(request);

            log.info("Límite de almacenamiento actualizado para cliente {} y módulo {}: {} bytes",
                    clientId, moduleId, newLimit);

        } catch (Exception e) {
            throw new ExternalServiceException("Document Service",
                    "Error actualizando limit de almacenamiento: " + e.getMessage(), e);
        }
    }

    /**
     * Envía notificación de asignación de módulo
     */
    private void sendModuleAssignmentNotification(Client client, Module module) {
        try {
            NotificationRequest notification = new NotificationRequest();
            notification.setClientId(client.getId());
            notification.setTargetType(TargetType.CLIENT);
            notification.setNotificationType("MODULE_ASSIGNED");
            notification.setSubject("Nuevo módulo asignado");
            notification.setContent("Se ha asignado el módulo \"" + module.getName() + "\" a su empresa. " +
                    "Ya puede acceder a las funcionalidades correspondientes.");

            notificationService.sendNotification(notification, (int) -1L); // Sistema
        } catch (Exception e) {
            log.error("Error enviando notificación de asignación de módulo: {}", e.getMessage(), e);
        }
    }
}