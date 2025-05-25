package com.netdatel.adminserviceapi.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.netdatel.adminserviceapi.entity.enums.ClientStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClientResponse {

    private Integer id;
    private String code;
    private String ruc;
    private String businessName;
    private String commercialName;
    private String taxpayerType;
    private LocalDate activityStartDate;
    private String fiscalAddress;
    private String economicActivity;
    private String contactNumber;
    private ClientStatus status;
    private Boolean notified;
    private Long allocatedStorage;
    private LocalDateTime registrationDate;
    private LocalDateTime lastUpdateDate;
    private String notes;
    private List<LegalRepresentativeResponse> legalRepresentatives;
    private List<ClientModuleResponse> modules;
    private List<ClientAdministratorResponse> administrators;
}
