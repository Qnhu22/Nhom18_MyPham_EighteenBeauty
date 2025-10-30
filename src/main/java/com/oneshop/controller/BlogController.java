package com.oneshop.controller;

import com.oneshop.entity.Blog;
import com.oneshop.service.BlogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class BlogController {
    private final BlogService blogService;

    @GetMapping("/blog")
    public String blogList(Model model) {
        model.addAttribute("blogs", blogService.getAllBlogs());
        model.addAttribute("pageTitle", "Blog - OneShop");
        return "blog/blog-list";
    }

    @GetMapping("/blog/{id}")
    public String blogDetail(@PathVariable Long id, Model model) {
        Blog blog = blogService.getBlogById(id);
        if (blog == null) return "redirect:/blog";
        model.addAttribute("blog", blog);
        model.addAttribute("pageTitle", blog.getTitle());
        return "blog/blog-detail";
    }
}
