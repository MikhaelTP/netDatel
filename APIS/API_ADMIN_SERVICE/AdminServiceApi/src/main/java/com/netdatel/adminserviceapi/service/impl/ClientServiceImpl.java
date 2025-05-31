package com.netdatel.adminserviceapi.service.impl;

import com.netdatel.adminserviceapi.api.IdentityServiceClient;
import com.netdatel.adminserviceapi.dto.event.ClientCreatedEvent;
import com.netdatel.adminserviceapi.dto.event.ClientStatusChangedEvent;
import com.netdatel.adminserviceapi.dto.request.ClientRequest;
import com.netdatel.adminserviceapi.dto.request.LegalRepresentativeRequest;
import com.netdatel.adminserviceapi.dto.request.NotificationRequest;
import com.netdatel.adminserviceapi.dto.response.ClientResponse;
import com.netdatel.adminserviceapi.dto.response.UserCredentialsResponse;
import com.netdatel.adminserviceapi.entity.Client;
import com.netdatel.adminserviceapi.entity.ClientAdministrator;
import com.netdatel.adminserviceapi.entity.ClientHistory;
import com.netdatel.adminserviceapi.entity.LegalRepresentative;
import com.netdatel.adminserviceapi.entity.enums.ClientStatus;
import com.netdatel.adminserviceapi.entity.enums.TargetType;
import com.netdatel.adminserviceapi.exception.DuplicateResourceException;
import com.netdatel.adminserviceapi.exception.ResourceNotFoundException;
import com.netdatel.adminserviceapi.mapper.ClientAdministratorMapper;
import com.netdatel.adminserviceapi.mapper.ClientMapper;
import com.netdatel.adminserviceapi.mapper.LegalRepresentativeMapper;
import com.netdatel.adminserviceapi.mapper.WorkersRegistrationMapper;
import com.netdatel.adminserviceapi.repository.*;
import com.netdatel.adminserviceapi.service.ClientModuleService;
import com.netdatel.adminserviceapi.service.ClientService;
import com.netdatel.adminserviceapi.service.NotificationService;
import com.netdatel.adminserviceapi.service.WorkersRegistrationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientServiceImpl implements ClientService {

    private final ClientRepository clientRepository;
    private final ClientHistoryRepository clientHistoryRepository;
    private final LegalRepresentativeRepository legalRepresentativeRepository;
    private final ClientAdministratorRepository clientAdministratorRepository;
    private final WorkersRegistrationRepository workersRegistrationRepository;

    private final ClientMapper clientMapper;
    private final LegalRepresentativeMapper legalRepresentativeMapper;
    private final ClientAdministratorMapper clientAdministratorMapper;
    private final WorkersRegistrationMapper workersRegistrationMapper;

    private final ClientModuleService clientModuleService;
    private final WorkersRegistrationService workersRegistrationService;
    private final NotificationService notificationService;

    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    private IdentityServiceClient identityServiceClient; // Cliente REST para IDENTITY

    @Override
    @Transactional
    public ClientResponse createClient(ClientRequest request, Integer userId) {
        // Verificar si ya existe un cliente con el mismo RUC
        if (clientRepository.existsByRuc(request.getRuc())) {
            throw new DuplicateResourceException("Cliente", "RUC", request.getRuc());
        }

        // Crear cliente a partir del request
        Client client = clientMapper.toEntity(request);

        System.out.println("🔍 DEBUG - userId recibido: " + userId);


        client.setCreatedBy(userId);

        // Generar código único de cliente
        String clientCode = generateClientCode();
        client.setCode(clientCode);

        // Guardar cliente
        Client savedClient = clientRepository.save(client);

        // Registrar historial
        ClientHistory history = new ClientHistory();
        history.setClientId(savedClient.getId());
        history.setAction("CREATED");
        history.setNewStatus(savedClient.getStatus().name());  // ✅ Enum directamente, no .name()
        history.setChangedBy(userId);
        history.setChangeDate(LocalDateTime.now());
        clientHistoryRepository.save(history);

        // Procesar representantes legales
        if (request.getLegalRepresentatives() != null && !request.getLegalRepresentatives().isEmpty()) {
            processLegalRepresentatives(savedClient, request.getLegalRepresentatives(), userId);
        }
        // Procesar Módulos
        if (request.getModules() != null && !request.getModules().isEmpty()) {
            try {
                log.info("Procesando {} módulos para cliente {}", request.getModules().size(), savedClient.getCode());
                request.getModules().forEach(moduleRequest -> {
                    try {
                        clientModuleService.assignModuleToClient(savedClient.getId(), moduleRequest, userId);
                        log.info("Módulo {} asignado correctamente al cliente {}", moduleRequest.getModuleId(), savedClient.getCode());
                    } catch (Exception e) {
                        log.error("Error asignando módulo {} al cliente {}: {}", moduleRequest.getModuleId(), savedClient.getCode(), e.getMessage(), e);
                        // No lanzamos excepción para no rollback todo, pero lo registramos
                    }
                });
            } catch (Exception e) {
                log.error("Error procesando módulos para cliente {}: {}", savedClient.getCode(), e.getMessage(), e);
            }
        }
        // Procesar administradores (se implementará en ClientAdministratorService)
        if (request.getAdministrators() != null && !request.getAdministrators().isEmpty()) {
            try {
                log.info("Procesando {} administradores para cliente {}", request.getAdministrators().size(), savedClient.getCode());
                request.getAdministrators().forEach(adminRequest -> {
                    try {
                        ClientAdministrator administrator = clientAdministratorMapper.toEntity(adminRequest);
                        administrator.setClient(savedClient);
                        administrator.setCreatedBy(userId);

                        ClientAdministrator savedAdmin = clientAdministratorRepository.save(administrator);
                        log.info("Administrador {} creado correctamente para cliente {}", savedAdmin.getEmail(), savedClient.getCode());

                        // ✅ Enviar notificación si sendNotification = true
                        if (adminRequest.getSendNotification() != null && adminRequest.getSendNotification()) {
                            sendAdministratorNotification(savedAdmin, savedClient, userId);
                        }
                    } catch (Exception e) {
                        log.error("Error creando administrador {} para cliente {}: {}", adminRequest.getEmail(), savedClient.getCode(), e.getMessage(), e);
                    }
                });
            } catch (Exception e) {
                log.error("Error procesando administradores para cliente {}: {}", savedClient.getCode(), e.getMessage(), e);
            }
        }
        // Procesar a los trabajadores
        if (request.getWorkers() != null && !request.getWorkers().isEmpty()) {
            try {
                log.info("Procesando {} trabajadores para cliente {}", request.getWorkers().size(), savedClient.getCode());
                request.getWorkers().forEach(workerRequest -> {
                    try {
                        workersRegistrationService.registerWorker(savedClient.getId(), workerRequest, userId);
                        log.info("Trabajador {} registrado correctamente para cliente {}", workerRequest.getEmail(), savedClient.getCode());
                    } catch (Exception e) {
                        log.error("Error registrando trabajador {} para cliente {}: {}", workerRequest.getEmail(), savedClient.getCode(), e.getMessage(), e);
                    }
                });
            } catch (Exception e) {
                log.error("Error procesando trabajadores para cliente {}: {}", savedClient.getCode(), e.getMessage(), e);
            }
        }

        // Publicar evento de cliente creado
        try {
            eventPublisher.publishEvent(new ClientCreatedEvent(
                    savedClient.getId(),
                    savedClient.getCode(),
                    savedClient.getBusinessName(),
                    savedClient.getAllocatedStorage(),
                    LocalDateTime.now()
            ));
            log.info("Evento ClientCreated publicado para cliente {}", savedClient.getCode());
        } catch (Exception e) {
            log.error("Error publicando evento para cliente {}: {}", savedClient.getCode(), e.getMessage(), e);
        }

        return clientMapper.toDto(savedClient);
    }

    @Override
    public ClientResponse getClientById(Integer id) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", id));

        return clientMapper.toDto(client);
    }

    @Override
    public ClientResponse getClientByCode(String code) {
        Client client = clientRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "código", code));

        return clientMapper.toDto(client);
    }

    @Override
    public Page<ClientResponse> getAllClients(Pageable pageable) {
        Page<Client> clientsPage = clientRepository.findAll(pageable);
        return clientsPage.map(clientMapper::toDto);
    }

    @Override
    public List<ClientResponse> searchClients(String term) {
        List<Client> clients = clientRepository.search(term);
        return clientMapper.toDtoList(clients);
    }

    @Override
    @Transactional
    public ClientResponse updateClient(Integer id, ClientRequest request, Integer userId) {
        Client existingClient = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", id));

        // Verificar si el RUC ha cambiado y si el nuevo RUC ya existe
        if (!existingClient.getRuc().equals(request.getRuc()) &&
                clientRepository.existsByRuc(request.getRuc())) {
            throw new DuplicateResourceException("Cliente", "RUC", request.getRuc());
        }

        // Guardar estado anterior para historial
        ClientStatus previousStatus = existingClient.getStatus();

        // Actualizar cliente con los nuevos datos
        clientMapper.updateClientFromDto(request, existingClient);
        existingClient.setLastUpdateDate(LocalDateTime.now());

        Client updatedClient = clientRepository.save(existingClient);

        // Registrar historial si hay cambio de estado
        if (previousStatus != updatedClient.getStatus()) {
            ClientHistory history = new ClientHistory();
            history.setClientId(updatedClient.getId());
            history.setAction("STATUS_CHANGED");
            history.setPreviousStatus(previousStatus.name());
            history.setNewStatus(updatedClient.getStatus().name());
            history.setChangedBy(userId);
            history.setChangeDate(LocalDateTime.now());
            clientHistoryRepository.save(history);

            // Publicar evento de cambio de estado
            eventPublisher.publishEvent(new ClientStatusChangedEvent(
                    updatedClient.getId(),
                    previousStatus.name(),
                    updatedClient.getStatus().name(),
                    LocalDateTime.now()
            ));
        }

        // Actualizar representantes legales
        if (request.getLegalRepresentatives() != null) {
            // Eliminar representantes actuales
            legalRepresentativeRepository.deleteByClientId(id);

            // Agregar nuevos representantes
            processLegalRepresentatives(updatedClient, request.getLegalRepresentatives(), userId);
        }

        return clientMapper.toDto(updatedClient);
    }

    @Override
    @Transactional
    public void changeClientStatus(Integer id, ClientStatus status, Integer userId) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", id));

        if (client.getStatus() == status) {
            return; // No hay cambio de estado
        }

        ClientStatus previousStatus = client.getStatus();
        client.setStatus(status);
        client.setLastUpdateDate(LocalDateTime.now());

        clientRepository.save(client);

        // Registrar historial
        ClientHistory history = new ClientHistory();
        history.setClientId(client.getId());
        history.setAction("STATUS_CHANGED");
        history.setPreviousStatus(previousStatus.name());
        history.setNewStatus(status.name());
        history.setChangedBy(userId);
        history.setChangeDate(LocalDateTime.now());
        clientHistoryRepository.save(history);

        // Publicar evento de cambio de estado
        eventPublisher.publishEvent(new ClientStatusChangedEvent(
                client.getId(),
                previousStatus.name(),
                status.name(),
                LocalDateTime.now()
        ));
    }

    @Override
    @Transactional
    public void deleteClient(Integer id, Integer userId) {
        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", id));

        // Registrar historial antes de eliminar
        ClientHistory history = new ClientHistory();
        history.setClientId(client.getId());
        history.setAction("DELETED");
        history.setPreviousStatus(client.getStatus().name());
        history.setChangedBy(userId);
        history.setChangeDate(LocalDateTime.now());
        clientHistoryRepository.save(history);

        // Eliminar cliente y sus dependencias
        clientRepository.delete(client);
    }

    /**
     * Genera un código único para el cliente.
     * Formato: CLI + año (2 dígitos) + mes (2 dígitos) + secuencia (4 dígitos)
     */
    private String generateClientCode() {
        String prefix = "CLI";
        String yearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyMM"));

        String latestCode = clientRepository.findLatestCodeByPrefix(prefix + yearMonth);
        int sequence = 1;

        if (latestCode != null) {
            try {
                sequence = Integer.parseInt(latestCode.substring(7)) + 1;
            } catch (Exception e) {
                log.warn("Error parsing sequence from latest code: {}", latestCode, e);
            }
        }

        return prefix + yearMonth + String.format("%04d", sequence);
    }

    /**
     * Procesa los representantes legales para un cliente.
     */
    private void processLegalRepresentatives(Client client, List<LegalRepresentativeRequest> representativesRequest, Integer userId) {
        for (LegalRepresentativeRequest request : representativesRequest) {
            LegalRepresentative representative = legalRepresentativeMapper.toEntity(request);
            representative.setClient(client);
            representative.setCreatedBy(userId);

            legalRepresentativeRepository.save(representative);
        }
    }


    private void sendAdministratorNotification(ClientAdministrator administrator, Client client, Integer userId) {
        try {
            // 1. Llamar al servicio IDENTITY para obtener las credenciales temporales
            UserCredentialsResponse credentials = identityServiceClient.getUserCredentials(administrator.getEmail());

            NotificationRequest notification = new NotificationRequest();
            notification.setClientId(client.getId());
            notification.setTargetType(TargetType.ADMINISTRATOR);
            notification.setTargetId(administrator.getId());
            notification.setNotificationType("ADMIN_REGISTRATION");
            notification.setSubject("Credenciales de acceso - " + client.getBusinessName());

            String content = "<h2>Bienvenido al sistema Netdatel</h2>" +
                    "<p>Estimado administrador,</p>" +
                    "<p>Ha sido registrado como administrador de <strong>" + client.getBusinessName() + "</strong>.</p>" +
                    "<p><strong>Sus credenciales temporales son:</strong></p>" +
                    "<div style='background-color: #f8f9fa; padding: 15px; border-radius: 5px; margin: 10px 0;'>" +
                    "<p><strong>Usuario:</strong> " + credentials.getUsername() + "</p>" +
                    "<p><strong>Contraseña temporal:</strong> " + credentials.getTemporaryPassword() + "</p>" +
                    "</div>" +
                    "<p><strong>IMPORTANTE:</strong> Por favor, cambie su contraseña en el primer inicio de sesión.</p>" +
                    "<p><a href='[LOGIN_LINK]' style='background-color: #007bff; color: white; padding: 10px 15px; text-decoration: none; border-radius: 5px;'>Iniciar Sesión</a></p>" +
                    "<br>" +
                    "<p><strong>Datos de la empresa:</strong></p>" +
                    "<ul>" +
                    "<li>RUC: " + client.getRuc() + "</li>" +
                    "<li>Razón Social: " + client.getBusinessName() + "</li>" +
                    "<li>Nombre Comercial: " + (client.getCommercialName() != null ? client.getCommercialName() : "N/A") + "</li>" +
                    "</ul>" +
                    "<br>" +
                    "<p>Saludos cordiales,<br>El equipo de Netdatel</p>";

            notification.setContent(content);
            notificationService.sendNotification(notification, userId);

            administrator.setNotificationSent(true);
            administrator.setNotificationDate(LocalDateTime.now());
            clientAdministratorRepository.save(administrator);

            log.info("Notificación con credenciales enviada a administrador: {}", administrator.getEmail());

        } catch (Exception e) {
            log.error("Error enviando notificación a administrador {}: {}", administrator.getEmail(), e.getMessage(), e);
        }
    }


}