package com.nanshakov.parser.integration.impl;

import com.nanshakov.common.dto.Platform;
import com.nanshakov.common.dto.PostDto;

import org.junit.jupiter.api.Test;

import java.util.Collection;

import javax.validation.constraints.Null;

import lombok.NonNull;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BaseIntegrationImplTest {

    BaseIntegrationImpl<Object, Object> baseIntegration = new BaseIntegrationImpl<Object, Object>() {
        @Override
        public void postConstruct() {

        }

        @Override
        public Platform getPlatform() {
            return null;
        }

        @Override
        Object getPage() {
            return null;
        }

        @Override
        @Null int incrementPage() {
            return 0;
        }

        @Override
        @Null Integer getNextPage(Object p) {
            return null;
        }

        @Override
        @NonNull PostDto parse(Object o) {
            return null;
        }

        @Override
        @NonNull Collection<Object> extractElement(Object p) {
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