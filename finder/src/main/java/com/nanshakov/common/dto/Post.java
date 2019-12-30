package com.nanshakov.common.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Post implements Serializable {

    String url;
    String alt;
    Platform from;
    Type type;
    @ToString.Exclude
    byte[] img;
}
