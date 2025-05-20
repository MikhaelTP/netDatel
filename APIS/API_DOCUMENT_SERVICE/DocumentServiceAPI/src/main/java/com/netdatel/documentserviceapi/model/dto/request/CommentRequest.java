package com.netdatel.documentserviceapi.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentRequest {
    @NotNull(message = "El ID del archivo no puede ser nulo")
    private Integer fileId;

    @NotBlank(message = "El texto del comentario no puede estar vacío")
    private String commentText;

    private Integer parentCommentId;
}