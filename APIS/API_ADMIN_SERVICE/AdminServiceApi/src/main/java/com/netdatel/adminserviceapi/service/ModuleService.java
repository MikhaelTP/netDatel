package com.netdatel.adminserviceapi.service;

import com.netdatel.adminserviceapi.dto.request.ModuleRequest;
import com.netdatel.adminserviceapi.dto.response.ModuleResponse;

import java.util.List;

public interface ModuleService {
    /**
     * Crea un nuevo módulo
     * @param request Datos del módulo
     * @param userId ID del usuario que realiza la operación
     * @return Datos del módulo creado
     */
    ModuleResponse createModule(ModuleRequest request, Integer userId);

    /**
     * Obtiene un módulo por su ID
     * @param id ID del módulo
     * @return Datos del módulo
     */
    ModuleResponse getModuleById(Integer id);

    /**
     * Obtiene un módulo por su código
     * @param code Código único del módulo
     * @return Datos del módulo
     */
    ModuleResponse getModuleByCode(String code);

    /**
     * Obtiene todos los módulos
     * @return Lista de módulos
     */
    List<ModuleResponse> getAllModules();

    /**
     * Obtiene solo los módulos activos
     * @return Lista de módulos activos
     */
    List<ModuleResponse> getActiveModules();

    /**
     * Actualiza un módulo existente
     * @param id ID del módulo
     * @param request Nuevos datos del módulo
     * @param userId ID del usuario que realiza la operación
     * @return Datos actualizados del módulo
     */
    ModuleResponse updateModule(Integer id, ModuleRequest request, Integer userId);

    /**
     * Activa o desactiva un módulo
     * @param id ID del módulo
     * @param active Estado de activación
     * @param userId ID del usuario que realiza la operación
     */
    void toggleModuleActive(Integer id, boolean active, Integer userId);
}