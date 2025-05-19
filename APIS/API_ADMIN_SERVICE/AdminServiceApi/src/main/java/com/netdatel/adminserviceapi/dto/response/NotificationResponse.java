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
public class NotificationResponse {

    private Integer id;
    private Integer clientId;
    private String targetType;
    private Integer targetId;
    private String notificationType;
    private String subject;
    private String content;
    private LocalDateTime sendDate;
    private String status;
    private String errorMessage;
    private Integer retryCount;
    private LocalDateTime lastRetry;

}