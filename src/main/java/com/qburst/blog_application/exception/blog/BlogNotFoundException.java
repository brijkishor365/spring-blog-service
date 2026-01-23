package com.qburst.blog_application.exception.blog;

import com.qburst.blog_application.exception.base.ApplicationException;
import org.springframework.http.HttpStatus;

public class BlogNotFoundException extends ApplicationException {
    public BlogNotFoundException(String message) {
        super(message, "Blog not found", HttpStatus.NOT_FOUND);
    }
}
