package com.nanshakov.worker.configuration;

import com.nanshakov.dto.Post;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@org.springframework.context.annotation.Configuration
@EnableKafka
@Import(Jedis.class)
public class Configuration {

    @Value("${spring.kafka.consumer.bootstrap.servers}")
    private String kafkaServer;
    @Value("${spring.kafka.consumer.group.id}")
    private String kafkaGroupId;
    @Value("${spring.kafka.consumer.topic}")
    private String topic;

    @Bean
    KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, Post>>
    kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Post> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory());
        //factory.setConcurrency(3);
        factory.getContainerProperties().setPollTimeout(3000);
        return factory;
    }

    @Bean
    public ConsumerFactory<String, Post> consumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerConfigs());
    }

    @Bean
    public Map<String, Object> consumerConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaGroupId);
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, true);
        return props;
    }

}
