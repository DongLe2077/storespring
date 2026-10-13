package com.example.storespring.controller.admin;

import com.example.storespring.dto.ProductSearchDTO;
import com.example.storespring.entity.Category;
import com.example.storespring.entity.Product;
import com.example.storespring.service.CategoryService;
import com.example.storespring.service.FileStorageService;
import com.example.storespring.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final FileStorageService fileStorageService;

    @Autowired
    public AdminProductController(ProductService productService,
                                  CategoryService categoryService,
                                  FileStorageService fileStorageService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.fileStorageService = fileStorageService;
    }

    @ModelAttribute("categories")
    public List<Category> allCategories() {
        return categoryService.getAllCategories();
    }

    // Danh sách sản phẩm (hỗ trợ phân trang + tìm kiếm)
    @GetMapping
    public String listProducts(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {

        ProductSearchDTO searchDTO = new ProductSearchDTO();
        searchDTO.setKeyword(keyword);
        searchDTO.setCategoryId(categoryId);
        searchDTO.setSortBy("newest");

        Page<Product> productPage = productService.searchAndFilter(searchDTO, page, 10);

        model.addAttribute("productPage", productPage);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("currentMenu", "products");

        return "admin/product/list";
    }

    // Giao diện thêm sản phẩm mới
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("isEdit", false);
        model.addAttribute("currentMenu", "products");
        return "admin/product/form";
    }

    // Xử lý thêm sản phẩm mới
    @PostMapping("/create")
    public String processCreate(
            @ModelAttribute Product product,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "thumbnailFile", required = false) MultipartFile thumbnailFile,
            RedirectAttributes redirectAttributes) {

        try {
            if (categoryId != null) {
                categoryService.getCategoryById(categoryId).ifPresent(product::setCategory);
            }

            if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
                String imagePath = fileStorageService.saveFile(thumbnailFile);
                product.setThumbnail(imagePath);
            }

            productService.saveProduct(product);
            redirectAttributes.addFlashAttribute("success", "Thêm sản phẩm '" + product.getTitle() + "' thành công!");
            return "redirect:/admin/products";

        } catch (IOException e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi lưu ảnh sản phẩm: " + e.getMessage());
            return "redirect:/admin/products/create";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi tạo sản phẩm: " + e.getMessage());
            return "redirect:/admin/products/create";
        }
    }

    // Giao diện sửa sản phẩm
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Product> productOpt = productService.getProductById(id);
        if (productOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Sản phẩm không tồn tại!");
            return "redirect:/admin/products";
        }

        model.addAttribute("product", productOpt.get());
        model.addAttribute("isEdit", true);
        model.addAttribute("currentMenu", "products");
        return "admin/product/form";
    }

    // Xử lý sửa sản phẩm
    @PostMapping("/edit/{id}")
    public String processEdit(
            @PathVariable Integer id,
            @ModelAttribute Product formProduct,
            @RequestParam(value = "categoryId", required = false) Integer categoryId,
            @RequestParam(value = "thumbnailFile", required = false) MultipartFile thumbnailFile,
            RedirectAttributes redirectAttributes) {

        Optional<Product> productOpt = productService.getProductById(id);
        if (productOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Sản phẩm không tồn tại!");
            return "redirect:/admin/products";
        }

        Product existingProduct = productOpt.get();

        try {
            existingProduct.setTitle(formProduct.getTitle());
            existingProduct.setPrice(formProduct.getPrice());
            existingProduct.setDiscount(formProduct.getDiscount());
            existingProduct.setQuantity(formProduct.getQuantity());
            existingProduct.setDescription(formProduct.getDescription());

            if (categoryId != null) {
                categoryService.getCategoryById(categoryId).ifPresent(existingProduct::setCategory);
            } else {
                existingProduct.setCategory(null);
            }

            // Nếu người dùng tải ảnh mới lên
            if (thumbnailFile != null && !thumbnailFile.isEmpty()) {
                // Xóa ảnh cũ nếu là ảnh upload cục bộ
                if (existingProduct.getThumbnail() != null) {
                    fileStorageService.deleteFile(existingProduct.getThumbnail());
                }
                String imagePath = fileStorageService.saveFile(thumbnailFile);
                existingProduct.setThumbnail(imagePath);
            }

            productService.saveProduct(existingProduct);
            redirectAttributes.addFlashAttribute("success", "Cập nhật sản phẩm #" + id + " thành công!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật sản phẩm: " + e.getMessage());
        }

        return "redirect:/admin/products";
    }

    // Xóa sản phẩm
    @PostMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        boolean deleted = productService.deleteProduct(id);
        if (deleted) {
            redirectAttributes.addFlashAttribute("success", "Đã xóa sản phẩm #" + id + " thành công!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy sản phẩm để xóa!");
        }
        return "redirect:/admin/products";
    }
}
