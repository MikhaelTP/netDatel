package com.netdatel.adminserviceapi.dto.external;

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
public class StorageInitializeRequest {
    private Integer clientId;
    private Integer moduleId;
    private Long storageLimitBytes;
    private String adminEmail;
}
