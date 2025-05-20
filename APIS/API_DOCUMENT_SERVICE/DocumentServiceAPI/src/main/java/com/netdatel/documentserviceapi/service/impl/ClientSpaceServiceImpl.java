package com.netdatel.documentserviceapi.service.impl;

import com.netdatel.documentserviceapi.exception.InvalidRequestException;
import com.netdatel.documentserviceapi.exception.ResourceNotFoundException;
import com.netdatel.documentserviceapi.model.dto.request.ClientSpaceRequest;
import com.netdatel.documentserviceapi.model.entity.ClientSpace;
import com.netdatel.documentserviceapi.repository.ClientSpaceRepository;
import com.netdatel.documentserviceapi.service.ClientSpaceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClientSpaceServiceImpl implements ClientSpaceService {
    private final ClientSpaceRepository clientSpaceRepository;

    @Override
    public ClientSpace createClientSpace(ClientSpaceRequest request, Integer userId) {
        log.info("Creating client space for client: {} and module: {}", request.getClientId(), request.getModuleId());

        if (clientSpaceRepository.existsByClientIdAndModuleId(request.getClientId(), request.getModuleId())) {
            throw new InvalidRequestException("Ya existe un espacio para este cliente y módulo");
        }

        ClientSpace clientSpace = ClientSpace.builder()
                .clientId(request.getClientId())
                .moduleId(request.getModuleId())
                .storagePath(generateStoragePath(request.getClientId(), request.getModuleId()))
                .totalQuotaBytes(request.getTotalQuotaBytes())
                .usedBytes(0L)
                .isActive(true)
                .createdBy(userId)
                .build();

        return clientSpaceRepository.save(clientSpace);
    }

    @Override
    public ClientSpace getClientSpace(Integer id) {
        return clientSpaceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Espacio de cliente no encontrado"));
    }

    @Override
    public ClientSpace getClientSpaceByClientAndModule(Integer clientId, Integer moduleId) {
        return clientSpaceRepository.findByClientIdAndModuleId(clientId, moduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Espacio de cliente no encontrado para el cliente y módulo especificados"));
    }

    @Override
    public List<ClientSpace> getClientSpacesByClient(Integer clientId) {
        return clientSpaceRepository.findByClientId(clientId);
    }

    @Override
    public ClientSpace updateClientSpace(Integer id, ClientSpaceRequest request, Integer userId) {
        log.info("Updating client space with id: {}", id);

        ClientSpace clientSpace = getClientSpace(id);

        // No permitir cambiar cliente o módulo, solo actualizar la cuota
        clientSpace.setTotalQuotaBytes(request.getTotalQuotaBytes());
        clientSpace.setUpdatedBy(userId);

        return clientSpaceRepository.save(clientSpace);
    }

    @Override
    public void deleteClientSpace(Integer id) {
        log.info("Deleting client space with id: {}", id);
        clientSpaceRepository.deleteById(id);
    }

    @Override
    public void updateUsedBytes(Integer id, Long usedBytes) {
        log.info("Updating used bytes for client space id: {} to {}", id, usedBytes);

        ClientSpace clientSpace = getClientSpace(id);
        clientSpace.setUsedBytes(usedBytes);

        clientSpaceRepository.save(clientSpace);
    }

    private String generateStoragePath(Integer clientId, Integer moduleId) {
        return String.format("clients/%d/module_%d", clientId, moduleId);
    }
}
