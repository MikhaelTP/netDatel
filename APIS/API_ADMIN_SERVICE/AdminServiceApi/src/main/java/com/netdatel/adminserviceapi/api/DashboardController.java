package com.netdatel.adminserviceapi.api;

import com.netdatel.adminserviceapi.dto.response.ClientResponse;
import com.netdatel.adminserviceapi.dto.response.ClientSummaryResponse;
import com.netdatel.adminserviceapi.dto.response.ModuleDistributionResponse;
import com.netdatel.adminserviceapi.dto.response.StorageUsageResponse;
import com.netdatel.adminserviceapi.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/clients/summary")
    @PreAuthorize("hasAuthority('admin:dashboard:read')")
    public ClientSummaryResponse getClientSummary() {
        return dashboardService.getClientSummary();
    }

    @GetMapping("/modules/distribution")
    @PreAuthorize("hasAuthority('admin:dashboard:read')")
    public List<ModuleDistributionResponse> getModuleDistribution() {
        return dashboardService.getModuleDistribution();
    }

    @GetMapping("/clients/recent")
    @PreAuthorize("hasAuthority('admin:dashboard:read')")
    public List<ClientResponse> getRecentClients(
            @RequestParam(defaultValue = "5") int limit) {
        return dashboardService.getRecentClients(limit);
    }

    /*
    @GetMapping("/storage/usage")
    @PreAuthorize("hasAuthority('admin:dashboard:read')")
    public List<StorageUsageResponse> getStorageUsage() {
        return dashboardService.getStorageUsage();
    }

     */
}