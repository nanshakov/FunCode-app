package com.nanshakov.finder;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class FinderApplication {

	public static void main(String[] args) {
		new SpringApplicationBuilder(FinderApplication.class)
				.web(WebApplicationType.NONE)
				.run(args);
	}

}
