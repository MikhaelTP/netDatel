package com.netdatel.documentserviceapi.repository;

import com.netdatel.documentserviceapi.model.entity.FileComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileCommentRepository extends JpaRepository<FileComment, Integer> {
    List<FileComment> findByFileIdAndIsActiveOrderByCreatedAtAsc(Integer fileId, boolean isActive);
    List<FileComment> findByParentCommentIdAndIsActiveOrderByCreatedAtAsc(Integer parentCommentId, boolean isActive);
}
