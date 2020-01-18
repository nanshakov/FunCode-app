package com.nanshakov.parser.integration.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nanshakov.common.dto.NineGagDto;
import com.nanshakov.common.dto.Platform;
import com.nanshakov.common.dto.PostDto;
import com.nanshakov.common.dto.Type;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.NetworkAdapter;
import net.dean.jraw.http.OkHttpNetworkAdapter;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.models.Account;
import net.dean.jraw.oauth.Credentials;
import net.dean.jraw.oauth.OAuthHelper;

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
public class Reddit extends BaseIntegrationImpl<NineGagDto, NineGagDto.Post> {

    @Autowired
    private ObjectMapper objectMapper;
    @Value("${Reddit.tags}")
    private String tags;
    @Value("${Reddit.recursion.enable:false}")
    private boolean isRecursionModeEnable;
    @Value("${Reddit.recursion.depth:1000}")
    private long recursionDepth;
    @Value("${Reddit.recursion.duplicates-count:100}")
    private long duplicatesCountLimit;
    @Value("${Reddit.download-url}")
    private String downloadUrl;

    @Value("${Reddit.username}")
    private String username;
    @Value("${Reddit.password}")
    private String password;
    @Value("${Reddit.clientId}")
    private String clientId;
    @Value("${Reddit.clientSecret}")
    private String clientSecret;

    @PostConstruct
    public void postConstruct() {
        addParams(tags, isRecursionModeEnable, recursionDepth, duplicatesCountLimit);
        // Create our credentials
        Credentials credentials = Credentials.script(username, password,
                clientId, clientSecret);
        // This is what really sends HTTP requests
        NetworkAdapter adapter = new OkHttpNetworkAdapter(
                new UserAgent("nanshakov", "com.nanshakov", "v0.1", "nanshakov"));
        // Authenticate and get a RedditClient instance
        RedditClient reddit = OAuthHelper.automatic(adapter, credentials);
        Account me = reddit.me().query().getAccount();

    }

    @Override
    public Platform getPlatform() {
        return Platform.Reddit;
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
                .checkLangNeeded(true)
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

    public List<String> extractTags(NineGagDto.Post el) {
        return el.getTags()
                .stream()
                .map(t -> t.getUrl().replace("/tag/", "").toLowerCase())
                .collect(Collectors.toList());
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
