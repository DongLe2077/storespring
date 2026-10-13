package com.example.storespring.controller;

import com.example.storespring.dto.UserLoginDTO;
import com.example.storespring.dto.UserRegisterDTO;
import com.example.storespring.entity.Category;
import com.example.storespring.entity.User;
import com.example.storespring.service.CartService;
import com.example.storespring.service.CategoryService;
import com.example.storespring.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class AuthController {

    private final UserService userService;
    private final CategoryService categoryService;
    private final CartService cartService;

    @Autowired
    public AuthController(UserService userService, CategoryService categoryService, CartService cartService) {
        this.userService = userService;
        this.categoryService = categoryService;
        this.cartService = cartService;
    }

    @ModelAttribute("headerCategories")
    public List<Category> headerCategories() {
        return categoryService.getAllCategories();
    }

    // === TRANG 7: REGISTER ===

    @GetMapping("/register")
    public String registerPage(Model model, HttpSession session) {
        if (session.getAttribute("currentUser") != null) {
            return "redirect:/";
        }
        model.addAttribute("registerDTO", new UserRegisterDTO());
        model.addAttribute("currentPage", "register");
        return "auth/register";
    }

    @PostMapping("/register")
    public String processRegister(@ModelAttribute UserRegisterDTO registerDTO,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        try {
            userService.register(registerDTO);
            redirectAttributes.addFlashAttribute("success", "Đăng ký thành công! Hãy đăng nhập để bắt đầu mua sắm.");
            return "redirect:/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("registerDTO", registerDTO);
            model.addAttribute("currentPage", "register");
            return "auth/register";
        }
    }

    // === TRANG 8: LOGIN ===

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String redirect, Model model, HttpSession session) {
        if (session.getAttribute("currentUser") != null) {
            return "redirect:/";
        }
        model.addAttribute("loginDTO", new UserLoginDTO());
        model.addAttribute("redirect", redirect);
        model.addAttribute("currentPage", "login");
        return "auth/login";
    }

    @PostMapping("/login")
    public String processLogin(@ModelAttribute UserLoginDTO loginDTO,
                               @RequestParam(required = false) String redirect,
                               Model model,
                               HttpSession session) {
        try {
            User user = userService.login(loginDTO.getUsername(), loginDTO.getPassword());
            // Lưu thông tin user vào Session
            session.setAttribute("currentUser", user);

            // Đồng bộ giỏ hàng từ cơ sở dữ liệu lên Session cho user
            cartService.syncUserCartOnLogin(session, user);

            // Kiểm tra redirect sau khi đăng nhập thành công
            if (redirect != null && !redirect.trim().isEmpty() && !redirect.startsWith("http")) {
                if ("ADMIN".equals(user.getRole())) {
                    return "redirect:" + redirect;
                } else if (!redirect.startsWith("/admin")) {
                    return "redirect:" + redirect;
                }
            }

            // Nếu là Admin, chuyển hướng vào Admin Panel
            if ("ADMIN".equals(user.getRole())) {
                return "redirect:/admin";
            }
            return "redirect:/";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("loginDTO", loginDTO);
            model.addAttribute("redirect", redirect);
            model.addAttribute("currentPage", "login");
            return "auth/login";
        }
    }

    // === LOGOUT ===

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        cartService.clearGuestCartOnLogout(session);
        return "redirect:/";
    }
}
