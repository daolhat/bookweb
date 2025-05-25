package com.programing.bookweb.service.impl;

import com.programing.bookweb.dto.TopProductDTO;
import com.programing.bookweb.entity.OrderDetail;
import com.programing.bookweb.entity.Product;
import com.programing.bookweb.repository.CategoryRepository;
import com.programing.bookweb.repository.ProductRepository;
import com.programing.bookweb.service.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

//    @Transactional
//    @Override
//    public Product addProduct(Product product, MultipartFile imageProduct) throws SQLException, IOException {
//        if (imageProduct != null && !imageProduct.isEmpty()){
//            try {
//                String originalFileName = imageProduct.getOriginalFilename();
//                String uniqueFileName = generateUniqueFileName(originalFileName);
//                Path imagePath = Paths.get(PRODUCT_IMAGE_DIR, uniqueFileName);
//                // Create directory if not exists
//                Files.createDirectories(imagePath.getParent());
//                Files.copy(imageProduct.getInputStream(), imagePath, StandardCopyOption.REPLACE_EXISTING);
//                product.setImageProduct(uniqueFileName);
//            } catch (IOException e) {
//                throw new RuntimeException("Lỗi khi lưu ảnh", e);
//            }
//        }
//        product.setQuantitySold(0);
//        product.setCreatedAt(LocalDateTime.now());
//        return productRepository.save(product);
//    }

    @Transactional
    @Override
    public Product addProduct(Product product, MultipartFile imageProduct) throws SQLException, IOException {
        // Kiểm tra trường bắt buộc
        if (product.getTitle() == null || product.getTitle().isEmpty()) {
            throw new IllegalArgumentException("Tiêu đề không được để trống");
        }
        if (product.getCategory() == null || product.getCategory().getId() == null) {
            throw new IllegalArgumentException("Thể loại không được để trống");
        }

        // Xử lý hình ảnh
        if (imageProduct != null && !imageProduct.isEmpty()) {
            String contentType = imageProduct.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("Tệp phải là hình ảnh (PNG, JPEG, v.v.)");
            }
            if (imageProduct.getSize() > 10 * 1024 * 1024) {
                throw new IllegalArgumentException("Tệp hình ảnh quá lớn, tối đa 10MB");
            }

            Path dirPath = Paths.get(PRODUCT_IMAGE_DIR);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
            if (!Files.isWritable(dirPath)) {
                throw new IOException("Thư mục " + PRODUCT_IMAGE_DIR + " không có quyền ghi");
            }

            String originalFileName = imageProduct.getOriginalFilename();
            String cleanFileName = originalFileName.replaceAll("[^a-zA-Z0-9.-]", "_");
            String uniqueFileName = System.currentTimeMillis() + "_" + cleanFileName;
            Path imagePath = Paths.get(PRODUCT_IMAGE_DIR, uniqueFileName);
            Files.copy(imageProduct.getInputStream(), imagePath, StandardCopyOption.REPLACE_EXISTING);
            product.setImageProduct(uniqueFileName);
        } else {
            product.setImageProduct("default-product.jpg");
        }

        // Đặt giá trị mặc định
        product.setQuantitySold(0);
        product.setCreatedAt(LocalDateTime.now());

        try {
            return productRepository.save(product);
        } catch (DataAccessException e) {
            throw new RuntimeException("Lỗi khi lưu sản phẩm vào cơ sở dữ liệu: " + e.getMessage(), e);
        }
    }


