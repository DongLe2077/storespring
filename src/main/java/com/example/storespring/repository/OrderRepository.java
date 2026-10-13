package com.example.storespring.repository;

import com.example.storespring.entity.Order;
import com.example.storespring.entity.OrderStatus;
import com.example.storespring.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {

    Optional<Order> findByCode(String code);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Order o where o.code = :code")
    Optional<Order> findForPaymentByCode(@org.springframework.data.repository.query.Param("code") String code);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    Optional<Order> findByPayosOrderCode(Long payosOrderCode);

    List<Order> findByUserOrderByOrderDateDesc(User user);

    List<Order> findByUserIdOrderByOrderDateDesc(Integer userId);

    List<Order> findAllByOrderByOrderDateDesc();

    Page<Order> findAllByOrderByOrderDateDesc(Pageable pageable);

    Page<Order> findByStatusOrderByOrderDateDesc(OrderStatus status, Pageable pageable);

    long countByStatus(OrderStatus status);

    @Query("SELECT COALESCE(SUM(o.totalPrice), 0.0) FROM Order o WHERE o.status = com.example.storespring.entity.OrderStatus.COMPLETED")
    Double sumTotalRevenue();
}
