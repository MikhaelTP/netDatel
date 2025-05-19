package com.netdatel.adminserviceapi.mapper;

import com.netdatel.adminserviceapi.dto.external.UserCreateRequest;
import com.netdatel.adminserviceapi.entity.ClientAdministrator;
import com.netdatel.adminserviceapi.dto.external.UserCreateRequest.UserAttributes;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Collections;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "username", expression = "java(generateUsername(administrator))")
    @Mapping(target = "firstName", ignore = true)
    @Mapping(target = "lastName", ignore = true)
    @Mapping(target = "userType", constant = "CLIENT_ADMIN")
    @Mapping(target = "attributes", expression = "java(createUserAttributes(administrator))")
    UserCreateRequest toUserCreateRequest(ClientAdministrator administrator);

    default String generateUsername(ClientAdministrator administrator) {
        String email = administrator.getEmail();
        return email.substring(0, email.indexOf('@')).toLowerCase();
    }

    default UserAttributes createUserAttributes(ClientAdministrator administrator) {
        UserAttributes attributes = new UserAttributes();
        attributes.setClientId(administrator.getClient().getId());

        // Obtener módulos contratados por el cliente
        java.util.List<String> modules = administrator.getClient().getClientModules().stream()
                .map(cm -> cm.getModule().getCode())
                .collect(java.util.stream.Collectors.toList());

        attributes.setModules(modules.isEmpty() ? Collections.emptyList() : modules);
        return attributes;
    }
}
