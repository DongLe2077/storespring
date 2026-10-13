package com.example.storespring.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "`order`") // Dùng backtick vì 'order' là từ khóa bảo lưu trong SQL
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Version
    @Column(nullable = false, columnDefinition = "BIGINT DEFAULT 0")
    private long version;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "order_date")
    private LocalDateTime orderDate;

    @Column(name = "total_price", nullable = false)
    private Double totalPrice = 0.0;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private OrderStatus status = OrderStatus.PENDING;

    // Thông tin nhận hàng bổ sung
    @Column(name = "fullname", length = 100)
    private String fullname;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "note", length = 500)
    private String note;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod = "COD";

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 30)
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;

    @Column(name = "payos_order_code")
    private Long payosOrderCode;

    @Column(name = "payos_payment_link_id", length = 100)
    private String payosPaymentLinkId;

    @Column(name = "payos_checkout_url", length = 500)
    private String payosCheckoutUrl;

    @Column(name = "payos_qr_code", columnDefinition = "TEXT")
    private String payosQrCode;

    private String bankBin;
    private String bankAccountNumber;
    private String bankAccountName;
    private String bankDescription;

    public String getBankBin() { return bankBin; }
    public void setBankBin(String value) { bankBin = value; }
    public String getBankAccountNumber() { return bankAccountNumber; }
    public void setBankAccountNumber(String value) { bankAccountNumber = value; }
    public String getBankAccountName() { return bankAccountName; }
    public void setBankAccountName(String value) { bankAccountName = value; }
    public String getBankDescription() { return bankDescription; }
    public void setBankDescription(String value) { bankDescription = value; }

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "transaction_reference", length = 100)
    private String transactionReference;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        if (this.orderDate == null) {
            this.orderDate = LocalDateTime.now();
        }
        if (this.status == null) {
            this.status = OrderStatus.PENDING;
        }
        if (this.paymentStatus == null) {
            this.paymentStatus = PaymentStatus.UNPAID;
        }
    }

    public Order() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDateTime orderDate) {
        this.orderDate = orderDate;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public void addOrderItem(OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Long getPayosOrderCode() {
        return payosOrderCode;
    }

    public void setPayosOrderCode(Long payosOrderCode) {
        this.payosOrderCode = payosOrderCode;
    }

    public String getPayosPaymentLinkId() {
        return payosPaymentLinkId;
    }

    public void setPayosPaymentLinkId(String payosPaymentLinkId) {
        this.payosPaymentLinkId = payosPaymentLinkId;
    }

    public String getPayosCheckoutUrl() {
        return payosCheckoutUrl;
    }

    public void setPayosCheckoutUrl(String payosCheckoutUrl) {
        this.payosCheckoutUrl = payosCheckoutUrl;
    }

    public String getPayosQrCode() {
        return payosQrCode;
    }

    public void setPayosQrCode(String payosQrCode) {
        this.payosQrCode = payosQrCode;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(LocalDateTime paidAt) {
        this.paidAt = paidAt;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }
}
