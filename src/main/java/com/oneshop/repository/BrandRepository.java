package com.oneshop.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.oneshop.entity.Brand;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long>{

}
