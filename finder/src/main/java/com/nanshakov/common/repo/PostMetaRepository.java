package com.nanshakov.common.repo;

import com.nanshakov.common.dto.PostDto;

public interface PostMetaRepository {

    boolean containsByUrl(String hash);

    boolean containsByContent(String hash);

    int add(PostDto p);

}
