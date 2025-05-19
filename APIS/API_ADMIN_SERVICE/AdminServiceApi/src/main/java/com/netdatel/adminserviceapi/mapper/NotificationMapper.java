package com.netdatel.adminserviceapi.mapper;

import com.netdatel.adminserviceapi.dto.request.NotificationRequest;
import com.netdatel.adminserviceapi.dto.response.NotificationResponse;
import com.netdatel.adminserviceapi.entity.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sendDate", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "errorMessage", ignore = true)
    @Mapping(target = "retryCount", constant = "0")
    @Mapping(target = "lastRetry", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    Notification toEntity(NotificationRequest request);

    NotificationResponse toDto(Notification notification);

    List<NotificationResponse> toDtoList(List<Notification> notifications);
}