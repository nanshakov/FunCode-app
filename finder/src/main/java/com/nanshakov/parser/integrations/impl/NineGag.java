package com.nanshakov.parser.integrations.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nanshakov.common.dto.NineGagDto;
import com.nanshakov.common.dto.Platform;
import com.nanshakov.common.dto.PostDto;
import com.nanshakov.common.dto.Type;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import com.nanshakov.lib.src.cue.lang.stop.StopWords;

import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.validation.constraints.Null;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Log4j2
@Service
public class NineGag extends BaseIntegrationImpl {

    @Autowired
    ObjectMapper objectMapper;
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
        currentTag = tags;
        if (!type.equals(getPlatform().toString())) { return; }
        printBaseInfo();
        log.info("Started...");
        while (true) {
            NineGagDto rawPosts = getPage();
            //possible parsing error
            if (rawPosts == null) {
                continue;
            }
            //получаем новые id
            if (rawPosts.getData().getNextCursor() == null && nextId > recursionDepth) {
                getAndApplyNextTag();
            }
            nextId = extractId(rawPosts.getData().getNextCursor());
            rawPosts.getData().getPosts().forEach(el -> {
                if (IsRecursionModeEnable) {
                    List<String> tags = el.getTags().stream().map(NineGagDto.Tag::getKey).collect(Collectors.toList());
                    tagsService.addTags(tags);
                }
                PostDto post = parse(el);
                String hash = calculateHash(post);
                total.increment();
                if (!existInRedis(hash)) {
                    sendToKafka(hash, post);
                } else {
                    log.info("Post {} with hash {} found in redis, do nothing", post, hash);
                    duplicates.increment();
                    getAndApplyNextTag();
                }
            }
        }
    }

    private void getAndApplyNextTag() {
        if (IsRecursionModeEnable) {
            currentTag = tagsService.pop();
            nextId = 10;
            if (currentTag != null) {
                log.info("Current tag is: {}", currentTag);
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
            return objectMapper.readValue(call(url.toString()), NineGagDto.class);
        } catch (IOException e) {
            log.error(e);
            errors.increment();
            nextId += 10;
        }
        return null;
    }

    @Null
    private String call(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent("APIs-Google (+https://developers.google.com/webmasters/APIs-Google.html)")
                .referrer("http://www.google.com")
                .ignoreContentType(true)
                .get().body().text();
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
        String[] split = str.split(Pattern.quote("="));
        if (split.length != 0) { return Integer.parseInt(split[split.length - 1]); }
        return -1;
    }
}
