package com.netdatel.documentserviceapi.service.impl;

import com.netdatel.documentserviceapi.exception.InvalidRequestException;
import com.netdatel.documentserviceapi.exception.ResourceNotFoundException;
import com.netdatel.documentserviceapi.model.dto.request.CommentRequest;
import com.netdatel.documentserviceapi.model.entity.File;
import com.netdatel.documentserviceapi.model.entity.FileComment;
import com.netdatel.documentserviceapi.repository.FileCommentRepository;
import com.netdatel.documentserviceapi.repository.FileRepository;
import com.netdatel.documentserviceapi.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final FileCommentRepository fileCommentRepository;
    private final FileRepository fileRepository;

    @Override
    public FileComment createComment(CommentRequest request, Integer userId) {
        log.info("Creating comment for file: {} by user: {}", request.getFileId(), userId);

        File file = fileRepository.findById(request.getFileId())
                .orElseThrow(() -> new ResourceNotFoundException("Archivo no encontrado"));

        FileComment parentComment = null;
        if (request.getParentCommentId() != null) {
            parentComment = fileCommentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Comentario padre no encontrado"));

            // Verificar que el comentario padre pertenezca al mismo archivo
            if (!parentComment.getFile().getId().equals(file.getId())) {
                throw new InvalidRequestException("El comentario padre no pertenece al archivo especificado");
            }
        }

        FileComment comment = FileComment.builder()
                .file(file)
                .userId(userId)
                .commentText(request.getCommentText())
                .parentComment(parentComment)
                .isActive(true)
                .build();

        return fileCommentRepository.save(comment);
    }

    @Override
    public List<FileComment> getFileComments(Integer fileId) {
        log.info("Getting comments for file: {}", fileId);

        if (!fileRepository.existsById(fileId)) {
            throw new ResourceNotFoundException("Archivo no encontrado");
        }

        return fileCommentRepository.findByFileIdAndIsActiveOrderByCreatedAtAsc(fileId, true);
    }

    @Override
    public FileComment getComment(Integer id) {
        return fileCommentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comentario no encontrado"));
    }

    @Override
    public FileComment updateComment(Integer id, String newText) {
        log.info("Updating comment: {}", id);

        FileComment comment = getComment(id);
        comment.setCommentText(newText);
        comment.setUpdatedAt(LocalDateTime.now());

        return fileCommentRepository.save(comment);
    }

    @Override
    public void deleteComment(Integer id) {
        log.info("Deleting comment: {}", id);

        FileComment comment = getComment(id);
        comment.setActive(false);

        fileCommentRepository.save(comment);
    }
}