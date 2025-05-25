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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Controller
@AllArgsConstructor
@RequestMapping("/dashboard/order_management")
public class AdminOrderController extends BaseController {

    private final IOrderService orderService;
    private final IOrderDetailService orderDetailService;

    @GetMapping
    public String showOrderPageManagement(@RequestParam(value = "page", defaultValue = "1") int page,
                                          @RequestParam(value = "status", required = false) String statusString,
                                          @RequestParam(value = "search", required = false) String search,
                                          @RequestParam(value = "fromDate", required = false) String fromDate,
                                          @RequestParam(value = "toDate", required = false) String toDate,
                                          Model model) {
        Pageable pageable = PageRequest.of(page - 1, 20, Sort.by("createdAt").descending());
        Page<Order> orders;
        String searchKeyword = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        String statusTemp = (statusString != null && !statusString.trim().isEmpty()) ? statusString.trim() : null;
        OrderStatus status = null;

        if (statusTemp != null) {
            status = OrderStatus.valueOf(statusTemp);
        }

        LocalDateTime startDate = null;
        if (fromDate != null && !fromDate.trim().isEmpty()){
            LocalDate localDate = LocalDate.parse(fromDate.trim());
            startDate = localDate.atStartOfDay();
        }

        LocalDateTime endDate = null;
        if (toDate != null && !toDate.trim().isEmpty()){
            LocalDate localDate = LocalDate.parse(toDate.trim());
            endDate = localDate.atTime(LocalTime.MAX);
        }

        try {
            if (status != null && startDate != null && endDate != null && startDate.isBefore(endDate) && searchKeyword == null) {
                orders = orderService.getOrdersByStatusAndBetween(status, startDate, endDate, pageable);
            } else if (status != null) {
                orders = orderService.getOrdersByStatus(status, pageable);
            } else if (startDate != null && endDate != null && startDate.isBefore(endDate)) {
                orders = orderService.getOrdersBetween(startDate, endDate, pageable);
            } else if (searchKeyword != null) {
                orders = orderService.getOrderSearch(searchKeyword, pageable);
            } else {
                orders = orderService.getAllOrders(pageable);
            }
        } catch (Exception e) {
            e.printStackTrace();
            orders = orderService.getAllOrders(pageable);
            model.addAttribute("error", "Có lỗi xảy ra khi tải danh sách đơn hàng: " + e.getMessage());
        }
        model.addAttribute("orders", orders);
        model.addAttribute("totalPages", orders.getTotalPages());
        model.addAttribute("pageNumber", page);
        model.addAttribute("selectedStatus", status != null ? status.toString() : null);
        model.addAttribute("searchKeyword", searchKeyword);
        model.addAttribute("startDate", fromDate);
        model.addAttribute("endDate", toDate);
        return "admin/orders";
    }


    @GetMapping("/detail/{id}")
    public String showOrderDetailPage(Model model,
                                      @PathVariable Long id,
                                      RedirectAttributes redirectAttributes) {
        try {
            Order order = orderService.getOrderById(id);
            if (order == null){
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng.");
                return "redirect:/dashboard/order_management";
            }
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
    public String setProcess(@PathVariable Long id,
                             RedirectAttributes redirectAttributes) {
        try {
            Order order = orderService.getOrderById(id);
            if (order == null){
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng.");
                return "redirect:/dashboard/order_management";
            }
            orderService.setProcessingOrder(order);
            redirectAttributes.addFlashAttribute("success", "Đã xác nhận đơn hàng.");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi xác nhận đơn hàng.");
        }
        return "redirect:/dashboard/order_management/detail/" + id;
    }


    @GetMapping("/detail/deliver/{id}")
    public String setDeliver(@PathVariable Long id,
                             RedirectAttributes redirectAttributes) {
        try {
            Order order = orderService.getOrderById(id);
            if (order == null){
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng.");
                return "redirect:/dashboard/order_management";
            }
            orderService.setDeliveringOrder(order);
            redirectAttributes.addFlashAttribute("success", "Đơn hàng đã xác nhận đang được giao.");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi xác nhận trạng thái đơn hàng.");
        }
        return "redirect:/dashboard/order_management/detail/" + id;
    }


    @GetMapping("/detail/delivered/{id}")
    public String setDelivered(@PathVariable Long id,
                               RedirectAttributes redirectAttributes) {
        try {
            Order order = orderService.getOrderById(id);
            if (order == null){
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng.");
                return "redirect:/dashboard/order_management";
            }
            orderService.setDeliveredOrder(order);
            redirectAttributes.addFlashAttribute("success", "Đơn hàng đã xác nhận giao hàng thành công cho khách hàng.");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi xác nhận trạng thái đơn hàng.");
        }
        return "redirect:/dashboard/order_management/detail/" + id;
    }


    @GetMapping("/detail/cancel/{id}")
    public String setCancel(@PathVariable Long id,
                            RedirectAttributes redirectAttributes) {
        try {
            Order order = orderService.getOrderById(id);
            if (order == null){
                redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn hàng.");
                return "redirect:/dashboard/order_management";
            }
            orderService.cancelOrder(order);
            redirectAttributes.addFlashAttribute("success", "Đơn hàng đã được huỷ.");
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Lỗi xác nhận trạng thái đơn hàng.");
        }
        return "redirect:/dashboard/order_management/detail/" + id;
    }

}
