package com.nanshakov.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class Post implements Serializable {

    String url;
    String alt;
    Platform from;
    Type type;
}
