package com.nanshakov.finder.Integrations.impl;

import com.nanshakov.finder.Dto.Post;
import com.nanshakov.finder.Integrations.BaseIntegration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class FaceBookService implements BaseIntegration {

    @Autowired
    private KafkaTemplate<String, Post> template;

    @PostConstruct
    void postConstruct() {
        template.send("topic1", getNext());
    }

    @Override
    public void start() {

    }

    @Override
    public Post getNext() {
        return new Post("test");
    }
}
