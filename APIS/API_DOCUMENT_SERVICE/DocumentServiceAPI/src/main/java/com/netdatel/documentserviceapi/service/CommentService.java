package com.netdatel.documentserviceapi.service;

import com.netdatel.documentserviceapi.model.dto.request.CommentRequest;
import com.netdatel.documentserviceapi.model.entity.FileComment;

import java.util.List;

public interface CommentService {
    FileComment createComment(CommentRequest request, Integer userId);
    List<FileComment> getFileComments(Integer fileId);
    FileComment getComment(Integer id);
    FileComment updateComment(Integer id, String newText);
    void deleteComment(Integer id);
}
