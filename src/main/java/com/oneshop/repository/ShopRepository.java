package com.oneshop.repository;

import com.oneshop.entity.Shop;
import com.oneshop.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {
	Shop findByManager(User manager);
	default Shop getSingleShop() {
        return findAll().stream().findFirst().orElse(null);
    }
}
