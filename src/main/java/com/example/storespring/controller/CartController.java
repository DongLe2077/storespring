package com.example.storespring.controller;

import com.example.storespring.entity.Category;
import com.example.storespring.service.CartService;
import com.example.storespring.service.CategoryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class CartController {

    private final CartService cartService;
    private final CategoryService categoryService;

    @Autowired
    public CartController(CartService cartService, CategoryService categoryService) {
        this.cartService = cartService;
        this.categoryService = categoryService;
    }

    @ModelAttribute("headerCategories")
    public List<Category> headerCategories() {
        return categoryService.getAllCategories();
    }

    // Trang 5: Xem giỏ hàng
    @GetMapping("/cart")
    public String cartPage(HttpSession session, Model model) {
        model.addAttribute("cartItems", cartService.getCartItems(session));
        model.addAttribute("totalAmount", cartService.getTotalAmount(session));
        model.addAttribute("totalQuantity", cartService.getTotalQuantity(session));
        model.addAttribute("currentPage", "cart");
        return "cart/cart";
    }

    // AJAX: Thêm vào giỏ hàng (trả JSON)
    @PostMapping("/cart/add")
    @ResponseBody
    public Map<String, Object> addToCart(@RequestParam Integer productId,
                                         @RequestParam(defaultValue = "1") int quantity,
                                         HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        boolean success = cartService.addToCart(session, productId, quantity);
        response.put("success", success);
        response.put("message", success ? "Đã thêm vào giỏ hàng thành công!" : "Không thể thêm sản phẩm (hết hàng hoặc không tồn tại).");
        response.put("totalItems", cartService.getTotalQuantity(session));
        return response;
    }

    // AJAX: Cập nhật số lượng (trả JSON)
    @PostMapping("/cart/update")
    @ResponseBody
    public Map<String, Object> updateCart(@RequestParam Integer productId,
                                          @RequestParam int quantity,
                                          HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        boolean success = cartService.updateQuantity(session, productId, quantity);
        response.put("success", success);
        return response;
    }

    // Xóa một sản phẩm khỏi giỏ
    @GetMapping("/cart/remove/{productId}")
    public String removeItem(@PathVariable Integer productId, HttpSession session) {
        cartService.removeItem(session, productId);
        return "redirect:/cart";
    }

    // Xóa toàn bộ giỏ hàng
    @GetMapping("/cart/clear")
    public String clearCart(HttpSession session) {
        cartService.clearCart(session);
        return "redirect:/cart";
    }
}
