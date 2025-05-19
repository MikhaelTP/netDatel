package com.netdatel.identityserviceapi.domain.mapper;

import com.netdatel.identityserviceapi.domain.dto.PermissionDto;
import com.netdatel.identityserviceapi.domain.entity.Permission;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PermissionMapper {

    PermissionDto toDto(Permission permission);

    @Mapping(target = "createdAt", ignore = true)
    Permission toEntity(PermissionDto permissionDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntityFromDto(PermissionDto permissionDto, @MappingTarget Permission permission);
}