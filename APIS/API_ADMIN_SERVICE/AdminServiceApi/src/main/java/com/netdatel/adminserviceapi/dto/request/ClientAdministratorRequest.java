package com.netdatel.adminserviceapi.dto.request;

import jakarta.validation.constraints.Email;
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
public class ClientAdministratorRequest {
    @NotBlank(message = "Email es obligatorio")
    @Email(message = "Formato de email inválido")
    @Size(max = 100, message = "Email no puede exceder 100 caracteres")
    private String email;

    @NotBlank(message = "DNI es obligatorio")
    @Size(max = 20, message = "DNI no puede exceder 20 caracteres")
    private String dni;

    private Boolean sendNotification = true;
}