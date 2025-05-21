package com.netdatel.documentserviceapi;


import com.netdatel.documentserviceapi.config.MinioProperties;
import com.netdatel.documentserviceapi.service.StorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@ConfigurationPropertiesScan("com.netdatel.documentserviceapi.config")
@EnableAsync
@Slf4j
public class DocumentServiceApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(DocumentServiceApiApplication.class, args);
    }

    @Bean
    public CommandLineRunner init(StorageService storageService,
                                  @Autowired(required = false) MinioProperties minioProperties) {
        return args -> {
            log.info("Initializing Document Service...");

            try {
                // Usar el nombre del bucket desde las propiedades si está disponible
                String bucketName = (minioProperties != null) ?
                        minioProperties.getBucketName() : "document-bucket";

                // Verificar y crear bucket de MinIO si no existe
                if (!storageService.bucketExists(bucketName)) {
                    log.info("Creating MinIO bucket: {}", bucketName);
                    storageService.createBucket(bucketName);
                }

                log.info("Document Service initialized successfully");
            } catch (Exception e) {
                log.error("Error initializing storage service: {}", e.getMessage(), e);
                // Dependiendo de la gravedad, podrías lanzar una RuntimeException
                // para detener la aplicación si es crítico
            }
        };
    }
}