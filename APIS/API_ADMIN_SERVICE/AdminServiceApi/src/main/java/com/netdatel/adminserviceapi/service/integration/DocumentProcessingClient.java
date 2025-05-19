package com.netdatel.adminserviceapi.service.integration;

import com.netdatel.adminserviceapi.dto.external.DocumentExtractRequest;
import com.netdatel.adminserviceapi.dto.external.DocumentExtractResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "document-processing", url = "${app.services.document-processing-url}")
public interface DocumentProcessingClient {

    @PostMapping("/api/documents/extract")
    DocumentExtractResponse extractDataFromDocument(
            @RequestBody DocumentExtractRequest request);
}