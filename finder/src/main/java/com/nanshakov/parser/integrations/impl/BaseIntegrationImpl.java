package com.nanshakov.parser.integrations.impl;

import com.nanshakov.common.Utils;
import com.nanshakov.common.dto.Post;
import com.nanshakov.configuration.Status;
import com.nanshakov.parser.integrations.BaseIntegration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;

import lombok.extern.log4j.Log4j2;

@Log4j2
public abstract class BaseIntegrationImpl implements BaseIntegration {

    @Value("${type}")
    String type;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private KafkaTemplate<String, Post> kafkaTemplate;
    @Value("${spring.kafka.topic}")
    private String topic;
    @Autowired
    private ApplicationContext ctx;
    static final io.prometheus.client.Counter total = io.prometheus.client.Counter.build()
            .name("parse_total").help("Total parsed.").register();
    static final io.prometheus.client.Counter errors = io.prometheus.client.Counter.build()
            .name("error_total").help("Total errors.").register();

    @SuppressWarnings("ConstantConditions")
    public boolean exist(String hash) {
        return !redisTemplate.opsForValue().setIfAbsent(hash, Status.ACCEPTED);
    }

    public void close() {
        log.error("Shit happens, exit");
        ((ConfigurableApplicationContext) ctx).close();
    }

    void sendToKafka(String hash, Post p) {
        kafkaTemplate.send(topic, hash, p);
    }

    public String calculateHash(Object o) {
        return Utils.calculateHashSha1(o);
    }


}
