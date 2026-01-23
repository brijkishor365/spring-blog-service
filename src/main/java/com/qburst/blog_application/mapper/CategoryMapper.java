package com.qburst.blog_application.mapper;

import com.qburst.blog_application.dto.request.category.Category;
import com.qburst.blog_application.entity.CategoryEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.Optional;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", ignore = false)
    @Mapping(target = "description", ignore = false)
    CategoryEntity toEntity(Category category);

    Category toResponse(CategoryEntity categoryEntity);

    List<Category> toResponseList(List<CategoryEntity> entities);

    @Mapping(target = "id", ignore = true) // Ensure the ID isn't overwritten by the DTO
    void updateEntityFromDto(Category category, @MappingTarget CategoryEntity entity);
}
