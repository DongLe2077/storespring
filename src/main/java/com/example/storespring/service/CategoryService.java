package com.example.storespring.service;

import com.example.storespring.entity.Category;
import com.example.storespring.repository.CategoryRepository;
import com.example.storespring.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAllByOrderByNameAsc();
    }

    public Optional<Category> getCategoryById(Integer id) {
        if (id == null) return Optional.empty();
        return categoryRepository.findById(id);
    }

    @Transactional
    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }

    @Transactional
    public boolean deleteCategory(Integer id) {
        if (id == null || !categoryRepository.existsById(id)) {
            return false;
        }
        // Kiểm tra xem có sản phẩm nào thuộc danh mục này không
        long productCount = productRepository.countByCategoryId(id);
        if (productCount > 0) {
            throw new IllegalStateException("Không thể xóa danh mục đang có " + productCount + " sản phẩm!");
        }
        categoryRepository.deleteById(id);
        return true;
    }

    public boolean isNameExists(String name, Integer excludeId) {
        if (excludeId == null) {
            return categoryRepository.existsByName(name);
        }
        return categoryRepository.existsByNameAndIdNot(name, excludeId);
    }
}
