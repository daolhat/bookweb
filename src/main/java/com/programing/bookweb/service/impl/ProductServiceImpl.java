package com.programing.bookweb.service.impl;

import com.programing.bookweb.entity.Product;
import com.programing.bookweb.repository.CategoryRepository;
import com.programing.bookweb.repository.ProductRepository;
import com.programing.bookweb.service.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements IProductService {

    private static final String PRODUCT_IMAGE_DIR = "src/main/resources/static/assets/img/product";

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public Long countProduct() {
        return productRepository.count();
    }

    @Transactional
    @Override
    public Product addProduct(Product product, MultipartFile imageProduct) throws SQLException, IOException {
        if (imageProduct != null && !imageProduct.isEmpty()){
            try {
                String originalFileName = imageProduct.getOriginalFilename();
                String uniqueFileName = generateUniqueFileName(originalFileName);
                Path imagePath = Paths.get(PRODUCT_IMAGE_DIR, uniqueFileName);
                // Create directory if not exists
                Files.createDirectories(imagePath.getParent());
                Files.copy(imageProduct.getInputStream(), imagePath, StandardCopyOption.REPLACE_EXISTING);
                product.setImageProduct(uniqueFileName);
            } catch (IOException e) {
                throw new RuntimeException("Lỗi khi lưu ảnh", e);
            }
        }
        product.setQuantitySold(0);
        product.setCreatedAt(LocalDateTime.now());
        return productRepository.save(product);
    }


    @Transactional
    @Override
    public Product updateProduct(Product product, MultipartFile imageProduct) {
        if (imageProduct != null && !imageProduct.isEmpty()){
            try {
                String originalFileName = imageProduct.getOriginalFilename();
                String uniqueFileName = generateUniqueFileName(originalFileName);
                Path imagePath = Paths.get(PRODUCT_IMAGE_DIR, uniqueFileName);
                // Create directory if not exists
                Files.createDirectories(imagePath.getParent());
                Files.copy(imageProduct.getInputStream(), imagePath, StandardCopyOption.REPLACE_EXISTING);
                product.setImageProduct(uniqueFileName);
            } catch (IOException e) {
                throw new RuntimeException("Lỗi khi lưu ảnh", e);
            }
        }
        product.setUpdatedAt(LocalDateTime.now());
        return productRepository.save(product);
    }


    @Override
    public void deleteProduct(Long productId) {
        if (!productRepository.existsById(productId)){
            throw new RuntimeException("Không có sản phẩm có id: " + productId);
        }
        productRepository.deleteById(productId);
    }


    @Override
    public Product getProductById(Long productId) {
        return productRepository.findById(productId).orElse(null);
    }

    @Override
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Override
    public List<Product> getAllProductByList() {
        return productRepository.findAll();
    }


    @Override
    public Page<Product> getProductByCategoryId(Long categoryId, Pageable pageable) {
        if (!categoryRepository.existsById(categoryId)){
            throw new RuntimeException("Không có danh mục thể loại sách trên!");
        }
        return productRepository.findByCategoryId(categoryId, pageable);
    }


    @Override
    public Page<Product> getProductByLayout(String layout, Pageable pageable) {
        return productRepository.findByLayout(layout, pageable);
    }


    @Override
    public List<Product> getBestSeller(Pageable pageable) {
        return productRepository.findTopByOrderByQuantitySoldDesc(pageable);
    }


    @Override
    public List<Product> getNewProducts(Pageable pageable) {
        return productRepository.findTopByOrderByCreatedAtDesc(pageable);
    }


    @Override
    public List<Product> getProductsByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findTopByCategoryIdOrderByCreatedAtDesc(categoryId, pageable);
    }

    @Override
    public List<Product> getTopSellingProductsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return List.of();
    }

    @Override
    public List<Product> getProductByCategoryId(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }

    @Override
    public List<Product> getProductByLayout(String layout) {
        return productRepository.findByLayout(layout);
    }

    @Override
    public List<Product> getProductByCategoryIdAndLayout(Long categoryId, String layout) {
        return productRepository.findByCategoryIdAndLayout(categoryId, layout);
    }

    @Override
    public Page<Product> getProductByKeywordUser(String keyword, Pageable pageable) {
        String nKeyword = keyword.trim().replaceAll("\\s+", " ");
        if (keyword.isEmpty()){
            return productRepository.findAll(pageable);
        }
        return productRepository.findByTitleContaining(nKeyword, pageable);
    }

    @Override
    public Page<Product> getProductByCategoryIdUser(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable);
    }

    @Override
    public Page<Product> getProductByLayoutUser(String layout, Pageable pageable) {
        return productRepository.findByLayout(layout, pageable);
    }

    @Override
    public Page<Product> getProductByCategoryIdAndKeywordUser(Long categoryId, String keyword, Pageable pageable) {
        return productRepository.findByCategoryIdAndTitleContaining(categoryId, keyword, pageable);
    }

    @Override
    public Page<Product> getProductByCategoryIdAndKeywordAndLayoutUser(Long categoryId, String keyword, String layout, Pageable pageable) {
        return productRepository.findByCategoryIdAndTitleContainingAndLayout(categoryId, keyword, layout, pageable);
    }

    @Override
    public Page<Product> getProductByCategoryIdAndLayoutUser(Long categoryId, String layout, Pageable pageable) {
        return productRepository.findByCategoryIdAndLayout(categoryId, layout, pageable);
    }

    @Override
    public Page<Product> getProductByLayoutAndKeywordUser(String layout, String keyword, Pageable pageable) {
        return productRepository.findByLayoutAndTitleContaining(layout, keyword, pageable);
    }


    private String generateUniqueFileName(String originalFileName) {
        return System.currentTimeMillis() + "_" + originalFileName;
    }
}
