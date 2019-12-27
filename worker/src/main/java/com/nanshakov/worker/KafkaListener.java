package com.nanshakov.worker;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nanshakov.dto.Post;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.utils.Bytes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
@RequiredArgsConstructor
public class KafkaListener {

    private final ObjectMapper objectMapper;
    @Value("${kafka.group.id}")
    private final String kafkaGroupId;

    @org.springframework.kafka.annotation.KafkaListener(topics = "${kafka.topic}")
    public void consume(
            @Header(value = KafkaHeaders.RECEIVED_MESSAGE_KEY, required = false) Bytes key,
            ConsumerRecord<String, Post> rawMessage) {
        log.info("=> consumed {}", rawMessage.value());
    }
}
