package com.netdatel.documentserviceapi.service;

import com.netdatel.documentserviceapi.model.dto.request.BatchDownloadRequest;
import com.netdatel.documentserviceapi.model.entity.BatchDownload;

import java.util.List;

public interface BatchDownloadService {
    BatchDownload startBatchDownload(BatchDownloadRequest request, Integer userId);
    BatchDownload getBatchDownload(Integer id, Integer userId);
    List<BatchDownload> getUserBatchDownloads(Integer userId);
}

