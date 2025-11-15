package com.receipthub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ReceipthubApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReceipthubApplication.class, args);
	}

}
