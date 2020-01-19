package com.nanshakov.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class TagsServiceTest {

    @Test
    void push() {
        TagsService tagsService = new TagsService();
        tagsService.push("a");
        tagsService.push("a");
        tagsService.push("b");
        assertEquals("a", tagsService.pop());
        tagsService.push("a");
        assertEquals("b", tagsService.pop());
    }

    @Test
    void pushSeveral() {
        TagsService tagsService = new TagsService();
        tagsService.push("a");
        tagsService.push("b");
        assertNotNull(tagsService.pop());
        assertNotNull(tagsService.pop());
    }

    @Test
    void invalidate() {
        TagsService tagsService = new TagsService();
        tagsService.push("a");
        tagsService.push("a");
        tagsService.invalidate("a");
        assertNull(tagsService.pop());
        tagsService.push("a");
        assertNull(tagsService.pop());

    }
}