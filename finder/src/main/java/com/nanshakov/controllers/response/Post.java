package com.nanshakov.controllers.response;

import com.nanshakov.common.dto.Platform;
import com.nanshakov.common.dto.Type;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Post implements Serializable {

    String id;

    LocalDateTime dateTime;

    String pathToContent;

    long likes;

    long dislikes;

    long comments;

    String author;

    String url;

    String alt;

    Platform from;

    Type type;
}
