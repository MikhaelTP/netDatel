package com.netdatel.adminserviceapi.service;

import com.netdatel.adminserviceapi.dto.response.RucDataExtractResponse;
import org.springframework.web.multipart.MultipartFile;

public interface RucFileService {
    /**
     * Procesa un archivo de ficha RUC para extraer información automáticamente
     * @param file Archivo PDF de ficha RUC
     * @return Datos extraídos del archivo
     */
    RucDataExtractResponse processRucFile(MultipartFile file);
}