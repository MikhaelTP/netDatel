package com.netdatel.documentserviceapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/health")
@Tag(name = "Health", description = "API para verificar el estado del servicio")
public class HealthController {

    @GetMapping
    @Operation(summary = "Verificar estado", description = "Verifica que el servicio esté en funcionamiento")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("service", "document-service");
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }
}