package com.nanshakov.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NineGagDto {

    @JsonProperty("data")
    private Data data;
    @JsonProperty("meta")
    private Meta meta;

    @lombok.Data
    public static class Data {

        @JsonProperty("posts")
        private List<NineGagDto.Post> posts;
        @JsonProperty("nextCursor")
        private String nextCursor;
    }

    @lombok.Data
    public static class Meta {

        @JsonProperty("timestamp")
        private Integer timestamp;
        @JsonProperty("status")
        private String status;
        @JsonProperty("sid")
        private String sid;
    }

    @lombok.Data
    public static class Post {

        @JsonProperty("id")
        private String id;
        @JsonProperty("url")
        private String url;
        @JsonProperty("title")
        private String title;
        @JsonProperty("type")
        private String type;
        @JsonProperty("nsfw")
        private Integer nsfw;
        @JsonProperty("upVoteCount")
        private Integer upVoteCount;
        @JsonProperty("downVoteCount")
        private Integer downVoteCount;
        @JsonProperty("creationTs")
        private Long creationTs;
        @JsonProperty("promoted")
        private Integer promoted;
        @JsonProperty("isVoteMasked")
        private Integer isVoteMasked;
        @JsonProperty("hasLongPostCover")
        private Integer hasLongPostCover;
        @JsonProperty("images")
        private Images images;
        @JsonProperty("sourceDomain")
        private String sourceDomain;
        @JsonProperty("sourceUrl")
        private String sourceUrl;
        @JsonProperty("commentsCount")
        private Integer commentsCount;
        @JsonProperty("postSection")
        private PostSection postSection;
        @JsonProperty("tags")
        private List<Tag> tags = null;
        @JsonProperty("descriptionHtml")
        private String descriptionHtml;

    }

    @lombok.Data
    public static class PostSection {

        @JsonProperty("name")
        private String name;
        @JsonProperty("url")
        private String url;
        @JsonProperty("imageUrl")
        private String imageUrl;
        @JsonProperty("webpUrl")
        private String webpUrl;
    }

    @lombok.Data
    public static class Tag {

        @JsonProperty("key")
        private String key;
        @JsonProperty("url")
        private String url;
    }

    @lombok.Data
    public static class Images {

        @JsonProperty("image700")
        private Image700 image700;
        @JsonProperty("Image460sv")
        private Image460sv image460sv;

        public boolean isContainsVideo() {
            if (image460sv != null && image460sv.getVp8Url() != null) {
                return true;
            }
            return false;
        }

        public String getUrlVideo() {
            if (image460sv != null && image460sv.getVp8Url() != null) {
                return image460sv.getVp8Url();
            }
            return "";
        }

    }

    @lombok.Data
    public static class Image700 {

        @JsonProperty("width")
        private Integer width;
        @JsonProperty("height")
        private Integer height;
        @JsonProperty("url")
        private String url;
        @JsonProperty("webpUrl")
        private String webpUrl;

    }

    @lombok.Data
    public static class Image460sv {

        @JsonProperty("vp8Url")
        private String vp8Url;

    }
}


