package com.example.storespring.controller.admin;

import com.example.storespring.entity.User;
import com.example.storespring.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserService userService;

    @Autowired
    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    // Danh sách tài khoản người dùng
    @GetMapping
    public String listUsers(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        model.addAttribute("currentMenu", "users");
        return "admin/user/list";
    }

    // Cấp quyền hoặc hạ quyền người dùng
    @PostMapping("/change-role")
    public String changeRole(@RequestParam("userId") Integer userId,
                             @RequestParam("role") String role,
                             HttpSession session,
                             RedirectAttributes redirectAttributes) {

        User currentUser = (User) session.getAttribute("currentUser");
        if (currentUser != null && currentUser.getId().equals(userId) && "USER".equalsIgnoreCase(role)) {
            redirectAttributes.addFlashAttribute("error", "Bạn không thể tự hạ quyền Admin của chính mình!");
            return "redirect:/admin/users";
        }

        if (!"ADMIN".equalsIgnoreCase(role) && !"USER".equalsIgnoreCase(role)) {
            redirectAttributes.addFlashAttribute("error", "Quyền người dùng không hợp lệ!");
            return "redirect:/admin/users";
        }

        boolean updated = userService.changeRole(userId, role.toUpperCase());
        if (updated) {
            redirectAttributes.addFlashAttribute("success", "Cập nhật quyền thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy người dùng!");
        }

        return "redirect:/admin/users";
    }
}
