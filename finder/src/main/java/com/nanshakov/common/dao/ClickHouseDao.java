package com.nanshakov.common.dao;

import com.nanshakov.common.dto.PostDto;
import com.nanshakov.common.repo.PostMetaRepository;
import com.nanshakov.controller.response.Post;
import com.nanshakov.controller.response.Result;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
@RequiredArgsConstructor
public class ClickHouseDao implements PostMetaRepository {

    private final JdbcTemplate jdbcTemplate;
    @Value("${schema}")
    private String schema;

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @PostConstruct
    private void postConstruct() {
//        jdbcTemplate.execute("create TABLE " + schema + " IF NOT EXISTS "
//                + "("
//                + "    'urlImgHash'    String,"
//                + "    'sourceUrl'     String,"
//                + "    'contentHash'   String,"
//                + "    'source'        String,"
//                + "    'datetime'      DateTime,"
//                + "    'pathToContent' String,"
//                + "    'likes'         Nullable(Int32),"
//                + "    'dislikes'      Nullable(Int32),"
//                + "    'comments'      Nullable(Int32),"
//                + "    'alt'           Nullable(String),"
//                + "    'title'         Nullable(String),"
//                + "    'author'        Nullable(String)"
//                + ")\n"
//                + "    engine = MergeTree() PARTITION BY toYYYYMM(datetime) ORDER BY ('urlImgHash', 'contentHash');");
    }

    @Override
    public boolean containsByUrl(String hash) {
        //noinspection ConstantConditions
        return jdbcTemplate.queryForObject(
                "select count(urlImgHash) from " + schema + " where urlImgHash=?",
                new Object[] {hash}, Boolean.class);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean containsByContent(String hash) {
        return jdbcTemplate.queryForObject(
                "select count(contentHash) from " + schema + " where contentHash=?",
                new Object[] {hash}, Boolean.class);
    }

    @Override
    public int add(PostDto p) {
        if (p.getDateTime() == null) { return 0; }
        log.trace("Saving to DB {}", p);
        SimpleJdbcInsert simpleJdbcInsert =
                new SimpleJdbcInsert(jdbcTemplate).withTableName(schema);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("urlImgHash", p.getUrlHash());
        parameters.put("sourceUrl", p.getUrl());
        parameters.put("contentHash", p.getContentHash());
        parameters.put("source", p.getFrom());
        parameters.put("datetime", p.getDateTime().format(dateTimeFormatter));
        parameters.put("pathToContent", p.getPathToContent());
        parameters.put("likes", p.getLikes());
        parameters.put("dislikes", p.getDislikes());
        parameters.put("totalScope", p.getLikes() + p.getDislikes());
        parameters.put("comments", p.getComments());
        parameters.put("alt", p.getAlt());
        parameters.put("title", p.getAlt());
        parameters.put("author", p.getAuthor());

        return simpleJdbcInsert.execute(parameters);
    }

    @Override
    public Post findById(String id) {
        return jdbcTemplate.queryForObject(
                "select * from " + schema + " where urlImgHash=?",
                new Object[] {id}, Post.class);
    }

    @Override
    public Result findByPage(int pageNum, int limit) {
        int pages = (getCount() / limit) + 1;
        return Result.builder()
                .currentPage(pageNum)
                .pages(pages)
                .count(limit)
                .posts(jdbcTemplate.query(
                        "select * from " + schema + " ORDER BY datetime DESC LIMIT ?, ?",
                        new Object[] {--pageNum * limit, limit}, (rs, rowNum) ->
                                getBuild(rs)
                )).build();
    }

    @Override
    public Result findHot(int pageNum, int limit) {
        int pages = (getCount() / limit) + 1;
        return Result.builder()
                .currentPage(pageNum)
                .pages(pages)
                .count(limit)
                .posts(jdbcTemplate.query(
                        "select * from " + schema + " ORDER BY totalScope DESC LIMIT ?, ?",
                        new Object[] {--pageNum * limit, limit}, (rs, rowNum) ->
                                getBuild(rs)
                )).build();
    }

    private int getCount() {
        return jdbcTemplate.queryForObject("select count(urlImgHash) from " + schema, Integer.class).intValue();
    }

    private Post getBuild(ResultSet rs) throws SQLException {
        return Post.builder()
                .alt(rs.getString("alt"))
                .author(rs.getString("author"))
                .comments(rs.getLong("comments"))
                .dateTime(LocalDateTime.parse(rs.getString("datetime"), dateTimeFormatter))
                .dislikes(rs.getLong("dislikes"))
                .likes(rs.getLong("likes"))
                .id(rs.getString("contentHash"))
                .pathToContent(rs.getString("pathToContent"))
                .url(rs.getString("sourceUrl"))
                .title(rs.getString("title"))
                .build();
    }

}
