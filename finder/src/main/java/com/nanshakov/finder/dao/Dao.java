package com.nanshakov.finder.dao;

import com.nanshakov.dto.Post;
import com.nanshakov.dto.PostWithMeta;
import com.nanshakov.finder.repo.PostMetaRepository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class Dao implements PostMetaRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Post> findAll() {
        return null;
    }

    @Override
    public Post findOne(String hash) {
        return null;
    }

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
