package com.nanshakov.controller.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Result {

    private int pages;
    private int currentPage;
    private int count;
    private List<Post> posts;
}
