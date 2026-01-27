package com.qburst.blog_application.repository;

import com.qburst.blog_application.entity.PostEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<PostEntity, Long> {

    Optional<PostEntity> findBySlug(String slug);

    Optional<PostEntity> findBySlugAndAuthorId(String slug, Long authorId);

    Page<PostEntity> findByIsPublishedTrueOrderByCreatedAtDesc(Pageable pageable);

    Page<PostEntity> findByCategoryId(Long categoryId, Pageable pageable);

    List<PostEntity> findByTitleContainingIgnoreCase(String keyword);

    boolean existsBySlug(String slug);

    Page<PostEntity> findByTitleContainingOrContentContaining(String title, String content, Pageable pageable);

    Page<PostEntity> findByAuthorId(Long userId, Pageable pageable);
}