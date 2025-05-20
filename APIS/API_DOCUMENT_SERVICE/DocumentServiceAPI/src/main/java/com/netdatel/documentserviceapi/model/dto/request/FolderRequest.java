package com.netdatel.documentserviceapi.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FolderRequest {
    private Integer ClientSpaceId;
    @NotBlank(message = "El nombre de la carpeta no puede estar vacío")
    @Size(max = 255, message = "El nombre no puede exceder los 255 caracteres")
    private String name;

    private String description;

    private Integer parentId;

    private Map<String, Object> attributes;
}