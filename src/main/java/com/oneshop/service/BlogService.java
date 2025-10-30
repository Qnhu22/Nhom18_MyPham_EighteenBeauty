package com.oneshop.service;

import com.oneshop.entity.Blog;
import java.util.List;

public interface BlogService {
    List<Blog> getAllBlogs();
    Blog getBlogById(Long id);
}
