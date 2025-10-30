package com.oneshop.repository;

import com.oneshop.entity.Blog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface BlogRepository extends JpaRepository<Blog, Long> {
	List<Blog> findTop3ByOrderByCreatedAtDesc();
}
