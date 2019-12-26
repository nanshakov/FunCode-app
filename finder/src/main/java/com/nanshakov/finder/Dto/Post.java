package com.nanshakov.finder.Dto;

import com.nanshakov.finder.Integrations.Platform;
import com.nanshakov.finder.Integrations.Type;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class Post {

    String url;
    String alt;
    Platform from;
    Type type;
}
