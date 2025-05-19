package com.netdatel.adminserviceapi.api;

import com.netdatel.adminserviceapi.dto.request.ModuleRequest;
import com.netdatel.adminserviceapi.dto.response.ModuleResponse;
import com.netdatel.adminserviceapi.security.CurrentUserId;
import com.netdatel.adminserviceapi.service.ModuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/modules")
@RequiredArgsConstructor
public class ModuleController {

    private final ModuleService moduleService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAuthority('admin:module:create')")
    public ModuleResponse createModule(
            @RequestBody @Valid ModuleRequest request,
            @CurrentUserId Integer userId) {
        return moduleService.createModule(request, userId);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('admin:module:read')")
    public ModuleResponse getModule(@PathVariable Integer id) {
        return moduleService.getModuleById(id);
    }

    @GetMapping("/code/{code}")
    @PreAuthorize("hasAuthority('admin:module:read')")
    public ModuleResponse getModuleByCode(@PathVariable String code) {
        return moduleService.getModuleByCode(code);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('admin:module:read')")
    public List<ModuleResponse> getAllModules() {
        return moduleService.getAllModules();
    }

    @GetMapping("/active")
    @PreAuthorize("hasAuthority('admin:module:read')")
    public List<ModuleResponse> getActiveModules() {
        return moduleService.getActiveModules();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('admin:module:update')")
    public ModuleResponse updateModule(
            @PathVariable Integer id,
            @RequestBody @Valid ModuleRequest request,
            @CurrentUserId Integer userId) {
        return moduleService.updateModule(id, request, userId);
    }

    @PatchMapping("/{id}/active")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasAuthority('admin:module:update')")
    public void toggleModuleActive(
            @PathVariable Integer id,
            @RequestParam boolean active,
            @CurrentUserId Integer userId) {
        moduleService.toggleModuleActive(id, active, userId);
    }
}