package com.netdatel.adminserviceapi.service;

import com.netdatel.adminserviceapi.dto.response.ClientResponse;
import com.netdatel.adminserviceapi.dto.response.ClientSummaryResponse;
import com.netdatel.adminserviceapi.dto.response.ModuleDistributionResponse;
import com.netdatel.adminserviceapi.dto.response.StorageUsageResponse;

import java.util.List;

public interface DashboardService {
    /**
     * Obtiene resumen estadístico de clientes
     * @return Resumen de clientes
     */
    ClientSummaryResponse getClientSummary();

    /**
     * Obtiene distribución de módulos entre clientes
     * @return Estadísticas de distribución de módulos
     */
    List<ModuleDistributionResponse> getModuleDistribution();

    /**
     * Obtiene los clientes más recientes
     * @param limit Cantidad máxima de clientes a retornar
     * @return Lista de clientes recientes
     */
    List<ClientResponse> getRecentClients(int limit);

    /**
     * Obtiene estadísticas de uso de almacenamiento
     * @return Datos de uso de almacenamiento por cliente
     */
    /*
    List<StorageUsageResponse> getStorageUsage();
*/
}
