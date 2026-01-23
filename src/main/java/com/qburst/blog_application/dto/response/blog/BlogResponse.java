package com.qburst.blog_application.dto.response.blog;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.Set;

public record BlogResponse(

        @JsonProperty("id")
        Long id,

        @JsonProperty("title")
        String title,

        @JsonProperty("slug")
        String slug,

        @JsonProperty("content")
        String content,

        @JsonProperty("summary")
        String summary,

        @JsonProperty("image_url")
        String imageUrl,

        @JsonProperty("category_name")
        String categoryName,

        @JsonProperty("author")
        String authorFullName,

        @JsonProperty("tags")
        Set<String> tags,

        @JsonProperty("view_count")
        Long viewCount,

        @JsonProperty("published")
        Boolean published,

        @JsonProperty("created_at")
        LocalDateTime createdAt,

        @JsonProperty("updated_at")
        LocalDateTime updatedAt
) {
}