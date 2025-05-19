package com.programing.bookweb.controller.admin;

import com.programing.bookweb.controller.BaseController;
import com.programing.bookweb.entity.Category;
import com.programing.bookweb.entity.Product;
import com.programing.bookweb.service.ICategoryService;
import com.programing.bookweb.service.IProductService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("/dashboard/product_management")
@Slf4j
public class AdminProductController extends BaseController {

    private final IProductService productService;
    private final ICategoryService categoryService;


    @GetMapping
    public String showProductPageManagement(@RequestParam(name = "categoryId", required = false) Long categoryId,
                                            @RequestParam(name = "layout", required = false) String layout,
                                            @RequestParam(name = "keyword", required = false) String keyword,
                                            @RequestParam(name = "sort", required = false) String sortBy,
                                            @RequestParam(name = "page", defaultValue = "1") int page,
                                            Model model) {

        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);

        log.info("Filter parameters: keyword={}, categoryId={}, layout={}, sort={}, page={}",
                keyword, categoryId, layout, sortBy, page);

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
                log.info("Filtering by all three criteria");
            } else if (normalizedKeyword != null && categoryId != null) {
                // Lọc theo từ khóa + danh mục
                products = productService.getProductByCategoryIdAndKeywordUser(categoryId, normalizedKeyword, pageable);
                log.info("Filtering by keyword and category");
            } else if (normalizedKeyword != null && layout != null) {
                // Lọc theo từ khóa + hình thức bìa
                products = productService.getProductByLayoutAndKeywordUser(layout, normalizedKeyword, pageable);
                log.info("Filtering by keyword and layout");
            } else if (categoryId != null && layout != null) {
                // Lọc theo danh mục + hình thức bìa
                products = productService.getProductByCategoryIdAndLayoutUser(categoryId, layout, pageable);
                log.info("Filtering by category and layout");
            } else if (normalizedKeyword != null) {
                // Chỉ lọc theo từ khóa
                products = productService.getProductByKeywordUser(normalizedKeyword, pageable);
                log.info("Filtering by keyword only");
            } else if (categoryId != null) {
                // Chỉ lọc theo danh mục
                products = productService.getProductByCategoryIdUser(categoryId, pageable);
                log.info("Filtering by category only: categoryId={}", categoryId);
            } else if (layout != null) {
                // Chỉ lọc theo hình thức bìa
                products = productService.getProductByLayoutUser(layout, pageable);
                log.info("Filtering by layout only: layout={}", layout);
            } else {
                // Không có bộ lọc nào
                products = productService.getAllProducts(pageable);
                log.info("No filters applied");
            }

            log.info("Query returned {} products", products.getContent().size());
        } catch (Exception e) {
            log.error("Error fetching products: ", e);
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
        model.addAttribute("product", new Product());

        return "admin/product-add";
    }

    @PostMapping("/add")
    public String addNewProduct(
            @ModelAttribute("product") Product product,
            BindingResult result,
            @RequestParam("imageProduct") MultipartFile imageProduct,
            @RequestParam(value = "category", required = false) Long categoryId,
            Model model,
            RedirectAttributes redirectAttributes)
            throws IOException {
        try {
            System.out.println("Adding new product: " + product.getTitle());
            System.out.println("Category ID: " + categoryId);

            // Kiểm tra các trường bắt buộc
            if (product.getTitle() == null || product.getTitle().isEmpty() ||
                    product.getAuthor() == null || product.getAuthor().isEmpty() ||
                    product.getSupplier() == null || product.getSupplier().isEmpty() ||
                    product.getPublisher() == null || product.getPublisher().isEmpty() ||
                    product.getPrice() <= 0) {
                List<Category> categories = categoryService.getAllCategories();
                model.addAttribute("categories", categories);
                model.addAttribute("error", "Vui lòng nhập đầy đủ thông tin cần thiết");
                return "admin/product-add";
            }

            // Xử lý category
            if (categoryId == null || categoryId <= 0) {
                List<Category> categories = categoryService.getAllCategories();
                model.addAttribute("categories", categories);
                model.addAttribute("error", "Vui lòng chọn thể loại sách");
                return "admin/product-add";
            }

            Category category = categoryService.getCategoryById(categoryId);
            if (category == null) {
                List<Category> categories = categoryService.getAllCategories();
                model.addAttribute("categories", categories);
                model.addAttribute("error", "Thể loại không tồn tại");
                return "admin/product-add";
            }

            // Gán category cho product
            product.setCategory(category);

            // Xử lý các giá trị mặc định
            if (product.getQuantitySold() == 0) {
                product.setQuantitySold(0);
            }

            // Thêm sản phẩm
            productService.addProduct(product, imageProduct);
            redirectAttributes.addFlashAttribute("success", "Thêm thành công sản phẩm");
            return "redirect:/dashboard/product_management/add";

        } catch (Exception e) {
            e.printStackTrace();
            List<Category> categories = categoryService.getAllCategories();
            model.addAttribute("categories", categories);
            model.addAttribute("error", "Lỗi khi thêm sản phẩm: " + e.getMessage());
            return "admin/product-add";
        }
    }


    @GetMapping("/update/{id}")
    public String showEditProductForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);

        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);

        return "admin/products-detail";
    }


    @PostMapping("/update/{id}")
    public String editProduct(
            @PathVariable Long id,
            @ModelAttribute("product") Product productForm,
            BindingResult result,
            @RequestParam(value = "image", required = false) MultipartFile imageFile,
            @RequestParam(value = "category", required = false) Long categoryId,
            Model model,
            RedirectAttributes redirectAttributes) {
        // Lấy sản phẩm hiện có từ database
        Product existingProduct = productService.getProductById(id);
        if (existingProduct == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy sản phẩm");
            return "redirect:/dashboard/product_management";
        }

        try {
            String currentImageName = existingProduct.getImageProduct();
            // Cập nhật các trường dữ liệu cơ bản
            existingProduct.setTitle(productForm.getTitle());
            existingProduct.setAuthor(productForm.getAuthor());
            existingProduct.setSupplier(productForm.getSupplier());
            existingProduct.setPublisher(productForm.getPublisher());
            existingProduct.setPublishingYear(productForm.getPublishingYear());
            existingProduct.setIntroduction(productForm.getIntroduction());
            existingProduct.setPrice(productForm.getPrice());
            existingProduct.setDiscount(productForm.getDiscount());
            existingProduct.setQuantitySold(productForm.getQuantitySold());
            existingProduct.setWeight(productForm.getWeight());
            existingProduct.setNumberOfPage(productForm.getNumberOfPage());
            existingProduct.setQuantity(productForm.getQuantity());
            existingProduct.setSize(productForm.getSize());
            existingProduct.setLayout(productForm.getLayout());
            existingProduct.setTranslator(productForm.getTranslator());
            // Xử lý category
            if (categoryId != null && categoryId > 0) {
                Category category = categoryService.getCategoryById(categoryId);
                if (category != null) {
                    existingProduct.setCategory(category);
                } else {
                    model.addAttribute("error", "Không tìm thấy category với ID:" + categoryId);
                }
            } else {
                model.addAttribute("error", "Vui lòng chọn thể loại sách");
            }
            // Kiểm tra nếu category vẫn null
            if (existingProduct.getCategory() == null) {
                List<Category> categories = categoryService.getAllCategories();
                model.addAttribute("categories", categories);
                model.addAttribute("error", "Vui lòng chọn thể loại sách");
                model.addAttribute("product", existingProduct);
                return "admin/products-detail";
            }

            // Xử lý file ảnh nếu được upload
            if (imageFile != null && !imageFile.isEmpty()) {
                Product updatedProduct = productService.updateProduct(existingProduct, imageFile);
                if (updatedProduct != null) {
                    redirectAttributes.addFlashAttribute("success", "Cập nhật sản phẩm thành công");
                } else {
                    redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật sản phẩm");
                }
            } else {
                // Đặt lại giá trị ảnh hiện tại - ĐÂY LÀ PHẦN QUAN TRỌNG
                existingProduct.setImageProduct(currentImageName);
                Product updatedProduct = productService.updateProduct(existingProduct, null);
                if (updatedProduct != null) {
                    redirectAttributes.addFlashAttribute("success", "Cập nhật sản phẩm thành công");
                } else {
                    redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật sản phẩm");
                }
            }

            return "redirect:/dashboard/product_management";

        } catch (Exception e) {
            e.printStackTrace();

            List<Category> categories = categoryService.getAllCategories();
            model.addAttribute("categories", categories);
            model.addAttribute("error", "Lỗi khi cập nhật sản phẩm: " + e.getMessage());
            model.addAttribute("product", existingProduct);

            return "admin/products-detail";
        }
    }


    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Product product = productService.getProductById(id);
            if (product != null) {
                // Lưu lại thông tin sản phẩm trước khi xóa để hiển thị trong thông báo
                String productTitle = product.getTitle();
                int productCount = product.getOrderDetails().size();
                if (productCount > 0){
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
