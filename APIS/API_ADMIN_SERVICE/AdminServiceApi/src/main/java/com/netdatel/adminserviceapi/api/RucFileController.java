package com.netdatel.adminserviceapi.api;

import com.netdatel.adminserviceapi.dto.response.RucDataExtractResponse;
import com.netdatel.adminserviceapi.service.RucFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/ruc")
@RequiredArgsConstructor
public class RucFileController {

    private final RucFileService rucFileService;

    @PostMapping("/process")
    @PreAuthorize("hasAuthority('admin:client:create') or hasAuthority('admin:client:update')")
    public RucDataExtractResponse processRucFile(
            @RequestParam("file") MultipartFile file) {
        return rucFileService.processRucFile(file);
    }
}