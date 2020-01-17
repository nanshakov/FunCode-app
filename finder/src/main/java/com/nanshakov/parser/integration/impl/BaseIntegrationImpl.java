package com.nanshakov.parser.integration.impl;

import com.nanshakov.common.TagsService;
import com.nanshakov.common.Utils;
import com.nanshakov.common.dto.PostDto;
import com.nanshakov.configuration.Status;
import com.nanshakov.lib.src.cue.lang.stop.StopWords;
import com.nanshakov.parser.integration.BaseIntegration;

import org.jsoup.Connection;
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

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired TagsService tagsService;
    @Value("${spring.kafka.topic}")
    private String topic;
    @Autowired
    private ApplicationContext ctx;

    Counter duplicatesCounter = Metrics.counter("parse.duplicates", "parse", "duplicates");
    Counter successfulCounter = Metrics.counter("parse.successful", "parse", "successful");
    Counter errorsCounter = Metrics.counter("parse.error", "parse", "error");
    Counter dropCounter = Metrics.counter("parse.drop", "parse", "drop");
    @Autowired
    private KafkaTemplate<String, PostDto> kafkaTemplate;

    long duplicatesCount = 0;

    @SuppressWarnings("ConstantConditions")
    public boolean existInRedis(String hash) {
        //TODO set Duration to 1 hr
        return !redisTemplate.opsForValue().setIfAbsent(hash, Status.ACCEPTED, Duration.of(60L, ChronoUnit.MINUTES));
    }

    public void close() {
        log.error("Shit happens, exit");
        ((ConfigurableApplicationContext) ctx).close();
    }

    boolean sendToKafka(PostDto post) {
        String hash = calculateHash(post);
        successfulCounter.increment();
        if (!existInRedis(hash)) {
            duplicatesCount = 0;
            //kafkaTemplate.send(topic, hash, post);
            return true;
        } else {
            log.trace("Post {} with hash {} found in redis, do nothing", post, hash);
            duplicatesCounter.increment();
            duplicatesCount++;
            return false;
        }

    }

    boolean checkLang(String str) {
        if (!str.isEmpty()) {
            if (!StopWords.German.isStopWord(str)) {
                dropCounter.increment();
                return false;
            }
            if (StopWords.German.stopWordCount(str) <= StopWords.English.stopWordCount(str)) {
                dropCounter.increment();
                return false;
            }
        }
        return true;
    }

    public String calculateHash(Object o) {
        return Utils.calculateHashSha1(o);
    }

    @Null Document call(String url, Connection.Method method) throws IOException {
        if (method == Connection.Method.POST) {
            return Jsoup.connect(url)
                    .ignoreContentType(true)
                    .userAgent("APIs-Google (+https://developers.google.com/webmasters/APIs-Google.html)")
                    .referrer("http://www.google.com")
                    .post();
        } else {
            return Jsoup.connect(url)
                    .ignoreContentType(true)
                    .userAgent("APIs-Google (+https://developers.google.com/webmasters/APIs-Google.html)")
                    .referrer("http://www.google.com")
                    .get();
        }
    }

    public void printInfo() {
        log.info(new StringBuilder()
                .append("Module : ").append(getPlatform()).append("\n"));
    }

}