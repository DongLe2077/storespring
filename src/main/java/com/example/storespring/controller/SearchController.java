package com.example.storespring.controller;

import com.example.storespring.dto.ProductSearchDTO;
import com.example.storespring.entity.Category;
import com.example.storespring.entity.Product;
import com.example.storespring.service.CategoryService;
import com.example.storespring.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class SearchController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @Autowired
    public SearchController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @ModelAttribute("headerCategories")
    public List<Category> headerCategories() {
        return categoryService.getAllCategories();
    }

    @GetMapping("/search")
    public String searchPage(@RequestParam(required = false) String keyword,
                             @RequestParam(required = false) Integer categoryId,
                             @RequestParam(required = false) Integer minPrice,
                             @RequestParam(required = false) Integer maxPrice,
                             @RequestParam(defaultValue = "newest") String sortBy,
                             @RequestParam(defaultValue = "0") int page,
                             @RequestParam(defaultValue = "12") int size,
                             Model model) {

        ProductSearchDTO searchDTO = new ProductSearchDTO(keyword, categoryId, minPrice, maxPrice, sortBy);
        Page<Product> productPage = productService.searchAndFilter(searchDTO, page, size);

        List<Category> categories = categoryService.getAllCategories();

        model.addAttribute("products", productPage);
        model.addAttribute("categories", categories);
        model.addAttribute("searchDTO", searchDTO);
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("currentPage", "search");

        return "search";
    }
}
