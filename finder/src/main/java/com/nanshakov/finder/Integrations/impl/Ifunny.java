package com.nanshakov.finder.Integrations.impl;

import com.nanshakov.finder.Dto.Post;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.validation.constraints.Null;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class Ifunny extends BaseIntegrationImpl {

    private String nextId = "1567508062";

    @Override
    public void start() {

    }

    @Override
    public Post getNext() {
        return null;
    }

    @SneakyThrows
    @PostConstruct
    void postConstruct() {
        long startTime = System.nanoTime();
        int count = 50;
        for (int i = 0; i < count; i++) {
            Document doc = getPage(i);
            if (doc == null) {
                log.error("Shit happens, exit");
                exit();
            }
            Elements listNews = doc.select("img");
            //получаем новые id
            if (doc.selectFirst("li[data-next]") != null) {
                nextId = doc.selectFirst("li[data-next]").attr("data-next");
            }

            List<Post> posts = new ArrayList<>(listNews.size());
            listNews.forEach(el -> {
                posts.add(parse(el));
            });
            if (!posts.isEmpty()) {
                //posts.forEach(p -> template.send(topic, p));
            } else {
                log.error("Empty data!");
            }
        }

        //статистика
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
        long totalCount = (count - 1) * 49;
        log.info(totalCount);
        long second = duration / 1_000_000_000;
        log.info(second + "s");
        log.info(totalCount / second + "per second");
    }

    @Null
    private Document getPage(long pageNum) {
        try {
            return call("https://ifunny.co/api/tags/deutsch/" + nextId + "?page=" + pageNum);
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
                    .build();
        }
        return null;
    }
}