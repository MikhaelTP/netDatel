package com.netdatel.adminserviceapi.mapper;

import com.netdatel.adminserviceapi.dto.request.LegalRepresentativeRequest;
import com.netdatel.adminserviceapi.dto.response.LegalRepresentativeResponse;
import com.netdatel.adminserviceapi.entity.LegalRepresentative;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface LegalRepresentativeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    LegalRepresentative toEntity(LegalRepresentativeRequest request);

    LegalRepresentativeResponse toDto(LegalRepresentative representative);

    List<LegalRepresentativeResponse> toDtoList(List<LegalRepresentative> representatives);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateLegalRepresentativeFromDto(LegalRepresentativeRequest request, @MappingTarget LegalRepresentative representative);
}
