package com.oneshop.service.impl;

import com.oneshop.entity.Promotion;
import com.oneshop.repository.PromotionRepository;
import com.oneshop.service.PromotionService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PromotionServiceImpl implements PromotionService {

	@Autowired
	private PromotionRepository promotionRepository;


	@Override
	public List<Promotion> getAllPromotions() {
	return promotionRepository.findAll();
	}


	@Override
	public Promotion getPromotionById(Long id) {
	Optional<Promotion> optional = promotionRepository.findById(id);
	return optional.orElse(null);
	}


	@Override
	public Promotion savePromotion(Promotion promotion) {
	return promotionRepository.save(promotion);
	}


	@Override
	public void deletePromotion(Long id) {
	promotionRepository.deleteById(id);
	}


	@Override
	public List<Promotion> searchByNameOrType(String keyword) {
	return promotionRepository.findByNameContainingIgnoreCaseOrTypeContaining(keyword, keyword);
	}

}
