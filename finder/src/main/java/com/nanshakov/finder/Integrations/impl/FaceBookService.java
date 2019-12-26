package com.nanshakov.finder.Integrations.impl;

import com.nanshakov.finder.Dto.Post;
import com.nanshakov.finder.Integrations.Platform;

import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class FaceBookService extends BaseIntegrationImpl {

    @PostConstruct
    void postConstruct() {
        //template.send(topic, getNext());
    }

    @Override
    public void start() {

    }

    @Override
    public Post getNext() {
        //return new Post("test");
        return null;
    }

    @Override
    public Platform getPlatform() {
        return null;
    }
}
