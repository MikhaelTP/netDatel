package com.netdatel.documentserviceapi.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionRequest {

    @NotNull(message = "El ID del usuario no puede ser nulo")
    private Integer userId;

    private boolean canRead = false;

    private boolean canWrite = false;

    private boolean canDelete = false;

    private boolean canDownload = false;

    private LocalDateTime validUntil;
}