//    @Transactional
//    @Override
//    public Product updateProduct(Product product, MultipartFile imageProduct) {
//        if (imageProduct != null && !imageProduct.isEmpty()){
//            try {
//                String originalFileName = imageProduct.getOriginalFilename();
//                String uniqueFileName = generateUniqueFileName(originalFileName);
//                Path imagePath = Paths.get(PRODUCT_IMAGE_DIR, uniqueFileName);
//                // Create directory if not exists
//                Files.createDirectories(imagePath.getParent());
//                Files.copy(imageProduct.getInputStream(), imagePath, StandardCopyOption.REPLACE_EXISTING);
//                product.setImageProduct(uniqueFileName);
//            } catch (IOException e) {
//                throw new RuntimeException("Lỗi khi lưu ảnh", e);
//            }
//        }
//        product.setUpdatedAt(LocalDateTime.now());
//        return productRepository.save(product);
//    }


    @Transactional
    @Override
    public Product updateProduct(Product product, MultipartFile imageProduct) throws SQLException, IOException{
        // Kiểm tra tồn tại sản phẩm
        Product existingProduct = productRepository.findById(product.getId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + product.getId()));

        // Kiểm tra trường bắt buộc
        if (product.getTitle() == null || product.getTitle().isEmpty()) {
            throw new IllegalArgumentException("Tiêu đề không được để trống");
        }
        if (product.getCategory() == null || product.getCategory().getId() == null) {
            throw new IllegalArgumentException("Thể loại không được để trống");
        }

        // Cập nhật các trường
        existingProduct.setTitle(product.getTitle());
        existingProduct.setAuthor(product.getAuthor());
        existingProduct.setSupplier(product.getSupplier());
        existingProduct.setPublisher(product.getPublisher());
        existingProduct.setPublishingYear(product.getPublishingYear());
        existingProduct.setIntroduction(product.getIntroduction());
        existingProduct.setPrice(product.getPrice());
        existingProduct.setDiscount(product.getDiscount());
        existingProduct.setQuantitySold(product.getQuantitySold());
        existingProduct.setWeight(product.getWeight());
        existingProduct.setNumberOfPage(product.getNumberOfPage());
        existingProduct.setQuantity(product.getQuantity());
        existingProduct.setSize(product.getSize());
        existingProduct.setLayout(product.getLayout());
        existingProduct.setTranslator(product.getTranslator());
        existingProduct.setCategory(product.getCategory());

        // Xử lý hình ảnh
        if (imageProduct != null && !imageProduct.isEmpty()) {
            String contentType = imageProduct.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new IllegalArgumentException("Tệp phải là hình ảnh (PNG, JPEG, v.v.)");
            }
            if (imageProduct.getSize() > 10 * 1024 * 1024) {
                throw new IllegalArgumentException("Tệp hình ảnh quá lớn, tối đa 10MB");
            }

            Path dirPath = Paths.get(PRODUCT_IMAGE_DIR);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }
            if (!Files.isWritable(dirPath)) {
                throw new IOException("Thư mục " + PRODUCT_IMAGE_DIR + " không có quyền ghi");
            }

            // Xóa ảnh cũ
            if (existingProduct.getImageProduct() != null && !existingProduct.getImageProduct().isEmpty()) {
                Path oldImagePath = Paths.get(PRODUCT_IMAGE_DIR, existingProduct.getImageProduct());
                Files.deleteIfExists(oldImagePath);
            }

            String originalFileName = imageProduct.getOriginalFilename();
            String cleanFileName = originalFileName.replaceAll("[^a-zA-Z0-9.-]", "_");
            String uniqueFileName = System.currentTimeMillis() + "_" + cleanFileName;
            Path imagePath = Paths.get(PRODUCT_IMAGE_DIR, uniqueFileName);
            Files.copy(imageProduct.getInputStream(), imagePath, StandardCopyOption.REPLACE_EXISTING);
            existingProduct.setImageProduct(uniqueFileName);
        } else {
            existingProduct.setImageProduct(existingProduct.getImageProduct()); // Giữ nguyên ảnh cũ
        }

        existingProduct.setUpdatedAt(LocalDateTime.now());

        try {
            return productRepository.save(existingProduct);
        } catch (DataAccessException e) {
            throw new RuntimeException("Lỗi khi cập nhật sản phẩm vào cơ sở dữ liệu: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void deleteProduct(Long productId) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            throw new RuntimeException("Không có sản phẩm có id: " + productId);
        }
        // Xóa an toàn bằng cách xóa các liên kết trước
        if (product.getOrderDetails() != null && !product.getOrderDetails().isEmpty()) {
            // Tạo một bản sao để tránh ConcurrentModificationException
            Set<OrderDetail> orderDetailsCopy = new HashSet<>(product.getOrderDetails());
            // Xóa liên kết hai chiều để tránh lỗi SQL constraint
            for (OrderDetail orderDetail : orderDetailsCopy) {
                orderDetail.setProduct(null); // Loại bỏ tham chiếu đến product
                product.getOrderDetails().remove(orderDetail); // Loại bỏ orderDetail khỏi danh sách của product
            }
        }
        // Sau khi đã xử lý các liên kết, an toàn để xóa sản phẩm
        productRepository.delete(product);
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
    public Page<Product> getProductByKeywordUser(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return productRepository.findAll(pageable);
        }
        String nKeyword = keyword.trim().replaceAll("\\s+", " ");
        return productRepository.findByTitleContaining(nKeyword, pageable);
    }

    @Override
    public Page<Product> getProductByCategoryIdUser(Long categoryId, Pageable pageable) {
        if (categoryId == null) {
            return productRepository.findAll(pageable);
        }
        return productRepository.findByCategoryId(categoryId, pageable);
    }

    @Override
    public Page<Product> getProductByLayoutUser(String layout, Pageable pageable) {
        if (layout == null || layout.trim().isEmpty()) {
            return productRepository.findAll(pageable);
        }
        return productRepository.findByLayout(layout, pageable);
    }

    @Override
    public Page<Product> getProductByCategoryIdAndKeywordUser(Long categoryId, String keyword, Pageable pageable) {
        if (categoryId == null || keyword == null || keyword.trim().isEmpty()) {
            if (categoryId != null) {
                return productRepository.findByCategoryId(categoryId, pageable);
            } else if (keyword != null && !keyword.trim().isEmpty()) {
                return getProductByKeywordUser(keyword, pageable);
            } else {
                return productRepository.findAll(pageable);
            }
        }
        return productRepository.findByCategoryIdAndTitleContaining(categoryId, keyword.trim(), pageable);
    }

    @Override
    public Page<Product> getProductByCategoryIdAndKeywordAndLayoutUser(Long categoryId, String keyword, String layout, Pageable pageable) {
        if (categoryId == null || keyword == null || keyword.trim().isEmpty() || layout == null || layout.trim().isEmpty()) {
            if (categoryId != null && layout != null && !layout.trim().isEmpty()) {
                return getProductByCategoryIdAndLayoutUser(categoryId, layout, pageable);
            } else if (categoryId != null && keyword != null && !keyword.trim().isEmpty()) {
                return getProductByCategoryIdAndKeywordUser(categoryId, keyword, pageable);
            } else if (layout != null && !layout.trim().isEmpty() && keyword != null && !keyword.trim().isEmpty()) {
                return getProductByLayoutAndKeywordUser(layout, keyword, pageable);
            } else if (categoryId != null) {
                return getProductByCategoryIdUser(categoryId, pageable);
            } else if (layout != null && !layout.trim().isEmpty()) {
                return getProductByLayoutUser(layout, pageable);
            } else if (keyword != null && !keyword.trim().isEmpty()) {
                return getProductByKeywordUser(keyword, pageable);
            } else {
                return productRepository.findAll(pageable);
            }
        }
        return productRepository.findByCategoryIdAndTitleContainingAndLayout(categoryId, keyword.trim(), layout, pageable);
    }

    @Override
    public Page<Product> getProductByCategoryIdAndLayoutUser(Long categoryId, String layout, Pageable pageable) {
        if (categoryId == null || layout == null || layout.trim().isEmpty()) {
            if (categoryId != null) {
                return getProductByCategoryIdUser(categoryId, pageable);
            } else if (layout != null && !layout.trim().isEmpty()) {
                return getProductByLayoutUser(layout, pageable);
            } else {
                return productRepository.findAll(pageable);
            }
        }
        return productRepository.findByCategoryIdAndLayout(categoryId, layout, pageable);
    }

    @Override
    public Page<Product> getProductByLayoutAndKeywordUser(String layout, String keyword, Pageable pageable) {
        if (layout == null || layout.trim().isEmpty() || keyword == null || keyword.trim().isEmpty()) {
            if (layout != null && !layout.trim().isEmpty()) {
                return getProductByLayoutUser(layout, pageable);
            } else if (keyword != null && !keyword.trim().isEmpty()) {
                return getProductByKeywordUser(keyword, pageable);
            } else {
                return productRepository.findAll(pageable);
            }
        }
        return productRepository.findByLayoutAndTitleContaining(layout, keyword.trim(), pageable);
    }

    @Override
    public List<Product> getProductsWithOldestDateAndMaxQuantity() {
        Pageable pageable = PageRequest.of(0, 10);
        return productRepository.findTop5ProductsWithOldestDateAndMaxQuantity(pageable);
    }

    @Override
    public List<TopProductDTO> getTopSellingProducts(LocalDateTime startDate, LocalDateTime endDate) {
        Pageable pageable = PageRequest.of(0, 10);
        if (startDate == null || endDate == null || startDate.isAfter(endDate)){
            throw  new RuntimeException("Lỗi");
        }
        return productRepository.findTopSellingProducts(startDate, endDate, pageable);
    }


    private String generateUniqueFileName(String originalFileName) {
        return System.currentTimeMillis() + "_" + originalFileName;
    }
}
