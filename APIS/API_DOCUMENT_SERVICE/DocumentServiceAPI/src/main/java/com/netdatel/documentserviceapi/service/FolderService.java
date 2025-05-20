package com.netdatel.documentserviceapi.service;

import com.netdatel.documentserviceapi.model.dto.request.FolderRequest;
import com.netdatel.documentserviceapi.model.entity.Folder;

import java.util.List;

public interface FolderService {
    Folder createFolder(FolderRequest request, Integer userId);
    Folder getFolder(Integer id);
    List<Folder> getRootFolders(Integer clientSpaceId);
    List<Folder> getSubfolders(Integer parentId);
    Folder updateFolder(Integer id, FolderRequest request, Integer userId);
    void deleteFolder(Integer id);
    List<Folder> searchFolders(Integer clientSpaceId, String query);
}