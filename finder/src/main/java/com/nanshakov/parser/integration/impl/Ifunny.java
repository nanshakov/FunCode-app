package com.nanshakov.parser.integration.impl;

import com.nanshakov.common.dto.Platform;
import com.nanshakov.common.dto.PostDto;
import com.nanshakov.common.dto.Type;

import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;

import javax.validation.constraints.Null;

import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

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
                sendToKafka(post);
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
            return call(url.toString(), Connection.Method.POST);
        } catch (IOException e) {
            errorsCounter.increment();
            log.error(e);
        }
        return null;
    }

    @Null
    private PostDto parse(Element el) {
        Elements img = el.select("img");

        String dataSrc = img.attr("data-src");
        if (dataSrc != null && !dataSrc.isEmpty()) {
            var split = dataSrc.split("/");
            if (split.length < 6) {
                throw new IllegalStateException("Error while parsing url " + dataSrc);
            }
            String url = downloadUrl + split[5];
            //ссылка на пост
            String href = "https://ifunny.co/" + el.select("a[href]").attr("href");
            //теги
            String alt = img.attr("alt");
            Document extendedPost = resolvePost(href);
            var postBuilder = PostDto.builder();
            if (extendedPost != null) {
                var info = extendedPost.select(".metapanel__user-nick").first();
                String userName = info.childNode(0).toString().trim();
                String date = info.select(".metapanel__time").text();
                if (date != null) {
                    postBuilder.author(userName);
                    postBuilder.dateTime(resolveDateTime(date));
                }
                var likes = extendedPost.select(".metapanel__meta")
                        .first()
                        .select("post-actions")
                        .attr("initial-smiles");
                if (likes != null) {
                    postBuilder.likes(Long.parseLong(likes));
                }
                var comments = extendedPost.select(".post-actions__item").first().text();
                if (comments != null) {
                    postBuilder.comments(resolveComments(comments));
                }
            }

            return postBuilder
                    .imgUrl(url)
                    .url(href)
                    .alt(alt)
                    .from(getPlatform())
                    .type(Type.PHOTO)
                    .build();
        }
        return null;
    }

    private LocalDateTime resolveDateTime(String datetime) {
        if (datetime.endsWith("d")) {
            return LocalDateTime.now().minusDays(Long.parseLong(datetime.substring(0, datetime.length() - 2)));
        }
        return null;
    }

    private long resolveComments(String commentCount) {
        if (commentCount.endsWith("k")) {
            return Long.parseLong(commentCount.substring(0, commentCount.length() - 2));
        }
        return 0;
    }

    private Document resolvePost(String url) {
        try {
            return call(url, Connection.Method.GET);
        } catch (IOException e) {
            return null;
        }
    }
}
