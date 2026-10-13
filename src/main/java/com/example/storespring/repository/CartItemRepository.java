package com.example.storespring.repository;

import com.example.storespring.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Integer> {

    List<CartItem> findByUserId(Integer userId);

    Optional<CartItem> findByUserIdAndProductId(Integer userId, Integer productId);

    @Modifying
    @Transactional
    void deleteByUserId(Integer userId);

    @Modifying
    @Transactional
    void deleteByUserIdAndProductId(Integer userId, Integer productId);
}
