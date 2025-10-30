package com.oneshop.service.impl;

import com.oneshop.entity.Blog;
import com.oneshop.repository.BlogRepository;
import com.oneshop.service.BlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BlogServiceImpl implements BlogService {
    private final BlogRepository blogRepository;

    @Override
    public List<Blog> getAllBlogs() {
        return blogRepository.findAll();
    }

    @Override
    public Blog getBlogById(Long id) {
        return blogRepository.findById(id).orElse(null);
    }
    
    @Override
    public List<Blog> getLatestBlogs(int limit) {
        List<Blog> blogs = blogRepository.findTop3ByOrderByCreatedAtDesc();
        return blogs.size() > limit ? blogs.subList(0, limit) : blogs;
    }
}
