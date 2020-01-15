package com.nanshakov.controllers.response;

import com.nanshakov.common.dao.data.Post;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Result {

    private int pages;
    private int currentPage;
    private List<Post> posts;
}
