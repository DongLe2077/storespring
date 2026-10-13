package com.example.storespring.dto;

import com.example.storespring.entity.Category;
import com.example.storespring.entity.Product;

import java.util.List;

public class CategoryOverviewDTO {

    private Category category;
    private long productCount;
    private List<Product> previewProducts;
    private String icon;
    private String description;

    public CategoryOverviewDTO() {
    }

    public CategoryOverviewDTO(Category category, long productCount, List<Product> previewProducts, String icon, String description) {
        this.category = category;
        this.productCount = productCount;
        this.previewProducts = previewProducts;
        this.icon = icon;
        this.description = description;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public long getProductCount() {
        return productCount;
    }

    public void setProductCount(long productCount) {
        this.productCount = productCount;
    }

    public List<Product> getPreviewProducts() {
        return previewProducts;
    }

    public void setPreviewProducts(List<Product> previewProducts) {
        this.previewProducts = previewProducts;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
