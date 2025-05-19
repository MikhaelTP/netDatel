package com.netdatel.adminserviceapi.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientRequest {

    @NotBlank(message = "RUC es obligatorio")
    @Size(max = 20, message = "RUC no puede exceder 20 caracteres")
    @Pattern(regexp = "^\\d{11}$", message = "El RUC debe tener 11 dígitos numéricos")
    private String ruc;

    @NotBlank(message = "Razón social es obligatoria")
    @Size(max = 200, message = "Razón social no puede exceder 200 caracteres")
    private String businessName;

    @Size(max = 200, message = "Nombre comercial no puede exceder 200 caracteres")
    private String commercialName;

    @Size(max = 50, message = "Tipo de contribuyente no puede exceder 50 caracteres")
    private String taxpayerType;

    @Past(message = "La fecha de inicio de actividad debe ser en el pasado")
    private LocalDate activityStartDate;

    @Size(max = 300, message = "Dirección fiscal no puede exceder 300 caracteres")
    private String fiscalAddress;

    @Size(max = 300, message = "Actividad económica no puede exceder 300 caracteres")
    private String economicActivity;

    @Size(max = 50, message = "Número de contacto no puede exceder 50 caracteres")
    private String contactNumber;

    @Min(value = 0, message = "El almacenamiento asignado no puede ser negativo")
    private Integer allocatedStorage;

    @Size(max = 1000, message = "Notas no pueden exceder 1000 caracteres")
    private String notes;

    @Valid
    private List<LegalRepresentativeRequest> legalRepresentatives;

    @Valid
    private List<ClientModuleRequest> modules;

    @Valid
    private List<ClientAdministratorRequest> administrators;

    @Valid
    private List<WorkerRegistrationRequest> workers;
}