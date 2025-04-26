package com.programing.bookweb.controller;

import com.programing.bookweb.entity.Order;
import com.programing.bookweb.entity.OrderDetail;
import com.programing.bookweb.service.IOrderDetailService;
import com.programing.bookweb.service.IOrderService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("/order")
public class OrderController extends BaseController{

    private final IOrderService orderService;
    private final IOrderDetailService orderDetailService;

    @GetMapping
    public String getAllOrders(Model model) {

        List<Order> orders = orderService.getAllOrdersByUser(getCurrentUser());
        model.addAttribute("orders", orders);

        Long countOrder = orderService.countOrderByUser(getCurrentUser());
        model.addAttribute("countOrder", countOrder);

        return "user/orders";
    }

    @GetMapping("{id}")
    public String details(Model model, @PathVariable Long id) {
        Order order = orderService.getOrderById(id);

        if (order == null) {
            return "redirect:/order?error=order_not_found";
        }

        List<OrderDetail> orderDetails = orderDetailService.getAllOrderDetailByOrder(order);
        model.addAttribute("order", order);
        model.addAttribute("ordersDetails", orderDetails);

        return "user/order-detail";
    }

    @GetMapping("cancel/{id}")
    public String cancel(@PathVariable Long id){
        Order order = orderService.getOrderById(id);

        if (order == null) {
            return "redirect:/order?error=order_not_found";
        }

        try {
            orderService.cancelOrder(order);
            return "redirect:/order/" + id + "?success=order_cancelled";
        } catch (IllegalStateException e) {
            return "redirect:/order/" + id + "?error=" + e.getMessage();
        }
    }

}
