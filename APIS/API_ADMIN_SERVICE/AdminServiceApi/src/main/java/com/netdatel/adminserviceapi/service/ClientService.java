package com.netdatel.adminserviceapi.service;

import com.netdatel.adminserviceapi.dto.request.ClientRequest;
import com.netdatel.adminserviceapi.dto.response.ClientResponse;
import com.netdatel.adminserviceapi.entity.enums.ClientStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ClientService {
    /**
     * Crea un nuevo cliente
     * @param request Datos del cliente a crear
     * @param userId ID del usuario que realiza la operación
     * @return Datos del cliente creado
     */
    ClientResponse createClient(ClientRequest request, Integer userId);

    /**
     * Obtiene un cliente por su ID
     * @param id ID del cliente
     * @return Datos del cliente
     */
    ClientResponse getClientById(Integer id);

    /**
     * Obtiene un cliente por su código
     * @param code Código único del cliente
     * @return Datos del cliente
     */
    ClientResponse getClientByCode(String code);

    /**
     * Obtiene una lista paginada de todos los clientes
     * @param pageable Configuración de paginación
     * @return Lista paginada de clientes
     */
    Page<ClientResponse> getAllClients(Pageable pageable);

    /**
     * Busca clientes por un término en sus campos
     * @param term Término de búsqueda
     * @return Lista de clientes que coinciden con el término
     */
    List<ClientResponse> searchClients(String term);

    /**
     * Actualiza un cliente existente
     * @param id ID del cliente a actualizar
     * @param request Nuevos datos del cliente
     * @param userId ID del usuario que realiza la operación
     * @return Datos actualizados del cliente
     */
    ClientResponse updateClient(Integer id, ClientRequest request, Integer userId);

    /**
     * Cambia el estado de un cliente
     * @param id ID del cliente
     * @param status Nuevo estado
     * @param userId ID del usuario que realiza la operación
     */
    void changeClientStatus(Integer id, ClientStatus status, Integer userId);

    /**
     * Elimina un cliente
     * @param id ID del cliente a eliminar
     * @param userId ID del usuario que realiza la operación
     */
    void deleteClient(Integer id, Integer userId);
}