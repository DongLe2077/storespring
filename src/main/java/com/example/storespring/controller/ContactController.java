package com.example.storespring.controller;

import com.example.storespring.entity.Category;
import com.example.storespring.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@Controller
public class ContactController {

    private final CategoryService categoryService;

    @Autowired
    public ContactController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @ModelAttribute("headerCategories")
    public List<Category> headerCategories() {
        return categoryService.getAllCategories();
    }

    @GetMapping("/contact")
    public String contactPage(Model model) {
        model.addAttribute("currentPage", "contact");
        return "contact";
    }
}
