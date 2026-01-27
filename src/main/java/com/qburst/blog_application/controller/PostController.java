package com.qburst.blog_application.controller;

import com.qburst.blog_application.dto.request.post.PostRequest;
import com.qburst.blog_application.dto.response.post.PostResponse;
import com.qburst.blog_application.service.post.Impl.PostServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostServiceImpl postService;

    @GetMapping
    public ResponseEntity<Page<PostResponse>> getAllPublishedPosts(@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(postService.getAllPublishedPosts(pageable));
    }

    @PostMapping
    public ResponseEntity<PostResponse> createPost(@Valid @RequestBody PostRequest request) {
        PostResponse response = postService.createPost(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{slug}")
    public ResponseEntity<PostResponse> getPostBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(postService.getPostBySlug(slug));
    }

    @DeleteMapping("/{slug}")
    public ResponseEntity<Boolean> deletePost(@PathVariable String slug) {
        log.info("Deleting post: {}", slug);

        postService.deletePostBySlug(slug);

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{slug}")
    public ResponseEntity<PostResponse> updatePost(@PathVariable String slug, @Valid @RequestBody PostRequest request) {
        PostResponse response = postService.updatePost(slug, request);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<PostResponse>> searchPost(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @RequestParam("keywords") String keywords) {

        Page<PostResponse> posts = postService.searchPost(pageable, keywords);

        return ResponseEntity.ok(posts);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<PostResponse>> getPostsByAuthor(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @PathVariable Long userId) {
        Page<PostResponse> userPosts = postService.getPostByUser(pageable, userId);

        return ResponseEntity.ok(userPosts);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<PostResponse>> getPostsByCategory(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @PathVariable Long categoryId) {
        Page<PostResponse> categoryPosts = postService.getPostByCategory(pageable, categoryId);

        return ResponseEntity.ok(categoryPosts);
    }
}