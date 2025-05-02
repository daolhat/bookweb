package com.programing.bookweb.controller;

import com.programing.bookweb.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/login")
public class LoginController extends BaseController{

    @GetMapping
    public String getLoginPage(Model model, RedirectAttributes redirectAttributes){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            // Người dùng đã đăng nhập, chuyển về trang checkout nếu có redirect
            if (model.getAttribute("checkoutRedirect") != null) {
                return "redirect:/cart/checkout";
            }
            return "redirect:/home";
        }

        // Chuyển thông báo về checkout redirect từ flash attribute vào model
        if (redirectAttributes.getFlashAttributes().containsKey("checkoutRedirect")) {
            model.addAttribute("checkoutRedirect", true);
        }

        return "user/login";
    }
}
