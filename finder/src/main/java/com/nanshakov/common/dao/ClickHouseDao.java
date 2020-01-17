package com.nanshakov.common.dao;

import com.nanshakov.common.dto.PostDto;
import com.nanshakov.common.repo.PostMetaRepository;
import com.nanshakov.controller.response.Post;
import com.nanshakov.controller.response.Result;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
@RequiredArgsConstructor
public class ClickHouseDao implements PostMetaRepository {

    private final JdbcTemplate jdbcTemplate;
    @Value("${schema}")
    private String schema;

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean containsByUrl(String hash) {
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
        log.trace("Saving to DB {}", p);
        SimpleJdbcInsert simpleJdbcInsert =
                new SimpleJdbcInsert(jdbcTemplate).withTableName(schema);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("urlImgHash", p.getUrlHash());
        parameters.put("sourceUrl", p.getUrl());
        parameters.put("contentHash", p.getContentHash());
        parameters.put("source", p.getFrom());
        parameters.put("datetime", p.getDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")));
        parameters.put("pathToContent", p.getPathToContent());
        parameters.put("likes", p.getLikes());
        parameters.put("dislikes", p.getDislikes());
        parameters.put("comments", p.getComments());
        parameters.put("alt", p.getAlt());
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
        int count = jdbcTemplate.queryForObject("select count(urlImgHash) from " + schema, Integer.class).intValue();
        int pages = (int) (count / limit) + 1;
        return Result.builder()
                .currentPage(pageNum)
                .pages(pages)
                .count(limit)
                .posts(jdbcTemplate.query(
                        "select * from " + schema + " ORDER BY datetime DESC LIMIT ?, ?",
                        new Object[] {--pageNum * limit, limit}, (rs, rowNum) ->
                                Post.builder()
                                        .alt(rs.getString("alt"))
                                        .author(rs.getString("author"))
                                        .comments(rs.getLong("comments"))
                                        .dateTime(null)
                                        .dislikes(rs.getLong("dislikes"))
                                        .likes(rs.getLong("likes"))
                                        //.from(rs.getString("source"))
                                        .id(rs.getString("contentHash"))
                                        .pathToContent(rs.getString("pathToContent"))
                                        .url(rs.getString("sourceUrl"))
                                        .build()
                )).build();
    }
}
