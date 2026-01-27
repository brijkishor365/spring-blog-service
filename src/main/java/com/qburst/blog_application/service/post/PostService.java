package com.qburst.blog_application.service.post;

import com.qburst.blog_application.dto.request.post.PostRequest;
import com.qburst.blog_application.dto.response.post.PostResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostService {

    PostResponse createPost(PostRequest request);

    PostResponse updatePost(String blogId, PostRequest request);

    void deletePostBySlug(String slug);

    PostResponse getPostById(Long blogId);

    Page<PostResponse> getAllPosts(Pageable pageable);

    Page<PostResponse> searchPost(Pageable pageable, String keywords);

    Page<PostResponse> getPostByCategory(Pageable pageable, Long categoryId);

    Page<PostResponse> getPostByUser(Pageable pageable, Long userId);
}
