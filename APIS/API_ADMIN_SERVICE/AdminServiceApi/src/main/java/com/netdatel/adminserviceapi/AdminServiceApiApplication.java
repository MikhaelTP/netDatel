package com.netdatel.adminserviceapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EntityScan(basePackages = "com.netdatel.adminserviceapi.entity")
@EnableFeignClients
public class AdminServiceApiApplication {

	@Bean
	public RestTemplate restTemplate() {
		return new RestTemplate();
	}
	public static void main(String[] args) {
		SpringApplication.run(AdminServiceApiApplication.class, args);
	}

}
