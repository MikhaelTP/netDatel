package com.netdatel.identityserviceapi.service.impl;

import com.netdatel.identityserviceapi.domain.dto.RoleDto;
import com.netdatel.identityserviceapi.domain.entity.Permission;
import com.netdatel.identityserviceapi.domain.entity.Role;
import com.netdatel.identityserviceapi.domain.mapper.RoleMapper;
import com.netdatel.identityserviceapi.exception.ResourceNotFoundException;
import com.netdatel.identityserviceapi.repository.PermissionRepository;
import com.netdatel.identityserviceapi.repository.RoleRepository;
import com.netdatel.identityserviceapi.service.AuditService;
import com.netdatel.identityserviceapi.service.RoleService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleMapper roleMapper;
    private final AuditService auditService;

    @Override
    @Transactional
    public RoleDto createRole(RoleDto roleDto) {
        if (roleRepository.existsByName(roleDto.getName())) {
            throw new IllegalArgumentException("Role name already exists");
        }

        Role role = roleMapper.toEntity(roleDto);
        Role savedRole = roleRepository.save(role);

        auditService.logEvent("ROLE_CREATED", "role", savedRole.getId().toString(), null, roleMapper.toDto(savedRole));

        log.info("Created new role: {}", savedRole.getName());
        return roleMapper.toDto(savedRole);
    }

    @Override
    @Transactional
    public RoleDto updateRole(Integer id, RoleDto roleDto) {
        Role existingRole = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));

        // Save old state for audit
        RoleDto oldRoleState = roleMapper.toDto(existingRole);

        // Check if name is being changed and is already taken
        if (!existingRole.getName().equals(roleDto.getName()) &&
                roleRepository.existsByName(roleDto.getName())) {
            throw new IllegalArgumentException("Role name already exists");
        }

        roleMapper.updateEntityFromDto(roleDto, existingRole);
        Role updatedRole = roleRepository.save(existingRole);

        auditService.logEvent("ROLE_UPDATED", "role", updatedRole.getId().toString(), oldRoleState, roleMapper.toDto(updatedRole));

        log.info("Updated role: {}", updatedRole.getName());
        return roleMapper.toDto(updatedRole);
    }

    @Override
    @Transactional
    public void deleteRole(Integer id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));

        roleRepository.delete(role);

        auditService.logEvent("ROLE_DELETED", "role", id.toString(), roleMapper.toDto(role), null);

        log.info("Deleted role: {}", role.getName());
    }

    @Override
    public RoleDto getRoleById(Integer id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));

        return roleMapper.toDto(role);
    }

    @Override
    public RoleDto getRoleByName(String name) {
        Role role = roleRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with name: " + name));

        return roleMapper.toDto(role);
    }

    @Override
    public Page<RoleDto> getAllRoles(Pageable pageable) {
        return roleRepository.findAll(pageable)
                .map(roleMapper::toDto);
    }

    @Override
    @Transactional
    public void addPermissionToRole(Integer roleId, Integer permissionId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));

        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with id: " + permissionId));

        // Check if role already has the permission
        if (role.getPermissions().contains(permission)) {
            return;
        }

        role.getPermissions().add(permission);
        roleRepository.save(role);

        auditService.logEvent("PERMISSION_ADDED_TO_ROLE", "role_permission", roleId + "_" + permissionId, null, null);

        log.info("Added permission {} to role {}", permission.getCode(), role.getName());
    }

    @Override
    @Transactional
    public void removePermissionFromRole(Integer roleId, Integer permissionId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + roleId));

        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with id: " + permissionId));

        role.getPermissions().remove(permission);
        roleRepository.save(role);

        auditService.logEvent("PERMISSION_REMOVED_FROM_ROLE", "role_permission", roleId + "_" + permissionId, null, null);

        log.info("Removed permission {} from role {}", permission.getCode(), role.getName());
    }

    @Override
    public List<RoleDto> getDefaultRoles() {
        return roleRepository.findByIsDefaultTrue()
                .stream()
                .map(roleMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void setRoleAsDefault(Integer id) {
        // First, remove default flag from all roles
        roleRepository.findByIsDefaultTrue().ifPresent(currentDefault -> {
            currentDefault.setDefault(false);
            roleRepository.save(currentDefault);
        });

        // Then set the new default role
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with id: " + id));

        role.setDefault(true);
        roleRepository.save(role);

        auditService.logEvent("ROLE_SET_AS_DEFAULT", "role", id.toString(), null, null);

        log.info("Set role {} as default", role.getName());
    }
}