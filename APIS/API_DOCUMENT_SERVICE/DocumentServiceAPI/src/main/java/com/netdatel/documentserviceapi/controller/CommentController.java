package com.netdatel.documentserviceapi.controller;

import com.netdatel.documentserviceapi.exception.PermissionDeniedException;
import com.netdatel.documentserviceapi.model.dto.request.CommentRequest;
import com.netdatel.documentserviceapi.model.dto.request.CommentUpdateRequest;
import com.netdatel.documentserviceapi.model.dto.response.ApiResponse;
import com.netdatel.documentserviceapi.model.dto.response.CommentResponse;
import com.netdatel.documentserviceapi.model.entity.FileComment;
import com.netdatel.documentserviceapi.security.CurrentUserId;
import com.netdatel.documentserviceapi.service.CommentService;
import com.netdatel.documentserviceapi.service.PermissionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@Tag(name = "Comments", description = "API para gestionar comentarios en archivos")
public class CommentController {
    private final CommentService commentService;
    private final PermissionService permissionService;

    @PostMapping
    @Operation(summary = "Crear comentario", description = "Agrega un comentario a un archivo")
    public ResponseEntity<ApiResponse<CommentResponse>> createComment(
            @Valid @RequestBody CommentRequest request,
            @CurrentUserId Integer userId) {

        // Verificar permiso de lectura en el archivo
        if (!permissionService.userCanReadFile(userId, request.getFileId())) {
            throw new PermissionDeniedException("No tienes permiso para comentar en este archivo");
        }

        FileComment comment = commentService.createComment(request, userId);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Comentario creado exitosamente", mapToResponse(comment)));
    }

    @GetMapping("/file/{fileId}")
    @Operation(summary = "Listar comentarios", description = "Lista todos los comentarios de un archivo")
    public ResponseEntity<ApiResponse<List<CommentResponse>>> getFileComments(
            @PathVariable Integer fileId,
            @CurrentUserId Integer userId) {

        // Verificar permiso de lectura en el archivo
        if (!permissionService.userCanReadFile(userId, fileId)) {
            throw new PermissionDeniedException("No tienes permiso para ver comentarios en este archivo");
        }

        List<FileComment> comments = commentService.getFileComments(fileId);

        return ResponseEntity.ok(ApiResponse.success(
                comments.stream().map(this::mapToResponse).collect(Collectors.toList())));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar comentario", description = "Actualiza el texto de un comentario existente")
    public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
            @PathVariable Integer id,
            @Valid @RequestBody CommentUpdateRequest request,
            @CurrentUserId Integer userId) {

        FileComment comment = commentService.getComment(id);

        // Verificar que el comentario sea del usuario o sea administrador
        if (!comment.getUserId().equals(userId)) {
            throw new PermissionDeniedException("No tienes permiso para modificar este comentario");
        }

        FileComment updatedComment = commentService.updateComment(id, request.getCommentText());

        return ResponseEntity.ok(ApiResponse.success("Comentario actualizado exitosamente", mapToResponse(updatedComment)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar comentario", description = "Marca un comentario como inactivo (eliminación lógica)")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @PathVariable Integer id,
            @CurrentUserId Integer userId) {

        FileComment comment = commentService.getComment(id);

        // Verificar que el comentario sea del usuario o sea administrador
        if (!comment.getUserId().equals(userId)) {
            throw new PermissionDeniedException("No tienes permiso para eliminar este comentario");
        }

        commentService.deleteComment(id);

        return ResponseEntity.ok(ApiResponse.success("Comentario eliminado exitosamente", null));
    }

    private CommentResponse mapToResponse(FileComment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .fileId(comment.getFile().getId())
                .userId(comment.getUserId())
                .commentText(comment.getCommentText())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .parentCommentId(comment.getParentComment() != null ? comment.getParentComment().getId() : null)
                .isActive(comment.isActive())
                .build();
    }
}
