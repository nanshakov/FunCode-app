package com.nanshakov.parser.integrations.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nanshakov.common.dto.NineGagDto;
import com.nanshakov.common.dto.Platform;
import com.nanshakov.common.dto.PostDto;
import com.nanshakov.common.dto.Type;

import org.jsoup.Connection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.validation.constraints.Null;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class NineGag extends BaseIntegrationImpl {

    @Autowired
    private ObjectMapper objectMapper;
    private int nextId = 10;
    @Value("${NineGag.tags}")
    private String tags;
    @Value("${NineGag.recursion.enable: false}")
    private boolean IsRecursionModeEnable;
    @Value("${NineGag.recursion.depth:1000}")
    private long recursionDepth;
    @Value("${NineGag.download-url}")
    private String downloadUrl;
    private String currentTag;

    @SneakyThrows
    @Override
    public void run() {
        Thread.sleep(1000);
        if (!type.equals(getPlatform().toString())) {
            return;
        }
        //todo автоматически делать это
        if (IsRecursionModeEnable) {
            tagsService.addTags(Arrays.stream(tags.split(",")).map(String::trim).collect(Collectors.toList()));
            currentTag = tagsService.pop();
        }
        printBaseInfo();
        log.info("Started...");
        while (true) {
            NineGagDto rawPosts = getPage();
            //Если произошла ошибка парсинга
            if (rawPosts == null) {
                continue;
            }

            //Если достигли лимита рекурсивного обхода
            if (IsRecursionModeEnable && nextId > recursionDepth) {
                getAndApplyNextTag();
            }
            //Если id нет то получаем новый тег
            if (rawPosts.getData().getNextCursor() == null) {
                getAndApplyNextTag();
            }
            //получаем новые id
            nextId = extractId(rawPosts.getData().getNextCursor());
            //что - то пошло не так
            if (nextId == -1) {
                getAndApplyNextTag();
            }
            for (NineGagDto.Post p : rawPosts.getData().getPosts()) {
                if (IsRecursionModeEnable) {
                    List<String> tags = p.getTags().stream().map(NineGagDto.Tag::getKey).collect(Collectors.toList());
                    tagsService.addTags(tags);
                }
                PostDto post = parse(p);
                if (!checkLang(post.getAlt())) {
                    continue;
                }
                if (!sendToKafka(post)) {
                    getAndApplyNextTag();
                }
            }
        }
    }

    /**
     * Получает новый тег и сбрасывает счетчик страницы
     */
    private void getAndApplyNextTag() {
        if (IsRecursionModeEnable) {
            currentTag = tagsService.pop();
            nextId = 10;
            if (currentTag != null) {
                log.info("Current tag is: {}", currentTag);
            } else {
                log.warn("Tag is null!");
            }
        }
    }

    @Override
    public Platform getPlatform() {
        return Platform.NineGag;
    }

    @Null
    private NineGagDto getPage() {
        try {
            StringBuilder url = new StringBuilder();
            url.append(downloadUrl)
                    .append(currentTag)
                    .append("&c=")
                    .append(nextId);
            return objectMapper.readValue(call(url.toString(), Connection.Method.GET).body().text(), NineGagDto.class);
        } catch (IOException e) {
            //часто падает с ошибкой парсинга, из за кривых данныех в строке
            //log.error(e);
            errors.increment();
            nextId += 10;
        }
        return null;
    }

    @Null
    private PostDto parse(NineGagDto.Post el) {
        return PostDto.builder()
                .url(el.getUrl())
                .imgUrl(el.getImages().getImage700().getUrl())
                .alt(el.getTitle())
                .from(getPlatform())
                .type(Type.PHOTO)
                .likes(el.getUpVoteCount())
                .dislikes(el.getDownVoteCount())
                .comments(el.getCommentsCount())
                .dateTime(new Timestamp(el.getCreationTs() * 1000L).toLocalDateTime())
                .build();
    }

    void printBaseInfo() {
        log.info(new StringBuilder()
                .append("Module : ").append(getPlatform()).append("\n")
                .append("Tags : ").append(tags));
    }

    @Null
    private int extractId(String str) {
        if (str != null) {
            String[] split = str.split(Pattern.quote("="));
            if (split.length != 0) { return Integer.parseInt(split[split.length - 1]); }
        }
        return -1;
    }
}
