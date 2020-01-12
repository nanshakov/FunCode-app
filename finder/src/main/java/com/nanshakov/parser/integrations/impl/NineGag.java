package com.nanshakov.parser.integrations.impl;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nanshakov.common.dto.Platform;
import com.nanshakov.common.dto.Post;
import com.nanshakov.common.dto.Type;
import com.nanshakov.common.dto.ninegag.NineGagDto;

import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.regex.Pattern;

import javax.validation.constraints.Null;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class NineGag extends BaseIntegrationImpl {

    @Autowired
    ObjectMapper objectMapper;
    private int nextId = 10;
    @Value("${tag}")
    private String tag;
    @Value("${IFUNNY.download-url}")
    private String downloadUrl;

    @Override
    public void start() throws InterruptedException {
        objectMapper.configure(
                DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, true);

        Thread.sleep(1000);
        if (!type.equals(getPlatform().toString())) { return; }
        printBaseInfo();
        log.info("Started...");
        int count = Integer.MAX_VALUE;
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
                Post post = parse(el);
                String hash = calculateHash(post);
                if (!exist(hash)) {
                    sendToKafka(hash, post);
                } else {
                    log.info("Post {} with hash {} found in redis, do nothing", post, hash);
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
            url.append("https://9gag.com/v1/search-posts?query=")
                    .append(tag)
                    .append("&c=")
                    .append(nextId);
            return objectMapper.readValue(call(url.toString()), NineGagDto.class);
        } catch (IOException e) {
            log.error(e);
            nextId += 10;
        }
        return null;
    }

    @Null
    private String call(String url) throws IOException {
        String d = Jsoup.connect(url)
                .userAgent("APIs-Google (+https://developers.google.com/webmasters/APIs-Google.html)")
                .referrer("http://www.google.com")
                .ignoreContentType(true)
                .get().body().text();
        return d;
    }

    @Null
    private Post parse(NineGagDto.Post el) {
        Post p = new Post();
        p.setUrl(el.getImages().getImage700().getUrl());
        p.setAlt(el.getTitle());
        p.setFrom(getPlatform());
        p.setType(Type.PHOTO);
        return Post.builder()
                .url(el.getUrl())
                .imgUrl(el.getImages().getImage700().getUrl())
                .alt(el.getTitle())
                .from(getPlatform())
                .type(Type.PHOTO)
                .likes(el.getUpVoteCount())
                .dislikes(el.getDownVoteCount())
                .comments(el.getCommentsCount())
                .build();
    }

    void printBaseInfo() {
        log.info(new StringBuilder()
                .append("Module : ").append(getPlatform()).append("\n")
                .append("Tags : ").append(tag));
    }

    @Null
    private int extractId(String str) {

        String[] split = str.split(Pattern.quote("="));
        if (split.length != 0) { return Integer.parseInt(split[split.length - 1]); }
        return -1;
    }
}
