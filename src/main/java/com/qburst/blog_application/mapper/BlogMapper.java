package com.qburst.blog_application.mapper;

import com.qburst.blog_application.dto.request.blog.BlogRequest;
import com.qburst.blog_application.dto.response.blog.BlogResponse;
import com.qburst.blog_application.entity.BlogEntity;
import com.qburst.blog_application.entity.UserEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BlogMapper {

    // 1. Convert Request to Entity
    // We must manually map 'isPublished' if the naming convention in Entity is just 'published'
    // MapStruct automatically maps matching names like 'title', 'content', 'summary', 'tags', 'imageUrl'
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "slug", ignore = true)
    BlogEntity toEntity(BlogRequest request);

    // 2. Convert Entity to Response
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "author", target = "authorFullName")
    // If Entity field is 'imageUrl' and Response field is 'imageUrl', it works automatically.
    // If Entity field is 'isPublished' and Response is 'isPublished', it works automatically.
    BlogResponse toResponse(BlogEntity blogEntity);

    List<BlogResponse> toResponseList(List<BlogEntity> entities);

    // 3. Update existing Entity from DTO
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "slug", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "category", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "viewCount", ignore = true)
    void updateEntityFromDto(BlogRequest request, @MappingTarget BlogEntity entity);

    // Helper for your 'authorFullName' mapping
    default String mapUserToFullName(UserEntity user) {
        if (user == null) return null;
        // Verify your UserEntity field names are indeed 'firstname' and 'lastname'
        return user.getFirstname() + " " + user.getLastname();
    }
}