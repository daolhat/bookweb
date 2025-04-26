package com.programing.bookweb.controller.admin;

import com.programing.bookweb.controller.BaseController;
import com.programing.bookweb.entity.User;
import com.programing.bookweb.service.IRoleService;
import com.programing.bookweb.service.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("/dashboard/user_management")
public class AdminUserController extends BaseController {

    private final IUserService userService;
    private final IRoleService roleService;

    @GetMapping
    public String showUserPageManagement(Model model) {

//        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
//        Page<User> userPage;
//
//        if (roleName != null && !roleName.isEmpty()) {
//            userPage = userService.getAllUserOrderByRoles(roleName, pageable);
//            model.addAttribute("currentRole", roleName);
//        } else {
//            userPage = userService.getAllUserOrderByCreatedDate(pageable);
//        }
//
//        model.addAttribute("userPage", userPage);
//        model.addAttribute("currentPage", page);
//        model.addAttribute("totalPages", userPage.getTotalPages());
//        model.addAttribute("roles", roleService.getAllRoles());

        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);

        return "admin/users";
    }


    @GetMapping("/{id}")
    public String viewUserDetail(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id);
        if (user == null) {
            return "redirect:/dashboard/user_management?error=user_not_found";
        }
        model.addAttribute("user", user);
        return "admin/user-detail";
    }


    @PostMapping("/{id}/toggle-status")
    public String toggleUserStatus(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        User user = userService.getUserById(id);

        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy người dùng");
            return "redirect:/dashboard/user_management";
        }

        // Cập nhật trạng thái ngược lại
        user.setEnabled(!user.isEnabled());
        userService.saveUser(user);

        String statusMessage = user.isEnabled() ? "kích hoạt" : "vô hiệu hóa";
        redirectAttributes.addFlashAttribute("success",
                "Đã " + statusMessage + " tài khoản của " + user.getFullName());

        return "redirect:/dashboard/user_management";
    }


    @PostMapping("/{id}/delete")
    public String deleteUser(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes,
            HttpServletRequest request) {

        User user = userService.getUserById(id);

        if (user == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy người dùng");
            return "redirect:/dashboard/user_management";
        }

        // Kiểm tra không cho phép tự xóa tài khoản đang đăng nhập
        User currentUser = getCurrentUser();
        if (currentUser != null && currentUser.getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "Không thể xóa tài khoản đang đăng nhập");
            return "redirect:/dashboard/user_management";
        }

        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", "Đã xóa người dùng " + user.getFullName() + " thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "Không thể xóa người dùng. Lỗi: " + e.getMessage());
        }

        return "redirect:/dashboard/user_management";
    }

}
