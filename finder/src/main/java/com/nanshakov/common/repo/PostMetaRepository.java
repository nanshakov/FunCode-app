package com.nanshakov.common.repo;

import com.nanshakov.common.dao.data.Post;
import com.nanshakov.common.dto.PostDto;
import com.nanshakov.controllers.response.Result;

public interface PostMetaRepository {

    boolean containsByUrl(String hash);

    boolean containsByContent(String hash);

    int add(PostDto p);

    Post findById(String id);

    Result findByPage(int pageNum, int limit);
}
