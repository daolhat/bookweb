package com.programing.bookweb.controller;

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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/shop")
@AllArgsConstructor
public class ShopController extends BaseController{

    private final IProductService productService;
    private final ICategoryService categoryService;


    @GetMapping
    public String getShopPage(
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "layout", required = false) String layout,
            @RequestParam(value = "sort", required = false) String sortBy,
            @RequestParam(name = "page", defaultValue = "1") int page,
            Model model) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);

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
            products = productService.getAllProducts(pageable);
        }
        model.addAttribute("products", products);
        model.addAttribute("totalPages", products.getTotalPages());
        model.addAttribute("pageNumber", page);
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("selectedLayout", layout);
        model.addAttribute("selectedSort", sortBy);
        return "user/shop";
    }


    @GetMapping("/product/{id}")
    public String viewProductDetail(@PathVariable Long id, Model model) {
        Pageable pageable = PageRequest.of(0, 6);
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);
        List<Product> products = productService.getProductsByCategory(product.getCategory().getId(), pageable);
        model.addAttribute("relatedProducts", products);
        return "user/product-detail";
    }


}
