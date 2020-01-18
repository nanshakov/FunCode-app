package com.nanshakov.common.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostDto implements Serializable {

    @ToString.Exclude
    byte[] img;

    String contentHash;

    String urlHash;

    LocalDateTime dateTime;

    String pathToContent;

    long likes;

    long dislikes;

    long comments;

    String author;

    String url;

    String imgUrl;

    String alt;

    String title;

    Platform from;

    Type type;

    List<String> tags;
}
