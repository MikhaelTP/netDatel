package com.netdatel.adminserviceapi.service.integration;

import com.netdatel.adminserviceapi.dto.external.StorageInitializeRequest;
import com.netdatel.adminserviceapi.dto.external.StorageInitializeResponse;
import com.netdatel.adminserviceapi.dto.external.StorageLimitUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "document-service", url = "${app.services.document-url}")
public interface DocumentServiceClient {

    @PostMapping("/api/storage/initialize")
    StorageInitializeResponse initializeStorage(
            @RequestBody StorageInitializeRequest request);

    @PutMapping("/api/storage/update-limit")
    void updateStorageLimit(@RequestBody StorageLimitUpdateRequest request);

    @GetMapping("/api/storage/usage/{clientId}")
    Integer getStorageUsage(@PathVariable Integer clientId);
}