package com.nanshakov.finder.integrations.impl;

import com.nanshakov.dto.Platform;
import com.nanshakov.dto.Post;
import com.nanshakov.dto.Type;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

import javax.annotation.PostConstruct;
import javax.validation.constraints.Null;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class Ifunny extends BaseIntegrationImpl {

    private String nextId = "1567508062";
    @Value("${tag}")
    private String tag;

    @PostConstruct
    public void postConstruct() {
        if (type.equals(getPlatform().toString())) { start(); }
    }

    @Override
    public void start() {
        printBaseInfo();
        log.info("Started...");
        int count = Integer.MAX_VALUE;
        for (int i = 0; i < count; i++) {
            Document doc = getPage(i);
            if (doc == null) {
                close();
                return;
            }
            Elements listNews = doc.select("img");
            //получаем новые id
            if (doc.selectFirst("li[data-next]") != null) {
                nextId = doc.selectFirst("li[data-next]").attr("data-next");
            } else {
                break;
            }
            listNews.forEach(el -> {
                Post post = parse(el);
                String hash = calculateHash(post);
                if (post != null && !exist(hash)) {
                    sendToKafka(hash, post);
                } else {
                    log.info("Post {} with hash {} found in DBs, do nopostthing", post, hash);
                }
            });
        }
    }

    @Override
    public Platform getPlatform() {
        return Platform.IFUNNY;
    }

    @Null
    private Document getPage(long pageNum) {
        try {
            StringBuilder url = new StringBuilder();
            url.append("https://ifunny.co/api/tags/")
                    .append(tag)
                    .append("/")
                    .append(nextId)
                    .append("?page=")
                    .append(pageNum);
            return call(url.toString());
        } catch (IOException e) {
            log.error(e);
        }
        return null;
    }

    @Null
    private Document call(String url) throws IOException {
        return Jsoup.connect(url)
                .userAgent("APIs-Google (+https://developers.google.com/webmasters/APIs-Google.html)")
                .referrer("http://www.google.com")
                .post();
    }

    @Null
    private Post parse(Element el) {
        String url = el.attr("data-src");
        if (url != null && !url.isEmpty()) {
            String alt = el.attr("alt");
            return Post.builder()
                    .url(url)
                    .alt(alt)
                    .from(getPlatform())
                    .type(Type.PHOTO)
                    .build();
        }
        return null;
    }

    void printBaseInfo() {
        log.info(new StringBuilder()
                .append("Module : ").append(getPlatform()).append("\n")
                .append("Tags : ").append(tag));
    }
}
