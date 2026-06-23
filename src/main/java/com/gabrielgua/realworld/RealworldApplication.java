package com.gabrielgua.realworld;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RealworldApplication {

	public static void main(String[] args) {
		SpringApplication.run(RealworldApplication.class, args);
	}

}
