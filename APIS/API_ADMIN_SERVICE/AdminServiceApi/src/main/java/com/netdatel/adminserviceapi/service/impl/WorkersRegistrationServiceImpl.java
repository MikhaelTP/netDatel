package com.netdatel.adminserviceapi.service.impl;

import com.netdatel.adminserviceapi.dto.batch.BatchError;
import com.netdatel.adminserviceapi.dto.batch.BatchProcessStatus;
import com.netdatel.adminserviceapi.dto.request.NotificationRequest;
import com.netdatel.adminserviceapi.dto.request.WorkerRegistrationRequest;
import com.netdatel.adminserviceapi.dto.response.BatchProcessStatusResponse;
import com.netdatel.adminserviceapi.dto.response.BatchWorkersRegistrationResponse;
import com.netdatel.adminserviceapi.dto.response.WorkersRegistrationResponse;
import com.netdatel.adminserviceapi.entity.Client;
import com.netdatel.adminserviceapi.entity.WorkersRegistration;
import com.netdatel.adminserviceapi.entity.enums.TargetType;
import com.netdatel.adminserviceapi.exception.DuplicateResourceException;
import com.netdatel.adminserviceapi.exception.ResourceNotFoundException;
import com.netdatel.adminserviceapi.exception.ValidationException;
import com.netdatel.adminserviceapi.mapper.BatchProcessMapper;
import com.netdatel.adminserviceapi.mapper.WorkersRegistrationMapper;
import com.netdatel.adminserviceapi.repository.ClientRepository;
import com.netdatel.adminserviceapi.repository.WorkersRegistrationRepository;
import com.netdatel.adminserviceapi.service.NotificationService;
import com.netdatel.adminserviceapi.service.WorkersRegistrationService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkersRegistrationServiceImpl implements WorkersRegistrationService {

    private final WorkersRegistrationRepository workersRegistrationRepository;
    private final ClientRepository clientRepository;
    private final WorkersRegistrationMapper workersRegistrationMapper;
    private final BatchProcessMapper batchProcessMapper;
    private final NotificationService notificationService;
    private final AsyncTaskExecutor taskExecutor;
    private final ConcurrentMap<String, BatchProcessStatus> batchProcessStatusMap = new ConcurrentHashMap<>();

    @Override
    @Transactional
    public WorkersRegistrationResponse registerWorker(Integer clientId, WorkerRegistrationRequest request, Integer userId) {
        // Validar cliente
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", clientId));

        // Verificar si ya existe
        if (workersRegistrationRepository.existsByClientIdAndEmail(clientId, request.getEmail())) {
            throw new DuplicateResourceException("Ya existe un trabajador con ese email");
        }

        // Crear registro
        WorkersRegistration worker = workersRegistrationMapper.toEntity(request);
        worker.setClientId(clientId);
        worker.setCreatedBy(userId);

        WorkersRegistration savedWorker = workersRegistrationRepository.save(worker);

        // Enviar notificación si se solicita
        if (request.getSendNotification() != null && request.getSendNotification()) {
            sendWorkerNotification(savedWorker, client, userId);
        }

        return workersRegistrationMapper.toDto(savedWorker);
    }

    @Override
    public BatchWorkersRegistrationResponse registerWorkersBatch(Integer clientId, MultipartFile file, Integer userId) {
        // Validar cliente
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", clientId));

        // Validar archivo
        if (file.isEmpty()) {
            throw new ValidationException("Archivo vacío");
        }

        String contentType = file.getContentType();
        if (contentType == null ||
                (!contentType.equals("application/vnd.ms-excel") &&
                        !contentType.equals("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))) {
            throw new ValidationException("Formato de archivo no soportado. Debe ser Excel (.xls o .xlsx)");
        }

        // Generar ID de batch
        String batchId = UUID.randomUUID().toString();

        // Inicializar estado del proceso
        BatchProcessStatus status = new BatchProcessStatus();
        status.setBatchId(batchId);
        status.setStatus("PROCESSING");
        status.setStartTime(LocalDateTime.now());
        status.setTotalRecords(0);
        status.setProcessedRecords(0);
        status.setSuccessRecords(0);
        status.setErrorRecords(0);

        batchProcessStatusMap.put(batchId, status);

        // Procesar archivo de forma asíncrona
        taskExecutor.execute(() -> processBatch(file, clientId, client, userId, batchId));

        // Devolver respuesta inmediata
        BatchWorkersRegistrationResponse response = new BatchWorkersRegistrationResponse();
        response.setBatchId(batchId);
        response.setStatus("PROCESSING");
        response.setMessage("Procesamiento iniciado");

        return response;
    }

    @Override
    public List<WorkersRegistrationResponse> getClientWorkers(Integer clientId) {
        // Validar cliente
        if (!clientRepository.existsById(clientId)) {
            throw new ResourceNotFoundException("Cliente", "id", clientId);
        }

        List<WorkersRegistration> workers = workersRegistrationRepository.findByClientId(clientId);
        return workersRegistrationMapper.toDtoList(workers);
    }

    @Override
    public WorkersRegistrationResponse getWorkerById(Integer clientId, Integer workerId) {
        WorkersRegistration worker = workersRegistrationRepository.findByIdAndClientId(workerId, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Trabajador", "id", workerId));

        return workersRegistrationMapper.toDto(worker);
    }

    @Override
    @Transactional
    public void deleteWorker(Integer clientId, Integer workerId) {
        WorkersRegistration worker = workersRegistrationRepository.findByIdAndClientId(workerId, clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Trabajador", "id", workerId));

        workersRegistrationRepository.delete(worker);
    }

    @Override
    public BatchProcessStatusResponse getBatchStatus(String batchId) {
        BatchProcessStatus status = batchProcessStatusMap.get(batchId);

        if (status == null) {
            throw new ResourceNotFoundException("Proceso batch no encontrado");
        }

        return batchProcessMapper.toDto(status);
    }

    private void processBatch(
            MultipartFile file, Integer clientId, Client client, Integer userId, String batchId) {

        BatchProcessStatus status = batchProcessStatusMap.get(batchId);
        List<BatchError> errors = new ArrayList<>();

        try {
            // Leer archivo Excel
            Workbook workbook = WorkbookFactory.create(file.getInputStream());
            Sheet sheet = workbook.getSheetAt(0);

            // Validar estructura (debe tener encabezados Email y DNI)
            Row headerRow = sheet.getRow(0);
            int emailColIdx = -1;
            int dniColIdx = -1;

            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                Cell cell = headerRow.getCell(i);
                if (cell != null) {
                    String value = cell.getStringCellValue().trim().toLowerCase();
                    if (value.equals("email")) {
                        emailColIdx = i;
                    } else if (value.equals("dni")) {
                        dniColIdx = i;
                    }
                }
            }

            if (emailColIdx == -1 || dniColIdx == -1) {
                throw new ValidationException("Estructura de archivo inválida. Se requieren columnas 'Email' y 'DNI'");
            }

            // Contar registros
            int totalRows = sheet.getLastRowNum();
            status.setTotalRecords(totalRows);

            // Procesar filas
            for (int i = 1; i <= totalRows; i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                try {
                    Cell emailCell = row.getCell(emailColIdx);
                    Cell dniCell = row.getCell(dniColIdx);

                    if (emailCell == null || dniCell == null) {
                        throw new ValidationException("Email o DNI faltante en fila " + (i + 1));
                    }

                    String email = "";
                    String dni = "";

                    // Obtener email
                    if (emailCell.getCellType() == CellType.STRING) {
                        email = emailCell.getStringCellValue().trim();
                    } else {
                        email = String.valueOf(emailCell.getNumericCellValue());
                    }

                    // Obtener DNI
                    if (dniCell.getCellType() == CellType.STRING) {
                        dni = dniCell.getStringCellValue().trim();
                    } else {
                        dni = String.valueOf((long) dniCell.getNumericCellValue());
                    }

                    // Validar email
                    if (!email.matches("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$")) {
                        throw new ValidationException("Email inválido: " + email);
                    }

                    // Verificar si ya existe
                    if (workersRegistrationRepository.existsByClientIdAndEmail(clientId, email)) {
                        throw new DuplicateResourceException("Ya existe un trabajador con email: " + email);
                    }

                    // Crear registro
                    WorkersRegistration worker = new WorkersRegistration();
                    worker.setClientId(clientId);
                    worker.setEmail(email);
                    worker.setDni(dni);
                    worker.setIsRegistered(false);
                    worker.setNotificationSent(false);
                    worker.setCreatedBy(userId);
                    worker.setCreatedAt(LocalDateTime.now());

                    workersRegistrationRepository.save(worker);

                    // Incrementar contador de éxito
                    status.setSuccessRecords(status.getSuccessRecords() + 1);

                } catch (Exception e) {
                    // Registrar error
                    BatchError error = new BatchError();
                    error.setRowNumber(i + 1);
                    error.setMessage(e.getMessage());
                    errors.add(error);

                    // Incrementar contador de errores
                    status.setErrorRecords(status.getErrorRecords() + 1);
                }

                // Actualizar progreso
                status.setProcessedRecords(status.getProcessedRecords() + 1);
            }

            workbook.close();

            // Actualizar estado final
            status.setStatus("COMPLETED");
            status.setEndTime(LocalDateTime.now());
            status.setErrors(errors);

        } catch (Exception e) {
            // Registrar error general
            log.error("Error procesando archivo batch: {}", e.getMessage(), e);
            status.setStatus("FAILED");
            status.setEndTime(LocalDateTime.now());
            status.setErrorMessage(e.getMessage());
        }
    }

    private void sendWorkerNotification(WorkersRegistration worker, Client client, Integer userId) {
        NotificationRequest notification = new NotificationRequest();
        notification.setClientId(client.getId());
        notification.setTargetType(TargetType.WORKERS);
        notification.setTargetId(worker.getId());
        notification.setNotificationType("WORKER_REGISTRATION");
        notification.setSubject("Invitación para completar registro");

        String content = "<p>Estimado usuario,</p>" +
                "<p>Has sido registrado como trabajador en el sistema de " + client.getBusinessName() + ".</p>" +
                "<p>Por favor, completa tu registro siguiendo este enlace: <a href='[REGISTRATION_LINK]'>Completar registro</a></p>" +
                "<p>Saludos cordiales,<br>El equipo de soporte</p>";

        notification.setContent(content);

        try {
            notificationService.sendNotification(notification, userId);

            worker.setNotificationSent(true);
            worker.setNotificationDate(LocalDateTime.now());
            workersRegistrationRepository.save(worker);
        } catch (Exception e) {
            log.error("Error enviando notificación a trabajador: {}", e.getMessage(), e);
        }
    }
}
