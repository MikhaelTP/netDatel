package com.netdatel.adminserviceapi.api;

import com.netdatel.adminserviceapi.dto.request.ClientModuleRequest;
import com.netdatel.adminserviceapi.dto.response.ClientModuleResponse;
import com.netdatel.adminserviceapi.dto.response.ClientSummaryResponse;
import com.netdatel.adminserviceapi.entity.enums.ModuleStatus;
import com.netdatel.adminserviceapi.security.CurrentUserId;
import com.netdatel.adminserviceapi.service.ClientModuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clients/{clientId}/modules")
@RequiredArgsConstructor
public class ClientModuleController {

    private final ClientModuleService clientModuleService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('admin:client-module:create')")
    public ClientModuleResponse assignModuleToClient(
            @PathVariable Integer clientId,
            @RequestBody @Valid ClientModuleRequest request,
            @CurrentUserId Integer userId) {
        return clientModuleService.assignModuleToClient(clientId, request, userId);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('admin:client-module:read')")
    public List<ClientModuleResponse> getClientModules(@PathVariable Integer clientId) {
        return clientModuleService.getClientModules(clientId);
    }

    @PutMapping("/{moduleId}")
    @PreAuthorize("hasAuthority('admin:client-module:update')")
    public ClientModuleResponse updateClientModule(
            @PathVariable Integer clientId,
            @PathVariable Integer moduleId,
            @RequestBody @Valid ClientModuleRequest request,
            @CurrentUserId Integer userId) {
        return clientModuleService.updateClientModule(clientId, moduleId, request, userId);
    }

    @PatchMapping("/{moduleId}/status")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('admin:client-module:update')")
    public void changeModuleStatus(
            @PathVariable Integer clientId,
            @PathVariable Integer moduleId,
            @RequestParam ModuleStatus status,
            @CurrentUserId Integer userId) {
        clientModuleService.changeModuleStatus(clientId, moduleId, status, userId);
    }

    @DeleteMapping("/{moduleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAuthority('admin:client-module:delete')")
    public void removeClientModule(
            @PathVariable Integer clientId,
            @PathVariable Integer moduleId,
            @CurrentUserId Integer userId) {
        clientModuleService.removeClientModule(clientId, moduleId, userId);
    }
}
