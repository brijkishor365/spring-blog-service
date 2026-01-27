package com.qburst.blog_application.service.post.Impl;

import com.qburst.blog_application.dto.request.post.PostRequest;
import com.qburst.blog_application.dto.response.post.PostResponse;
import com.qburst.blog_application.entity.PostEntity;
import com.qburst.blog_application.entity.CategoryEntity;
import com.qburst.blog_application.entity.UserEntity;
import com.qburst.blog_application.exception.post.PostNotFoundException;
import com.qburst.blog_application.exception.category.CategoryNotFoundException;
import com.qburst.blog_application.exception.user.UserNotFoundException;
import com.qburst.blog_application.mapper.PostMapper;
import com.qburst.blog_application.repository.PostRepository;
import com.qburst.blog_application.repository.CategoryRepository;
import com.qburst.blog_application.repository.UserRepository;
import com.qburst.blog_application.service.post.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final PostMapper postMapper;

    @Transactional
    @Override
    public PostResponse createPost(PostRequest request) {
        // Fetch existing User (Author)
        UserEntity author = userRepository.findById(request.authorId())
                .orElseThrow(() -> new UserNotFoundException("User not found: " + request.authorId()));

        // Fetch existing Category
        CategoryEntity category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException("Category ID: '" + request.categoryId() + "' does not exist"));

        // Use the unique slug generator
        String uniqueSlug = generateUniqueSlug(request.title());

        // Map Record DTO to Entity
        PostEntity post = PostEntity.builder()
                .title(request.title())
                .content(request.content())
                .summary(request.summary())
                .imageUrl(request.imageUrl())
                .isPublished(request.published())
                .tags(request.tags())
                .slug(uniqueSlug)
                .author(author)   // Link existing User entity
                .category(category) // Link existing Category entity
                .build();

        PostEntity savedPost = postRepository.save(post);

        // Return the Response Record
        return mapToResponse(savedPost);
    }

    private String generateUniqueSlug(String title) {
        String baseSlug = title.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replace(" ", "-");

        String finalSlug;
        do {
            String suffix = UUID.randomUUID().toString().substring(0, 5);
            finalSlug = baseSlug + "-" + suffix;
            // Only loop if the slug is taken by an ACTIVE post
        } while (postRepository.existsBySlug(finalSlug));

        return finalSlug;
    }

    private PostResponse mapToResponse(PostEntity post) {
        return new PostResponse(
                post.getId(),
                post.getTitle(),
                post.getSlug(),
                post.getContent(),
                post.getSummary(),
                post.getImageUrl(),
                post.getCategory() != null ? post.getCategory().getName() : "Uncategorized",
                post.getAuthor().getFirstname() + " " + post.getAuthor().getLastname(),
                post.getTags(),
                post.getViewCount(),
                post.getIsPublished(),
                post.getCreatedAt(),
                post.getUpdatedAt()
        );
    }

    @Transactional(readOnly = true)
    public PostResponse getPostBySlug(String slug) {
        return postRepository.findBySlug(slug)
                .map(this::mapToResponse) // Convert Entity to Record DTO
                .orElseThrow(() -> new PostNotFoundException("Post not found with slug: " + slug));
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> getAllPublishedPosts(Pageable pageable) {
        Page<PostEntity> blogs = postRepository.findByIsPublishedTrueOrderByCreatedAtDesc(pageable);

        return blogs.map(postMapper::toResponse);
    }

    @Override
    public Page<PostResponse> getAllPosts(Pageable pageable) {
        return null;
    }

    @Override
    @Transactional
    public PostResponse updatePost(String slug, PostRequest request) {
        PostEntity existingPost = postRepository.findBySlug(slug)
                .orElseThrow(() -> new PostNotFoundException("Post not found with slug: " + slug));

        // Security Check: Only the author can update
//        Long currentUserId = SecurityUtils.getCurrentUserId();
//        if (!existingPost.getAuthor().getId().equals(currentUserId)) {
//            throw new UnauthorizedException("You are not authorized to update this post.");
//        }

        // Category Update: If ID changed, fetch the new entity
        if (!existingPost.getCategory().getId().equals(request.categoryId())) {
            CategoryEntity newCategory = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new CategoryNotFoundException("Category not found"));

            // set new category
            existingPost.setCategory(newCategory);
        }

        // Note: ignored categoryId in Mapper to avoid conflicts
        postMapper.updateEntityFromDto(request, existingPost);

        PostEntity updatedPost = postRepository.save(existingPost);
        return postMapper.toResponse(updatedPost);
    }

    @Transactional
    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void deletePostBySlug(String slug) {
        PostEntity post = postRepository.findBySlug(slug)
                .orElseThrow(() -> new PostNotFoundException("Post not found with slug: " + slug));

        // Do not delete other users post
//        PostEntity post = postRepository.findBySlugAndAuthorId(slug, currentUserId)
//                .orElseThrow(() -> new UnauthorizedException("You do not own this post, or it doesn't exist."));

        // Perform soft delete
        postRepository.delete(post);
    }

    @Override
    public PostResponse getPostById(Long blogId) {
        return postRepository.findById(blogId)
                .map(this::mapToResponse) // Convert Entity to Record DTO
                .orElseThrow(() -> new PostNotFoundException("Post not found with ID: " + blogId));
    }

    @Override
    public Page<PostResponse> searchPost(Pageable pageable, String keywords) {
        Page<PostEntity> posts = postRepository.findByTitleContainingOrContentContaining(keywords, keywords, pageable);

        return posts.map(postMapper::toResponse);
    }

    @Override
    public Page<PostResponse> getPostByCategory(Pageable pageable, Long categoryId) {
        Page<PostEntity> authorPosts = postRepository.findByCategoryId(categoryId, pageable);

        if (authorPosts.isEmpty()) {
            throw new PostNotFoundException("Post not found for User: " + categoryId);
        }

        return authorPosts.map(postMapper::toResponse);
    }

    @Override
    public Page<PostResponse> getPostByUser(Pageable pageable, Long userId) {
        Page<PostEntity> authorPosts = postRepository.findByAuthorId(userId, pageable);

        if (authorPosts.isEmpty()) {
            throw new PostNotFoundException("Post not found for User: " + userId);
        }

        return authorPosts.map(postMapper::toResponse);
    }
}