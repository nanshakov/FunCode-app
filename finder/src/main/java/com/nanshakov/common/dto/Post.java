package com.nanshakov.common.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Post implements Serializable {

    String url;
    String alt;
    Platform from;
    Type type;

}
