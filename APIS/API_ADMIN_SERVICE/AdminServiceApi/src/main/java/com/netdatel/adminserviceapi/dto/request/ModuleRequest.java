package com.netdatel.adminserviceapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ModuleRequest {
    @NotBlank(message = "Código es obligatorio")
    @Size(max = 20, message = "Código no puede exceder 20 caracteres")
    private String code;

    @NotBlank(message = "Nombre es obligatorio")
    @Size(max = 100, message = "Nombre no puede exceder 100 caracteres")
    private String name;

    private String description;

    @Size(max = 20, message = "Versión no puede exceder 20 caracteres")
    private String version;

    private Boolean isActive = true;

    private String features;
}
