package com.netdatel.identityserviceapi.domain.mapper;

import com.netdatel.identityserviceapi.domain.dto.UserDto;
import com.netdatel.identityserviceapi.domain.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring",
        uses = {RoleMapper.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper {
    @Mapping(target = "password", ignore = true)  // Este debería ser el correcto
        // En lugar de intentar mapear passwordHash a algo que no existe
    UserDto toDto(User user);

    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "roles", ignore = true)
    User toEntity(UserDto userDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)  // CRÍTICO: No mapear passwordHash en update
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "lastLogin", ignore = true)
    @Mapping(target = "enabled", ignore = true)  // NUEVO: Preservar enabled
    @Mapping(target = "accountNonLocked", ignore = true)  // NUEVO: Preservar accountNonLocked
    @Mapping(target = "roles", ignore = true)
    void updateEntityFromDto(UserDto userDto, @MappingTarget User user);
}