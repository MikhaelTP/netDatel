package com.netdatel.adminserviceapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LegalRepresentativeRequest {
    @NotBlank(message = "Tipo de documento es obligatorio")
    @Size(max = 30, message = "Tipo de documento no puede exceder 30 caracteres")
    private String documentType;

    @NotBlank(message = "Número de documento es obligatorio")
    @Size(max = 30, message = "Número de documento no puede exceder 30 caracteres")
    private String documentNumber;

    @NotBlank(message = "Nombre completo es obligatorio")
    @Size(max = 200, message = "Nombre completo no puede exceder 200 caracteres")
    private String fullName;

    @NotBlank(message = "Cargo es obligatorio")
    @Size(max = 100, message = "Cargo no puede exceder 100 caracteres")
    private String position;

    @NotNull(message = "Fecha de inicio es obligatoria")
    @Past(message = "La fecha de inicio debe ser en el pasado")
    private LocalDate startDate;

    private LocalDate endDate;

    private Boolean isActive = true;
}