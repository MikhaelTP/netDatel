package com.netdatel.identityserviceapi.api;

import com.netdatel.identityserviceapi.domain.dto.RoleDto;
import com.netdatel.identityserviceapi.service.RoleService;
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
@RequestMapping("/api/roles")
@RequiredArgsConstructor
@Tag(name = "Roles", description = "Role management API")
@SecurityRequirement(name = "bearerAuth")
public class RoleController {

    private final RoleService roleService;

    @GetMapping
    @Operation(summary = "Get all roles")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CLIENT_ADMIN')")
    public ResponseEntity<Page<RoleDto>> getAllRoles(Pageable pageable) {
        log.debug("REST request to get all Roles");
        return ResponseEntity.ok(roleService.getAllRoles(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get role by id")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CLIENT_ADMIN')")
    public ResponseEntity<RoleDto> getRole(@PathVariable Integer id) {
        log.debug("REST request to get Role : {}", id);
        return ResponseEntity.ok(roleService.getRoleById(id));
    }


    @GetMapping("/name/{name}")
    @Operation(summary = "Get role by name")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CLIENT_ADMIN')")
    public ResponseEntity<RoleDto> getRoleByName(@PathVariable String name) {
        log.debug("REST request to get Role by name : {}", name);
        return ResponseEntity.ok(roleService.getRoleByName(name));
    }

    @GetMapping("/default")
    @Operation(summary = "Get default roles")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CLIENT_ADMIN')")
    public ResponseEntity<List<RoleDto>> getDefaultRoles() {
        log.debug("REST request to get default Roles");
        return ResponseEntity.ok(roleService.getDefaultRoles());
    }

    @PostMapping
    @Operation(summary = "Create new role")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<RoleDto> createRole(@Valid @RequestBody RoleDto roleDto) {
        log.debug("REST request to create Role : {}", roleDto);
        return ResponseEntity.ok(roleService.createRole(roleDto));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update role")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<RoleDto> updateRole(
            @PathVariable Integer id,
            @Valid @RequestBody RoleDto roleDto) {
        log.debug("REST request to update Role : {}", id);
        return ResponseEntity.ok(roleService.updateRole(id, roleDto));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete role")
    @ApiResponse(responseCode = "204", description = "Role deleted successfully")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteRole(@PathVariable Integer id) {
        log.debug("REST request to delete Role : {}", id);
        roleService.deleteRole(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{roleId}/permissions/{permissionId}")
    @Operation(summary = "Add permission to role")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> addPermissionToRole(
            @PathVariable Integer roleId,
            @PathVariable Integer permissionId) {
        log.debug("REST request to add Permission {} to Role {}", permissionId, roleId);
        roleService.addPermissionToRole(roleId, permissionId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{roleId}/permissions/{permissionId}")
    @Operation(summary = "Remove permission from role")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> removePermissionFromRole(
            @PathVariable Integer roleId,
            @PathVariable Integer permissionId) {
        log.debug("REST request to remove Permission {} from Role {}", permissionId, roleId);
        roleService.removePermissionFromRole(roleId, permissionId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/default")
    @Operation(summary = "Set role as default")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> setRoleAsDefault(@PathVariable Integer id) {
        log.debug("REST request to set Role {} as default", id);
        roleService.setRoleAsDefault(id);
        return ResponseEntity.ok().build();
    }
}