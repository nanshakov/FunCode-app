package com.nanshakov.parser.integrations.impl;

import com.nanshakov.common.dto.Platform;
import com.nanshakov.common.dto.Post;
import com.nanshakov.common.dto.Type;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

import javax.validation.constraints.Null;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class Ifunny extends BaseIntegrationImpl {

    private String nextId = "1567508062";
    @Value("${tag}")
    private String tag;
    @Value("${IFUNNY.download-url}")
    private String downloadUrl;

    @Override
    public void start() throws InterruptedException {
        Thread.sleep(1000);
        if (!type.equals(getPlatform().toString())) { return; }
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
                total.increment();
                String hash = calculateHash(post) + "02";
                if (!exist(hash)) {
                    sendToKafka(hash, post);
                } else {
                    log.trace("Post {} with hash {} found in redis, do nothing", post, hash);
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
            errors.increment();
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
        String dataSrc = el.attr("data-src");
        if (dataSrc != null && !dataSrc.isEmpty()) {
            String alt = el.attr("alt");
            String[] parts = dataSrc.split("/");
            //TODO [экстремальное программирование] хорошо бы как-то проверить что их и правда 5...
            String url = downloadUrl + parts[5];
            Post p = new Post();
            p.setUrl(url);
            p.setAlt(alt);
            p.setFrom(getPlatform());
            p.setType(Type.PHOTO);
            return p;
        }
        return null;
    }

    void printBaseInfo() {
        log.info(new StringBuilder()
                .append("Module : ").append(getPlatform()).append("\n")
                .append("Tags : ").append(tag));
    }
}
