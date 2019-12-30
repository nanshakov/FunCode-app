package com.nanshakov.parser.dao;

import com.nanshakov.common.dto.PostWithMeta;
import com.nanshakov.parser.repo.PostMetaRepository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class Dao implements PostMetaRepository {

    private final JdbcTemplate jdbcTemplate;

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean contains(String hash) {
        return jdbcTemplate.queryForObject(
                "select count(hash) from meta where hash=?",
                new Object[] {hash}, Boolean.class);
    }

    @Override
    public void add(PostWithMeta p) {

    }
}