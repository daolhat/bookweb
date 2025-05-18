package com.programing.bookweb.controller;

import com.programing.bookweb.entity.User;
import com.programing.bookweb.service.IUserService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@AllArgsConstructor
@RequestMapping("/register")
public class RegisterController extends BaseController{

    @Autowired
    private IUserService userService;


    @GetMapping()
    public String showRegistrationForm(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return "redirect:/";
        }
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new User());
        }
        return "user/register";
    }


    @PostMapping
    public String registerUser(@ModelAttribute("user") @Valid User user,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("error", "Vui lòng điền đầy đủ thông tin hợp lệ.");
            return "user/register";
        }

        if (user.getPassword() == null || user.getPassword().length() < 8) {
            model.addAttribute("error", "Mật khẩu phải có ít nhất 8 ký tự.");
            return "user/register";
        }

        try {
            boolean isRegistered = userService.registerUser(user);
            if (isRegistered) {
                redirectAttributes.addFlashAttribute("success", "Đăng ký thành công!");
                return "redirect:/login";
            } else {
                model.addAttribute("error", "Đã có lỗi xảy ra trong quá trình đăng ký.");
                return "user/register";
            }
        } catch (RuntimeException e) {
            model.addAttribute("error", e.getMessage());
            return "user/register";
        }
    }

}
