package com.nanshakov.parser.repo;

import com.nanshakov.common.dto.PostWithMeta;

public interface PostMetaRepository {

    boolean contains(String hash);

    void add(PostWithMeta p);

}
