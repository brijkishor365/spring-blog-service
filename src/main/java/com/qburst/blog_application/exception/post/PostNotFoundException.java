package com.qburst.blog_application.exception.post;

import com.qburst.blog_application.exception.base.ApplicationException;
import org.springframework.http.HttpStatus;

public class PostNotFoundException extends ApplicationException {
    public PostNotFoundException(String message) {
        super(message, "Post not found", HttpStatus.NOT_FOUND);
    }
}
