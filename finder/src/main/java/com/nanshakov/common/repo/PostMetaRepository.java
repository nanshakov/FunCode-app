package com.nanshakov.common.repo;

import com.nanshakov.common.dto.PostWithMeta;

public interface PostMetaRepository {

    boolean containsByUrl(String hash);

    boolean containsByContent(String hash);

    int add(PostWithMeta p);

}
