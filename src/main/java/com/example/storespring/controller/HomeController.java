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

import java.util.List;

@Controller
public class HomeController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @Autowired
    public HomeController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    // Cung cấp danh mục cho Header & Footer trên MỌI trang của Controller này
    @ModelAttribute("headerCategories")
    public List<Category> headerCategories() {
        return categoryService.getAllCategories();
    }

    @GetMapping("/")
    public String home(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        List<Product> latestProducts = productService.getLatestProducts();
        List<Product> hotSaleProducts = productService.getHotSaleProducts();

        model.addAttribute("categories", categories);
        model.addAttribute("latestProducts", latestProducts);
        model.addAttribute("hotSaleProducts", hotSaleProducts);
        model.addAttribute("currentPage", "home");

        return "home";
    }
}
