package com.netdatel.documentserviceapi.model.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClientSpaceRequest {

    @NotNull(message = "El ID del cliente no puede ser nulo")
    private Integer clientId;

    @NotNull(message = "El ID del módulo no puede ser nulo")
    private Integer moduleId;

    @NotNull(message = "La cuota total de almacenamiento no puede ser nula")
    @Min(value = 1, message = "La cuota debe ser mayor que cero")
    private Long totalQuotaBytes;
}