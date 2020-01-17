package com.nanshakov.controller;


import com.nanshakov.common.repo.PostMetaRepository;
import com.nanshakov.controller.response.Post;
import com.nanshakov.controller.response.Result;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RestController
@Log4j2
@RequiredArgsConstructor
public class FeedController {

    @Autowired
    private PostMetaRepository postMetaRepository;

    @GetMapping("/feed")
    public Result getFeed(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum,
                          @RequestParam(value = "count", defaultValue = "50") int count) {
        return postMetaRepository.findByPage(pageNum, count);
    }

    @GetMapping("/feed/{id}")
    public Post getContent(@PathVariable(value = "id") String id) {
        return postMetaRepository.findById(id);
    }
}
