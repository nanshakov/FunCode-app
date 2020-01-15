/*
   Copyright 2009 IBM Corp

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.nanshakov.lib.src.cue.lang.stop;

import com.nanshakov.lib.src.cue.lang.Counter;
import com.nanshakov.lib.src.cue.lang.WordIterator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import lombok.Getter;

/**
 * @author Jonathan Feinberg <jdf@us.ibm.com>
 */
public enum StopWords {
    English(), German();

    public final boolean stripApostrophes;
    @Getter
    private final Set<String> stopwords = new HashSet<String>();

    StopWords() {
        this(false);
    }

    StopWords(final boolean stripApostrophes) {
        this.stripApostrophes = stripApostrophes;
        loadLanguage();
    }

    public static StopWords guess(final String text) {
        return guess(new Counter<String>(new WordIterator(text)));
    }

    public static StopWords guess(final Counter<String> wordCounter) {
        return guess(wordCounter.getMostFrequent(50));
    }

    public static StopWords guess(final Collection<String> words) {
        StopWords currentWinner = null;
        int currentMax = 0;
        for (final StopWords stopWords : StopWords.values()) {
            int count = 0;
            for (final String word : words) {
                if (stopWords.isStopWord(word)) {
                    count++;
                }
            }
            if (count > currentMax) {
                currentWinner = stopWords;
                currentMax = count;
            }
        }
        return currentWinner;
    }

    public boolean isStopWord(final String s) {
        if (s.length() == 1) {
            return true;
        }
        // check rightquotes as apostrophes
        String temp = s.replace('\u2019', '\'').toLowerCase(Locale.ENGLISH);
        List<String> split = Arrays.stream(temp.split(" ")).collect(Collectors.toList());
        for (String str : split) {
            if (stopwords.contains(str)) {
                return true;
            }
        }
        return false;
    }

    private void loadLanguage() {
        final String wordlistResource = name().toLowerCase(Locale.ENGLISH);
        if (!wordlistResource.equals("custom")) {
            readStopWords(StopWords.class.getResourceAsStream("/stop/" + wordlistResource),
                    StandardCharsets.UTF_8);
        }
    }

    public void readStopWords(final InputStream inputStream, final Charset encoding) {
        try {
            final BufferedReader in = new BufferedReader(new InputStreamReader(inputStream,
                    encoding));
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    line = line.replaceAll("\\|.*", "").trim();
                    if (line.length() == 0) {
                        continue;
                    }
                    for (final String w : line.split("\\s+")) {
                        stopwords.add(w.toLowerCase(Locale.ENGLISH));
                    }
                }
            } finally {
                in.close();
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }
}
