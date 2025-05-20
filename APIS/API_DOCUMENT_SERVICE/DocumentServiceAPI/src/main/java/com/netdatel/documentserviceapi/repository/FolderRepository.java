package com.netdatel.documentserviceapi.repository;

import com.netdatel.documentserviceapi.model.entity.Folder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FolderRepository extends JpaRepository<Folder, Integer> {
    List<Folder> findByClientSpaceIdAndParentIsNull(Integer clientSpaceId);
    List<Folder> findByParentId(Integer parentId);
    List<Folder> findByClientSpaceIdAndPathContaining(Integer clientSpaceId, String pathFragment);
    Optional<Folder> findByClientSpaceIdAndParentIdAndName(Integer clientSpaceId, Integer parentId, String name);
    boolean existsByClientSpaceIdAndParentIdAndName(Integer clientSpaceId, Integer parentId, String name);
}