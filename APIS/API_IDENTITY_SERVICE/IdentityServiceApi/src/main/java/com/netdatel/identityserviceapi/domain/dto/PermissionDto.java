package com.netdatel.identityserviceapi.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PermissionDto {

    private Integer id;

    @NotBlank(message = "Permission code is required")
    @Size(max = 100, message = "Permission code cannot exceed 100 characters")
    private String code;

    @NotBlank(message = "Permission name is required")
    @Size(max = 100, message = "Permission name cannot exceed 100 characters")
    private String name;

    private String description;

    private String category;

    @NotBlank(message = "Service name is required")
    private String service;

    private boolean isActive;

    private LocalDateTime createdAt;
}