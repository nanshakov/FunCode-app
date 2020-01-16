package com.nanshakov.parser.integrations;

import com.nanshakov.common.dto.Platform;

public interface BaseIntegration extends Runnable {

    Platform getPlatform();

    void printInfo();
}
