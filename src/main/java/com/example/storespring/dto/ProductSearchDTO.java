package com.example.storespring.dto;

public class ProductSearchDTO {

    private String keyword;
    private Integer categoryId;
    private Integer minPrice;
    private Integer maxPrice;
    private String sortBy = "default"; // "default", "price_asc", "price_desc", "newest"

    public ProductSearchDTO() {
    }

    public ProductSearchDTO(String keyword, Integer categoryId, Integer minPrice, Integer maxPrice, String sortBy) {
        this.keyword = keyword;
        this.categoryId = categoryId;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.sortBy = sortBy;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Integer getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(Integer minPrice) {
        this.minPrice = minPrice;
    }

    public Integer getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(Integer maxPrice) {
        this.maxPrice = maxPrice;
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }
}
