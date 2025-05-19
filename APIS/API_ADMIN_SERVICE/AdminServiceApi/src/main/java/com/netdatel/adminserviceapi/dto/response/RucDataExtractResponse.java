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
public class RucDataExtractResponse {
    private String ruc;
    private String businessName;
    private String commercialName;
    private String taxpayerType;
    private java.time.LocalDate activityStartDate;
    private String fiscalAddress;
    private String economicActivity;
}
