package com.netdatel.documentserviceapi.model.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchDownloadRequest {
    @NotNull(message = "El ID de la carpeta no puede ser nulo")
    private Integer folderId;

    private boolean includeSubfolders = true;
}