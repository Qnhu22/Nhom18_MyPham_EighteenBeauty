package com.oneshop.service;
import java.util.List;

import com.oneshop.entity.Brand;

public interface BrandService {
	List<Brand> findAll();
	Brand findById(Long id);
}
