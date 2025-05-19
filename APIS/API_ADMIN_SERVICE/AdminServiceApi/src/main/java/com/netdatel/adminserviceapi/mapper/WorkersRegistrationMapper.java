package com.netdatel.adminserviceapi.mapper;

import com.netdatel.adminserviceapi.dto.request.WorkerRegistrationRequest;
import com.netdatel.adminserviceapi.dto.response.WorkersRegistrationResponse;
import com.netdatel.adminserviceapi.entity.WorkersRegistration;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WorkersRegistrationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "identityUserId", ignore = true)
    @Mapping(target = "isRegistered", constant = "false")
    @Mapping(target = "registrationDate", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "notificationSent", constant = "false")
    @Mapping(target = "notificationDate", ignore = true)
    WorkersRegistration toEntity(WorkerRegistrationRequest request);

    WorkersRegistrationResponse toDto(WorkersRegistration worker);

    List<WorkersRegistrationResponse> toDtoList(List<WorkersRegistration> workers);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateWorkerFromDto(WorkerRegistrationRequest request, @MappingTarget WorkersRegistration worker);
}