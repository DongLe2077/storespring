package com.example.storespring.service;

import com.example.storespring.dto.ProductSearchDTO;
import com.example.storespring.entity.Category;
import com.example.storespring.entity.Product;
import com.example.storespring.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final FileStorageService fileStorageService;

    @Autowired
    public ProductService(ProductRepository productRepository, FileStorageService fileStorageService) {
        this.productRepository = productRepository;
        this.fileStorageService = fileStorageService;
    }

    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public List<Product> getAllProductsList() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Integer id) {
        if (id == null) return Optional.empty();
        return productRepository.findById(id);
    }

    @Transactional
    public Product saveProduct(Product product) {
        return productRepository.save(product);
    }

    @Transactional
    public boolean deleteProduct(Integer id) {
        Optional<Product> productOpt = productRepository.findById(id);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            // Xóa file ảnh vật lý nếu có
            if (product.getThumbnail() != null) {
                fileStorageService.deleteFile(product.getThumbnail());
            }
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<Product> getLatestProducts() {
        return productRepository.findTop8ByOrderByCreatedAtDesc();
    }

    public List<Product> getHotSaleProducts() {
        return productRepository.findTop8ByDiscountGreaterThanOrderByDiscountDesc(0);
    }

    public List<Product> getRelatedProducts(Category category, Integer currentProductId) {
        if (category == null) return List.of();
        return productRepository.findTop4ByCategoryAndIdNot(category, currentProductId);
    }

    public Page<Product> searchAndFilter(ProductSearchDTO searchDTO, int page, int size) {
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        if (searchDTO != null && searchDTO.getSortBy() != null) {
            switch (searchDTO.getSortBy()) {
                case "price_asc":
                    sort = Sort.by(Sort.Direction.ASC, "price");
                    break;
                case "price_desc":
                    sort = Sort.by(Sort.Direction.DESC, "price");
                    break;
                case "newest":
                default:
                    sort = Sort.by(Sort.Direction.DESC, "createdAt");
                    break;
            }
        }

        Pageable pageable = PageRequest.of(Math.max(0, page), size, sort);

        String keyword = null;
        Integer categoryId = null;
        Integer minPrice = null;
        Integer maxPrice = null;

        if (searchDTO != null) {
            if (searchDTO.getKeyword() != null && !searchDTO.getKeyword().trim().isEmpty()) {
                keyword = searchDTO.getKeyword().trim();
            }
            categoryId = searchDTO.getCategoryId();
            minPrice = searchDTO.getMinPrice();
            maxPrice = searchDTO.getMaxPrice();
        }

        return productRepository.filterProducts(categoryId, keyword, minPrice, maxPrice, pageable);
    }

    public long countAllProducts() {
        return productRepository.count();
    }

    public long countByCategoryId(Integer categoryId) {
        if (categoryId == null) return 0;
        return productRepository.countByCategoryId(categoryId);
    }

    public List<Product> getTopProductsByCategory(Integer categoryId, int limit) {
        if (categoryId == null) return List.of();
        return productRepository.findByCategoryId(
                categoryId,
                PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"))
        ).getContent();
    }
}
