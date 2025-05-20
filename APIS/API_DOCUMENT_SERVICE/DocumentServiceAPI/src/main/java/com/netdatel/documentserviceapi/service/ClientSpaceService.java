package com.netdatel.documentserviceapi.service;

import com.netdatel.documentserviceapi.model.dto.request.ClientSpaceRequest;
import com.netdatel.documentserviceapi.model.entity.ClientSpace;

import java.util.List;

public interface ClientSpaceService {
    ClientSpace createClientSpace(ClientSpaceRequest request, Integer userId);
    ClientSpace getClientSpace(Integer id);
    ClientSpace getClientSpaceByClientAndModule(Integer clientId, Integer moduleId);
    List<ClientSpace> getClientSpacesByClient(Integer clientId);
    ClientSpace updateClientSpace(Integer id, ClientSpaceRequest request, Integer userId);
    void deleteClientSpace(Integer id);
    void updateUsedBytes(Integer id, Long usedBytes);
}