package com.netdatel.adminserviceapi.service.integration;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "provider-service", url = "${app.services.provider-url}")
@Service
public interface ProviderServiceClient {

    @PostMapping("/api/provider/initialize")
    void initializeProviderService(
            @RequestBody ProviderInitializeRequest  request);

    @PutMapping("/api/provider/clients/{clientId}/disable")
    void disableClientInProviderService(@PathVariable Integer clientId);
}