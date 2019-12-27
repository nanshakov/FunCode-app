package com.nanshakov.worker;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class WorkerApplication implements CommandLineRunner {

	public static void main(String[] args) {
		new SpringApplicationBuilder(WorkerApplication.class)
				.web(WebApplicationType.NONE)
				.run(args);
	}

	@Override
	public void run(String... args) throws Exception {

	}
}
