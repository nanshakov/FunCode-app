package com.nanshakov.parser.integration.impl;

import com.nanshakov.common.dto.Platform;
import com.nanshakov.common.dto.PostDto;
import com.nanshakov.common.dto.Type;

import net.dean.jraw.RedditClient;
import net.dean.jraw.http.NetworkAdapter;
import net.dean.jraw.http.OkHttpNetworkAdapter;
import net.dean.jraw.http.UserAgent;
import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.SubredditSort;
import net.dean.jraw.models.TimePeriod;
import net.dean.jraw.oauth.Credentials;
import net.dean.jraw.oauth.OAuthHelper;
import net.dean.jraw.pagination.DefaultPaginator;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.validation.constraints.Null;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class Reddit extends BaseIntegrationImpl<Object, Object> {

    @Value("${Reddit.tags}")
    private String tags;

    @Value("${Reddit.username}")
    private String username;
    @Value("${Reddit.password}")
    private String password;
    @Value("${Reddit.clientId}")
    private String clientId;
    @Value("${Reddit.clientSecret}")
    private String clientSecret;
    @Value("${Reddit.recursion.enable:false}")
    private boolean isRecursionModeEnable;
    @Value("${Reddit.recursion.depth:1000}")
    private long recursionDepth;
    @Value("${Reddit.recursion.duplicates-count:100}")
    private long duplicatesCountLimit;
    @Value("${multi-instance}")
    private boolean multiInstance;
    RedditClient redditClient;

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
        redditClient = OAuthHelper.automatic(adapter, credentials);
    }

    @Override
    public void run() {
        while (!tagsService.isEmpty()) {
            try {
                if (multiInstance) {
                    Thread.sleep(2500);
                }
                DefaultPaginator<Submission> paginator = redditClient.subreddit(currentTag)
                        .posts()
                        .sorting(SubredditSort.NEW)
                        .timePeriod(TimePeriod.ALL)
                        .limit(50)
                        .build();

                for (Listing<Submission> nextPage : paginator) {
                    for (Submission s : nextPage.getChildren()) {
                        if (s.getUrl().endsWith("jpg") || s.getUrl().endsWith("png")) {
                            var post = PostDto.builder()
                                    .url("https://www.reddit.com" + s.getPermalink())
                                    .imgUrl(s.getUrl())
                                    .alt(s.getLinkFlairText())
                                    .from(getPlatform())
                                    .type(Type.PHOTO)
                                    .author(s.getAuthor())
                                    .likes(s.getScore())
                                    .comments(s.getCommentCount())
                                    .title(s.getTitle())
                                    .dateTime(new Timestamp(s.getCreated().getTime()).toLocalDateTime())
                                    .build();

                            if (!checkLang(post.getAlt())) {
                                continue;
                            }

                            sendToKafka(post);

                            if (duplicatesCount > duplicatesCountLimit) {
                                log.info("Switch by duplicates {}", duplicatesCount);
                                setNextTag();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                log.error(e);
                errorsCounter.increment();
            } finally {
                log.info("Switch by cursor (nextId) is null. End of data.");
                setNextTag();
            }
        }
    }

    @Override
    public Platform getPlatform() {
        return Platform.Reddit;
    }

    @Override
    Object getPage() {
        throw new NotImplementedException("com.nanshakov.parser.integration.impl.Reddit.getPage");
    }

    @Override
    @Null int incrementPage() {
        throw new NotImplementedException("com.nanshakov.parser.integration.impl.Reddit.incrementPage");
    }

    @Override
    @Null Integer getNextPage(Object p) {
        throw new NotImplementedException("com.nanshakov.parser.integration.impl.Reddit.getNextPage");
    }

    @Override
    @NonNull PostDto parse(Object o, Object ob) {
        throw new NotImplementedException("com.nanshakov.parser.integration.impl.Reddit.parse");
    }

    @Override
    @NonNull Collection<Object> extractElement(Object p) {
        throw new NotImplementedException("com.nanshakov.parser.integration.impl.Reddit.extractElement");
    }
}
