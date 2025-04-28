package com.programing.bookweb.controller.admin;

import com.programing.bookweb.controller.BaseController;
import com.programing.bookweb.entity.Product;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("/dashboard/user_management")
public class AdminUserController extends BaseController {

    private final IUserService userService;
    private final IRoleService roleService;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm dd-MM-yyyy");

    @GetMapping
    public String showUserPageManagement(@RequestParam(name = "page", defaultValue = "1") int page,
                                         Model model) {
        Pageable pageable = PageRequest.of(page - 1, 10);
        Page<User> users = userService.getAllUserPage(pageable);
        //List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        model.addAttribute("totalPages", users.getTotalPages());
        model.addAttribute("pageNumber", page);
        return "admin/users";
    }


    @GetMapping("/{id}")
    public String viewUserDetail(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getUserById(id);
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy người dùng");
                return "redirect:/dashboard/user_management";
            }
            String formattedCreatedAt = user.getCreatedAt() != null ? user.getCreatedAt().format(DATE_TIME_FORMATTER) : "N/A";
            String formattedLastLogin = user.getLastLogin() != null ? user.getLastLogin().format(DATE_TIME_FORMATTER) : "N/A";
            model.addAttribute("user", user);
            model.addAttribute("formattedCreatedAt", formattedCreatedAt);
            model.addAttribute("formattedLastLogin", formattedLastLogin);
            return "admin/user-detail";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi khi tải thông tin người dùng: " + e.getMessage());
            return "redirect:/dashboard/user_management";
        }
    }


    @PostMapping("/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.getUserById(id);
            if (user == null) {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy người dùng");
                return "redirect:/dashboard/user_management";
            }
            // Cập nhật trạng thái ngược lại
            boolean newStatus = !user.isEnabled();
            user.setEnabled(newStatus);
            userService.saveUser(user);
            String statusMessage = newStatus ? "kích hoạt" : "vô hiệu hóa";
            redirectAttributes.addFlashAttribute("success", "Đã " + statusMessage + " tài khoản của " + user.getFullName());
            // Chuyển hướng về trang chi tiết người dùng
            return "redirect:/dashboard/user_management/" + id;
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi khi cập nhật trạng thái người dùng: " + e.getMessage());
            return "redirect:/dashboard/user_management";
        }
    }


    @PostMapping("/{id}/delete")
    @Transactional
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
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
            String userName = user.getFullName(); // Lưu lại tên người dùng trước khi xóa
            user.getRoles().clear();
            userService.saveUser(user);
            // Sau đó xóa user
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", "Đã xóa người dùng " + userName + " thành công");
            return "redirect:/dashboard/user_management";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Không thể xóa người dùng: " + e.getMessage());
            return "redirect:/dashboard/user_management";
        }
    }

}
