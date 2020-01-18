package com.nanshakov.parser.integration;

import com.nanshakov.common.dto.Platform;

public interface BaseIntegration extends Runnable {

    void postConstruct();

    Platform getPlatform();

    void printInfo();
}
