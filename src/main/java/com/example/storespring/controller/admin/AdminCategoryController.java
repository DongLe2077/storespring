package com.example.storespring.controller.admin;

import com.example.storespring.entity.Category;
import com.example.storespring.service.CategoryService;
import com.example.storespring.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    private final CategoryService categoryService;
    private final ProductService productService;

    @Autowired
    public AdminCategoryController(CategoryService categoryService, ProductService productService) {
        this.categoryService = categoryService;
        this.productService = productService;
    }

    // Danh sách danh mục kèm số lượng sản phẩm của từng danh mục
    @GetMapping
    public String listCategories(Model model) {
        List<Category> categories = categoryService.getAllCategories();

        Map<Integer, Long> productCountMap = new HashMap<>();
        for (Category cat : categories) {
            productCountMap.put(cat.getId(), productService.countByCategoryId(cat.getId()));
        }

        model.addAttribute("categories", categories);
        model.addAttribute("productCountMap", productCountMap);
        model.addAttribute("currentMenu", "categories");

        return "admin/category/list";
    }

    // Thêm danh mục mới
    @PostMapping("/create")
    public String createCategory(@RequestParam("name") String name,
                                 @RequestParam(value = "thumbnail", required = false) String thumbnail,
                                 RedirectAttributes redirectAttributes) {
        if (name == null || name.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Tên danh mục không được để trống!");
            return "redirect:/admin/categories";
        }

        String trimmedName = name.trim();
        if (categoryService.isNameExists(trimmedName, null)) {
            redirectAttributes.addFlashAttribute("error", "Tên danh mục '" + trimmedName + "' đã tồn tại!");
            return "redirect:/admin/categories";
        }

        Category category = new Category();
        category.setName(trimmedName);
        category.setThumbnail(thumbnail != null && !thumbnail.trim().isEmpty() ? thumbnail.trim() : null);

        categoryService.saveCategory(category);
        redirectAttributes.addFlashAttribute("success", "Thêm danh mục '" + trimmedName + "' thành công!");

        return "redirect:/admin/categories";
    }

    // Sửa danh mục
    @PostMapping("/edit/{id}")
    public String editCategory(@PathVariable Integer id,
                               @RequestParam("name") String name,
                               @RequestParam(value = "thumbnail", required = false) String thumbnail,
                               RedirectAttributes redirectAttributes) {

        Optional<Category> catOpt = categoryService.getCategoryById(id);
        if (catOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Danh mục không tồn tại!");
            return "redirect:/admin/categories";
        }

        if (name == null || name.trim().isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Tên danh mục không được để trống!");
            return "redirect:/admin/categories";
        }

        String trimmedName = name.trim();
        if (categoryService.isNameExists(trimmedName, id)) {
            redirectAttributes.addFlashAttribute("error", "Tên danh mục '" + trimmedName + "' đã được sử dụng!");
            return "redirect:/admin/categories";
        }

        Category category = catOpt.get();
        category.setName(trimmedName);
        if (thumbnail != null) {
            category.setThumbnail(thumbnail.trim().isEmpty() ? null : thumbnail.trim());
        }

        categoryService.saveCategory(category);
        redirectAttributes.addFlashAttribute("success", "Cập nhật danh mục #" + id + " thành công!");

        return "redirect:/admin/categories";
    }

    // Xóa danh mục (bảo vệ nếu đang có sản phẩm)
    @PostMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            boolean deleted = categoryService.deleteCategory(id);
            if (deleted) {
                redirectAttributes.addFlashAttribute("success", "Đã xóa danh mục thành công!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy danh mục để xóa!");
            }
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/admin/categories";
    }
}
