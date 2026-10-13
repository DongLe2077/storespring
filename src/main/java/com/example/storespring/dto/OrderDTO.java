package com.example.storespring.dto;

public class OrderDTO {

    private String fullname;
    private String phone;
    private String email;
    private String address;
    private String note;
    private String paymentMethod = "COD";

    public OrderDTO() {
    }

    public OrderDTO(String fullname, String phone, String email, String address, String note) {
        this.fullname = fullname;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.note = note;
        this.paymentMethod = "COD";
    }

    public OrderDTO(String fullname, String phone, String email, String address, String note, String paymentMethod) {
        this.fullname = fullname;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.note = note;
        this.paymentMethod = paymentMethod;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
}
