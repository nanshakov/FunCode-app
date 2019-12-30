package com.nanshakov.parser.integrations.impl;

import com.nanshakov.common.dto.Post;
import com.nanshakov.configuration.Status;
import com.nanshakov.parser.integrations.BaseIntegration;
import com.nanshakov.parser.repo.PostMetaRepository;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;

import java.io.Serializable;

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
    @Autowired
    private PostMetaRepository postMetaRepository;

    @SuppressWarnings("ConstantConditions")
    public boolean exist(String hash) {
        if (!redisTemplate.opsForValue().setIfAbsent(hash, Status.ACCEPTED)) {
            return true;
        } else {
            return postMetaRepository.contains(hash);
        }
    }

    public void close() {
        log.error("Shit happens, exit");
        ((ConfigurableApplicationContext) ctx).close();
    }

    void sendToKafka(String hash, Post p) {
        kafkaTemplate.send(topic, hash, p);
    }

    public String calculateHash(Object o) {
        return DigestUtils.sha1Hex(DigestUtils.sha1(SerializationUtils.serialize((Serializable) o)));
    }


}
