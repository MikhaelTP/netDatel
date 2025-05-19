package com.netdatel.adminserviceapi.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClientModuleResponse {
    private Integer id;
    private Integer moduleId;
    private String moduleCode;
    private String moduleName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status;
    private Integer maxUserAccounts;
    private Long specificStorageLimit;
    private LocalDateTime activationDate;
    private LocalDateTime deactivationDate;
    private LocalDateTime createdAt;
    private String configuration;
}