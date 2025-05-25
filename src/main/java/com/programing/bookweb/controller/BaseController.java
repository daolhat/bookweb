package com.programing.bookweb.controller;

import com.programing.bookweb.dto.CartDTO;
import com.programing.bookweb.entity.Category;
import com.programing.bookweb.entity.User;
import com.programing.bookweb.service.ICartService;
import com.programing.bookweb.service.ICategoryService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@Controller
public abstract class BaseController {

    @Autowired
    private ICartService cartService;

    @Autowired
    private ICategoryService categoryService;


    @ModelAttribute("cartItemCount")
    public int cartItemCount(HttpSession session) {
        CartDTO cart = cartService.getCart(session);
        if (cart == null || cart.getCartItems() == null || cart.getCartItems().isEmpty()){
            return 0;
        }
        return cart.getCartItems().size();
    }


    @ModelAttribute("categories")
    public List<Category> getAllCategory(){
        return categoryService.getAllCategories();
    }


    @ModelAttribute("currentUser")
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        return null;
    }
}
