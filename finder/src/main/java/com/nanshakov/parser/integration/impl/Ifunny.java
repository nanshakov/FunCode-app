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
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.validation.constraints.Null;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class Ifunny extends BaseIntegrationImpl<Document, Element> {

    private String nextId = "1567508062";
    @Value("${IFUNNY.tags}")
    private String tags;
    @Value("${IFUNNY.download-url}")
    private String downloadUrl;
    @Value("${type}")
    String type;
    @Value("${NineGag.recursion.enable:false}")
    private boolean isRecursionModeEnable;
    @Value("${NineGag.recursion.depth:1000}")
    private long recursionDepth;
    @Value("${NineGag.recursion.duplicates-count:100}")
    private long duplicatesCountLimit;

    @PostConstruct
    public void postConstruct() {
        if (type.contains(getPlatform().toString())) {
            addParams(tags, isRecursionModeEnable, recursionDepth, duplicatesCountLimit);
        }
    }

    @Override
    public Platform getPlatform() {
        return Platform.IFUNNY;
    }

    @Override
    Document getPage() {
        try {
            StringBuilder url = new StringBuilder();
            url.append("https://ifunny.co/api/tags/")
                    .append(currentTag)
                    .append("/")
                    .append(nextId)
                    .append("?page=")
                    .append(page);
            return call(url.toString(), Connection.Method.POST);
        } catch (IOException e) {
            return null;
        }

    }

    @Override
    @Null int incrementPage() {
        return page++;
    }

    @Override
    @Null Integer getNextPage(Document doc) {
        return page + 1;
    }

    @Null
    PostDto parse(Element el, Document doc) {
        if (doc.selectFirst("li[data-next]") != null) {
            nextId = doc.selectFirst("li[data-next]").attr("data-next");
        }
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
            var postBuilder = PostDto.builder();
            //теги
            String alt = img.attr("alt");
            //postBuilder.tags(Arrays.stream(alt.split(",")).collect(Collectors.toList()));
            //если есть ',' то это список тегов, в них нет смысла искать язык
            if (!alt.contains(",") && checkLang(alt)) {
                Document extendedPost = resolvePost(href);
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
                    var comments = extendedPost.select(".post-actions__item").get(1).text();
                    if (comments != null) {
                        postBuilder.comments(resolveComments(comments));
                    }
                }
            }

            return postBuilder
                    .imgUrl(url)
                    .url(href)
                    .alt(alt)
                    .from(getPlatform())
                    .type(resolveType(el.select("a[data-type]").attr("data-type")))
                    .build();
        }
        return null;
    }

    @Override
    @NonNull List<Element> extractElement(Document doc) {
        return new ArrayList<>(doc.select(".post__media"));
    }

    private Type resolveType(String type) {
        if (type.equals("video")) {
            return Type.VIDEO;
        }
        return Type.PHOTO;
    }

    private LocalDateTime resolveDateTime(String datetime) {
        if (datetime.endsWith("d")) {
            return LocalDateTime.now().minusDays(Long.parseLong(datetime.substring(0, datetime.length() - 2)));
        }
        return null;
    }

    private long resolveComments(String commentCount) {
        if (commentCount.endsWith("k")) {
            return (long) (Double.parseDouble(commentCount.substring(0, commentCount.length() - 2)) * 1000);
        }
        return Long.parseLong(commentCount);
    }

    private Document resolvePost(String url) {
        try {
            return call(url, Connection.Method.GET);
        } catch (IOException e) {
            return null;
        }
    }
}
