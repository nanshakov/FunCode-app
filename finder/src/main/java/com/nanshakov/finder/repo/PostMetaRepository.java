package com.nanshakov.finder.repo;

import com.nanshakov.dto.PostWithMeta;

public interface PostMetaRepository {

    boolean contains(String hash);

    void add(PostWithMeta p);

}
