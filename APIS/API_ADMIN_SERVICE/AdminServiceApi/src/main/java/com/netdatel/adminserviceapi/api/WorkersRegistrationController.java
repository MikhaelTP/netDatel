package com.netdatel.adminserviceapi.api;

import com.netdatel.adminserviceapi.dto.request.WorkerRegistrationRequest;
import com.netdatel.adminserviceapi.dto.response.BatchProcessStatusResponse;
import com.netdatel.adminserviceapi.dto.response.BatchWorkersRegistrationResponse;
import com.netdatel.adminserviceapi.dto.response.WorkersRegistrationResponse;
import com.netdatel.adminserviceapi.security.CurrentUserId;
import com.netdatel.adminserviceapi.service.WorkersRegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/clients/{clientId}/workers")
@RequiredArgsConstructor
public class WorkersRegistrationController {

    private final WorkersRegistrationService workersRegistrationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('admin:worker:create')")
    public WorkersRegistrationResponse registerWorker(
            @PathVariable Integer clientId,
            @RequestBody @Valid WorkerRegistrationRequest request,
            @CurrentUserId Integer userId) {
        return workersRegistrationService.registerWorker(clientId, request, userId);
    }

    @PostMapping("/batch")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @PreAuthorize("hasAuthority('admin:worker:create')")
    public BatchWorkersRegistrationResponse registerWorkersBatch(
            @PathVariable Integer clientId,
            @RequestParam("file") MultipartFile file,
            @CurrentUserId Integer userId) {
        return workersRegistrationService.registerWorkersBatch(clientId, file, userId);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('admin:worker:read')")
    public List<WorkersRegistrationResponse> getClientWorkers(@PathVariable Integer clientId) {
        return workersRegistrationService.getClientWorkers(clientId);
    }

    @GetMapping("/{workerId}")
    @PreAuthorize("hasAuthority('admin:worker:read')")
    public WorkersRegistrationResponse getWorker(
            @PathVariable Integer clientId,
            @PathVariable Integer workerId) {
        return workersRegistrationService.getWorkerById(clientId, workerId);
    }

    @DeleteMapping("/{workerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('admin:worker:delete')")
    public void deleteWorker(
            @PathVariable Integer clientId,
            @PathVariable Integer workerId) {
        workersRegistrationService.deleteWorker(clientId, workerId);
    }

    @GetMapping("/batch/{batchId}/status")
    @PreAuthorize("hasAuthority('admin:worker:read')")
    public BatchProcessStatusResponse getBatchStatus(@PathVariable String batchId) {
        return workersRegistrationService.getBatchStatus(batchId);
    }
}