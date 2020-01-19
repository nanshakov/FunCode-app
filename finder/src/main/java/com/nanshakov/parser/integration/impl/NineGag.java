package com.nanshakov.parser.integration.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nanshakov.common.dto.NineGagDto;
import com.nanshakov.common.dto.Platform;
import com.nanshakov.common.dto.PostDto;
import com.nanshakov.common.dto.Type;

import org.jsoup.Connection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.validation.constraints.Null;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class NineGag extends BaseIntegrationImpl<NineGagDto, NineGagDto.Post> {

    @Autowired
    private ObjectMapper objectMapper;
    @Value("${NineGag.tags}")
    private String tags;
    @Value("${NineGag.recursion.enable:false}")
    private boolean isRecursionModeEnable;
    @Value("${NineGag.recursion.depth:1000}")
    private long recursionDepth;
    @Value("${NineGag.recursion.duplicates-count:100}")
    private long duplicatesCountLimit;
    @Value("${NineGag.download-url}")
    private String downloadUrl;
    @Value("${type}")
    String type;

    @PostConstruct
    public void postConstruct() {
        if (type.contains(getPlatform().toString())) {
            addParams(tags, isRecursionModeEnable, recursionDepth, duplicatesCountLimit);
        }
    }

    @Override
    public Platform getPlatform() {
        return Platform.NineGag;
    }

    @Null
    public NineGagDto getPage() {
        try {
            StringBuilder url = new StringBuilder();
            url.append(downloadUrl)
                    .append(currentTag)
                    .append("&c=")
                    .append(page);
            return objectMapper.readValue(call(url.toString(), Connection.Method.GET).body().text(), NineGagDto.class);
        } catch (IOException e) {
            return null;
        }

    }

    public int incrementPage() {
        return page + 10;
    }

    public List<String> extractTags(NineGagDto.Post el) {
        return el.getTags()
                .stream()
                .map(t -> t.getUrl().replace("/tag/", "").toLowerCase())
                .collect(Collectors.toList());
    }

    public @Null Integer getNextPage(NineGagDto p) {
        if (p.getData().getNextCursor() == null) {
            log.info("Switch by cursor (nextId) is null. End of data.");
            return null;
        }

        //получаем новые id
        var id = extractId(p.getData().getNextCursor());
        if (id == null) {
            log.info("Switch by wrong parsing nextId");
        }
        return id;
    }

    @Null
    PostDto parse(NineGagDto.Post el) {
        String imgUrl;
        Type type;
        if (el.getImages().isContainsVideo()) {
            imgUrl = el.getImages().getUrlVideo();
            type = Type.VIDEO;
        } else {
            imgUrl = el.getImages().getImage700().getUrl();
            type = Type.PHOTO;
        }

        return PostDto.builder()
                .url(el.getUrl())
                .tags(extractTags(el))
                .imgUrl(imgUrl)
                .alt(el.getTitle())
                .from(getPlatform())
                .type(type)
                .author(el.getPostSection().getName())
                .likes(el.getUpVoteCount())
                .dislikes(el.getDownVoteCount())
                .comments(el.getCommentsCount())
                .title(el.getTitle())
                .dateTime(new Timestamp(el.getCreationTs() * 1000L).toLocalDateTime())
                .build();
    }

    @NonNull List<NineGagDto.Post> extractElement(NineGagDto p) {
        return p.getData().getPosts();
    }

    @Null
    private Integer extractId(String str) {
        if (str != null) {
            String[] split = str.split(Pattern.quote("="));
            if (split.length != 0) { return Integer.parseInt(split[split.length - 1]); }
        }
        return null;
    }
}
