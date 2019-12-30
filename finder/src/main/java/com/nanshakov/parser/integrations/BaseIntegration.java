package com.nanshakov.parser.integrations;

import com.nanshakov.common.dto.Platform;

public interface BaseIntegration {

    void start() throws InterruptedException;

    Platform getPlatform();
}
