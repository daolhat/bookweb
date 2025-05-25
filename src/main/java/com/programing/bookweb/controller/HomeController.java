package com.programing.bookweb.controller;

import com.programing.bookweb.entity.Category;
import com.programing.bookweb.entity.Product;
import com.programing.bookweb.service.ICategoryService;
import com.programing.bookweb.service.IProductService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@AllArgsConstructor
public class HomeController extends BaseController{

    private final IProductService productService;
    private final ICategoryService categoryService;


    @GetMapping("/")
    String getUserHomePage(Model model){
        Pageable pageable = PageRequest.of(0, 12);
        List<Product> bestSeller = productService.getBestSeller(pageable);
        model.addAttribute("bestSeller", bestSeller);
        List<Product> newProducts = productService.getNewProducts(pageable);
        model.addAttribute("newProducts", newProducts);
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        return "user/index";
    }

}
