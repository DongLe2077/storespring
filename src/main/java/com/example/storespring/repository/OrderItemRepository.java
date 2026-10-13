package com.example.storespring.repository;

import com.example.storespring.entity.Order;
import com.example.storespring.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {

    List<OrderItem> findByOrder(Order order);

    List<OrderItem> findByOrderId(Integer orderId);

    List<OrderItem> findByProductId(Integer productId);
}
