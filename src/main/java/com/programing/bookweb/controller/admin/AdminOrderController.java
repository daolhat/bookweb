package com.programing.bookweb.controller.admin;

import com.programing.bookweb.controller.BaseController;
import com.programing.bookweb.entity.Order;
import com.programing.bookweb.entity.OrderDetail;
import com.programing.bookweb.enums.OrderStatus;
import com.programing.bookweb.service.IOrderDetailService;
import com.programing.bookweb.service.IOrderService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("/dashboard/order_management")
public class AdminOrderController extends BaseController {

    private final IOrderService orderService;
    private final IOrderDetailService orderDetailService;


    @GetMapping
    public String showOrderPageManagement(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "status", required = false) String statusString,
            Model model) {
        try {
            int pageSize = 100;
            Pageable pageable = PageRequest.of(page - 1, pageSize, Sort.by("createdAt").descending());

            Page<Order> orderPage;
            OrderStatus status = null;

            // Chỉ chuyển đổi status khi statusString không null và không rỗng
            if (statusString != null && !statusString.isEmpty()) {
                try {
                    status = OrderStatus.valueOf(statusString.toUpperCase());
                    // orderPage = orderService.getOrdersByStatus(status, pageable);
                } catch (IllegalArgumentException e) {
                    // Xử lý khi statusString không khớp với bất kỳ giá trị enum nào
                    // Chỉ log lỗi, không ném exception
                    e.printStackTrace();
                }
            }

            // Mặc định lấy tất cả đơn hàng
            orderPage = orderService.getAllOrders(pageable);

            model.addAttribute("orders", orderPage);
            model.addAttribute("selectedStatus", status);
            return "admin/orders";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Có lỗi xảy ra khi tải danh sách đơn hàng: " + e.getMessage());
            return "admin/orders";
        }
    }



    @GetMapping("/detail/{id}")
    public String showOrderDetailPage(Model model, @PathVariable Long id) {
        try {
            Order order = orderService.getOrderById(id);
            List<OrderDetail> orderDetails = orderDetailService.getAllOrderDetailByOrder(order);

            model.addAttribute("order", order);
            model.addAttribute("ordersDetails", orderDetails);

            return "admin/order-detail";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Không thể tải thông tin đơn hàng: " + e.getMessage());
            return "redirect:/dashboard/order_management";
        }
    }


    @GetMapping("/detail/process/{id}")
    public String setProcess(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        orderService.setProcessingOrder(order);

        return "redirect:/dashboard/order_management/detail/" + id;
    }


    @GetMapping("/detail/deliver/{id}")
    public String setDeliver(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        orderService.setDeliveringOrder(order);

        return "redirect:/dashboard/order_management/detail/" + id;
    }


    @GetMapping("/detail/delivered/{id}")
    public String setDelivered(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        orderService.setDeliveredOrder(order);

        return "redirect:/dashboard/order_management/detail/" + id;
    }


    @GetMapping("/detail/cancel/{id}")
    public String setCancel(@PathVariable Long id) {
        Order order = orderService.getOrderById(id);
        orderService.cancelOrder(order);

        return "redirect:/dashboard/order_management/detail/" + id;
    }

}
