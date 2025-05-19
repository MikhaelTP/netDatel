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
public class ClientCreatedEvent {

    private Integer clientId;
    private String clientCode;
    private String businessName;
    private Long allocatedStorage;
    private LocalDateTime createdAt;

}