package com.nanshakov.worker;

import com.nanshakov.dto.Post;
import com.nanshakov.worker.dto.Status;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
@RequiredArgsConstructor
public class KafkaListener {

    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${spring.kafka.consumer.group.id}")
    private String kafkaGroupId;

    @SuppressWarnings("ConstantConditions")
    @org.springframework.kafka.annotation.KafkaListener(topics = "${spring.kafka.consumer.topic}")
    public void consume(ConsumerRecord<String, Post> rawMessage) {
        log.trace("=> consumed {}", rawMessage.value());
        if (!redisTemplate.opsForValue().setIfAbsent(rawMessage.key(), Status.ACCEPTED)) {
            log.trace("{} found in redis, do nothing", rawMessage.key());
            return;
        }
        log.trace("Processing {}", rawMessage.key());

    }

}
