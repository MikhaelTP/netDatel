package com.netdatel.documentserviceapi.controller;

import com.netdatel.documentserviceapi.model.dto.request.ClientSpaceRequest;
import com.netdatel.documentserviceapi.model.dto.response.ApiResponse;
import com.netdatel.documentserviceapi.model.dto.response.ClientSpaceResponse;
import com.netdatel.documentserviceapi.model.entity.ClientSpace;
import com.netdatel.documentserviceapi.security.CurrentUserId;
import com.netdatel.documentserviceapi.service.ClientSpaceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/client-spaces")
@RequiredArgsConstructor
@Tag(name = "Client Spaces", description = "API para gestionar espacios de clientes")
public class ClientSpaceController {
    private final ClientSpaceService clientSpaceService;

    @PostMapping
    @Operation(summary = "Crear un espacio de cliente", description = "Crea un nuevo espacio de almacenamiento para un cliente y módulo específico")
    public ResponseEntity<ApiResponse<ClientSpaceResponse>> createClientSpace(
            @Valid @RequestBody ClientSpaceRequest request,
            @CurrentUserId Integer userId) {
        ClientSpace clientSpace = clientSpaceService.createClientSpace(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Espacio de cliente creado exitosamente", mapToResponse(clientSpace)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un espacio de cliente", description = "Obtiene un espacio de cliente por su ID")
    public ResponseEntity<ApiResponse<ClientSpaceResponse>> getClientSpace(@PathVariable Integer id) {
        ClientSpace clientSpace = clientSpaceService.getClientSpace(id);
        return ResponseEntity.ok(ApiResponse.success(mapToResponse(clientSpace)));
    }

    @GetMapping("/client/{clientId}")
    @Operation(summary = "Obtener espacios por cliente", description = "Obtiene todos los espacios asignados a un cliente")
    public ResponseEntity<ApiResponse<List<ClientSpaceResponse>>> getClientSpacesByClient(@PathVariable Integer clientId) {
        List<ClientSpace> clientSpaces = clientSpaceService.getClientSpacesByClient(clientId);
        return ResponseEntity.ok(ApiResponse.success(
                clientSpaces.stream().map(this::mapToResponse).collect(Collectors.toList())));
    }

    @GetMapping("/client/{clientId}/module/{moduleId}")
    @Operation(summary = "Obtener espacio por cliente y módulo", description = "Obtiene un espacio específico para un cliente y módulo")
    public ResponseEntity<ApiResponse<ClientSpaceResponse>> getClientSpaceByClientAndModule(
            @PathVariable Integer clientId, @PathVariable Integer moduleId) {
        ClientSpace clientSpace = clientSpaceService.getClientSpaceByClientAndModule(clientId, moduleId);
        return ResponseEntity.ok(ApiResponse.success(mapToResponse(clientSpace)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un espacio de cliente", description = "Actualiza un espacio de cliente existente")
    public ResponseEntity<ApiResponse<ClientSpaceResponse>> updateClientSpace(
            @PathVariable Integer id,
            @Valid @RequestBody ClientSpaceRequest request,
            @CurrentUserId Integer userId) {
        ClientSpace clientSpace = clientSpaceService.updateClientSpace(id, request, userId);
        return ResponseEntity.ok(ApiResponse.success("Espacio de cliente actualizado exitosamente", mapToResponse(clientSpace)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un espacio de cliente", description = "Elimina un espacio de cliente por su ID")
    public ResponseEntity<ApiResponse<Void>> deleteClientSpace(@PathVariable Integer id) {
        clientSpaceService.deleteClientSpace(id);
        return ResponseEntity.ok(ApiResponse.success("Espacio de cliente eliminado exitosamente", null));
    }

    private ClientSpaceResponse mapToResponse(ClientSpace clientSpace) {
        return ClientSpaceResponse.builder()
                .id(clientSpace.getId())
                .clientId(clientSpace.getClientId())
                .moduleId(clientSpace.getModuleId())
                .storagePath(clientSpace.getStoragePath())
                .totalQuotaBytes(clientSpace.getTotalQuotaBytes())
                .usedBytes(clientSpace.getUsedBytes())
                .isActive(clientSpace.isActive())
                .createdAt(clientSpace.getCreatedAt())
                .updatedAt(clientSpace.getUpdatedAt())
                .build();
    }
}