package com.netdatel.adminserviceapi.mapper;

import com.netdatel.adminserviceapi.dto.request.ModuleRequest;
import com.netdatel.adminserviceapi.dto.response.ModuleResponse;
import com.netdatel.adminserviceapi.entity.Module;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ModuleMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "lastUpdate", ignore = true)
    Module toEntity(ModuleRequest request);

    ModuleResponse toDto(Module module);

    List<ModuleResponse> toDtoList(List<Module> modules);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateModuleFromDto(ModuleRequest request, @MappingTarget Module module);
}
