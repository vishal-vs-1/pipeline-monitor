package com.vigilant.vigilant_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class VigilantBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(VigilantBackendApplication.class, args);
	}

}
