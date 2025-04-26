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
        model.addAttribute("book", new Product());

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
            @Valid @ModelAttribute("product") Product product,
            BindingResult result,
            @RequestParam(value = "imageProduct", required = false) MultipartFile imageProduct,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            List<Category> categories = categoryService.getAllCategories();
            model.addAttribute("categories", categories);
            model.addAttribute("error", "Nhập lại thông tin");

            return "admin/products-detail";
        }
        if (productService.getProductById(id) == null) {
            return "redirect:/dashboard/product_management";
        }
        product.setId(id);
        if (imageProduct == null || imageProduct.isEmpty()) {
            Product existingProduct = productService.getProductById(id);
            product.setImageProduct(existingProduct.getImageProduct());
            redirectAttributes.addFlashAttribute("info", "Sản phẩm được cập nhật mà không thay đổi hình ảnh");
        }
        Product updatedProduct = productService.updateProduct(product, imageProduct);
        if (updatedProduct != null) {
            redirectAttributes.addFlashAttribute("success", "Cập nhật sản phẩm thành công");
        } else {
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật sản phẩm");
        }

        return "redirect:/admin/products";
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
