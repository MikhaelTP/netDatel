package com.netdatel.adminserviceapi.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientModuleRequest {

    @NotNull(message = "ID de módulo es obligatorio")
    private Integer moduleId;

    @NotNull(message = "Fecha de inicio es obligatoria")
    private LocalDate startDate;

    private LocalDate endDate;

    @Min(value = 1, message = "Número máximo de cuentas debe ser al menos 1")
    private Integer maxUserAccounts = 10;

    @Min(value = 0, message = "Límite de almacenamiento específico no puede ser negativo")
    private Integer specificStorageLimit;

    private String configuration;
}