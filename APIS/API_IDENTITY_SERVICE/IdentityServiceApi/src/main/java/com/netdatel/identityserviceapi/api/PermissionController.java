package com.netdatel.identityserviceapi.api;

import com.netdatel.identityserviceapi.domain.dto.PermissionDto;
import com.netdatel.identityserviceapi.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
@Tag(name = "Permissions", description = "Permission management API")
@SecurityRequirement(name = "bearerAuth")
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping
    @Operation(summary = "Get all permissions")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CLIENT_ADMIN')")
    public ResponseEntity<Page<PermissionDto>> getAllPermissions(Pageable pageable) {
        log.debug("REST request to get all Permissions");
        return ResponseEntity.ok(permissionService.getAllPermissions(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get permission by id")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CLIENT_ADMIN')")
    public ResponseEntity<PermissionDto> getPermission(@PathVariable Integer id) {
        log.debug("REST request to get Permission : {}", id);
        return ResponseEntity.ok(permissionService.getPermissionById(id));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get permission by code")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CLIENT_ADMIN')")
    public ResponseEntity<PermissionDto> getPermissionByCode(@PathVariable String code) {
        log.debug("REST request to get Permission by code : {}", code);
        return ResponseEntity.ok(permissionService.getPermissionByCode(code));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get permissions by category")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CLIENT_ADMIN')")
    public ResponseEntity<List<PermissionDto>> getPermissionsByCategory(@PathVariable String category) {
        log.debug("REST request to get Permissions by category : {}", category);
        return ResponseEntity.ok(permissionService.getPermissionsByCategory(category));
    }

    @GetMapping("/service/{service}")
    @Operation(summary = "Get permissions by service")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CLIENT_ADMIN')")
    public ResponseEntity<List<PermissionDto>> getPermissionsByService(@PathVariable String service) {
        log.debug("REST request to get Permissions by service : {}", service);
        return ResponseEntity.ok(permissionService.getPermissionsByService(service));
    }

    @PostMapping
    @Operation(summary = "Create new permission")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<PermissionDto> createPermission(@Valid @RequestBody PermissionDto permissionDto) {
        log.debug("REST request to create Permission : {}", permissionDto);
        return ResponseEntity.ok(permissionService.createPermission(permissionDto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update permission")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<PermissionDto> updatePermission(
            @PathVariable Integer id,
            @Valid @RequestBody PermissionDto permissionDto) {
        log.debug("REST request to update Permission : {}", id);
        return ResponseEntity.ok(permissionService.updatePermission(id, permissionDto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete permission")
    @ApiResponse(responseCode = "204", description = "Permission deleted successfully")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deletePermission(@PathVariable Integer id) {
        log.debug("REST request to delete Permission : {}", id);
        permissionService.deletePermission(id);
        return ResponseEntity.noContent().build();
    }
}
