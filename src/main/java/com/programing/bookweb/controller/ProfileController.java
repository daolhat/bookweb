package com.programing.bookweb.controller;

import com.programing.bookweb.dto.UserDTO;
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

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Controller
@AllArgsConstructor
@RequestMapping("/profile")
public class ProfileController extends BaseController{

    private final IUserService userService;
    private final PasswordEncoder passwordEncoder;

    private static final String USER_IMAGE_DIR = "public/images/user";

    @GetMapping
    public String getUser(Model model) {
        try {
            User user = userService.getUserById(getCurrentUser().getId());
            model.addAttribute("user", user);

            UserDTO userDTO = new UserDTO();
            userDTO.setAddress(user.getAddress());
            userDTO.setGender(user.getGender());
            userDTO.setFullName(user.getFullName());
            userDTO.setPhoneNumber(user.getPhoneNumber());

            Date date = Date.from(user.getBirthday().atStartOfDay(ZoneId.systemDefault()).toInstant());
            userDTO.setBirthday(date);

            model.addAttribute("userDTO", userDTO);
        } catch (Exception e) {
            System.out.println("Lỗi: " + e.getMessage());
            return "redirect:/";
        }
        return "user/user-profile";
    }


    @PostMapping("/update")
    public String updateUser(@Valid @ModelAttribute("userDTO") UserDTO userDTO,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes) {

        try {
            User user = userService.getUserById(getCurrentUser().getId());
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy thông tin tài khoản");
                return "redirect:/profile";
            }
            model.addAttribute("user", user);

            if (result.hasErrors()) {
                return "user/user-profile";
            }

            Path uploadDir = Paths.get(USER_IMAGE_DIR);

            if (!userDTO.getAvatar().isEmpty()) {
                //Xoá ảnh cũ
                Path oldImagePath = uploadDir.resolve(user.getAvatar());
                try {
                    Files.delete(oldImagePath);
                } catch (Exception e) {
                    System.out.println("Lỗi: " + e.getMessage());
                }
                MultipartFile image = userDTO.getAvatar();
                Date createdAt = new Date();
                String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();
                Path filePath = uploadDir.resolve(storageFileName);
                try (InputStream inputStream = image.getInputStream()) {
                    Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                }
                user.setAvatar(storageFileName);
            }

//            MultipartFile image = userDTO.getAvatar();
//            Date createdAt = new Date();
//            String storageFileName = createdAt.getTime() + "_" + image.getOriginalFilename();
//            try {
//                if (!Files.exists(uploadDir)){
//                    Files.createDirectories(uploadDir);
//                }
//                Path filePath = uploadDir.resolve(storageFileName);
//                try (InputStream inputStream = image.getInputStream()) {
//                    Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
//                }
//            } catch (Exception e) {
//                System.out.println("Lỗi ngoại lệ: " + e.getMessage());
//            }

//            user.setAvatar(storageFileName);
            user.setFullName(userDTO.getFullName());
            user.setGender(userDTO.getGender());
            user.setPhoneNumber(userDTO.getPhoneNumber());
            user.setAddress(userDTO.getAddress());
            user.setUpdatedAt(LocalDateTime.now());
            LocalDate localDate = userDTO.getBirthday().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            user.setBirthday(localDate);

            try {
                userService.updateUser(user);
                redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin tài khoản thành công.");
            } catch (Exception e) {
                System.out.println("Lỗi: " + e.getMessage());
                redirectAttributes.addFlashAttribute("error", "Cập nhật thông tin tài khoản không thành công.");
            }
        } catch (Exception e) {
            System.out.println("Lỗi: " + e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Lỗi cập nhật thông tin tài khoản");
        }
        return "redirect:/profile";
    }


    @GetMapping("/change-password")
    public String showChangePasswordForm(Model model) {
        User user = userService.getUserById(getCurrentUser().getId());
        model.addAttribute("user", user);
        return "user/change-password";
    }


    @PostMapping("/change-password")
    public String changePassword(@RequestParam("currentPassword") @NotBlank String oldPassword,
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
            redirectAttributes.addFlashAttribute("success", "Đổi mật khẩu thành công!");
            return "redirect:/login";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Lỗi: " + e.getMessage());
        }
        return "redirect:/profile";
    }
}
