package com.netdatel.adminserviceapi.mapper;

import com.netdatel.adminserviceapi.dto.response.ClientHistoryResponse;
import com.netdatel.adminserviceapi.entity.ClientHistory;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ClientHistoryMapper {

    ClientHistoryResponse toDto(ClientHistory history);

    List<ClientHistoryResponse> toDtoList(List<ClientHistory> history);
}
