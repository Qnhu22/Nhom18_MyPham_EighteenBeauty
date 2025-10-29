package com.oneshop.service.impl;

import com.oneshop.entity.Brand;
import com.oneshop.repository.BrandRepository;
import com.oneshop.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class BrandServiceImpl implements BrandService {

	@Autowired
	private BrandRepository brandRepository;

	@Override
	public List<Brand> findAll() {
		return brandRepository.findAll();
	}

	@Override
	public Brand findById(Long id) {
		return brandRepository.findById(id).orElse(null);
	}
}
