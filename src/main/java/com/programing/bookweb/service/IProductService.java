package com.programing.bookweb.service;

import com.programing.bookweb.dto.TopProductDTO;
import com.programing.bookweb.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface IProductService {

    Long countProduct();

    void addProduct(Product product);

    void updateProduct(Product product);

    void deleteProduct(Long productId);

    Product getProductById(Long productId);

    Page<Product> getAllProducts(Pageable pageable);

    List<Product> getBestSeller(Pageable pageable);

    List<Product> getNewProducts(Pageable pageable);

    List<Product> getProductsByCategory(Long categoryId, Pageable pageable);

    Page<Product> getProductByKeywordUser(String keyword, Pageable pageable);

    Page<Product> getProductByCategoryIdUser(Long categoryId, Pageable pageable);

    Page<Product> getProductByLayoutUser(String layout, Pageable pageable);

    Page<Product> getProductByCategoryIdAndKeywordUser(Long categoryId, String keyword, Pageable pageable);

    Page<Product> getProductByCategoryIdAndKeywordAndLayoutUser(Long categoryId, String keyword, String layout, Pageable pageable);

    Page<Product> getProductByCategoryIdAndLayoutUser(Long categoryId, String layout, Pageable pageable);

    Page<Product> getProductByLayoutAndKeywordUser(String layout, String keyword, Pageable pageable);

    List<Product> getProductsWithOldestDateAndMaxQuantity();

    List<TopProductDTO> getTopSellingProducts(LocalDateTime startDate, LocalDateTime endDate);
}
