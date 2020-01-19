package com.nanshakov.parser.integration.impl;

import com.nanshakov.common.TagsService;
import com.nanshakov.common.Utils;
import com.nanshakov.common.dto.Platform;
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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;

import java.io.IOException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import javax.validation.constraints.Null;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

@Log4j2
public abstract class BaseIntegrationImpl<PageObject, SingleObject> implements BaseIntegration {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    String currentTag;
    int page = 0;

    @Value("${spring.kafka.topic}")
    private String topic;
    @Autowired
    TagsService tagsService;
    @Autowired
    private ApplicationContext ctx;
    @Autowired
    private KafkaTemplate<String, PostDto> kafkaTemplate;
    //Дпоплнительные параметры, передаются классами - парсерами
    private String tags;
    private boolean isRecursionModeEnable;
    private long recursionDepth;
    long duplicatesCountLimit;

    Counter duplicatesCounter = Metrics.counter("parse.duplicates", "parse", "duplicates");
    Counter successfulCounter = Metrics.counter("parse.successful", "parse", "successful");
    Counter errorsCounter = Metrics.counter("parse.error", "parse", "error");
    Counter dropCounter = Metrics.counter("parse.drop", "parse", "drop");

    long duplicatesCount = 0;

    public void addParams(
            String tags,
            boolean isRecursionModeEnable,
            long recursionDepth,
            long duplicatesCountLimit) {
        this.tags = tags;
        this.isRecursionModeEnable = isRecursionModeEnable;
        this.recursionDepth = recursionDepth;
        this.duplicatesCountLimit = duplicatesCountLimit;
        loadTags();
        printInfo();
        setNextTag();
    }

    @Override
    public void run() {
        while (!tagsService.isEmpty()) {
            PageObject rawPosts = getPage();
            //Если произошла ошибка парсинга
            if (rawPosts == null) {
                page = incrementPage();
                errorsCounter.increment();
                continue;
            }

            for (SingleObject singleObject : extractElement(rawPosts)) {
                PostDto post = parse(singleObject);
                if (isRecursionModeEnable) {
                    tagsService.addTags(post.getTags());
                }
                if (post.isCheckLangNeeded() && !checkLang(post.getAlt())) {
                    continue;
                }
                sendToKafka(post);

                if (duplicatesCount > duplicatesCountLimit) {
                    log.info("Switch by duplicates {}", duplicatesCount);
                    setNextTag();
                }
            }

            //Если достигли лимита рекурсивного обхода
            if (isRecursionModeEnable && page > recursionDepth) {
                log.info("Switch by limit nextId {}, recursionDepth {}", page, recursionDepth);
                setNextTag();
            }

            Integer page = getNextPage(rawPosts);
            if (page == null) {
                setNextTag();
            } else {
                this.page = page;
            }
        }
        log.info("Done");
    }

    private void loadTags() {
        tagsService.addTags(Arrays.stream(tags.split(",")).map(String::trim).collect(Collectors.toList()));
        //tagsService.addTags(StopWords.German.getStopwords());
    }

    /**
     * Получает новый тег и сбрасывает счетчик страницы
     */
    void setNextTag() {
        duplicatesCount = 0;
        log.trace("Tags: {}", tagsService.getTags());
        currentTag = tagsService.pop();
        page = 0;
        if (currentTag != null) {
            log.info("Current tag is: {}", currentTag);
        } else {
            log.warn("Tag is null!");
        }
    }

    @SuppressWarnings("ConstantConditions")
    boolean existInRedis(String hash) {
        return !redisTemplate.opsForValue().setIfAbsent(hash, Status.ACCEPTED, Duration.of(60L, ChronoUnit.MINUTES));
    }

    void sendToKafka(PostDto post) {
        String hash = calculateHash(post);
        successfulCounter.increment();
        if (!existInRedis(hash)) {
            duplicatesCount = 0;
            kafkaTemplate.send(topic, hash, post);
        } else {
            log.trace("Post {} with hash {} found in redis, do nothing", post, hash);
            duplicatesCounter.increment();
            duplicatesCount++;
        }

    }

    boolean checkLang(@Null String str) {
        if (str == null) { return true; }
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

    String calculateHash(Object o) {
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

    @Override
    public abstract Platform getPlatform();

    public void printInfo() {
        log.info(new StringBuilder()
                .append("Module : ").append(getPlatform()).append("\n")
                .append("Tags : ").append(tagsService.getTags()));
    }

    /**
     * @return Обьект страницы после парсинга
     */
    @Null
    abstract PageObject getPage();

    /**
     * @return Правило инкремента счетчика страниц
     */
    @Null
    abstract int incrementPage();

    /**
     * @param p Обьект страницы после парсинга com.nanshakov.parser.integration.impl.BaseIntegrationImpl#getPage()
     * @return номер следующей страницы
     */
    @Null
    abstract Integer getNextPage(PageObject p);

    /**
     * @param singleObject единица контента com.nanshakov.parser.integration.impl.BaseIntegrationImpl#extractElement(java.lang.Object)
     * @return Универсальный обьект com.nanshakov.common.dto.PostDto
     */
    @NonNull
    abstract PostDto parse(SingleObject singleObject);

    /**
     * @param p Обьект страницы после парсинга com.nanshakov.parser.integration.impl.BaseIntegrationImpl#getPage()
     * @return Спискок единиц контента
     */
    @NonNull
    abstract Collection<SingleObject> extractElement(PageObject p);
}
