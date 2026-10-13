package com.example.storespring.repository;

import com.example.storespring.entity.Category;
import com.example.storespring.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Integer> {

    // Lọc theo danh mục
    Page<Product> findByCategoryId(Integer categoryId, Pageable pageable);

    // Tìm kiếm theo tên sản phẩm
    Page<Product> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);

    boolean existsByTitle(String title);

    java.util.Optional<Product> findByTitle(String title);

    // Lấy sản phẩm mới nhất hiển thị trang chủ
    List<Product> findTop8ByOrderByCreatedAtDesc();

    // Lấy sản phẩm đang có giảm giá sốc nhất
    List<Product> findTop8ByDiscountGreaterThanOrderByDiscountDesc(Integer minDiscount);

    // Lấy sản phẩm liên quan cùng danh mục (loại trừ chính sản phẩm đang xem)
    List<Product> findTop4ByCategoryAndIdNot(Category category, Integer productId);

    // Đếm số lượng sản phẩm theo danh mục
    long countByCategoryId(Integer categoryId);

    // Tìm kiếm nâng cao đa tiêu chí: danh mục + từ khóa + khoảng giá
    @Query("SELECT p FROM Product p WHERE " +
           "(:categoryId IS NULL OR p.category.id = :categoryId) AND " +
           "(:keyword IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<Product> filterProducts(
            @Param("categoryId") Integer categoryId,
            @Param("keyword") String keyword,
            @Param("minPrice") Integer minPrice,
            @Param("maxPrice") Integer maxPrice,
            Pageable pageable
    );
}
