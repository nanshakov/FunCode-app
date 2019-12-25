package com.nanshakov.finder.Integrations.impl;

import com.nanshakov.finder.Dto.Post;
import com.nanshakov.finder.Integrations.BaseIntegration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;

abstract class BaseIntegrationImpl implements BaseIntegration {

    @Autowired
    KafkaTemplate<String, Post> template;

    @Value("${spring.kafka.producer.topic}")
    String topic;

}
