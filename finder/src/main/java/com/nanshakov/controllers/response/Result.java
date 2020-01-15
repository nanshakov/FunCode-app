package com.nanshakov.controllers.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Result {

    private int pages;
    private int currentPage;
    private List<Post> posts;
}
