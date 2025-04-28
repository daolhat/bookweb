package com.programing.bookweb.service;

import com.programing.bookweb.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.method.P;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public interface IProductService {

    Long countProduct();

    Product addProduct(Product product, MultipartFile imageProduct) throws SQLException, IOException;

    Product updateProduct(Product product, MultipartFile imageProduct);

    void deleteProduct(Long productId);

    Product getProductById(Long productId);

    Page<Product> getAllProducts(Pageable pageable);

    List<Product> getBestSeller(Pageable pageable);

    List<Product> getNewProducts(Pageable pageable);

    List<Product> getProductsByCategory(Long categoryId, Pageable pageable);

    List<Product> getTopSellingProductsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    //User
    Page<Product> getProductByKeywordUser(String keyword, Pageable pageable);

    Page<Product> getProductByCategoryIdUser(Long categoryId, Pageable pageable);

    Page<Product> getProductByLayoutUser(String layout, Pageable pageable);

    Page<Product> getProductByCategoryIdAndKeywordUser(Long categoryId, String keyword, Pageable pageable);

    Page<Product> getProductByCategoryIdAndKeywordAndLayoutUser(Long categoryId, String keyword, String layout, Pageable pageable);

    Page<Product> getProductByCategoryIdAndLayoutUser(Long categoryId, String layout, Pageable pageable);

    Page<Product> getProductByLayoutAndKeywordUser(String layout, String keyword, Pageable pageable);
}
