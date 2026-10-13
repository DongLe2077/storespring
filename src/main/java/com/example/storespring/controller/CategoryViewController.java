package com.example.storespring.controller;

import com.example.storespring.dto.CategoryOverviewDTO;
import com.example.storespring.dto.ProductSearchDTO;
import com.example.storespring.entity.Category;
import com.example.storespring.entity.Product;
import com.example.storespring.service.CategoryService;
import com.example.storespring.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class CategoryViewController {

    private final CategoryService categoryService;
    private final ProductService productService;

    @Autowired
    public CategoryViewController(CategoryService categoryService, ProductService productService) {
        this.categoryService = categoryService;
        this.productService = productService;
    }

    @ModelAttribute("headerCategories")
    public List<Category> headerCategories() {
        return categoryService.getAllCategories();
    }

    @GetMapping({"/categories", "/category"})
    public String categoriesPage(Model model) {
        List<Category> allCategories = categoryService.getAllCategories();
        List<CategoryOverviewDTO> categoryOverviews = new ArrayList<>();

        for (Category cat : allCategories) {
            long count = productService.countByCategoryId(cat.getId());
            List<Product> previewProducts = productService.getTopProductsByCategory(cat.getId(), 4);
            String icon = getCategoryIcon(cat.getName());
            String desc = getCategoryDescription(cat.getName());
            categoryOverviews.add(new CategoryOverviewDTO(cat, count, previewProducts, icon, desc));
        }

        model.addAttribute("categoryOverviews", categoryOverviews);
        model.addAttribute("currentPage", "categories");
        return "categories";
    }

    private String getCategoryIcon(String name) {
        if (name == null) return "🎮";
        String lower = name.toLowerCase();
        if (lower.contains("phím")) return "⌨️";
        if (lower.contains("chuột")) return "🖱️";
        if (lower.contains("tai nghe")) return "🎧";
        if (lower.contains("kiện") || lower.contains("lót")) return "🎒";
        return "🎮";
    }

    private String getCategoryDescription(String name) {
        if (name == null) return "Phụ kiện gaming cao cấp chính hãng.";
        String lower = name.toLowerCase();
        if (lower.contains("phím")) {
            return "Bàn phím cơ cao cấp, switch hot-swap, keycap PBT siêu bền, LED RGB sống động chuẩn eSports.";
        }
        if (lower.contains("chuột")) {
            return "Cảm biến quang học chính xác từng pixel, siêu nhẹ, kết nối không dây độ trễ siêu thấp.";
        }
        if (lower.contains("tai nghe")) {
            return "Âm thanh vòm định vị 7.1 chuẩn xác, micro lọc tạp âm và đệm tai êm ái cho thời gian dài.";
        }
        if (lower.contains("kiện") || lower.contains("lót")) {
            return "Pad chuột chống trượt chuyên dụng speed/control, cáp custom và phụ kiện nâng tầm góc setup.";
        }
        return "Dòng sản phẩm gaming và phụ kiện công nghệ chính hãng hàng đầu tại Aura.";
    }

    @GetMapping("/category/{id}")
    public String categoryPage(@PathVariable Integer id,
                               @RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "12") int size,
                               @RequestParam(defaultValue = "newest") String sortBy,
                               Model model) {
        Optional<Category> categoryOpt = categoryService.getCategoryById(id);
        if (categoryOpt.isEmpty()) {
            return "redirect:/";
        }

        Category category = categoryOpt.get();

        Sort sort = switch (sortBy) {
            case "price_asc" -> Sort.by(Sort.Direction.ASC, "price");
            case "price_desc" -> Sort.by(Sort.Direction.DESC, "price");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };

        Page<Product> productPage = productService.getAllProducts(
                PageRequest.of(Math.max(0, page), size, sort)
        );
        // Lọc theo category sử dụng repository
        productPage = productService.searchAndFilter(
                new com.example.storespring.dto.ProductSearchDTO(null, id, null, null, sortBy),
                page, size
        );

        model.addAttribute("category", category);
        model.addAttribute("products", productPage);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("currentPage", "category");

        return "category";
    }
}
