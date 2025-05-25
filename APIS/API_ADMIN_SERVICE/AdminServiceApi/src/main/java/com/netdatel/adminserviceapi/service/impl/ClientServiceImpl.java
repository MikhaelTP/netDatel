package com.netdatel.adminserviceapi.service.impl;

import com.netdatel.adminserviceapi.dto.event.ClientCreatedEvent;
import com.netdatel.adminserviceapi.dto.event.ClientStatusChangedEvent;
import com.netdatel.adminserviceapi.dto.request.ClientRequest;
import com.netdatel.adminserviceapi.dto.request.LegalRepresentativeRequest;
import com.netdatel.adminserviceapi.dto.response.ClientResponse;
import com.netdatel.adminserviceapi.entity.Client;
import com.netdatel.adminserviceapi.entity.ClientHistory;
import com.netdatel.adminserviceapi.entity.LegalRepresentative;
import com.netdatel.adminserviceapi.entity.enums.ClientStatus;
import com.netdatel.adminserviceapi.exception.DuplicateResourceException;
import com.netdatel.adminserviceapi.exception.ResourceNotFoundException;
import com.netdatel.adminserviceapi.mapper.ClientMapper;
import com.netdatel.adminserviceapi.mapper.LegalRepresentativeMapper;
import com.netdatel.adminserviceapi.repository.*;
import com.netdatel.adminserviceapi.service.ClientService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final ClientMapper clientMapper;
    private final LegalRepresentativeMapper legalRepresentativeMapper;
    private final ApplicationEventPublisher eventPublisher;

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

        // Procesar módulos (se implementará en ClientModuleService)

        // Procesar administradores (se implementará en ClientAdministratorService)

        // Publicar evento de cliente creado
        eventPublisher.publishEvent(new ClientCreatedEvent(
                savedClient.getId(),
                savedClient.getCode(),
                savedClient.getBusinessName(),
                savedClient.getAllocatedStorage(),
                LocalDateTime.now()
        ));

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
}