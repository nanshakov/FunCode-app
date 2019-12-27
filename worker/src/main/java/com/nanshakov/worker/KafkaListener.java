package com.nanshakov.worker;

import com.nanshakov.dto.Post;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class KafkaListener {

    @Value("${spring.kafka.consumer.group.id}")
    private String kafkaGroupId;

    @org.springframework.kafka.annotation.KafkaListener(topics = "${spring.kafka.consumer.topic}")
    public void consume(ConsumerRecord<String, Post> rawMessage) {
        log.info("=> consumed {}", rawMessage.value());
    }
}
