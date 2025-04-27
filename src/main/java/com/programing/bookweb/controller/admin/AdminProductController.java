package com.programing.bookweb.controller.admin;

import com.programing.bookweb.controller.BaseController;
import com.programing.bookweb.entity.Category;
import com.programing.bookweb.entity.Product;
import com.programing.bookweb.service.ICategoryService;
import com.programing.bookweb.service.IProductService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
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
import java.sql.SQLException;
import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("/dashboard/product_management")
public class AdminProductController extends BaseController {

    private final IProductService productService;
    private final ICategoryService categoryService;


    @GetMapping
    public String showProductPageManagement(@RequestParam(name = "categoryId", required = false) Long categoryId,
                                            @RequestParam(name = "layout", required = false) String layout,
//                                            @RequestParam(name = "page", defaultValue = "1") int page,
                                            Model model) {
//        int pageSize = 100;
//        Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by("createdAt").descending());
//        Page<Product> products = productService.getAllProducts(pageable);
        List<Product> products;
        if (categoryId != null && layout != null){
            products = productService.getProductByCategoryIdAndLayout(categoryId, layout);
        } else if (categoryId != null) {
            products = productService.getProductByCategoryId(categoryId);
        } else if (layout != null) {
            products = productService.getProductByLayout(layout);
        } else {
            products = productService.getAllProductByList();
        }
        model.addAttribute("products", products);

        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
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
            @Valid @ModelAttribute("product") Product product,
            BindingResult result,
            @RequestParam("imageProduct") MultipartFile imageProduct,
            Model model,
            RedirectAttributes redirectAttributes)
    throws IOException {
        if (result.hasErrors()) {
            List<Category> categories = categoryService.getAllCategories();
            model.addAttribute("categories", categories);
            model.addAttribute("error", "Nhập lại thông tin");
            return "admin/product-add";
        }

        try {
            productService.addProduct(product, imageProduct);
            redirectAttributes.addFlashAttribute("success", "Thêm thành công sản phẩm");
            return "redirect:/dashboard/product_management";
        } catch (SQLException | IOException e) {
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("error", "Lỗi tải file: " + e.getMessage());
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
            System.out.println("ERROR: Không tìm thấy sản phẩm với ID: " + id);
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy sản phẩm");
            return "redirect:/dashboard/product_management";
        }

        System.out.println("--------- DEBUG INFO ---------");
        System.out.println("ID sản phẩm: " + id);
        System.out.println("CategoryId từ form: " + categoryId);
        // Tránh in đối tượng Product trực tiếp vì có thể gây StackOverflowError
        System.out.println("Thông tin cơ bản từ form: " + productForm.getTitle() + ", " + productForm.getAuthor());
        System.out.println("Thông tin category hiện tại: " + (existingProduct.getCategory() != null ?
                existingProduct.getCategory().getId() + " - " + existingProduct.getCategory().getName() : "NULL"));

        try {
            String currentImageName = existingProduct.getImageProduct();
            System.out.println("Current image name: " + currentImageName);
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
                    System.out.println("Gán category: " + category.getId() + " - " + category.getName());
                    existingProduct.setCategory(category);
                } else {
                    System.out.println("WARNING: Không tìm thấy category với ID: " + categoryId);
                }
            } else {
                // Nếu không có categoryId, giữ nguyên category hiện tại
                System.out.println("Giữ nguyên category hiện tại");
            }
            // Kiểm tra nếu category vẫn null
            if (existingProduct.getCategory() == null) {
                System.out.println("ERROR: Category là null sau khi cập nhật!");
                List<Category> categories = categoryService.getAllCategories();
                model.addAttribute("categories", categories);
                model.addAttribute("error", "Vui lòng chọn thể loại sách");
                model.addAttribute("product", existingProduct);
                return "admin/products-detail";
            }

            System.out.println("Đã cập nhật các trường và category thành công, chuẩn bị lưu vào DB");

            // Xử lý file ảnh nếu được upload
            if (imageFile != null && !imageFile.isEmpty()) {
                System.out.println("Uploading new image: " + imageFile.getOriginalFilename());
                Product updatedProduct = productService.updateProduct(existingProduct, imageFile);
                if (updatedProduct != null) {
                    redirectAttributes.addFlashAttribute("success", "Cập nhật sản phẩm thành công");
                } else {
                    redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật sản phẩm");
                }
            } else {
                System.out.println("Keeping existing image: " + currentImageName);

                // Đặt lại giá trị ảnh hiện tại - ĐÂY LÀ PHẦN QUAN TRỌNG
                existingProduct.setImageProduct(currentImageName);
                Product updatedProduct = productService.updateProduct(existingProduct, null);
                if (updatedProduct != null) {
                    redirectAttributes.addFlashAttribute("success", "Cập nhật sản phẩm thành công");
                } else {
                    redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật sản phẩm");
                }
            }

            System.out.println("Update completed successfully");
            return "redirect:/dashboard/product_management";

        } catch (Exception e) {
            System.out.println("ERROR during update: " + e.getMessage());
            e.printStackTrace();

            List<Category> categories = categoryService.getAllCategories();
            model.addAttribute("categories", categories);
            model.addAttribute("error", "Lỗi khi cập nhật sản phẩm: " + e.getMessage());
            model.addAttribute("product", existingProduct); // Giữ lại dữ liệu sản phẩm

            return "admin/products-detail";
        }
    }


    @GetMapping("/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (productService.getProductById(id) != null) {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("success", "Xoá sản phẩm thành công");
        }
        return "redirect:/dashboard/product_management";
    }

}
