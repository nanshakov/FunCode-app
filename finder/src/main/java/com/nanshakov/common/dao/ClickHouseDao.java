package com.nanshakov.common.dao;

import com.nanshakov.common.dto.Post;
import com.nanshakov.common.repo.PostMetaRepository;

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
                "select count(urlhash) from " + schema + " where urlhash=?",
                new Object[] {hash}, Boolean.class);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean containsByContent(String hash) {
        return jdbcTemplate.queryForObject(
                "select count(contenthash) from " + schema + " where contenthash=?",
                new Object[] {hash}, Boolean.class);
    }

    @Override
    public int add(Post p) {
        log.info("Saving to DB {}", p);
        SimpleJdbcInsert simpleJdbcInsert =
                new SimpleJdbcInsert(jdbcTemplate).withTableName("meta");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("urlImgHash", p.getUrlHash());
        parameters.put("sourceUrl", p.getUrl());
        parameters.put("contentHash", p.getContentHash());
        parameters.put("source", p.getFrom());
        parameters.put("datetime", p.getDateTime().format(DateTimeFormatter.ofPattern("YYYY-MM-DD hh:mm:ss")));
        parameters.put("pathToContent", p.getPathToContent());
        parameters.put("likes", p.getLikes());
        parameters.put("dislikes", p.getDislikes());
        parameters.put("comments", p.getComments());
        parameters.put("alt", p.getAlt());
        parameters.put("author", p.getAuthor());

        return simpleJdbcInsert.execute(parameters);
    }
}
