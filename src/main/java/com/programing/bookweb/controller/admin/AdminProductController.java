package com.programing.bookweb.controller.admin;

import com.programing.bookweb.controller.BaseController;
import com.programing.bookweb.dto.ProductDTO;
import com.programing.bookweb.entity.Category;
import com.programing.bookweb.entity.Product;
import com.programing.bookweb.service.ICategoryService;
import com.programing.bookweb.service.IProductService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Controller
@AllArgsConstructor
@RequestMapping("/dashboard/product_management")
public class AdminProductController extends BaseController {

    private final IProductService productService;
    private final ICategoryService categoryService;

//    private static final String PRODUCT_IMAGE_DIR = "src/main/resources/static/assets/img/product/";
    private static final String PRODUCT_IMAGE_DIR = "public/images/product/";

    @GetMapping
    public String showProductPageManagement(@RequestParam(name = "categoryId", required = false) Long categoryId,
                                            @RequestParam(name = "layout", required = false) String layout,
                                            @RequestParam(name = "keyword", required = false) String keyword,
                                            @RequestParam(name = "sort", required = false) String sortBy,
                                            @RequestParam(name = "page", defaultValue = "1") int page,
                                            Model model) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        // Xử lý keyword trước khi tìm kiếm
        String normalizedKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        Pageable pageable;
        if (sortBy != null) {
            switch (sortBy) {
                case "newest":
                    pageable = PageRequest.of(page - 1, 12, Sort.by("createdAt").descending());
                    break;
                case "price_asc":
                    pageable = PageRequest.of(page - 1, 12, Sort.by("price").ascending());
                    break;
                case "price_desc":
                    pageable = PageRequest.of(page - 1, 12, Sort.by("price").descending());
                    break;
                default:
                    pageable = PageRequest.of(page - 1, 12);
            }
        } else {
            pageable = PageRequest.of(page - 1, 12);
        }

        Page<Product> products;

