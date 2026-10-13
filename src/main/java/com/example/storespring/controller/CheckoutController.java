package com.example.storespring.controller;

import com.example.storespring.dto.OrderDTO;
import com.example.storespring.entity.Category;
import com.example.storespring.entity.Order;
import com.example.storespring.entity.User;
import com.example.storespring.service.CartService;
import com.example.storespring.service.CategoryService;
import com.example.storespring.service.OrderService;
import com.example.storespring.service.PayOSService;
import com.example.storespring.service.PaymentAccess;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class CheckoutController {

    private final CartService cartService;
    private final OrderService orderService;
    private final CategoryService categoryService;
    private final PayOSService payOSService;

    @Value("${payos.base-url:http://localhost:8080}")
    private String paymentBaseUrl;

    @Autowired
    public CheckoutController(CartService cartService,
                              OrderService orderService,
                              CategoryService categoryService,
                              PayOSService payOSService) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.categoryService = categoryService;
        this.payOSService = payOSService;
    }

    @ModelAttribute("headerCategories")
    public List<Category> headerCategories() {
        return categoryService.getAllCategories();
    }

    // Trang 6: Trang thanh toán
    @GetMapping("/checkout")
    public String checkoutPage(HttpSession session, Model model) {
        if (cartService.getCartItems(session).isEmpty()) {
            return "redirect:/cart";
        }

        User currentUser = (User) session.getAttribute("currentUser");

        OrderDTO orderDTO = new OrderDTO();
        // Tự điền thông tin nếu đã đăng nhập
        if (currentUser != null) {
            orderDTO.setFullname(currentUser.getFullname());
            orderDTO.setEmail(currentUser.getEmail());
        }

        model.addAttribute("orderDTO", orderDTO);
        model.addAttribute("cartItems", cartService.getCartItems(session));
        model.addAttribute("totalAmount", cartService.getTotalAmount(session));
        model.addAttribute("currentPage", "checkout");

        return "order/checkout";
    }

    // Xử lý đặt hàng
    @PostMapping("/checkout")
    public String placeOrder(@ModelAttribute OrderDTO orderDTO,
                             HttpServletRequest request,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {
        try {
            User currentUser = (User) session.getAttribute("currentUser");
            Order order = orderService.createOrder(session, orderDTO, currentUser);
            PaymentAccess.remember(session, order.getCode());

            // Tự động kết nối PayOS tạo Payment Link cho phương thức thanh toán Online
            if ("BANK_TRANSFER".equalsIgnoreCase(order.getPaymentMethod()) ||
                "VNPAY".equalsIgnoreCase(order.getPaymentMethod())) {
                try {
                    payOSService.createPaymentLink(order, paymentBaseUrl.replaceAll("/+$", ""));
                } catch (Exception payEx) {
                    redirectAttributes.addFlashAttribute("paymentError", "Chưa tạo được mã QR. Đơn hàng đã lưu, hãy thử tạo thanh toán lại.");
                }
            }

            return "redirect:/order/success/" + order.getCode();
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/checkout";
        }
    }

    // Trang đặt hàng thành công
    @GetMapping("/order/success/{code}")
    public String orderSuccess(@PathVariable String code,
                               HttpSession session,
                               Model model) {
        Order order = orderService.getOrderByCode(code).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!PaymentAccess.allows(session, order)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        model.addAttribute("order", order);
        model.addAttribute("paymentCsrf", PaymentAccess.token(session));
        model.addAttribute("orderCode", code);
        model.addAttribute("currentPage", "order-success");
        return "order/success";
    }

    @PostMapping("/order/{code}/payment/retry")
    public String retryPayment(@PathVariable String code, @RequestParam String token,
                               HttpSession session, RedirectAttributes attributes) {
        Order order = orderService.getOrderByCode(code).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!PaymentAccess.allows(session, order)) throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        if (!PaymentAccess.token(session).equals(token)) throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        synchronized (session) {
            try {
                if (order.getPayosCheckoutUrl() == null) {
                    payOSService.createPaymentLink(order, paymentBaseUrl.replaceAll("/+$", ""));
                }
            } catch (Exception ex) {
                attributes.addFlashAttribute("paymentError", "Chưa kết nối được PayOS. Vui lòng thử lại sau, không chuyển tiền khi chưa có mã QR.");
            }
        }
        return "redirect:/order/success/" + code;
    }
}
