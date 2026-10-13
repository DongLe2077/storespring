package com.example.storespring;

import com.example.storespring.controller.CheckoutController;
import com.example.storespring.controller.PaymentRestController;
import com.example.storespring.entity.*;
import com.example.storespring.repository.OrderRepository;
import com.example.storespring.service.*;
import com.google.zxing.*;
import com.google.zxing.common.HybridBinarizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.ui.ExtendedModelMap;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.*;
import vn.payos.model.webhooks.*;
import javax.imageio.ImageIO;
import java.io.ByteArrayInputStream;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PaymentFlowTest {
    PayOS sdk;
    OrderRepository repository;
    PayOSService payment;
    OrderService orders;
    PaymentRestController api;
    Order order;
    MockHttpSession session;

    @BeforeEach void setup() {
        sdk = mock(PayOS.class, RETURNS_DEEP_STUBS);
        repository = mock(OrderRepository.class);
        payment = new PayOSService(sdk, repository);
        orders = mock(OrderService.class);
        api = new PaymentRestController(payment, orders);
        order = new Order();
        order.setId(42);
        order.setCode("ORD-123-4567");
        order.setPaymentMethod("BANK_TRANSFER");
        order.setTotalPrice(25000d);
        order.setPayosOrderCode(42L);
        order.setPayosPaymentLinkId("link-42");
        session = new MockHttpSession();
        PaymentAccess.remember(session, order.getCode());
        when(repository.findByPayosOrderCode(42L)).thenReturn(Optional.of(order));
        when(repository.findForPaymentByCode(order.getCode())).thenReturn(Optional.of(order));
        when(orders.getOrderByCode(order.getCode())).thenReturn(Optional.of(order));
    }

    WebhookData receipt() {
        var data = new WebhookData();
        data.setOrderCode(42L); data.setAmount(25000L); data.setPaymentLinkId("link-42");
        data.setCode("00"); data.setCurrency("VND"); data.setReference("bank-reference");
        data.setDescription("AURA 1234567"); data.setAccountNumber("123456");
        data.setTransactionDateTime("2026-10-13 10:00:00"); data.setDesc("Thành công");
        return data;
    }

    @Test void verifiedReceiptMarksPaidAndDuplicatesDoNotChangeReceipt() {
        payment.processPaymentSuccess(receipt());
        assertEquals(PaymentStatus.PAID, order.getPaymentStatus());
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        var paidAt = order.getPaidAt();
        var duplicate = receipt(); duplicate.setReference("different-reference");
        payment.processPaymentSuccess(duplicate);
        assertEquals(paidAt, order.getPaidAt());
        assertEquals("bank-reference", order.getTransactionReference());
        verify(repository, times(1)).save(order);
    }

    @Test void wrongAmountCannotMarkPaid() {
        var data = receipt(); data.setAmount(20000L);
        assertThrows(IllegalArgumentException.class, () -> payment.processPaymentSuccess(data));
        assertEquals(PaymentStatus.UNPAID, order.getPaymentStatus());
        verify(repository, never()).save(any());
    }

    @Test void wrongLinkCannotMarkPaid() {
        var data = receipt(); data.setPaymentLinkId("other-link");
        assertThrows(IllegalArgumentException.class, () -> payment.processPaymentSuccess(data));
    }

    @Test void failedTransactionOrWrongCurrencyCannotMarkPaid() {
        var data = receipt(); data.setCode("01");
        assertThrows(IllegalArgumentException.class, () -> payment.processPaymentSuccess(data));
        data.setCode("00"); data.setCurrency("USD");
        assertThrows(IllegalArgumentException.class, () -> payment.processPaymentSuccess(data));
    }

    @Test void cancelledOrderIsNotReopenedWhenMoneyArrives() {
        order.setStatus(OrderStatus.CANCELLED);
        payment.processPaymentSuccess(receipt());
        assertEquals(PaymentStatus.PAID, order.getPaymentStatus());
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test void invalidSignatureRejectedByActualSdk() {
        var real = new PayOS("test-client", "test-api", "test-checksum");
        var webhook = new Webhook();
        webhook.setCode("00"); webhook.setDesc("success"); webhook.setSuccess(true);
        webhook.setData(receipt()); webhook.setSignature("invalid-signature");
        var service = new PayOSService(real, repository);
        var response = new PaymentRestController(service, orders).webhook(webhook);
        assertEquals(400, response.getStatusCode().value());
        verify(repository, never()).save(any());
    }

    Webhook signedWebhook(PayOS real) {
        var hook = new Webhook();
        hook.setCode("00"); hook.setDesc("success"); hook.setSuccess(true); hook.setData(receipt());
        hook.setSignature(real.getCrypto().createSignatureFromObj(hook.getData(), "test-checksum"));
        return hook;
    }

    @Test void actualSignedWebhookWorksThroughHttpJsonBinding() throws Exception {
        var real = new PayOS("test-client", "test-api", "test-checksum");
        var controller = new PaymentRestController(new PayOSService(real, repository), orders);
        var mvc = org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup(controller).build();
        var json = new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsBytes(signedWebhook(real));
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/payment/payos-webhook")
                .contentType("application/json").content(json))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.status().isOk());
        assertEquals(PaymentStatus.PAID, order.getPaymentStatus());
    }

    @Test void tamperingWithAmountAfterSigningIsRejected() {
        var real = new PayOS("test-client", "test-api", "test-checksum");
        var hook = signedWebhook(real); hook.getData().setAmount(10000L);
        var response = new PaymentRestController(new PayOSService(real, repository), orders).webhook(hook);
        assertEquals(400, response.getStatusCode().value());
        assertEquals(PaymentStatus.UNPAID, order.getPaymentStatus());
    }

    @Test void storageFailureRequestsWebhookRetryInsteadOfAcknowledgingSuccess() {
        var real = new PayOS("test-client", "test-api", "test-checksum");
        when(repository.findByPayosOrderCode(42L)).thenThrow(new IllegalStateException("database offline"));
        var response = new PaymentRestController(new PayOSService(real, repository), orders).webhook(signedWebhook(real));
        assertEquals(500, response.getStatusCode().value());
    }

    @Test void serverReconciliationConfirmsPaidWithoutWebhook() {
        var link = mock(PaymentLink.class);
        when(link.getStatus()).thenReturn(PaymentLinkStatus.PAID);
        when(link.getId()).thenReturn("link-42"); when(link.getOrderCode()).thenReturn(42L);
        when(link.getAmount()).thenReturn(25000L); when(link.getAmountPaid()).thenReturn(25000L);
        when(sdk.paymentRequests().get(42L)).thenReturn(link);
        assertTrue(payment.refreshPayment(order));
        assertEquals(PaymentStatus.PAID, order.getPaymentStatus());
    }

    @Test void pendingOrUnderpaidLinkCannotMarkPaid() {
        var link = mock(PaymentLink.class);
        when(link.getStatus()).thenReturn(PaymentLinkStatus.UNDERPAID);
        when(sdk.paymentRequests().get(42L)).thenReturn(link);
        assertFalse(payment.refreshPayment(order));
        assertEquals(PaymentStatus.UNPAID, order.getPaymentStatus());
    }

    @Test void qrPngDecodesToExactlyThePayosPayload() throws Exception {
        String payload = "00020101021238540010A00000072701240006970422011003836898910208QRIBFTTA53037045405250005802VN6304ABCD";
        order.setPayosQrCode(payload);
        var response = api.qr(order.getCode(), session);
        var image = ImageIO.read(new ByteArrayInputStream(response.getBody()));
        var pixels = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
        var bitmap = new BinaryBitmap(new HybridBinarizer(new RGBLuminanceSource(image.getWidth(), image.getHeight(), pixels)));
        assertEquals(payload, new MultiFormatReader().decode(bitmap).getText());
    }

    @Test void anotherSessionCannotReadPaymentOrQr() throws Exception {
        order.setPayosQrCode("example");
        var stranger = new MockHttpSession();
        assertEquals(404, api.status(order.getCode(), stranger).getStatusCode().value());
        assertEquals(404, api.qr(order.getCode(), stranger).getStatusCode().value());
    }

    @Test void accountOwnerCanAccessOrdersAfterStartingNewSession() {
        var user = new User(); user.setId(7); order.setUser(user);
        var ownerSession = new MockHttpSession(); ownerSession.setAttribute("currentUser", user);
        assertTrue(PaymentAccess.allows(ownerSession, order));
    }

    @Test void returnPageDoesNotMarkOrderPaid() {
        var controller = new CheckoutController(mock(CartService.class), orders, mock(CategoryService.class), payment);
        assertEquals("order/success", controller.orderSuccess(order.getCode(), session, new ExtendedModelMap()));
        assertEquals(PaymentStatus.UNPAID, order.getPaymentStatus());
        verify(repository, never()).save(any());
    }

    @Test void createRequestHasExactAmountAndNoPaidTrueReturnUrl() {
        var response = mock(CreatePaymentLinkResponse.class);
        when(response.getOrderCode()).thenReturn(42L); when(response.getAmount()).thenReturn(25000L);
        when(response.getQrCode()).thenReturn("qr-payload"); when(response.getCheckoutUrl()).thenReturn("https://pay.payos.vn/example");
        when(response.getAccountNumber()).thenReturn("123456"); when(response.getAccountName()).thenReturn("TEST");
        when(response.getPaymentLinkId()).thenReturn("link-42");
        when(sdk.paymentRequests().create(any(CreatePaymentLinkRequest.class))).thenReturn(response);
        payment.createPaymentLink(order, "https://shop.example");
        var request = org.mockito.ArgumentCaptor.forClass(CreatePaymentLinkRequest.class);
        verify(sdk.paymentRequests()).create(request.capture());
        assertEquals(25000L, request.getValue().getAmount());
        assertFalse(request.getValue().getReturnUrl().contains("paid=true"));
        assertEquals("qr-payload", order.getPayosQrCode());
        assertEquals("123456", order.getBankAccountNumber());
    }

    @Test void tinyAmountIsRejectedInsteadOfSilentlyIncreasingCharge() {
        order.setTotalPrice(1000d);
        assertThrows(IllegalArgumentException.class, () -> PayOSService.amount(order));
    }

    @Test void retryReusesExistingPaymentLinkWithoutCreatingAnother() {
        order.setPayosCheckoutUrl("https://pay.payos.vn/existing");
        assertNull(payment.createPaymentLink(order, "https://shop.example"));
        verify(sdk, never()).paymentRequests();
    }

    @Test void remoteFailureDoesNotPretendPaymentSucceeded() {
        when(sdk.paymentRequests().get(42L)).thenThrow(new IllegalStateException("offline"));
        var response = api.status(order.getCode(), session);
        var result = (Map<?, ?>) response.getBody();
        assertEquals(false, result.get("isPaid"));
        assertEquals(true, result.get("verificationUnavailable"));
        assertEquals(PaymentStatus.UNPAID, order.getPaymentStatus());
    }

    @Test void pollingThrottlesRequestsToPayos() {
        var link = mock(PaymentLink.class); when(link.getStatus()).thenReturn(PaymentLinkStatus.PENDING);
        when(sdk.paymentRequests().get(42L)).thenReturn(link);
        api.status(order.getCode(), session); api.status(order.getCode(), session);
        verify(sdk.paymentRequests(), times(1)).get(42L);
    }

    @Test void csrfMismatchCannotRetryPayment() {
        var controller = new CheckoutController(mock(CartService.class), orders, mock(CategoryService.class), payment);
        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> controller.retryPayment(order.getCode(), "forged", session,
                        new org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap()));
        verify(repository, never()).findForPaymentByCode(anyString());
    }

    @Test void paymentTemplateRendersOnlineCodPaidAndCancelledStates() {
        var resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/"); resolver.setSuffix(".html"); resolver.setCharacterEncoding("UTF-8");
        var engine = new SpringTemplateEngine(); engine.setTemplateResolver(resolver);
        var servlet = new MockServletContext();
        var request = new MockHttpServletRequest(servlet); request.setSession(session);
        var exchange = JakartaServletWebApplication.buildApplication(servlet).buildExchange(request, new MockHttpServletResponse());
        var context = new WebContext(exchange, Locale.forLanguageTag("vi"));
        context.setVariable("order", order); context.setVariable("paymentCsrf", "test-token");
        context.setVariable("headerCategories", List.of());
        String unavailable = engine.process("order/success", context);
        assertTrue(unavailable.contains("Tạo lại mã QR"));
        order.setPayosCheckoutUrl("https://pay.payos.vn/example"); order.setBankAccountName("TEST ACCOUNT");
        String online = engine.process("order/success", context);
        assertTrue(online.contains("/api/payment/qr/ORD-123-4567"));
        assertTrue(online.contains("TEST ACCOUNT"));
        order.setPaymentMethod("COD");
        assertTrue(engine.process("order/success", context).contains("nhân viên giao hàng"));
        order.setPaymentStatus(PaymentStatus.PAID); order.setPaidAt(java.time.LocalDateTime.now());
        assertTrue(engine.process("order/success", context).contains("Thời gian ghi nhận"));
        order.setStatus(OrderStatus.CANCELLED);
        assertTrue(engine.process("order/success", context).contains("Đơn hàng đã hủy"));
    }
}
