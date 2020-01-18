package com.nanshakov.parser.integration.impl;

import com.nanshakov.common.dto.Platform;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BaseIntegrationImplTest {

    BaseIntegrationImpl<N, P> baseIntegration = new BaseIntegrationImpl<N, P>(tags, isRecursionModeEnable,
            recursionDepth,
            duplicatesCountLimit, downloadUrl) {
        @Override
        public Platform getPlatform() {
            return null;
        }

        @Override
        public void run() {

        }
    };

    @Test
    void checkLang() {
        assertFalse(baseIntegration.checkLang("Just WTF?"));
        assertTrue(baseIntegration.checkLang("Das ist mal ein statement"));
    }
}