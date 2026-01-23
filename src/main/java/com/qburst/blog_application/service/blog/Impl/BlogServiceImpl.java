package com.qburst.blog_application.service.blog.Impl;

import com.qburst.blog_application.dto.request.blog.BlogRequest;
import com.qburst.blog_application.dto.response.blog.BlogResponse;
import com.qburst.blog_application.entity.BlogEntity;
import com.qburst.blog_application.entity.CategoryEntity;
import com.qburst.blog_application.entity.UserEntity;
import com.qburst.blog_application.exception.blog.BlogNotFoundException;
import com.qburst.blog_application.exception.category.CategoryNotFoundException;
import com.qburst.blog_application.exception.user.UserNotFoundException;
import com.qburst.blog_application.mapper.BlogMapper;
import com.qburst.blog_application.repository.BlogRepository;
import com.qburst.blog_application.repository.CategoryRepository;
import com.qburst.blog_application.repository.UserRepository;
import com.qburst.blog_application.service.blog.BlogService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlogServiceImpl implements BlogService {

    private final BlogRepository blogRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final BlogMapper blogMapper;

    @Transactional
    @Override
    public BlogResponse createBlog(BlogRequest request) {
        // Fetch existing User (Author)
        UserEntity author = userRepository.findById(request.authorId())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + request.authorId()));

        // Fetch existing Category
        CategoryEntity category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Category ID: '" + request.categoryId() + "' does not exist"));

        // Use the unique slug generator
        String uniqueSlug = generateUniqueSlug(request.title());

        // Map Record DTO to Entity
        BlogEntity blog = BlogEntity.builder()
                .title(request.title())
                .content(request.content())
                .summary(request.summary())
                .imageUrl(request.imageUrl())
                .published(request.published())
                .tags(request.tags())
                .slug(uniqueSlug)
                .author(author)   // Link existing User entity
                .category(category) // Link existing Category entity
                .build();

        BlogEntity savedBlog = blogRepository.save(blog);

        // Return the Response Record
        return mapToResponse(savedBlog);
    }

    private String generateUniqueSlug(String title) {
        String baseSlug = title.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replace(" ", "-");

        String finalSlug;
        do {
            String suffix = UUID.randomUUID().toString().substring(0, 5);
            finalSlug = baseSlug + "-" + suffix;
            // Only loop if the slug is taken by an ACTIVE blog
        } while (blogRepository.existsBySlug(finalSlug));

        return finalSlug;
    }

    private BlogResponse mapToResponse(BlogEntity blog) {
        return new BlogResponse(
                blog.getId(),
                blog.getTitle(),
                blog.getSlug(),
                blog.getContent(),
                blog.getSummary(),
                blog.getImageUrl(),
                blog.getCategory() != null ? blog.getCategory().getName() : "Uncategorized",
                blog.getAuthor().getFirstname() + " " + blog.getAuthor().getLastname(),
                blog.getTags(),
                blog.getViewCount(),
                blog.getPublished(),
                blog.getCreatedAt(),
                blog.getUpdatedAt()
        );
    }

    @Transactional(readOnly = true)
    public BlogResponse getBlogBySlug(String slug) {
        return blogRepository.findBySlug(slug)
                .map(this::mapToResponse) // Convert Entity to Record DTO
                .orElseThrow(() -> new BlogNotFoundException("Blog not found with slug: " + slug));
    }

    @Transactional(readOnly = true)
    public Page<BlogResponse> getAllPublishedBlogs(Pageable pageable) {
        Page<BlogEntity> blogs = blogRepository.findByPublishedTrueOrderByCreatedAtDesc(pageable);

        return blogs.map(blogMapper::toResponse);
    }

    @Override
    public Page<BlogResponse> getAllBlogs(Pageable pageable) {
        return null;
    }

    @Override
    @Transactional
    public BlogResponse updateBlog(String slug, BlogRequest request) {
        BlogEntity existingBlog = blogRepository.findBySlug(slug)
                .orElseThrow(() -> new BlogNotFoundException("Blog not found with slug: " + slug));

        // Security Check: Only the author can update
//        Long currentUserId = SecurityUtils.getCurrentUserId();
//        if (!existingBlog.getAuthor().getId().equals(currentUserId)) {
//            throw new UnauthorizedException("You are not authorized to update this blog.");
//        }

        // Category Update: If ID changed, fetch the new entity
        if (!existingBlog.getCategory().getId().equals(request.categoryId())) {
            CategoryEntity newCategory = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new CategoryNotFoundException("Category not found"));

            // set new category
            existingBlog.setCategory(newCategory);
        }

        // Note: ignored categoryId in Mapper to avoid conflicts
        blogMapper.updateEntityFromDto(request, existingBlog);

        BlogEntity updatedBlog = blogRepository.save(existingBlog);
        return blogMapper.toResponse(updatedBlog);
    }

    @Transactional
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteBlogBySlug(String slug) {
        BlogEntity blog = blogRepository.findBySlug(slug)
                .orElseThrow(() -> new BlogNotFoundException("Blog not found with slug: " + slug));

        // Do not delete other users post
//        BlogEntity blog = blogRepository.findBySlugAndAuthorId(slug, currentUserId)
//                .orElseThrow(() -> new UnauthorizedException("You do not own this blog or it doesn't exist."));

        // Perform soft delete
        blogRepository.delete(blog);
    }

    @Override
    public BlogResponse getBlogById(Long blogId) {
        return blogRepository.findById(blogId)
                .map(this::mapToResponse) // Convert Entity to Record DTO
                .orElseThrow(() -> new EntityNotFoundException("Blog not found with ID: " + blogId));
    }

    @Override
    public List<BlogResponse> searchBlog(String keywords) {
        return List.of();
    }

    @Override
    public List<BlogResponse> getBlogByCategory(Long categoryId) {
        return List.of();
    }

    @Override
    public List<BlogResponse> getBlogByUser(Long userId) {
        return List.of();
    }
}