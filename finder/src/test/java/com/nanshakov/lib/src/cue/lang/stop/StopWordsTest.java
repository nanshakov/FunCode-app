package com.nanshakov.lib.src.cue.lang.stop;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StopWordsTest {

    @Test
    void checkGerman() {
        assertTrue(StopWords.German.isStopWord("Teletubbies"));
    }

    @Test
    void checkEnglish() {
        assertTrue(StopWords.German.isStopWord("Found this beauty"));
    }

    /**
     * com.nanshakov.parser.integrations.impl.BaseIntegrationImpl#checkLang(java.lang.String)
     */
    @Test
    void makeGuess() {
        int g = StopWords.German.stopWordCount("Found this beauty");
        int e = StopWords.English.stopWordCount("Found this beauty");
        assertEquals(1, g);
        assertEquals(3, e);
    }
}