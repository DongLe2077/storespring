package com.example.storespring.service;

import com.example.storespring.entity.Order;
import com.example.storespring.entity.User;
import jakarta.servlet.http.HttpSession;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Payment details are available to the buyer or the session that placed the order. */
public final class PaymentAccess {
    private PaymentAccess() {}

    @SuppressWarnings("unchecked")
    public static void remember(HttpSession session, String code) {
        Set<String> codes = (Set<String>) session.getAttribute("paymentOrders");
        if (codes == null) codes = new HashSet<>();
        codes.add(code);
        session.setAttribute("paymentOrders", codes);
    }

    public static boolean allows(HttpSession session, Order order) {
        if (session == null) return false;
        Object codes = session.getAttribute("paymentOrders");
        if (codes instanceof Set<?> set && set.contains(order.getCode())) return true;
        User user = (User) session.getAttribute("currentUser");
        return user != null && order.getUser() != null
                && user.getId().equals(order.getUser().getId());
    }

    public static String token(HttpSession session) {
        String token = (String) session.getAttribute("paymentCsrf");
        if (token == null) {
            token = UUID.randomUUID().toString();
            session.setAttribute("paymentCsrf", token);
        }
        return token;
    }
}
