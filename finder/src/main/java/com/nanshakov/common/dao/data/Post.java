package com.nanshakov.common.dao.data;

import com.nanshakov.common.dto.Platform;
import com.nanshakov.common.dto.Type;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Post implements Serializable {

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

    Platform from;

    Type type;
}
