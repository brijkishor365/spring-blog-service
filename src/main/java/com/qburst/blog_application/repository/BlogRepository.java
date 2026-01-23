package com.qburst.blog_application.repository;

import com.qburst.blog_application.entity.BlogEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BlogRepository extends JpaRepository<BlogEntity, Long> {

    Optional<BlogEntity> findBySlug(String slug);

    Optional<BlogEntity> findBySlugAndAuthorId(String slug, Long authorId);

    Page<BlogEntity> findByPublishedTrueOrderByCreatedAtDesc(Pageable pageable);

    List<BlogEntity> findByCategoryId(Long categoryId);

    List<BlogEntity> findByTitleContainingIgnoreCase(String keyword);

    boolean existsBySlug(String slug);
}