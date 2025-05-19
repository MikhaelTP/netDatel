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
public class WorkersRegistrationResponse {

    private Integer id;
    private Integer clientId;
    private String email;
    private String dni;
    private Long identityUserId;
    private Boolean isRegistered;
    private LocalDateTime registrationDate;
    private LocalDateTime createdAt;
    private Boolean notificationSent;
    private LocalDateTime notificationDate;
}