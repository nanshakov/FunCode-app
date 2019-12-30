package com.nanshakov;


import com.nanshakov.parser.integrations.BaseIntegration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

import java.util.List;

@SpringBootApplication
public class FinderApplication implements CommandLineRunner {

    @Autowired
    private List<BaseIntegration> integrations;

    public static void main(String[] args) {
        new SpringApplicationBuilder(FinderApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

    @Override
    public void run(String... args) throws Exception {
        for (BaseIntegration integration : integrations) {
            integration.start();
        }
    }
}
