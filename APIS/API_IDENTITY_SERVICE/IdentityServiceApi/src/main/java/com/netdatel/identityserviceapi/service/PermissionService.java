package com.netdatel.identityserviceapi.service;

import com.netdatel.identityserviceapi.domain.dto.PermissionDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PermissionService {

    PermissionDto createPermission(PermissionDto permissionDto);

    PermissionDto updatePermission(Integer id, PermissionDto permissionDto);

    void deletePermission(Integer id);

    PermissionDto getPermissionById(Integer id);

    PermissionDto getPermissionByCode(String code);

    Page<PermissionDto> getAllPermissions(Pageable pageable);

    List<PermissionDto> getPermissionsByCategory(String category);

    List<PermissionDto> getPermissionsByService(String service);
}
