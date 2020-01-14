package com.nanshakov;


import com.nanshakov.lib.src.cue.lang.stop.StopWords;
import com.nanshakov.lib.src.cue.lang.unicode.BlockUtil;
import com.nanshakov.parser.integrations.BaseIntegration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.core.task.TaskExecutor;

import java.util.List;

@SpringBootApplication
public class FinderApplication implements CommandLineRunner {

    @Autowired
    private List<BaseIntegration> integrations;
    @Autowired
    private TaskExecutor taskExecutor;

    public static void main(String[] args) {
        new SpringApplicationBuilder(FinderApplication.class)
                .web(WebApplicationType.SERVLET)
                .run(args);
    }

    @Override
    public void run(String... args) throws Exception {
        System.out.println(BlockUtil.guessUnicodeBlock("Fremd im eigenen land"));
        System.out.println(StopWords.German.isStopWord("im"));

        for (BaseIntegration integration : integrations) {
            taskExecutor.execute(integration);
        }
    }
}
