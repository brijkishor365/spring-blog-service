package com.qburst.blog_application.mapper;

import com.qburst.blog_application.dto.request.user.UserAddRequest;
import com.qburst.blog_application.dto.response.user.UserAddResponse;
import com.qburst.blog_application.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // Map Request Record -> Entity
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true) // We encode this manually in Service
    UserEntity toEntity(UserAddRequest request);

    // Map Entity -> Response Record
    UserAddResponse toResponse(UserEntity entity);
}