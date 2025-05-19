package com.netdatel.adminserviceapi.service;

import com.netdatel.adminserviceapi.dto.request.WorkerRegistrationRequest;
import com.netdatel.adminserviceapi.dto.response.BatchProcessStatusResponse;
import com.netdatel.adminserviceapi.dto.response.BatchWorkersRegistrationResponse;
import com.netdatel.adminserviceapi.dto.response.WorkersRegistrationResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface WorkersRegistrationService {
    /**
     * Registra un trabajador para un cliente
     * @param clientId ID del cliente
     * @param request Datos del trabajador
     * @param userId ID del usuario que realiza la operación
     * @return Datos del trabajador registrado
     */
    WorkersRegistrationResponse registerWorker(Integer clientId, WorkerRegistrationRequest request, Integer userId);

    /**
     * Registra múltiples trabajadores mediante archivo Excel
     * @param clientId ID del cliente
     * @param file Archivo Excel con datos de trabajadores
     * @param userId ID del usuario que realiza la operación
     * @return Resumen del proceso de registro por lotes
     */
    BatchWorkersRegistrationResponse registerWorkersBatch(Integer clientId, MultipartFile file, Integer userId);

    /**
     * Obtiene todos los trabajadores de un cliente
     * @param clientId ID del cliente
     * @return Lista de trabajadores registrados
     */
    List<WorkersRegistrationResponse> getClientWorkers(Integer clientId);

    /**
     * Obtiene un trabajador específico
     * @param clientId ID del cliente
     * @param workerId ID del trabajador
     * @return Datos del trabajador
     */
    WorkersRegistrationResponse getWorkerById(Integer clientId, Integer workerId);

    /**
     * Elimina un trabajador
     * @param clientId ID del cliente
     * @param workerId ID del trabajador
     */
    void deleteWorker(Integer clientId, Integer workerId);

    /**
     * Obtiene el estado de un proceso batch
     * @param batchId ID del proceso batch
     * @return Estado actual del proceso
     */
    BatchProcessStatusResponse getBatchStatus(String batchId);
}
