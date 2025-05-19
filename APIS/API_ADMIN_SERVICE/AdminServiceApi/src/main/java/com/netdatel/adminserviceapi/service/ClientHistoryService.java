package com.netdatel.adminserviceapi.service;

import com.netdatel.adminserviceapi.dto.response.ClientHistoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ClientHistoryService {
    /**
     * Obtiene el historial de cambios de un cliente
     * @param clientId ID del cliente
     * @return Lista de registros históricos
     */
    List<ClientHistoryResponse> getClientHistory(Integer clientId);

    /**
     * Obtiene el historial de cambios de un cliente de forma paginada
     * @param clientId ID del cliente
     * @param pageable Configuración de paginación
     * @return Lista paginada de registros históricos
     */
    Page<ClientHistoryResponse> getClientHistoryPaginated(Integer clientId, Pageable pageable);
}