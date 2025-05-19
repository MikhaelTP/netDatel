package com.netdatel.identityserviceapi.service;

import com.netdatel.identityserviceapi.domain.dto.RoleDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RoleService {

    RoleDto createRole(RoleDto roleDto);

    RoleDto updateRole(Integer id, RoleDto roleDto);

    void deleteRole(Integer id);

    RoleDto getRoleById(Integer id);

    RoleDto getRoleByName(String name);

    Page<RoleDto> getAllRoles(Pageable pageable);

    void addPermissionToRole(Integer roleId, Integer permissionId);

    void removePermissionFromRole(Integer roleId, Integer permissionId);

    List<RoleDto> getDefaultRoles();

    void setRoleAsDefault(Integer id);
}
