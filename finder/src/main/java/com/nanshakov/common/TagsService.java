package com.nanshakov.common;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.concurrent.NotThreadSafe;
import javax.validation.constraints.Null;

import lombok.Builder;
import lombok.Data;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@NotThreadSafe
public class TagsService {

    private final TreeSet<Tag> tagsStore = new TreeSet<>();
    private final Map<String, Integer> frequencies = new HashMap<>();
    private final Set<String> processedTags = new HashSet<>();

    public void addTags(Collection<String> tags) {
        for (String tag : tags) {
            push(tag);
        }
    }

    public Set<String> getTags() {
        return frequencies.keySet();
    }

    public void push(String text) {
        if (!processedTags.contains(text)) {
            var tag = getLastTagByString(text);
            tagsStore.remove(tag);
            tag.increment();
            tagsStore.add(tag);
            frequencies.put(text, tag.getCount());
        }
    }

    public boolean isEmpty() {
        return frequencies.isEmpty();
    }

    public void invalidate(String text) {
        processedTags.add(text);
        tagsStore.remove(getLastTagByString(text));
    }

    @SuppressWarnings("ConstantConditions")
    @Null
    public String pop() {
        if (!tagsStore.isEmpty()) {
            String tag = tagsStore.pollLast().text;
            processedTags.add(tag);
            return tag;
        }
        return null;
    }

    private Tag getLastTagByString(String tag) {
        return Tag.builder()
                .text(tag)
                .count(frequencies.getOrDefault(tag, 0))
                .build();
    }

    @Data
    @Builder
    public static class Tag implements Comparable<Tag> {

        String text;
        int count;

        public void increment() {
            count++;
        }

        @Override
        public int compareTo(Tag o) {
            return Integer.compare(count, o.count);
        }
    }
}
