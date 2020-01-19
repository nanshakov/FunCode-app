package com.nanshakov.common;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.concurrent.NotThreadSafe;
import javax.validation.constraints.Null;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@NotThreadSafe
public class TagsService {

    private final Map<String, Integer> frequencies = new HashMap<>();
    private final Set<String> processedTags = new HashSet<>();

    public void addTags(Collection<String> tags) {
        for (String tag : tags) {
            push(tag.trim());
        }
    }

    public Map<String, Integer> getTags() {
        return frequencies;
    }

    public void push(String text) {
        if (!processedTags.contains(text)) {
            var count = frequencies.getOrDefault(text, 0);
            frequencies.put(text, ++count);
        }
    }

    public boolean isEmpty() {
        return frequencies.isEmpty();
    }

    public void invalidate(String text) {
        processedTags.add(text);
        frequencies.remove(text);
    }

    @SuppressWarnings("ConstantConditions")
    @Null
    public String pop() {
        if (!frequencies.isEmpty()) {
            var tag = frequencies.entrySet().stream().max(Map.Entry.comparingByValue()).orElse(null);
            if (tag == null) { return null; }
            processedTags.add(tag.getKey());
            frequencies.remove(tag.getKey());
            return tag.getKey();
        }
        return null;
    }

}
