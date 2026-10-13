package com.example.storespring.service;

import com.example.storespring.entity.Order;
import com.example.storespring.entity.OrderStatus;
import com.example.storespring.entity.PaymentStatus;
import com.example.storespring.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;
import vn.payos.model.v2.paymentRequests.PaymentLinkStatus;
import vn.payos.model.webhooks.Webhook;
import vn.payos.model.webhooks.WebhookData;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class PayOSService {
    private final PayOS payOS;
    private final OrderRepository orders;

    public PayOSService(PayOS payOS, OrderRepository orders) {
        this.payOS = payOS;
        this.orders = orders;
    }

    public static boolean isOnline(Order order) {
        return "BANK_TRANSFER".equals(order.getPaymentMethod()) || "VNPAY".equals(order.getPaymentMethod());
    }

    public static long amount(Order order) {
        Double total = order.getTotalPrice();
        if (total == null || !Double.isFinite(total) || total < 2000 || total != Math.rint(total)) {
            throw new IllegalArgumentException("Thanh toán VietQR cần số tiền nguyên từ 2.000 đồng.");
        }
        return total.longValue();
    }

    @Transactional
    public CreatePaymentLinkResponse createPaymentLink(Order order, String baseUrl) {
        order = orders.findForPaymentByCode(order.getCode()).orElseThrow();
        if (!isOnline(order) || order.getPaymentStatus() == PaymentStatus.PAID
                || order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalArgumentException("Đơn hàng không thể tạo thanh toán chuyển khoản.");
        }
        if (order.getPayosCheckoutUrl() != null) return null;
        long orderCode = order.getPayosOrderCode() != null ? order.getPayosOrderCode()
                : order.getOrderDate().toInstant(java.time.ZoneOffset.UTC).toEpochMilli() * 1000L + order.getId() % 1000;
        String description = "AURA " + order.getCode().replace("ORD-", "").replace("-", "");
        if (description.length() > 25) description = description.substring(0, 25);
        var request = CreatePaymentLinkRequest.builder()
                .orderCode(orderCode).amount(amount(order)).description(description)
                .returnUrl(baseUrl + "/order/success/" + order.getCode())
                .cancelUrl(baseUrl + "/order/success/" + order.getCode() + "?cancel=true")
                .buyerName(order.getFullname()).buyerPhone(order.getPhone()).buyerAddress(order.getAddress())
                .build();
        var response = payOS.paymentRequests().create(request);
        if (!Objects.equals(response.getOrderCode(), orderCode)
                || !Objects.equals(response.getAmount(), amount(order))
                || response.getQrCode() == null || response.getCheckoutUrl() == null) {
            throw new IllegalStateException("Thông tin thanh toán từ PayOS không khớp đơn hàng.");
        }
        order.setPayosOrderCode(orderCode);
        order.setPayosPaymentLinkId(response.getPaymentLinkId());
        order.setPayosCheckoutUrl(response.getCheckoutUrl());
        order.setPayosQrCode(response.getQrCode());
        order.setBankBin(response.getBin());
        order.setBankAccountNumber(response.getAccountNumber());
        order.setBankAccountName(response.getAccountName());
        order.setBankDescription(response.getDescription());
        orders.save(order);
        return response;
    }

    public WebhookData verifyWebhook(Webhook webhook) {
        return payOS.webhooks().verify(webhook);
    }

    @Transactional
    public Order processPaymentSuccess(WebhookData data) {
        if (data == null || !"00".equals(data.getCode()) || !"VND".equals(data.getCurrency())) {
            throw new IllegalArgumentException("Giao dịch chưa thành công hoặc sai loại tiền.");
        }
        Order order = orders.findByPayosOrderCode(data.getOrderCode()).orElse(null);
        // PayOS sends a signed example transaction when registering a webhook.
        if (order == null) return null;
        if (!isOnline(order) || !Objects.equals(data.getPaymentLinkId(), order.getPayosPaymentLinkId())
                || !Objects.equals(data.getAmount(), amount(order))
                || data.getReference() == null || data.getReference().isBlank()) {
            throw new IllegalArgumentException("Giao dịch không khớp mã thanh toán hoặc số tiền đơn hàng.");
        }
        markPaid(order, data.getReference());
        return order;
    }

    /** Server verification also handles delayed webhooks and localhost testing. */
    @Transactional
    public boolean refreshPayment(Order order) {
        if (order.getPaymentStatus() == PaymentStatus.PAID) return true;
        if (!isOnline(order) || order.getPayosOrderCode() == null) return false;
        var link = payOS.paymentRequests().get(order.getPayosOrderCode());
        if (link.getStatus() != PaymentLinkStatus.PAID) return false;
        if (!Objects.equals(link.getId(), order.getPayosPaymentLinkId())
                || !Objects.equals(link.getOrderCode(), order.getPayosOrderCode())
                || !Objects.equals(link.getAmount(), amount(order))
                || link.getAmountPaid() == null || link.getAmountPaid() < amount(order)) {
            throw new IllegalArgumentException("Thông tin xác nhận PayOS không khớp đơn hàng.");
        }
        Order locked = orders.findByPayosOrderCode(order.getPayosOrderCode()).orElseThrow();
        markPaid(locked, "PAYOS-" + link.getId());
        return true;
    }

    private void markPaid(Order order, String reference) {
        if (order.getPaymentStatus() == PaymentStatus.PAID) return;
        order.setPaymentStatus(PaymentStatus.PAID);
        if (order.getStatus() == OrderStatus.PENDING) order.setStatus(OrderStatus.CONFIRMED);
        order.setPaidAt(LocalDateTime.now());
        order.setTransactionReference(reference);
        orders.save(order);
    }
}
