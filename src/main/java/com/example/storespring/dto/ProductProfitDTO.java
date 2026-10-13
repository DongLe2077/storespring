package com.example.storespring.dto;

public class ProductProfitDTO {

    private Integer id;
    private String title;
    private String thumbnail;
    private String categoryName;
    private Integer price;
    private Integer costPrice;
    private Integer profitPerUnit;
    private int quantitySold;
    private int stockQuantity;
    private double totalRevenue;
    private double totalProfit;
    private double profitMarginPercent;
    private String stockStatus;

    public ProductProfitDTO() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Integer getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(Integer costPrice) {
        this.costPrice = costPrice;
    }

    public Integer getProfitPerUnit() {
        return profitPerUnit;
    }

    public void setProfitPerUnit(Integer profitPerUnit) {
        this.profitPerUnit = profitPerUnit;
    }

    public int getQuantitySold() {
        return quantitySold;
    }

    public void setQuantitySold(int quantitySold) {
        this.quantitySold = quantitySold;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public double getTotalProfit() {
        return totalProfit;
    }

    public void setTotalProfit(double totalProfit) {
        this.totalProfit = totalProfit;
    }

    public double getProfitMarginPercent() {
        return profitMarginPercent;
    }

    public void setProfitMarginPercent(double profitMarginPercent) {
        this.profitMarginPercent = profitMarginPercent;
    }

    public String getStockStatus() {
        return stockStatus;
    }

    public void setStockStatus(String stockStatus) {
        this.stockStatus = stockStatus;
    }
}
