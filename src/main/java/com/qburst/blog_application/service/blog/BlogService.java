package com.qburst.blog_application.service.blog;

import com.qburst.blog_application.dto.request.blog.BlogRequest;
import com.qburst.blog_application.dto.response.blog.BlogResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface BlogService {

    BlogResponse createBlog(BlogRequest request);

    BlogResponse updateBlog(String blogId, BlogRequest request);

    @Transactional
    void deleteBlogBySlug(String slug);

    BlogResponse getBlogById(Long blogId);

    Page<BlogResponse> getAllBlogs(Pageable pageable);

    List<BlogResponse> searchBlog(String keywords);

    List<BlogResponse> getBlogByCategory(Long categoryId);

    List<BlogResponse> getBlogByUser(Long userId);
}
