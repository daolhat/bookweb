package com.programing.bookweb.controller.admin;

import com.programing.bookweb.controller.BaseController;
import com.programing.bookweb.entity.Category;
import com.programing.bookweb.service.ICategoryService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("/dashboard/category_management")
public class AdminCategoryController extends BaseController {

    private final ICategoryService categoryService;

    @GetMapping()
    public String showCategoryPageManagement(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        return "admin/categories";
    }


    @GetMapping("/add")
    public String showAddCategoryForm(Model model) {
        model.addAttribute("category", new Category());
        return "admin/category-add";
    }


    @PostMapping("/add")
    public String addNewCategory(@Valid @ModelAttribute("category") Category category,
                                 BindingResult result,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("error", "Nhập lại thông tin");
            return "admin/category-add";
        }
        if (categoryService.existsByName(category)) {
            result.rejectValue("name", "error.category", "Danh mục thể loại đã tồn tại");
            return "admin/category-add";
        }
        categoryService.addCategory(category);
        redirectAttributes.addFlashAttribute("success", "Category added successfully");
        return "redirect:/dashboard/category_management";
    }


    @GetMapping("/update/{id}")
    public String showEditCategoryForm(Model model, @PathVariable Long id) {
        model.addAttribute("category", categoryService.getCategoryById(id));
        return "admin/category-detail";
    }


    @PostMapping("/update/{id}")
    public String editCategory(@PathVariable Long id,
                                 @Valid @ModelAttribute("category") Category category,
                                 BindingResult result,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("error", "Nhập lại thông tin");
            return "admin/category-detail";
        }
        categoryService.updateCategory(id, category);
        redirectAttributes.addFlashAttribute("success", "Category updated successfully");
        return "redirect:/dashboard/category_management";
    }


    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Category category = categoryService.getCategoryById(id);
            if (category != null) {
                // Lưu lại tên danh mục trước khi xóa
                String categoryName = category.getName();
                // Kiểm tra số lượng sản phẩm
                int productCount = category.getProducts().size();
                if (productCount > 0) {
                    redirectAttributes.addFlashAttribute("error",
                            "Không thể xóa thể loại vì có " + productCount +
                                    " sản phẩm đang sử dụng thể loại này. Vui lòng xóa các sản phẩm trước.");
                    return "redirect:/dashboard/category_management";
                }
                // Thực hiện xóa khi không có sản phẩm
                categoryService.deleteCategory(id);
                redirectAttributes.addFlashAttribute("success", "Xóa danh mục " + categoryName + " thành công");
            } else {
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy danh mục");
            }
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Không thể xóa danh mục: " + e.getMessage());
        }
        return "redirect:/dashboard/category_management";
    }

}
