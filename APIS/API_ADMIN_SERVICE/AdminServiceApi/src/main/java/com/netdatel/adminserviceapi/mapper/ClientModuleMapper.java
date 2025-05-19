package com.netdatel.adminserviceapi.mapper;

import com.netdatel.adminserviceapi.dto.request.ClientModuleRequest;
import com.netdatel.adminserviceapi.dto.response.ClientModuleResponse;
import com.netdatel.adminserviceapi.entity.ClientModule;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ClientModuleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "module", ignore = true)
    @Mapping(target = "activationDate", ignore = true)
    @Mapping(target = "deactivationDate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "lastUpdate", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    ClientModule toEntity(ClientModuleRequest request);

    @Mapping(target = "moduleId", source = "module.id")
    @Mapping(target = "moduleCode", source = "module.code")
    @Mapping(target = "moduleName", source = "module.name")
    ClientModuleResponse toDto(ClientModule clientModule);

    List<ClientModuleResponse> toDtoList(List<ClientModule> clientModules);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateClientModuleFromDto(ClientModuleRequest request, @MappingTarget ClientModule clientModule);
}