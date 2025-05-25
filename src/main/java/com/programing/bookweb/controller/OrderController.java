package com.programing.bookweb.controller;

import com.programing.bookweb.entity.Order;
import com.programing.bookweb.entity.OrderDetail;
import com.programing.bookweb.entity.User;
import com.programing.bookweb.service.IOrderDetailService;
import com.programing.bookweb.service.IOrderService;
import com.programing.bookweb.service.IUserService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("/order")
public class OrderController extends BaseController{

    private final IOrderService orderService;
    private final IOrderDetailService orderDetailService;
    private final IUserService userService;

    @GetMapping
    public String getAllOrders(@RequestParam(name = "page", defaultValue = "1") int page,
                               Model model) {
        Pageable pageable = PageRequest.of(page - 1, 3);
        Page<Order> orders = orderService.getAllOrdersByUserPage(getCurrentUser(), pageable);
        model.addAttribute("orders", orders);
        model.addAttribute("totalPages", orders.getTotalPages());
        model.addAttribute("pageNumber", page);
        Long countOrder = orderService.countOrderByUser(getCurrentUser());
        model.addAttribute("countOrder", countOrder);
        User user = userService.getUserById(getCurrentUser().getId());
        model.addAttribute("user", user);
        return "user/orders";
    }


    @GetMapping("{id}")
    public String details(Model model,
                          @PathVariable Long id,
                          RedirectAttributes redirectAttributes) {
        Order order = orderService.getOrderById(id);
        if (order == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng");
            return "redirect:/order?error=order_not_found";
        }
        List<OrderDetail> orderDetails = orderDetailService.getAllOrderDetailByOrder(order);
        model.addAttribute("order", order);
        model.addAttribute("ordersDetails", orderDetails);
        User user = userService.getUserById(getCurrentUser().getId());
        model.addAttribute("user", user);
        return "user/order-detail";
    }

    @GetMapping("cancel/{id}")
    public String cancel(@PathVariable Long id,
                         RedirectAttributes redirectAttributes){
        Order order = orderService.getOrderById(id);
        if (order == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng");
            return "redirect:/order?error=order_not_found";
        }
        try {
            orderService.cancelOrder(order);
            redirectAttributes.addFlashAttribute("success", "Huỷ đơn hàng thành công!");
            return "redirect:/order/" + id + "?success=order_cancelled";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("error", "Huỷ đơn hàng không thành công!");
            return "redirect:/order/" + id + "?error=" + e.getMessage();
        }
    }

}
