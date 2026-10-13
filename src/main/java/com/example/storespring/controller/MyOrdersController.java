package com.example.storespring.controller;

import com.example.storespring.entity.Category;
import com.example.storespring.entity.Order;
import com.example.storespring.entity.OrderStatus;
import com.example.storespring.entity.User;
import com.example.storespring.service.CategoryService;
import com.example.storespring.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/my-orders")
public class MyOrdersController {

    private final OrderService orderService;
    private final CategoryService categoryService;

    @Autowired
    public MyOrdersController(OrderService orderService, CategoryService categoryService) {
        this.orderService = orderService;
        this.categoryService = categoryService;
    }

    @ModelAttribute("headerCategories")
    public List<Category> headerCategories() {
        return categoryService.getAllCategories();
    }

    // Xem danh sách lịch sử đơn hàng của người dùng hiện tại
    @GetMapping
    public String myOrdersPage(HttpSession session, Model model) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login?redirect=/my-orders";
        }

        List<Order> orders = orderService.getOrdersByUser(currentUser);
        model.addAttribute("orders", orders);
        model.addAttribute("currentPage", "my-orders");
        return "order/my-orders";
    }

    // Hủy đơn hàng (chỉ khi đơn hàng còn ở trạng thái PENDING)
    @PostMapping("/cancel/{id}")
    public String cancelOrder(@PathVariable Integer id,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser == null) {
            return "redirect:/login?redirect=/my-orders";
        }

        Optional<Order> orderOpt = orderService.getOrderById(id);
        if (orderOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Đơn hàng không tồn tại!");
            return "redirect:/my-orders";
        }

        Order order = orderOpt.get();
        // Kiểm tra bảo mật: đơn hàng phải thuộc về chính tài khoản đang đăng nhập
        if (order.getUser() == null || !order.getUser().getId().equals(currentUser.getId())) {
            redirectAttributes.addFlashAttribute("error", "Bạn không có quyền thao tác trên đơn hàng này!");
            return "redirect:/my-orders";
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            redirectAttributes.addFlashAttribute("error", "Chỉ có thể hủy đơn hàng khi đang ở trạng thái 'Chờ xử lý'!");
            return "redirect:/my-orders";
        }

        boolean updated = orderService.updateOrderStatus(id, OrderStatus.CANCELLED);
        if (updated) {
            redirectAttributes.addFlashAttribute("success", "Đã hủy đơn hàng #" + order.getCode() + " thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi hủy đơn hàng!");
        }

        return "redirect:/my-orders";
    }
}
