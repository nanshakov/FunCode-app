package com.nanshakov.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostWithMeta extends Post {

    @ToString.Exclude
    byte[] img;

    String hash;

    public PostWithMeta(Post p) {
        super(p.url, p.alt, p.from, p.type);
    }
}
