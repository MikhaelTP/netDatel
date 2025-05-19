package com.netdatel.adminserviceapi.mapper;

import com.netdatel.adminserviceapi.dto.request.ClientRequest;
import com.netdatel.adminserviceapi.dto.response.ClientResponse;
import com.netdatel.adminserviceapi.entity.Client;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", uses = {
        LegalRepresentativeMapper.class,
        ClientModuleMapper.class,
        ClientAdministratorMapper.class
})
public interface ClientMapper {

    @Mapping(target = "code", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "notified", constant = "false")
    @Mapping(target = "registrationDate", ignore = true)
    @Mapping(target = "lastUpdateDate", ignore = true)
    @Mapping(target = "legalRepresentatives", ignore = true)
    @Mapping(target = "clientModules", ignore = true)
    @Mapping(target = "administrators", ignore = true)
    Client toEntity(ClientRequest request);

    @Mapping(target = "status", source = "status")
    @Mapping(target = "legalRepresentatives", source = "legalRepresentatives")
    @Mapping(target = "modules", source = "clientModules")
    @Mapping(target = "administrators", source = "administrators")
    ClientResponse toDto(Client client);

    List<ClientResponse> toDtoList(List<Client> clients);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateClientFromDto(ClientRequest request, @MappingTarget Client client);
}
