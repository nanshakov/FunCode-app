package com.nanshakov.controllers;


import com.nanshakov.common.repo.PostMetaRepository;
import com.nanshakov.controllers.response.Post;
import com.nanshakov.controllers.response.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
