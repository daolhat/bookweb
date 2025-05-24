package com.programing.bookweb.repository;

import com.programing.bookweb.dto.TopProductDTO;
import com.programing.bookweb.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByTitleContaining(String keyword, Pageable pageable);

    Page<Product> findByLayout(String layout, Pageable pageable);

    Page<Product> findByCategoryId(Long categoryId, Pageable pageable);

    Page<Product> findByCategoryIdAndTitleContaining(Long categoryId, String keyword, Pageable pageable);

    Page<Product> findByCategoryIdAndTitleContainingAndLayout(Long categoryId, String keyword, String layout, Pageable pageable);

    Page<Product> findByCategoryIdAndLayout(Long categoryId, String layout, Pageable pageable);

    Page<Product> findByLayoutAndTitleContaining(String layout, String keyword, Pageable pageable);

    @Query("SELECT p FROM Product p ORDER BY p.quantitySold DESC")
    List<Product> findTopByOrderByQuantitySoldDesc(Pageable pageable);

    @Query("SELECT p FROM Product p ORDER BY p.createdAt DESC")
    List<Product> findTopByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId ORDER BY p.createdAt DESC")
    List<Product> findTopByCategoryIdOrderByCreatedAtDesc(@Param("categoryId") Long categoryId, Pageable pageable);

    @Query("SELECT p FROM Product p " +
            "JOIN OrderDetail od ON p.id = od.product.id " +
            "JOIN Order o ON od.order.id = o.id " +
            "WHERE o.createdAt BETWEEN :startDate AND :endDate " +
            "GROUP BY p " +
            "ORDER BY p.quantitySold DESC")
    List<Product> findTopSellingProductsByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

//    @Query("""
//        SELECT p FROM Product p
//        WHERE p.createdAt = (SELECT MIN(p2.createdAt) FROM Product p2)
//        AND p.quantity = (
//            SELECT MAX(p3.quantity)
//            FROM Product p3
//            WHERE p3.createdAt = (SELECT MIN(p4.createdAt) FROM Product p4)
//        )
//        """)
//    List<Product> findTop5ProductsWithOldestDateAndMaxQuantity(Pageable pageable);

    @Query("""
        SELECT p FROM Product p 
        WHERE p.createdAt <= (
            SELECT MIN(p2.createdAt) FROM Product p2
        )
        AND p.createdAt >= (
            SELECT MIN(p2.createdAt) FROM Product p2
        )
        ORDER BY p.quantity DESC
        """)
    List<Product> findTop5ProductsWithOldestDateAndMaxQuantity(Pageable pageable);

    @Query("""
        SELECT new com.programing.bookweb.dto.TopProductDTO(p.id, p.title, p.price, p.discount, SUM(od.quantity)) 
        FROM Product p 
        JOIN p.orderDetails od 
        JOIN od.order o 
        WHERE o.status = 'DELIVERED' 
        AND o.createdAt BETWEEN :startDate AND :endDate 
        GROUP BY p.id 
        ORDER BY SUM(od.quantity) DESC
        """)
    List<TopProductDTO> findTopSellingProducts(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

}
