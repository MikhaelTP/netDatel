package com.netdatel.documentserviceapi.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Document Service API",
                version = "1.0",
                description = "API para gestión de documentos, carpetas y permisos",
                contact = @Contact(
                        name = "Equipo de Desarrollo",
                        email = "desarrollo@netdatel.com",
                        url = "https://www.netdatel.com"
                ),
                license = @License(
                        name = "Propietario",
                        url = "https://www.netdatel.com/licencias"
                )
        ),
        servers = {
                @Server(url = "/", description = "Servidor principal")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Token JWT obtenido del servicio de Identity"
)
public class OpenApiConfig {
    // Configuración adicional si es necesaria
}