package com.nanshakov.parser.integrations.impl;

import com.nanshakov.common.dto.Platform;
import com.nanshakov.common.dto.PostDto;
import com.nanshakov.common.dto.Type;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.validation.constraints.Null;
import java.io.IOException;

@Log4j2
@Service
public class Ifunny extends BaseIntegrationImpl {

    private String nextId = "1567508062";
    @Value("${IFUNNY.tags}")
    private String tags;
    @Value("${IFUNNY.download-url}")
    private String downloadUrl;

    @SneakyThrows
    @Override
    public void run() {
        //Thread.sleep(1000);
        if (!type.equals(getPlatform().toString())) {
            return;
        }
        printBaseInfo();
        log.info("Started...");
        int count = Integer.MAX_VALUE;
        for (int i = 0; i < count; i++) {
            Document doc = getPage(i);
            if (doc == null) {
                close();
                return;
            }
            //получаем новые id
            if (doc.selectFirst("li[data-next]") != null) {
                nextId = doc.selectFirst("li[data-next]").attr("data-next");
            } else {
                break;
            }
            Elements listNews = doc.select(".post__media");
            listNews.forEach(el -> {
                PostDto post = parse(el);
                total.increment();
                String hash = calculateHash(post);
                if (!existInRedis(hash)) {
                    sendToKafka(hash, post);
                } else {
                    log.trace("Post {} with hash {} found in redis, do nothing", post, hash);
                    duplicates.increment();
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
                    .append(tags)
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
    private PostDto parse(Element el) {
        Elements img = el.select("img");

        String dataSrc = img.attr("data-src");
        if (dataSrc != null && !dataSrc.isEmpty()) {
            //TODO [экстремальное программирование] хорошо бы как-то проверить что их и правда 5...
            String url = downloadUrl + dataSrc.split("/")[5];
            //ссылка на пост
            String href = "https://ifunny.co/" + el.select("a[href]").attr("href");
            //список тегов
            String alt = img.attr("alt");

            return PostDto.builder()
                    .imgUrl(url)
                    .url(href)
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
                .append("Tags : ").append(tags));
    }
}
