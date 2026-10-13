package com.example.storespring.dto;

import java.io.Serializable;

public class CartItemDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer productId;
    private String productTitle;
    private Integer price; // Giá gốc
    private Integer discount; // % giảm giá
    private Integer discountPrice; // Giá bán thực tế sau giảm
    private String thumbnail;
    private Integer quantity;

    public CartItemDTO() {
    }

    public CartItemDTO(Integer productId, String productTitle, Integer price, Integer discount, Integer discountPrice, String thumbnail, Integer quantity) {
        this.productId = productId;
        this.productTitle = productTitle;
        this.price = price;
        this.discount = discount;
        this.discountPrice = discountPrice;
        this.thumbnail = thumbnail;
        this.quantity = quantity;
    }

    public Double getSubtotal() {
        if (discountPrice != null && quantity != null) {
            return (double) discountPrice * quantity;
        }
        return 0.0;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getProductTitle() {
        return productTitle;
    }

    public void setProductTitle(String productTitle) {
        this.productTitle = productTitle;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Integer getDiscount() {
        return discount;
    }

    public void setDiscount(Integer discount) {
        this.discount = discount;
    }

    public Integer getDiscountPrice() {
        return discountPrice;
    }

    public void setDiscountPrice(Integer discountPrice) {
        this.discountPrice = discountPrice;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
