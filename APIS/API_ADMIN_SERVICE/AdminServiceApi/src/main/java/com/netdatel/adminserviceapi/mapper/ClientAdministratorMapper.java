package com.netdatel.adminserviceapi.mapper;

import com.netdatel.adminserviceapi.dto.request.ClientAdministratorRequest;
import com.netdatel.adminserviceapi.dto.response.ClientAdministratorResponse;
import com.netdatel.adminserviceapi.entity.ClientAdministrator;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ClientAdministratorMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "identityUserId", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "notificationSent", constant = "false")
    @Mapping(target = "notificationDate", ignore = true)
    ClientAdministrator toEntity(ClientAdministratorRequest request);

    ClientAdministratorResponse toDto(ClientAdministrator administrator);

    List<ClientAdministratorResponse> toDtoList(List<ClientAdministrator> administrators);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateClientAdministratorFromDto(ClientAdministratorRequest request, @MappingTarget ClientAdministrator administrator);
}