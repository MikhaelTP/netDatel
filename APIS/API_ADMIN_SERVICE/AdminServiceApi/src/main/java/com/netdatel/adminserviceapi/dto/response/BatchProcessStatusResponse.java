package com.netdatel.adminserviceapi.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.netdatel.adminserviceapi.dto.batch.BatchError;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BatchProcessStatusResponse {

    private String batchId;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer totalRecords;
    private Integer processedRecords;
    private Integer successRecords;
    private Integer errorRecords;
    private String errorMessage;
    private List<BatchErrorResponse> errors;

}
