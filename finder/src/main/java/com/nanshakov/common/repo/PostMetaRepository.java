package com.nanshakov.common.repo;

import com.nanshakov.common.dto.PostDto;
import com.nanshakov.controller.response.Post;
import com.nanshakov.controller.response.Result;

public interface PostMetaRepository {

    boolean containsByUrl(String hash);

    boolean containsByContent(String hash);

    int add(PostDto p);

    Post findById(String id);

    Result findByPage(int pageNum, int limit);

    Result findHot(int pageNum, int limit);
}