        try {
            // Áp dụng các bộ lọc theo trường hợp
            if (normalizedKeyword != null && categoryId != null && layout != null) {
                // Lọc theo cả 3 tiêu chí: từ khóa + danh mục + hình thức bìa
                products = productService.getProductByCategoryIdAndKeywordAndLayoutUser(categoryId, normalizedKeyword, layout, pageable);
            } else if (normalizedKeyword != null && categoryId != null) {
                // Lọc theo từ khóa + danh mục
                products = productService.getProductByCategoryIdAndKeywordUser(categoryId, normalizedKeyword, pageable);
            } else if (normalizedKeyword != null && layout != null) {
                // Lọc theo từ khóa + hình thức bìa
                products = productService.getProductByLayoutAndKeywordUser(layout, normalizedKeyword, pageable);
            } else if (categoryId != null && layout != null) {
                // Lọc theo danh mục + hình thức bìa
                products = productService.getProductByCategoryIdAndLayoutUser(categoryId, layout, pageable);
            } else if (normalizedKeyword != null) {
                // Chỉ lọc theo từ khóa
                products = productService.getProductByKeywordUser(normalizedKeyword, pageable);
            } else if (categoryId != null) {
                // Chỉ lọc theo danh mục
                products = productService.getProductByCategoryIdUser(categoryId, pageable);
            } else if (layout != null) {
                // Chỉ lọc theo hình thức bìa
                products = productService.getProductByLayoutUser(layout, pageable);
            } else {
                // Không có bộ lọc nào
                products = productService.getAllProducts(pageable);
            }
        } catch (Exception e) {
            // Fallback to all products if there's an error
            products = productService.getAllProducts(pageable);
        }
        model.addAttribute("products", products);
        model.addAttribute("totalPages", products.getTotalPages());
        model.addAttribute("pageNumber", page);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedLayout", layout);
        model.addAttribute("selectedSort", sortBy);
        return "admin/products";
    }


    @GetMapping("/add")
    public String showAddProductForm(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);

        ProductDTO productDTO = new ProductDTO();
        model.addAttribute("productDTO", productDTO);
        return "admin/products-add";
    }


    @PostMapping("/add")
    public String addNewProduct(@Valid @ModelAttribute("productDTO") ProductDTO productDTO,
                                 BindingResult result,
                                 Model model,
                                 RedirectAttributes redirectAttributes) throws IOException {
        if (productDTO.getImageProduct().isEmpty()) {
            result.addError(new FieldError("productDTO", "imageProduct", "Ảnh bìa không được để trống"));
        }

        if (result.hasErrors()) {
            return "admin/products-add";
        }

        MultipartFile image = productDTO.getImageProduct();
        Date createdAt = new Date();
        String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();

        try {
            Path uploadDir = Paths.get(PRODUCT_IMAGE_DIR);
            if (!Files.exists(uploadDir)){
                Files.createDirectories(uploadDir);
            }
            Path filePath = uploadDir.resolve(storageFileName);
            try (InputStream inputStream = image.getInputStream()){
                Files.copy(inputStream, filePath,
                        StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (Exception e) {
            System.out.println("Lỗi ngoại lệ: " + e.getMessage());
        }

        Category category = null;
        List<Category> categories = categoryService.getAllCategories();
        if (categories != null) { // Kiểm tra null
            for (Category item : categories) {
                if (Objects.equals(item.getId(), productDTO.getCategoryId())) {
                    category = item;
                    break;
                }
            }
        } else {
            throw new IllegalStateException("Danh sách danh mục không thể tải.");
        }
        if (category == null) {
            throw new IllegalArgumentException("Không tìm thấy danh mục với ID: " + productDTO.getCategoryId());
        }

        Product product = new Product();
        product.setTitle(productDTO.getTitle());
        product.setAuthor(productDTO.getAuthor());
        product.setTranslator(productDTO.getTranslator());
        product.setImageProduct(storageFileName);
        product.setSupplier(productDTO.getSupplier());
        product.setPublisher(productDTO.getPublisher());
        product.setPublishingYear(productDTO.getPublishingYear());
        product.setCategory(category);
        product.setIntroduction(productDTO.getIntroduction());
        product.setPrice(productDTO.getPrice());
        product.setDiscount(productDTO.getDiscount());
        product.setQuantitySold(0);
        product.setQuantity(productDTO.getQuantity());
        product.setWeight(productDTO.getWeight());
        product.setNumberOfPage(productDTO.getNumberOfPage());
        product.setSize(productDTO.getSize());
        product.setLayout(productDTO.getLayout().trim());

        LocalDateTime dateTime = LocalDateTime.now();
        product.setCreatedAt(dateTime);
        product.setUpdatedAt(dateTime);

        try {
            productService.addProduct(product);
            redirectAttributes.addFlashAttribute("success", "Thêm sản phẩm thành công.");
        } catch (Exception e) {
            System.out.println("Lỗi: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Lỗi khi thêm sản phẩm.");
        }
        return "redirect:/dashboard/product_management";
    }


    @GetMapping("/update/{id}")
    public String showEditProductForm(@PathVariable Long id,
                                      Model model) {
        try {
            Product product = productService.getProductById(id);
            model.addAttribute("product", product);

            List<Category> categories = categoryService.getAllCategories();
            model.addAttribute("categories", categories);

            ProductDTO productDTO = new ProductDTO();
            productDTO.setTitle(product.getTitle());
            productDTO.setAuthor(product.getAuthor());
            productDTO.setTranslator(product.getTranslator());
            productDTO.setSupplier(product.getSupplier());
            productDTO.setPublisher(product.getPublisher());
            productDTO.setPublishingYear(product.getPublishingYear());
            productDTO.setCategoryId(product.getCategory().getId());
            productDTO.setIntroduction(product.getIntroduction());
            productDTO.setPrice(product.getPrice());
            productDTO.setDiscount(product.getDiscount());
            productDTO.setQuantity(product.getQuantity());
            productDTO.setWeight(product.getWeight());
            productDTO.setNumberOfPage(product.getNumberOfPage());
            productDTO.setSize(product.getSize());
            productDTO.setLayout(product.getLayout().trim());

            model.addAttribute("productDTO", productDTO);
        } catch (Exception e) {
            System.out.println("Lỗi: " + e.getMessage());
            return "redirect:/dashboard/product_management";
        }

        return "admin/products-detail";
    }


    @PostMapping("/update/{id}")
    public String editProduct(@PathVariable Long id,
                              @Valid @ModelAttribute("productDTO") ProductDTO productDTO,
                              BindingResult result,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        try {
            Product product = productService.getProductById(id);
            if (product == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy sản phẩm.");
                return "redirect:/dashboard/product_management";
            }
            model.addAttribute("product", product);

            if (result.hasErrors()) {
                return "admin/products-detail";
            }

            if (!productDTO.getImageProduct().isEmpty()) {
                //Xoá ảnh cũ
                Path uploadDir = Paths.get(PRODUCT_IMAGE_DIR);
                Path oldImagePath = uploadDir.resolve(product.getImageProduct());

                try {
                    Files.delete(oldImagePath);
                } catch (Exception e) {
                    System.out.println("Lỗi: " + e.getMessage());
                }

                MultipartFile image = productDTO.getImageProduct();
                Date createdAt = new Date();
                String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();
                Path filePath = uploadDir.resolve(storageFileName);
                try (InputStream inputStream = image.getInputStream()) {
                    Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                }
                product.setImageProduct(storageFileName);
            }

            Category category = null;
            List<Category> categories = categoryService.getAllCategories();
            if (categories != null) { // Kiểm tra null
                for (Category item : categories) {
                    if (Objects.equals(item.getId(), productDTO.getCategoryId())) {
                        category = item;
                        break;
                    }
                }
            } else {
                throw new IllegalStateException("Danh sách danh mục không thể tải.");
            }
            if (category == null) {
                throw new IllegalArgumentException("Không tìm thấy danh mục với ID: " + productDTO.getCategoryId());
            }

            product.setTitle(productDTO.getTitle());
            product.setAuthor(productDTO.getAuthor());
            product.setTranslator(productDTO.getTranslator());
            product.setSupplier(productDTO.getSupplier());
            product.setPublisher(productDTO.getPublisher());
            product.setPublishingYear(productDTO.getPublishingYear());
            product.setCategory(category);
            product.setIntroduction(productDTO.getIntroduction());
            product.setPrice(productDTO.getPrice());
            product.setDiscount(productDTO.getDiscount());
            product.setQuantitySold(0);
            product.setQuantity(productDTO.getQuantity());
            product.setWeight(productDTO.getWeight());
            product.setNumberOfPage(productDTO.getNumberOfPage());
            product.setSize(productDTO.getSize());
            product.setLayout(productDTO.getLayout().trim());

            LocalDateTime dateTime = LocalDateTime.now();
            product.setUpdatedAt(dateTime);
            try {
                productService.updateProduct(product);
                redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin sản phẩm thành công.");
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Lỗi cập nhật thông tin sản phẩm.");
                System.out.println("Lỗi: " + e.getMessage());
            }

        } catch (Exception e) {
           System.out.println("Lỗi: " + e.getMessage());
           redirectAttributes.addAttribute("error", "Lỗi khi cập nhật sản phẩm: " + e.getMessage());
        }
        return "redirect:/dashboard/product_management";
    }


    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id,
                                RedirectAttributes redirectAttributes) {
        try {
            Product product = productService.getProductById(id);
            if (product != null) {
                // Lưu lại thông tin sản phẩm trước khi xóa để hiển thị trong thông báo
                String productTitle = product.getTitle();
                int productCount = product.getOrderDetails().size();
                if (productCount > 0) {
                    redirectAttributes.addFlashAttribute("error",
                            "Không thể xóa sản phẩm vì có " + productCount +
                                    " đơn hàng đang có chứa sản phẩm này.");
                    return "redirect:/dashboard/product_management";
                }
                // Xóa sản phẩm
                productService.deleteProduct(id);
                redirectAttributes.addFlashAttribute("success", "Xoá sản phẩm \"" + productTitle + "\" thành công");
            } else {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy sản phẩm");
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi khi xóa sản phẩm: " + e.getMessage());
        }
        return "redirect:/dashboard/product_management";
    }

}
