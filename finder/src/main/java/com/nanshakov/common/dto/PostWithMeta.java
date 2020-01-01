package com.nanshakov.common.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostWithMeta extends Post {

    @ToString.Exclude
    @NonNull
    byte[] img;
    @NonNull
    String contentHash;
    @NonNull
    String urlHash;
    @NonNull
    LocalDateTime dateTime;
    @NonNull
    String pathToContent;
    long likes;
    long comments;
    String author;

    public PostWithMeta(Post p) {
        super(p.url, p.alt, p.from, p.type);
    }

    public void applyPost(Post p) {
        this.alt = p.getAlt();
        this.from = p.getFrom();
        this.url = p.getUrl();
        this.type = p.getType();
    }
}
