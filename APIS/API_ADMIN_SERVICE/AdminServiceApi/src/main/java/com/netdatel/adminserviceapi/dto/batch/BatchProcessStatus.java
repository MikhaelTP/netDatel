package com.netdatel.adminserviceapi.dto.batch;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchProcessStatus {
    private String batchId;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer totalRecords;
    private Integer processedRecords;
    private Integer successRecords;
    private Integer errorRecords;
    private String errorMessage;
    private List<BatchError> errors = new ArrayList<>();
}