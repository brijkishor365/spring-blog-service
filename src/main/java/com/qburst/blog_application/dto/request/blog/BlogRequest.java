package com.qburst.blog_application.dto.request.blog;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record BlogRequest(

        @JsonProperty("title")
        @NotBlank(message = "Title is required")
        @Size(min = 5, max = 150, message = "Title should be between 5 and 150 characters")
        String title,

        @JsonProperty("content")
        @NotBlank(message = "Content cannot be empty")
        String content,

        @JsonProperty("summary")
        @Size(max = 255)
        String summary,

        @JsonProperty("category_id")
        @NotNull(message = "Category ID is required")
        Long categoryId,

        @JsonProperty("tags")
        Set<String> tags,

        @JsonProperty("author_id")
        @NotNull(message = "Author ID is required")
        Long authorId,

        @JsonProperty("published")
        Boolean published,

        @JsonProperty("imageUrl")
        String imageUrl
) {
    public BlogRequest {
        if (tags == null) {
            tags = Set.of();
        }

        if (published == null) {
            published = false;
        }
    }
}