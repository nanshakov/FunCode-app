package com.nanshakov.common;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.validation.constraints.Null;

@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TagsService {

    private final Set<String> tagsStore = new HashSet<>();
    private final Set<String> processedTags = new HashSet<>();

    public void addTags(Collection<String> tag) {
        tag.forEach(t -> {
            if (!processedTags.contains(t)) {
                tagsStore.add(t);
            }
        });
    }

    public void addTag(String tag) {
        if (!processedTags.contains(tag)) {
            tagsStore.add(tag);
        }
    }

    public void markTagAsProcessed(String tag) {
        processedTags.add(tag);
        tagsStore.remove(tag);
    }

    @Null
    public String getNextTag() {
        return tagsStore.stream().findAny().orElse(null);
    }
}
