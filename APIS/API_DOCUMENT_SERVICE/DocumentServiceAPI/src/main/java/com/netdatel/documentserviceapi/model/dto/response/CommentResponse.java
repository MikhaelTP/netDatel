package com.netdatel.documentserviceapi.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponse {
    private Integer id;
    private Integer fileId;
    private Integer userId;
    private String commentText;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer parentCommentId;
    private boolean isActive;
}