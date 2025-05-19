package com.netdatel.adminserviceapi.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClientHistoryResponse {

    private Integer id;
    private Integer clientId;
    private String action;
    private String previousStatus;
    private String newStatus;
    private LocalDateTime changeDate;
    private Long changedBy;
    private String notes;
    private String details;

}