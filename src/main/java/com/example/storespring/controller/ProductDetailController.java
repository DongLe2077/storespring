package com.example.storespring.controller;

import com.example.storespring.entity.Category;
import com.example.storespring.entity.Product;
import com.example.storespring.service.CategoryService;
import com.example.storespring.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Optional;

@Controller
public class ProductDetailController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @Autowired
    public ProductDetailController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @ModelAttribute("headerCategories")
    public List<Category> headerCategories() {
        return categoryService.getAllCategories();
    }

    @GetMapping("/product/{id}")
    public String productDetail(@PathVariable Integer id, Model model) {
        Optional<Product> productOpt = productService.getProductById(id);
        if (productOpt.isEmpty()) {
            return "redirect:/";
        }

        Product product = productOpt.get();
        List<Product> relatedProducts = productService.getRelatedProducts(product.getCategory(), product.getId());

        model.addAttribute("product", product);
        model.addAttribute("relatedProducts", relatedProducts);
        model.addAttribute("currentPage", "product");

        return "product/detail";
    }
}
