package com.nanshakov.finder.integrations;

import com.nanshakov.dto.Platform;

public interface BaseIntegration {

    void start();

    Platform getPlatform();
}
