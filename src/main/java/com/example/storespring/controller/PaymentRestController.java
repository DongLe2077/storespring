package com.example.storespring.controller;

import com.example.storespring.entity.PaymentStatus;
import com.example.storespring.service.OrderService;
import com.example.storespring.service.PayOSService;
import com.example.storespring.service.PaymentAccess;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.qrcode.QRCodeWriter;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.payos.model.webhooks.Webhook;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/payment")
public class PaymentRestController {
    private static final Logger log = LoggerFactory.getLogger(PaymentRestController.class);
    private final PayOSService payOSService;
    private final OrderService orderService;

    public PaymentRestController(PayOSService payOSService, OrderService orderService) {
        this.payOSService = payOSService;
        this.orderService = orderService;
    }

    @GetMapping("/payos-webhook")
    public ResponseEntity<?> webhookHealth() {
        return ResponseEntity.ok(Map.of("status", "UP"));
    }

    @PostMapping("/payos-webhook")
    public ResponseEntity<?> webhook(@RequestBody Webhook webhook) {
        vn.payos.model.webhooks.WebhookData data;
        try {
            data = payOSService.verifyWebhook(webhook);
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("message", "Chữ ký webhook không hợp lệ."));
        }
        if (!Boolean.TRUE.equals(webhook.getSuccess()) || !"00".equals(webhook.getCode())) {
            return ResponseEntity.ok(Map.of("code", "00", "desc", "ignored"));
        }
        try {
            payOSService.processPaymentSuccess(data);
            return ResponseEntity.ok(Map.of("code", "00", "desc", "success"));
        } catch (IllegalArgumentException ex) {
            // Acknowledge signed registration samples and permanently mismatched transactions,
            // without changing any order. Only transient storage errors need delivery retries.
            log.warn("Bỏ qua giao dịch PayOS không khớp đơn hàng: {}", ex.getMessage());
            return ResponseEntity.ok(Map.of("code", "00", "desc", "ignored"));
        } catch (Exception ex) {
            log.error("Không thể lưu kết quả thanh toán PayOS", ex);
            return ResponseEntity.internalServerError().body(Map.of("message", "Vui lòng gửi lại webhook."));
        }
    }

    @GetMapping("/check-status/{orderCode}")
    public ResponseEntity<?> status(@PathVariable String orderCode, HttpSession session) {
        var order = orderService.getOrderByCode(orderCode).orElse(null);
        if (order == null || !PaymentAccess.allows(session, order)) return ResponseEntity.notFound().build();
        String key = "paymentChecked:" + orderCode;
        boolean unavailable = false;
        synchronized (session) {
            Long last = (Long) session.getAttribute(key);
            if (order.getPaymentStatus() != PaymentStatus.PAID && PayOSService.isOnline(order)
                    && order.getPayosOrderCode() != null && (last == null || System.currentTimeMillis() - last >= 15000)) {
                session.setAttribute(key, System.currentTimeMillis());
                try {
                    boolean paid = payOSService.refreshPayment(order);
                    if (paid) order.setPaymentStatus(PaymentStatus.PAID);
                } catch (Exception ex) {
                    unavailable = true;
                    log.warn("Chưa thể kiểm tra PayOS cho đơn {}", orderCode);
                }
            }
        }
        Map<String, Object> result = new HashMap<>();
        result.put("isPaid", order.getPaymentStatus() == PaymentStatus.PAID);
        result.put("paymentStatus", order.getPaymentStatus().name());
        result.put("orderStatus", order.getStatus().name());
        result.put("verificationUnavailable", unavailable);
        return ResponseEntity.ok().header("Cache-Control", "no-store").body(result);
    }

    @GetMapping(value = "/qr/{orderCode}", produces = "image/png")
    public ResponseEntity<byte[]> qr(@PathVariable String orderCode, HttpSession session) throws Exception {
        var order = orderService.getOrderByCode(orderCode).orElse(null);
        if (order == null || !PaymentAccess.allows(session, order) || order.getPayosQrCode() == null
                || order.getPaymentStatus() == PaymentStatus.PAID
                || order.getStatus() == com.example.storespring.entity.OrderStatus.CANCELLED) {
            return ResponseEntity.notFound().build();
        }
        var matrix = new QRCodeWriter().encode(order.getPayosQrCode(), BarcodeFormat.QR_CODE, 320, 320,
                Map.of(EncodeHintType.CHARACTER_SET, "UTF-8", EncodeHintType.MARGIN, 4));
        var image = new BufferedImage(320, 320, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < 320; y++) for (int x = 0; x < 320; x++) {
            image.setRGB(x, y, matrix.get(x, y) ? 0x000000 : 0xFFFFFF);
        }
        var output = new ByteArrayOutputStream();
        ImageIO.write(image, "PNG", output);
        return ResponseEntity.ok().header("Cache-Control", "no-store").body(output.toByteArray());
    }
}
