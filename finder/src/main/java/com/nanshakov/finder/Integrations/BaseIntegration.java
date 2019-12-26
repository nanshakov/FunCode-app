package com.nanshakov.finder.Integrations;

import com.nanshakov.finder.Dto.Post;

public interface BaseIntegration {

    void start();

    Post getNext();

    Platform getPlatform();
}
