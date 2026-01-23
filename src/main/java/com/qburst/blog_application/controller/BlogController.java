package com.qburst.blog_application.controller;

import com.qburst.blog_application.dto.request.blog.BlogRequest;
import com.qburst.blog_application.dto.response.blog.BlogResponse;
import com.qburst.blog_application.service.blog.Impl.BlogServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/blogs")
@RequiredArgsConstructor
public class BlogController {

    private final BlogServiceImpl blogService;

    @GetMapping
    public ResponseEntity<Page<BlogResponse>> getAllPublishedBlogs(@PageableDefault(size = 5, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(blogService.getAllPublishedBlogs(pageable));
    }

    @PostMapping
    public ResponseEntity<BlogResponse> createBlog(@Valid @RequestBody BlogRequest request) {
        BlogResponse response = blogService.createBlog(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{slug}")
    public ResponseEntity<BlogResponse> getBlogBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(blogService.getBlogBySlug(slug));
    }

    @DeleteMapping("/{slug}")
    public ResponseEntity<Boolean> deleteBlog(@PathVariable String slug) {
        log.info("Deleting blog: {}", slug);

        blogService.deleteBlogBySlug(slug);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{slug}")
    public ResponseEntity<BlogResponse> updateBlog(@PathVariable String slug, @Valid @RequestBody BlogRequest request) {
        BlogResponse response = blogService.updateBlog(slug, request);

        return ResponseEntity.ok(response);
    }
}