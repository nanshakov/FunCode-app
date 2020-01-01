package com.nanshakov.common.dao;

import com.nanshakov.common.dto.PostWithMeta;
import com.nanshakov.common.repo.PostMetaRepository;

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

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean containsByUrl(String hash) {
        return jdbcTemplate.queryForObject(
                "select count(urlhash) from meta where urlhash=?",
                new Object[] {hash}, Boolean.class);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean containsByContent(String hash) {
        return jdbcTemplate.queryForObject(
                "select count(contenthash) from meta where contenthash=?",
                new Object[] {hash}, Boolean.class);
    }

    @Override
    public int add(PostWithMeta p) {
        log.info("Saving to DB {}", p);
        SimpleJdbcInsert simpleJdbcInsert =
                new SimpleJdbcInsert(jdbcTemplate).withTableName("meta");
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("urlhash", p.getUrlHash());
        parameters.put("contenthash", p.getContentHash());
        parameters.put("source", p.getFrom());
        parameters.put("datetime", p.getDateTime().format(DateTimeFormatter.ofPattern("YYYY-MM-DD hh:mm:ss")));
        parameters.put("pathtocontent", p.getPathToContent());
        parameters.put("likes", p.getLikes());
        parameters.put("comments", p.getComments());
        parameters.put("alt", p.getAlt());
        parameters.put("author", p.getAuthor());

        return simpleJdbcInsert.execute(parameters);
    }
}
