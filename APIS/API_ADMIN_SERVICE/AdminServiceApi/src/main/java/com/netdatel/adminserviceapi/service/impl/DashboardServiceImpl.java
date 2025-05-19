package com.netdatel.adminserviceapi.service.impl;

import com.netdatel.adminserviceapi.dto.response.ClientResponse;
import com.netdatel.adminserviceapi.dto.response.ClientSummaryResponse;
import com.netdatel.adminserviceapi.dto.response.ModuleDistributionResponse;
import com.netdatel.adminserviceapi.dto.response.StorageUsageResponse;
import com.netdatel.adminserviceapi.entity.Client;
import com.netdatel.adminserviceapi.entity.Module;
import com.netdatel.adminserviceapi.entity.enums.ClientStatus;
import com.netdatel.adminserviceapi.entity.enums.ModuleStatus;
import com.netdatel.adminserviceapi.mapper.ClientMapper;
import com.netdatel.adminserviceapi.repository.ClientModuleRepository;
import com.netdatel.adminserviceapi.repository.ClientRepository;
import com.netdatel.adminserviceapi.repository.ModuleRepository;
import com.netdatel.adminserviceapi.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ClientRepository clientRepository;
    private final ModuleRepository moduleRepository;
    private final ClientModuleRepository clientModuleRepository;
    private final ClientMapper clientMapper;

    @Override
    public ClientSummaryResponse getClientSummary() {
        ClientSummaryResponse summary = new ClientSummaryResponse();

        Integer totalClients = (int) clientRepository.count();
        Integer activeClients = clientRepository.countByStatus(ClientStatus.ACTIVE);
        Integer inactiveClients = clientRepository.countByStatus(ClientStatus.INACTIVE);
        Integer suspendedClients = clientRepository.countByStatus(ClientStatus.SUSPENDED);

        summary.setTotalClients(totalClients);
        summary.setActiveClients(activeClients);
        summary.setInactiveClients(inactiveClients);
        summary.setSuspendedClients(suspendedClients);

        return summary;
    }

    @Override
    public List<ModuleDistributionResponse> getModuleDistribution() {
        List<Module> modules = moduleRepository.findAll();
        List<ModuleDistributionResponse> distribution = new ArrayList<>();

        for (Module module : modules) {
            ModuleDistributionResponse entry = new ModuleDistributionResponse();
            entry.setModuleId(module.getId());
            entry.setModuleName(module.getName());
            entry.setModuleCode(module.getCode());

            long activeSubscriptions = clientModuleRepository.countByModuleIdAndStatus(
                    module.getId(), ModuleStatus.ACTIVE);

            entry.setActiveSubscriptions(activeSubscriptions);
            distribution.add(entry);
        }

        return distribution;
    }

    @Override
    public List<ClientResponse> getRecentClients(int limit) {
        List<Client> recentClients = clientRepository.findTop10ByOrderByRegistrationDateDesc();

        // Si se solicita un límite menor, aplicar el límite
        if (limit > 0 && limit < recentClients.size()) {
            recentClients = recentClients.subList(0, limit);
        }

        return clientMapper.toDtoList(recentClients);
    }

    /*

    @Override
    public List<StorageUsageResponse> getStorageUsage() {
        List<Object[]> results = clientRepository.getStorageUsageByClient();
        List<StorageUsageResponse> storageUsage = new ArrayList<>();

        for (Object[] result : results) {
            StorageUsageResponse entry = new StorageUsageResponse();
            entry.setClientId((int) ((Number) result[0]).longValue());
            entry.setClientName((String) result[1]);
            entry.setAllocatedStorageBytes((int) ((Number) result[2]).longValue());
            entry.setUsedStorageBytes((int) ((Number) result[3]).longValue());

            // Calcular porcentaje de uso
            double usagePercent = entry.getAllocatedStorageBytes() > 0
                    ? (double) entry.getUsedStorageBytes() / entry.getAllocatedStorageBytes() * 100
                    : 0;
            entry.setUsagePercent(Math.round(usagePercent * 100) / 100.0); // Redondear a 2 decimales

            storageUsage.add(entry);
        }

        return storageUsage;
    }

     */
}