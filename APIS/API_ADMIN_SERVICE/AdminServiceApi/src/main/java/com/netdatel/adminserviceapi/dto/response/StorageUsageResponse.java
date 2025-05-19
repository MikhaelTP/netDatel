package com.netdatel.adminserviceapi.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StorageUsageResponse {
    private Integer clientId;
    private String clientName;
    private Integer allocatedStorageBytes;
    private Integer usedStorageBytes;
    private Double usagePercent;
}
