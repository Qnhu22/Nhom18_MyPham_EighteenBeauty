package com.oneshop.service;

import java.util.List;

import com.oneshop.entity.Promotion;

public interface PromotionService {
	List<Promotion> getAllPromotions();
	Promotion getPromotionById(Long id);
	Promotion savePromotion(Promotion promotion);
	void deletePromotion(Long id);
	List<Promotion> searchByNameOrType(String keyword);
}
