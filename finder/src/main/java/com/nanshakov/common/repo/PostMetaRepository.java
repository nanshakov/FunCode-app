package com.nanshakov.common.repo;

import com.nanshakov.common.dto.Post;

public interface PostMetaRepository {

    boolean containsByUrl(String hash);

    boolean containsByContent(String hash);

    int add(Post p);

}
