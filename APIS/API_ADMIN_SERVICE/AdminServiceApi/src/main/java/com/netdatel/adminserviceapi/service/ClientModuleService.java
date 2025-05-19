package com.netdatel.adminserviceapi.service;

import com.netdatel.adminserviceapi.dto.request.ClientModuleRequest;
import com.netdatel.adminserviceapi.dto.response.ClientModuleResponse;
import com.netdatel.adminserviceapi.entity.enums.ModuleStatus;

import java.util.List;

public interface ClientModuleService {
    /**
     * Asigna un módulo a un cliente
     * @param clientId ID del cliente
     * @param request Datos de asignación
     * @param userId ID del usuario que realiza la operación
     * @return Datos de la asignación creada
     */
    ClientModuleResponse assignModuleToClient(Integer clientId, ClientModuleRequest request, Integer userId);

    /**
     * Obtiene los módulos asignados a un cliente
     * @param clientId ID del cliente
     * @return Lista de módulos asignados
     */
    List<ClientModuleResponse> getClientModules(Integer clientId);

    /**
     * Actualiza la configuración de un módulo asignado
     * @param clientId ID del cliente
     * @param moduleId ID del módulo
     * @param request Nuevos datos de configuración
     * @param userId ID del usuario que realiza la operación
     * @return Datos actualizados de la asignación
     */
    ClientModuleResponse updateClientModule(Integer clientId, Integer moduleId, ClientModuleRequest request, Integer userId);

    /**
     * Cambia el estado de un módulo asignado
     * @param clientId ID del cliente
     * @param moduleId ID del módulo
     * @param status Nuevo estado
     * @param userId ID del usuario que realiza la operación
     */
    void changeModuleStatus(Integer clientId, Integer moduleId, ModuleStatus status, Integer userId);

    /**
     * Elimina un módulo asignado
     * @param clientId ID del cliente
     * @param moduleId ID del módulo
     * @param userId ID del usuario que realiza la operación
     */
    void removeClientModule(Integer clientId, Integer moduleId, Integer userId);

    /**
     * Actualiza los módulos expirados
     * @return Número de módulos actualizados
     */
    int updateExpiredModules();

    /**
     * Marca un módulo como expirado
     * @param moduleId ID del módulo asignado
     * @param userId ID del usuario que realiza la operación
     */
    void expireModule(Integer moduleId, Integer userId);
}
