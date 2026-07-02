package com.vigilant.vigilant_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import org.springframework.data.web.config.EnableSpringDataWebSupport;

@SpringBootApplication
@EnableScheduling
@EnableSpringDataWebSupport(pageSerializationMode = EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO)
public class VigilantBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(VigilantBackendApplication.class, args);
	}

}
