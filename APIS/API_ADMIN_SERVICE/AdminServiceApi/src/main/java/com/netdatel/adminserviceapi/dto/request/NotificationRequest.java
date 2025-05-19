package com.netdatel.adminserviceapi.dto.request;

import com.netdatel.adminserviceapi.entity.enums.TargetType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    private Integer clientId;

    @NotNull(message = "Tipo de destinatario es obligatorio")
    private TargetType targetType;

    private Integer targetId;

    @NotBlank(message = "Tipo de notificación es obligatorio")
    @Size(max = 50, message = "Tipo de notificación no puede exceder 50 caracteres")
    private String notificationType;

    @NotBlank(message = "Asunto es obligatorio")
    @Size(max = 200, message = "Asunto no puede exceder 200 caracteres")
    private String subject;

    @NotBlank(message = "Contenido es obligatorio")
    private String content;
}