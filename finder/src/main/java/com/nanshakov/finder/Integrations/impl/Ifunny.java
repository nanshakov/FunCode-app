package com.nanshakov.finder.Integrations.impl;

import com.nanshakov.finder.Dto.Post;
import com.nanshakov.finder.Integrations.BaseIntegration;
import com.nanshakov.finder.Integrations.Platform;
import com.nanshakov.finder.Integrations.Type;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.SerializationUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.kafka.core.KafkaTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.Null;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Log4j2
@RequiredArgsConstructor
public class Ifunny implements BaseIntegration {

    private String nextId = "1567508062";
    private final ApplicationContext ctx;
    private final KafkaTemplate<String, Post> template;
    private final String topic;
    private final String tag;

    @Override
    public void start() {
        printBaseInfo();
        int count = Integer.MAX_VALUE;
        for (int i = 0; i < count; i++) {
            Document doc = getPage(i);
            if (doc == null) {
                log.error("Shit happens, exit");
                ((ConfigurableApplicationContext) ctx).close();
            }
            Elements listNews = doc.select("img");
            //получаем новые id
            if (doc.selectFirst("li[data-next]") != null) {
                nextId = doc.selectFirst("li[data-next]").attr("data-next");
            } else {
                break;
            }
            List<Post> posts = new ArrayList<>(listNews.size());
            listNews.forEach(el -> {
                Post post = parse(el);
                if (post != null) {
                    posts.add(post);
                }
            });
            if (!posts.isEmpty()) {
                posts.forEach(this::sendToKafka);
            } else {
                log.error("Empty data!");
            }
        }
    }

    private void sendToKafka(Post p) {
        template.send(topic, DigestUtils.sha512Hex(DigestUtils.sha256(SerializationUtils.serialize(p))), p);
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
                .append("Модуль : ").append(getPlatform()).append("\n")
                .append("Теги : ").append(tag));
    }
}
