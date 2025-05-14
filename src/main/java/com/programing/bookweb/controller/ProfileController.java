package com.programing.bookweb.controller;

import com.programing.bookweb.entity.User;
import com.programing.bookweb.service.IUserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@AllArgsConstructor
@RequestMapping("/profile")
public class ProfileController extends BaseController{

    private final IUserService userService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public String getUser(Model model) {
        User user = userService.getUserById(getCurrentUser().getId());
        model.addAttribute("user", user);
        return "user/user-profile";
    }

    @PostMapping("/update")
    public String updateUser(
            @ModelAttribute("user") @Valid User user,
            BindingResult bindingResult,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar,
            RedirectAttributes redirectAttributes) {

        // Lấy thông tin người dùng hiện tại từ DB
        User existingUser = userService.getUserById(user.getId());
        if (existingUser == null) {
            redirectAttributes.addFlashAttribute("error", "Người dùng không tồn tại!");
            return "redirect:/profile";
        }

        // Cập nhật thông tin từ form
        existingUser.setFullName(user.getFullName());
        existingUser.setEmail(user.getEmail());
        existingUser.setPhoneNumber(user.getPhoneNumber());
        existingUser.setGender(user.getGender());
        existingUser.setBirthday(user.getBirthday());
        existingUser.setAddress(user.getAddress());
        userService.updateUser(existingUser, avatar);

        redirectAttributes.addFlashAttribute("message", "Cập nhật thông tin thành công!");
        return "redirect:/profile";
    }

    @GetMapping("/change-password")
    public String showChangePasswordForm(Model model) {

        User user = userService.getUserById(getCurrentUser().getId());
        model.addAttribute("user", user);
        return "user/change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam("currentPassword") @NotBlank String oldPassword,
            @RequestParam("newPassword") @NotBlank String newPassword,
            @RequestParam("confirmPassword") @NotBlank String confirmPassword,
            RedirectAttributes redirectAttributes) {
        User user = userService.getUserById(getCurrentUser().getId());
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu cũ không trùng khớp!");
            return "redirect:/profile";
        }

        if (oldPassword.equals(newPassword)) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu cũ trùng với mật khẩu mới!");
            return "redirect:/profile";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu mới không trùng khớp!");
            return "redirect:/profile";
        }

        try {
            userService.changePassword(user, newPassword);
            redirectAttributes.addFlashAttribute("message", "Đổi mật khẩu thành công!");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }

        return "redirect:/profile";
    }
}
