package com.nanshakov.parser.integrations.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nanshakov.common.dto.NineGagDto;
import com.nanshakov.common.dto.Platform;
import com.nanshakov.common.dto.PostDto;
import com.nanshakov.common.dto.Type;

import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.regex.Pattern;

import javax.validation.constraints.Null;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class NineGag extends BaseIntegrationImpl {

    @Autowired
    ObjectMapper objectMapper;
    private int nextId = 10;
    @Value("${NineGag.tags}")
    private String tags;
    @Value("${NineGag.recursion}")
    private boolean recursion;
    @Value("${NineGag.download-url}")
    private String downloadUrl;
    private String currentTag;

    @Override
    public void start() throws InterruptedException {
        Thread.sleep(1000);
        currentTag = tags;
        if (!type.equals(getPlatform().toString())) { return; }
        printBaseInfo();
        log.info("Started...");
        while (true) {
            NineGagDto rawPosts = getPage();
            if (rawPosts == null) {
                continue;
            }
            //получаем новые id
            if (rawPosts.getData().getNextCursor() == null) {
                break;
            }
            nextId = extractId(rawPosts.getData().getNextCursor());
            rawPosts.getData().getPosts().forEach(el -> {
                el.getTags().forEach(t -> tagsService.addTag(t.getKey()));
                PostDto post = parse(el);
                String hash = calculateHash(post);
                total.increment();
                if (!existInRedis(hash)) {
                    sendToKafka(hash, post);
                } else {
                    log.info("Post {} with hash {} found in redis, do nothing", post, hash);
                    duplicates.increment();
                    tagsService.markTagAsProcessed(currentTag);
                }
            });
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
                    .append(tags)
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
                .dateTime(new Timestamp(Long.valueOf(el.getCreationTs()) * 1000).toLocalDateTime())
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