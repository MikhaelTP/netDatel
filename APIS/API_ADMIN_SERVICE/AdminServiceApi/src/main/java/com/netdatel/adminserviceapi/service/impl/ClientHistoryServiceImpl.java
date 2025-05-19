package com.netdatel.adminserviceapi.service.impl;

import com.netdatel.adminserviceapi.dto.response.ClientHistoryResponse;
import com.netdatel.adminserviceapi.exception.ResourceNotFoundException;
import com.netdatel.adminserviceapi.mapper.ClientHistoryMapper;
import com.netdatel.adminserviceapi.repository.ClientHistoryRepository;
import com.netdatel.adminserviceapi.repository.ClientRepository;
import com.netdatel.adminserviceapi.service.ClientHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientHistoryServiceImpl implements ClientHistoryService {

    private final ClientHistoryRepository clientHistoryRepository;
    private final ClientRepository clientRepository;
    private final ClientHistoryMapper clientHistoryMapper;

    @Override
    public List<ClientHistoryResponse> getClientHistory(Integer clientId) {
        // Validar que el cliente existe
        if (!clientRepository.existsById(clientId)) {
            throw new ResourceNotFoundException("Cliente", "id", clientId);
        }

        return clientHistoryMapper.toDtoList(
                clientHistoryRepository.findByClientIdOrderByChangeDateDesc(clientId)
        );
    }

    @Override
    public Page<ClientHistoryResponse> getClientHistoryPaginated(Integer clientId, Pageable pageable) {
        // Validar que el cliente existe
        if (!clientRepository.existsById(clientId)) {
            throw new ResourceNotFoundException("Cliente", "id", clientId);
        }

        return clientHistoryRepository.findByClientIdOrderByChangeDateDesc(clientId, pageable)
                .map(clientHistoryMapper::toDto);
    }
}