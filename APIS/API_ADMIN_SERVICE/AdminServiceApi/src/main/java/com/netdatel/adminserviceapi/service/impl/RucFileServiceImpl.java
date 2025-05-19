package com.netdatel.adminserviceapi.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.netdatel.adminserviceapi.dto.external.DocumentExtractRequest;
import com.netdatel.adminserviceapi.dto.external.DocumentExtractResponse;
import com.netdatel.adminserviceapi.dto.response.RucDataExtractResponse;
import com.netdatel.adminserviceapi.exception.ExternalServiceException;
import com.netdatel.adminserviceapi.exception.FileProcessingException;
import com.netdatel.adminserviceapi.service.RucFileService;
import com.netdatel.adminserviceapi.service.integration.DocumentProcessingClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class RucFileServiceImpl implements RucFileService {

    private final DocumentProcessingClient documentProcessingClient;

    @Override
    public RucDataExtractResponse processRucFile(MultipartFile file) {
        // Validar archivo
        if (file.isEmpty()) {
            throw new FileProcessingException("Archivo vacío");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new FileProcessingException("Formato de archivo no soportado. Debe ser PDF");
        }

        try {
            // Convertir archivo a Base64
            byte[] fileContent = file.getBytes();
            String base64Content = Base64.getEncoder().encodeToString(fileContent);

            // Crear solicitud para el servicio de procesamiento
            DocumentExtractRequest request = new DocumentExtractRequest();
            request.setFileContent(base64Content);
            request.setDocumentType("RUC");

            // Enviar al servicio de procesamiento
            DocumentExtractResponse response = documentProcessingClient.extractDataFromDocument(request);

            // Verificar resultado
            if (!response.getSuccess()) {
                throw new FileProcessingException("Error al procesar el documento: " + response.getMessage());
            }

            // Mapear respuesta
            return mapToRucDataResponse(response.getExtractedData());

        } catch (ExternalServiceException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error procesando archivo RUC: {}", e.getMessage(), e);
            throw new FileProcessingException("Error al procesar el archivo: " + e.getMessage(), e);
        }
    }

    private RucDataExtractResponse mapToRucDataResponse(JsonNode extractedData) {
        RucDataExtractResponse response = new RucDataExtractResponse();

        try {
            // Mapear datos obligatorios
            response.setRuc(extractedData.get("ruc").asText());
            response.setBusinessName(extractedData.get("businessName").asText());

            // Mapear datos opcionales
            if (extractedData.has("commercialName")) {
                response.setCommercialName(extractedData.get("commercialName").asText());
            }

            if (extractedData.has("taxpayerType")) {
                response.setTaxpayerType(extractedData.get("taxpayerType").asText());
            }

            if (extractedData.has("activityStartDate")) {
                String dateStr = extractedData.get("activityStartDate").asText();
                try {
                    response.setActivityStartDate(LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE));
                } catch (DateTimeException e) {
                    log.warn("Formato de fecha incorrecto: {}", dateStr);
                }
            }

            if (extractedData.has("fiscalAddress")) {
                response.setFiscalAddress(extractedData.get("fiscalAddress").asText());
            }

            if (extractedData.has("economicActivity")) {
                response.setEconomicActivity(extractedData.get("economicActivity").asText());
            }

            return response;

        } catch (Exception e) {
            log.error("Error mapeando datos extraídos: {}", e.getMessage(), e);
            throw new FileProcessingException("Error procesando datos extraídos: " + e.getMessage(), e);
        }
    }
}