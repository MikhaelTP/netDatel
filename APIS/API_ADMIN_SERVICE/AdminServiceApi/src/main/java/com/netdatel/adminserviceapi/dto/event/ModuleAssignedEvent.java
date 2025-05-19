package com.netdatel.adminserviceapi.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleAssignedEvent {
    private Integer clientId;
    private Integer moduleId;
    private String moduleCode;
    private java.time.LocalDate startDate;
    private java.time.LocalDate endDate;
    private Integer storageLimit;
    private Integer maxUsers;
    private LocalDateTime assignedAt;
}
