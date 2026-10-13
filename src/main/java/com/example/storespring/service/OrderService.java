package com.example.storespring.service;

import com.example.storespring.dto.CartItemDTO;
import com.example.storespring.dto.OrderDTO;
import com.example.storespring.entity.*;
import com.example.storespring.repository.OrderRepository;
import com.example.storespring.repository.ProductRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CartService cartService;

    @Autowired
    public OrderService(OrderRepository orderRepository, ProductRepository productRepository, CartService cartService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.cartService = cartService;
    }

    @Transactional
    public Order createOrder(HttpSession session, OrderDTO orderDTO, User currentUser) {
        List<CartItemDTO> cartItems = cartService.getCartItems(session);
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("Giỏ hàng đang trống, không thể đặt hàng!");
        }

        Order order = new Order();
        // Sinh mã đơn hàng ngẫu nhiên, ví dụ: ORD-172760-4921
        order.setCode("ORD-" + (System.currentTimeMillis() % 1000000) + "-" + (1000 + new Random().nextInt(9000)));
        order.setUser(currentUser);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setTotalPrice(cartService.getTotalAmount(session));

        order.setFullname(orderDTO.getFullname());
        order.setPhone(orderDTO.getPhone());
        order.setAddress(orderDTO.getAddress());
        order.setNote(orderDTO.getNote());
        String pm = orderDTO.getPaymentMethod();
        order.setPaymentMethod((pm != null && !pm.trim().isEmpty()) ? pm.trim().toUpperCase() : "COD");
        if (!java.util.Set.of("COD", "BANK_TRANSFER", "VNPAY").contains(order.getPaymentMethod())) {
            throw new IllegalArgumentException("Phương thức thanh toán chưa được hỗ trợ. Hãy chọn COD hoặc chuyển khoản VietQR.");
        }
        if (PayOSService.isOnline(order)) PayOSService.amount(order);

        for (CartItemDTO cartItem : cartItems) {
            Product product = productRepository.findById(cartItem.getProductId())
                    .orElseThrow(() -> new IllegalArgumentException("Sản phẩm ID " + cartItem.getProductId() + " không tồn tại!"));

            // Trừ số lượng tồn kho
            if (product.getQuantity() < cartItem.getQuantity()) {
                throw new IllegalStateException("Sản phẩm '" + product.getTitle() + "' không đủ số lượng tồn kho!");
            }
            product.setQuantity(product.getQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            // Tạo chi tiết đơn hàng
            OrderItem orderItem = new OrderItem(
                    order,
                    product,
                    cartItem.getQuantity(),
                    cartItem.getDiscountPrice().doubleValue()
            );
            order.addOrderItem(orderItem);
        }

        Order savedOrder = orderRepository.save(order);

        // Xóa sạch giỏ hàng sau khi đặt thành công
        cartService.clearCart(session);

        return savedOrder;
    }

    public Optional<Order> getOrderByCode(String code) {
        if (code == null) return Optional.empty();
        return orderRepository.findByCode(code);
    }

    public Optional<Order> getOrderById(Integer id) {
        if (id == null) return Optional.empty();
        return orderRepository.findById(id);
    }

    public List<Order> getOrdersByUser(User user) {
        if (user == null || user.getId() == null) return List.of();
        return orderRepository.findByUserIdOrderByOrderDateDesc(user.getId());
    }

    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAllByOrderByOrderDateDesc(pageable);
    }

    public List<Order> getAllOrdersList() {
        return orderRepository.findAllByOrderByOrderDateDesc();
    }

    public Page<Order> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatusOrderByOrderDateDesc(status, pageable);
    }

    @Transactional
    public boolean updateOrderStatus(Integer orderId, OrderStatus newStatus) {
        Optional<Order> orderOpt = orderRepository.findById(orderId);
        if (orderOpt.isEmpty()) {
            return false;
        }

        Order order = orderOpt.get();
        OrderStatus oldStatus = order.getStatus();

        // Nếu hủy đơn hàng, hoàn lại số lượng tồn kho cho các sản phẩm
        if (newStatus == OrderStatus.CANCELLED && oldStatus != OrderStatus.CANCELLED) {
            for (OrderItem item : order.getOrderItems()) {
                Product product = item.getProduct();
                if (product != null) {
                    product.setQuantity(product.getQuantity() + item.getQuantity());
                    productRepository.save(product);
                }
            }
        }

        order.setStatus(newStatus);
        orderRepository.save(order);
        return true;
    }

    public long countAllOrders() {
        return orderRepository.count();
    }

    public long countOrdersByStatus(OrderStatus status) {
        return orderRepository.countByStatus(status);
    }

    public Double getTotalRevenue() {
        Double revenue = orderRepository.sumTotalRevenue();
        return revenue != null ? revenue : 0.0;
    }
}
