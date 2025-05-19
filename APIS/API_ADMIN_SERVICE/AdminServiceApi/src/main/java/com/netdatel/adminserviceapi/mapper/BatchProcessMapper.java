package com.netdatel.adminserviceapi.mapper;

import com.netdatel.adminserviceapi.dto.batch.BatchError;
import com.netdatel.adminserviceapi.dto.batch.BatchProcessStatus;
import com.netdatel.adminserviceapi.dto.response.BatchErrorResponse;
import com.netdatel.adminserviceapi.dto.response.BatchProcessStatusResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BatchProcessMapper {

    BatchErrorResponse toDto(BatchError error);

    List<BatchErrorResponse> toErrorDtoList(List<BatchError> errors);

    BatchProcessStatusResponse toDto(BatchProcessStatus status);
}