package com.example.storespring.controller.admin;

import com.example.storespring.entity.Order;
import com.example.storespring.entity.OrderStatus;
import com.example.storespring.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    private final OrderService orderService;

    @Autowired
    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // Danh sách đơn hàng toàn hệ thống (lọc theo trạng thái + phân trang)
    @GetMapping
    public String listOrders(
            @RequestParam(value = "status", required = false) String statusStr,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {

        OrderStatus status = null;
        if (statusStr != null && !statusStr.trim().isEmpty() && !"ALL".equalsIgnoreCase(statusStr)) {
            try {
                status = OrderStatus.valueOf(statusStr.toUpperCase());
            } catch (IllegalArgumentException ignored) {
            }
        }

        Page<Order> orderPage;
        if (status != null) {
            orderPage = orderService.getOrdersByStatus(status, PageRequest.of(page, 10));
        } else {
            orderPage = orderService.getAllOrders(PageRequest.of(page, 10));
        }

        model.addAttribute("orderPage", orderPage);
        model.addAttribute("selectedStatus", status != null ? status.name() : "ALL");
        model.addAttribute("allStatuses", OrderStatus.values());
        model.addAttribute("currentMenu", "orders");

        return "admin/order/list";
    }

    // Xem chi tiết đơn hàng
    @GetMapping("/{id}")
    public String orderDetail(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Order> orderOpt = orderService.getOrderById(id);
        if (orderOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Đơn hàng #" + id + " không tồn tại!");
            return "redirect:/admin/orders";
        }

        model.addAttribute("order", orderOpt.get());
        model.addAttribute("allStatuses", OrderStatus.values());
        model.addAttribute("currentMenu", "orders");

        return "admin/order/detail";
    }

    // Cập nhật trạng thái đơn hàng (tự động hoàn kho nếu chuyển sang CANCELLED)
    @PostMapping("/update-status")
    public String updateStatus(@RequestParam("orderId") Integer orderId,
                               @RequestParam("status") String statusStr,
                               RedirectAttributes redirectAttributes) {
        try {
            OrderStatus newStatus = OrderStatus.valueOf(statusStr.toUpperCase());
            boolean updated = orderService.updateOrderStatus(orderId, newStatus);
            if (updated) {
                redirectAttributes.addFlashAttribute("success", "Cập nhật trạng thái đơn hàng #" + orderId + " thành công!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Không thể cập nhật trạng thái đơn hàng!");
            }
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", "Trạng thái đơn hàng không hợp lệ!");
        }

        return "redirect:/admin/orders/" + orderId;
    }
}
