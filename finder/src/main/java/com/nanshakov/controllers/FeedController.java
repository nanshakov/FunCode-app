package com.nanshakov.controllers;


import com.nanshakov.common.repo.PostMetaRepository;
import com.nanshakov.controllers.response.Result;

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
    public Result getFeed(@RequestParam(value = "pageNum", defaultValue = "1") int pageNum) {
        postMetaRepository.findByPage(pageNum);
        return new Result();
    }

    @GetMapping("/feed/{id}")
    public Result getContent(@PathVariable(value = "id") String id) {
        postMetaRepository.findById(id);
        return new Result();
    }
}
