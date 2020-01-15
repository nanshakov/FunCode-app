package com.nanshakov.parser.integrations.impl;

import com.nanshakov.common.TagsService;
import com.nanshakov.common.Utils;
import com.nanshakov.common.dto.PostDto;
import com.nanshakov.configuration.Status;
import com.nanshakov.parser.integrations.BaseIntegration;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

import javax.validation.constraints.Null;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import lombok.extern.log4j.Log4j2;

@Log4j2
public abstract class BaseIntegrationImpl implements BaseIntegration {

    @Value("${type}")
    String type;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired TagsService tagsService;
    @Value("${spring.kafka.topic}")
    private String topic;
    @Autowired
    private ApplicationContext ctx;
    Counter duplicates = Metrics.counter("parse.duplicates", "parse", "duplicates");

    Counter total = Metrics.counter("parse.total", "parse", "total");
    Counter errors = Metrics.counter("parse.error", "parse", "error");
    Counter drop = Metrics.counter("parse.drop", "parse", "drop");
    @Autowired
    private KafkaTemplate<String, PostDto> kafkaTemplate;

    @SuppressWarnings("ConstantConditions")
    public boolean existInRedis(String hash) {
        //TODO set Duration to 1 hr
        return !redisTemplate.opsForValue().setIfAbsent(hash, Status.ACCEPTED, Duration.of(60L, ChronoUnit.SECONDS));
    }

    public void close() {
        log.error("Shit happens, exit");
        ((ConfigurableApplicationContext) ctx).close();
    }

    void sendToKafka(String hash, PostDto p) {
        //kafkaTemplate.send(topic, hash, p);
    }

    public String calculateHash(Object o) {
        return Utils.calculateHashSha1(o);
    }

    @Null Document call(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent("APIs-Google (+https://developers.google.com/webmasters/APIs-Google.html)")
                .referrer("http://www.google.com")
                .post();
    }

}
