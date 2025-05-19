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
public class StorageInitializeResponse {
    private Integer clientSpaceId;
    private String storagePath;
    private Boolean success;
    private String message;
}