package com.nanshakov.finder.Integrations.impl;

import com.nanshakov.finder.Dto.Post;
import com.nanshakov.finder.Integrations.BaseIntegration;

import org.apache.kafka.common.protocol.types.Field;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class FaceBookService implements BaseIntegration {

    @Autowired
    private KafkaTemplate<String, Post> template;

    @Value("${spring.kafka.producer.topic}")
    private String topic;

    @PostConstruct
    void postConstruct() {
        template.send(topic, getNext());
    }

    @Override
    public void start() {

    }

    @Override
    public Post getNext() {
        return new Post("test");
    }
}
