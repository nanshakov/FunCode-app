package com.nanshakov.finder.integrations;

import com.nanshakov.dto.Platform;
import com.nanshakov.finder.dto.Post;
import com.nanshakov.finder.integrations.impl.Ifunny;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
@RequiredArgsConstructor
public class Runner {

    private final ApplicationContext ctx;
    private final KafkaTemplate<String, Post> kafkaTemplate;
    @Value("${spring.kafka.producer.topic}")
    private String topic;
    @Value("${type}")
    private Platform type;
    @Value("${tag}")
    private String tag;

    @PostConstruct
    void postConstruct() {
        BaseIntegration integration = null;
        if (type == Platform.IFUNNY) {
            integration = new Ifunny(ctx, kafkaTemplate, topic, tag);
        }
        if (integration != null) {
            integration.start();
        } else {
            throw new IllegalStateException("Не удалось найти реализацию для типа " + type);
        }
    }
}
