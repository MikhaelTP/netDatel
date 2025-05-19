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
public class ClientStatusChangedEvent {
    private Integer clientId;
    private String previousStatus;
    private String newStatus;
    private LocalDateTime changedAt;
}