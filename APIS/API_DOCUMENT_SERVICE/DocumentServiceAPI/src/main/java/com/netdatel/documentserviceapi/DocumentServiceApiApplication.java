package com.netdatel.documentserviceapi;


import com.netdatel.documentserviceapi.service.StorageService;
import lombok.extern.slf4j.Slf4j;
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
    public CommandLineRunner init(StorageService storageService) {
        return args -> {
            log.info("Initializing Document Service...");

            // Verificar y crear bucket de MinIO si no existe
            if (!storageService.bucketExists("document-bucket")) {
                log.info("Creating MinIO bucket: document-bucket");
                storageService.createBucket("document-bucket");
            }

            log.info("Document Service initialized successfully");
        };
    }
}