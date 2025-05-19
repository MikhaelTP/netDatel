package com.netdatel.adminserviceapi.service.impl;


import com.netdatel.adminserviceapi.dto.request.ModuleRequest;
import com.netdatel.adminserviceapi.dto.response.ModuleResponse;
import com.netdatel.adminserviceapi.entity.Module;
import com.netdatel.adminserviceapi.exception.DuplicateResourceException;
import com.netdatel.adminserviceapi.exception.ResourceNotFoundException;
import com.netdatel.adminserviceapi.mapper.ModuleMapper;
import com.netdatel.adminserviceapi.repository.ModuleRepository;
import com.netdatel.adminserviceapi.service.ModuleService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ModuleServiceImpl implements ModuleService {

    private final ModuleRepository moduleRepository;
    private final ModuleMapper moduleMapper;

    @Override
    @Transactional
    public ModuleResponse createModule(ModuleRequest request, Integer userId) {
        // Verificar si ya existe un módulo con el mismo código
        if (moduleRepository.findByCode(request.getCode()).isPresent()) {
            throw new DuplicateResourceException("Módulo", "código", request.getCode());
        }

        Module module = moduleMapper.toEntity(request);
        Module savedModule = moduleRepository.save(module);

        return moduleMapper.toDto(savedModule);
    }

    @Override
    public ModuleResponse getModuleById(Integer id) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Módulo", "id", id));

        return moduleMapper.toDto(module);
    }

    @Override
    public ModuleResponse getModuleByCode(String code) {
        Module module = moduleRepository.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Módulo", "código", code));

        return moduleMapper.toDto(module);
    }

    @Override
    public List<ModuleResponse> getAllModules() {
        List<Module> modules = moduleRepository.findAll();
        return moduleMapper.toDtoList(modules);
    }

    @Override
    public List<ModuleResponse> getActiveModules() {
        List<Module> activeModules = moduleRepository.findByIsActiveTrue();
        return moduleMapper.toDtoList(activeModules);
    }

    @Override
    @Transactional
    public ModuleResponse updateModule(Integer id, ModuleRequest request, Integer userId) {
        Module existingModule = moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Módulo", "id", id));

        // Verificar si el código ha cambiado y si el nuevo código ya existe
        if (!existingModule.getCode().equals(request.getCode()) &&
                moduleRepository.findByCode(request.getCode()).isPresent()) {
            throw new DuplicateResourceException("Módulo", "código", request.getCode());
        }

        moduleMapper.updateModuleFromDto(request, existingModule);
        existingModule.setLastUpdate(LocalDateTime.now());

        Module updatedModule = moduleRepository.save(existingModule);

        return moduleMapper.toDto(updatedModule);
    }

    @Override
    @Transactional
    public void toggleModuleActive(Integer id, boolean active, Integer userId) {
        Module module = moduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Módulo", "id", id));

        module.setIsActive(active);
        module.setLastUpdate(LocalDateTime.now());

        moduleRepository.save(module);
    }
}