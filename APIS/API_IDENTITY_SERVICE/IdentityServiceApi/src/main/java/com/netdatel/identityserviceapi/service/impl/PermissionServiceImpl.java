package com.netdatel.identityserviceapi.service.impl;

import com.netdatel.identityserviceapi.domain.dto.PermissionDto;
import com.netdatel.identityserviceapi.domain.entity.Permission;
import com.netdatel.identityserviceapi.domain.mapper.PermissionMapper;
import com.netdatel.identityserviceapi.exception.ResourceNotFoundException;
import com.netdatel.identityserviceapi.repository.PermissionRepository;
import com.netdatel.identityserviceapi.service.AuditService;
import com.netdatel.identityserviceapi.service.PermissionService;
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
public class PermissionServiceImpl implements PermissionService {

    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;
    private final AuditService auditService;

    @Override
    @Transactional
    public PermissionDto createPermission(PermissionDto permissionDto) {
        if (permissionRepository.existsByCode(permissionDto.getCode())) {
            throw new IllegalArgumentException("Permission code already exists");
        }

        Permission permission = permissionMapper.toEntity(permissionDto);
        Permission savedPermission = permissionRepository.save(permission);

        auditService.logEvent("PERMISSION_CREATED", "permission", savedPermission.getId().toString(),
                null, permissionMapper.toDto(savedPermission));

        log.info("Created new permission: {}", savedPermission.getCode());
        return permissionMapper.toDto(savedPermission);
    }

    @Override
    @Transactional
    public PermissionDto updatePermission(Integer id, PermissionDto permissionDto) {
        Permission existingPermission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with id: " + id));

        // Save old state for audit
        PermissionDto oldPermissionState = permissionMapper.toDto(existingPermission);

        // Check if code is being changed and is already taken
        if (!existingPermission.getCode().equals(permissionDto.getCode()) &&
                permissionRepository.existsByCode(permissionDto.getCode())) {
            throw new IllegalArgumentException("Permission code already exists");
        }

        permissionMapper.updateEntityFromDto(permissionDto, existingPermission);
        Permission updatedPermission = permissionRepository.save(existingPermission);

        auditService.logEvent("PERMISSION_UPDATED", "permission", updatedPermission.getId().toString(),
                oldPermissionState, permissionMapper.toDto(updatedPermission));

        log.info("Updated permission: {}", updatedPermission.getCode());
        return permissionMapper.toDto(updatedPermission);
    }

    @Override
    @Transactional
    public void deletePermission(Integer id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with id: " + id));

        permissionRepository.delete(permission);

        auditService.logEvent("PERMISSION_DELETED", "permission", id.toString(),
                permissionMapper.toDto(permission), null);

        log.info("Deleted permission: {}", permission.getCode());
    }

    @Override
    public PermissionDto getPermissionById(Integer id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with id: " + id));

        return permissionMapper.toDto(permission);
    }

    @Override
    public PermissionDto getPermissionByCode(String code) {
        Permission permission = permissionRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Permission not found with code: " + code));

        return permissionMapper.toDto(permission);
    }

    @Override
    public Page<PermissionDto> getAllPermissions(Pageable pageable) {
        return permissionRepository.findAll(pageable)
                .map(permissionMapper::toDto);
    }

    @Override
    public List<PermissionDto> getPermissionsByCategory(String category) {
        return permissionRepository.findByCategory(category)
                .stream()
                .map(permissionMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<PermissionDto> getPermissionsByService(String service) {
        return permissionRepository.findByService(service)
                .stream()
                .map(permissionMapper::toDto)
                .collect(Collectors.toList());
    }
}