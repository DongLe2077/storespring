package com.example.storespring.config;

import com.example.storespring.entity.User;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        User currentUser = (session != null) ? (User) session.getAttribute("currentUser") : null;
        String requestUri = request.getRequestURI();

        // 1. Kiểm tra quyền truy cập Admin (/admin/**)
        if (requestUri.startsWith("/admin")) {
            if (currentUser == null) {
                String redirectParam = URLEncoder.encode(requestUri, StandardCharsets.UTF_8);
                response.sendRedirect("/login?redirect=" + redirectParam);
                return false;
            }

            if (!"ADMIN".equalsIgnoreCase(currentUser.getRole())) {
                // Người dùng thông thường không có quyền truy cập Admin -> Trả lỗi HTTP 403 Forbidden mặc định
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied: Quyền truy cập Admin bị từ chối.");
                return false;
            }

            return true;
        }

        // 2. Kiểm tra trang cần đăng nhập của User thông thường (/my-orders/**)
        if (requestUri.startsWith("/my-orders")) {
            if (currentUser == null) {
                String redirectParam = URLEncoder.encode(requestUri, StandardCharsets.UTF_8);
                response.sendRedirect("/login?redirect=" + redirectParam);
                return false;
            }
            return true;
        }

        return true;
    }
}
