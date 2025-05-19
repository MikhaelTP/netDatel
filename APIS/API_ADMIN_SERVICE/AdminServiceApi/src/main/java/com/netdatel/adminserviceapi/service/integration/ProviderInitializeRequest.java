package com.netdatel.adminserviceapi.service.integration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProviderInitializeRequest {
    /**
     * ID del cliente en el sistema
     */
    private Integer clientId;

    /**
     * Nombre comercial del cliente
     */
    private String businessName;

    /**
     * Email del administrador del cliente
     */
    private String adminEmail;

    /**
     * Número máximo de proveedores permitidos
     */
    private Integer maxProviders;

    /**
     * Número máximo de auditores permitidos
     */
    private Integer maxAuditors;
}