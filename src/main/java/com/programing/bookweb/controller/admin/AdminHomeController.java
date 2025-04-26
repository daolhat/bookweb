package com.programing.bookweb.controller.admin;

import com.programing.bookweb.controller.BaseController;
import com.programing.bookweb.entity.Order;
import com.programing.bookweb.entity.Product;
import com.programing.bookweb.entity.User;
import com.programing.bookweb.service.IOrderService;
import com.programing.bookweb.service.IProductService;
import com.programing.bookweb.service.IUserService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@AllArgsConstructor
@RequestMapping("/dashboard")
public class AdminHomeController extends BaseController {

    private final IOrderService orderService;
    private final IUserService userService;
    private final IProductService productService;


    @GetMapping
    public String getAdminHomePage(Model model) {
        try {
            // Thông tin đơn hàng
            Map<String, Object> orderStats = getOrderStats();
            model.addAttribute("orderStats", orderStats);
            // Thông tin doanh thu
            Map<String, Object> revenueStats = getRevenueStats();
            model.addAttribute("revenueStats", revenueStats);
            // Thông tin người dùng
            Map<String, Object> userStats = getUserStats();
            model.addAttribute("userStats", userStats);
            // Dữ liệu biểu đồ
            Map<String, Object> chartData = getChartData();
            model.addAttribute("chartLabels", chartData.get("chartLabels"));
            model.addAttribute("chartSalesData", chartData.get("chartSalesData"));
            model.addAttribute("chartRevenueData", chartData.get("chartRevenueData"));
            model.addAttribute("chartUserData", chartData.get("chartUserData"));
            // Sản phẩm bán chạy
            List<Map<String, Object>> topSellingProducts = getTopSellingProducts();
            model.addAttribute("topSellingProducts", topSellingProducts != null ? topSellingProducts : new ArrayList<>());
            // Khách hàng tiêu nhiều tiền nhất
            List<Map<String, Object>> topSpendingUsers = getTopSpendingUsers();
            model.addAttribute("topSpendingUsers", topSpendingUsers != null ? topSpendingUsers : new ArrayList<>());
            // Thông tin tổng quát
            BigDecimal totalRevenue = orderService.getTotalRevenue();
            model.addAttribute("totalRevenue", totalRevenue != null ? totalRevenue : BigDecimal.ZERO);

            Long numberOfUsers = userService.countUser();
            model.addAttribute("numberOfUsers", numberOfUsers != null ? numberOfUsers : 0L);

            Long numberOfBooks = productService.countProduct();
            model.addAttribute("numberOfBooks", numberOfBooks != null ? numberOfBooks : 0L);

            Long numberOfOrders = orderService.countOrder();
            model.addAttribute("numberOfOrders", numberOfOrders != null ? numberOfOrders : 0L);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "admin/index";
    }


    private Map<String, Object> getOrderStats() {
        Map<String, Object> stats = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        // Số đơn hàng hôm nay
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = now.toLocalDate().atTime(23, 59, 59);
        long todayOrderCount = orderService.countOrdersByDateRange(startOfDay, endOfDay);
        stats.put("todayOrderCount", todayOrderCount);
        // Số đơn hàng hôm qua
        LocalDateTime startOfYesterday = now.minusDays(1).toLocalDate().atStartOfDay();
        LocalDateTime endOfYesterday = now.minusDays(1).toLocalDate().atTime(23, 59, 59);
        long yesterdayOrderCount = orderService.countOrdersByDateRange(startOfYesterday, endOfYesterday);
        // Tính tỷ lệ tăng trưởng
        double orderGrowthRate = 0.0;
        if (yesterdayOrderCount > 0) {
            orderGrowthRate = ((double) (todayOrderCount - yesterdayOrderCount) / yesterdayOrderCount) * 100;
        }
        stats.put("orderGrowthRate", orderGrowthRate);
        return stats;
    }


    private Map<String, Object> getRevenueStats() {
        Map<String, Object> stats = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();
        // Doanh thu tháng này
        LocalDateTime startOfMonth = now.toLocalDate().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfMonth = now.toLocalDate().withDayOfMonth(now.toLocalDate().lengthOfMonth())
                .atTime(23, 59, 59);
        BigDecimal monthRevenue = orderService.getRevenueByDateRange(startOfMonth, endOfMonth);
        if (monthRevenue == null) {
            monthRevenue = BigDecimal.ZERO;
        }
        stats.put("monthRevenue", monthRevenue);
        // Doanh thu tháng trước
        LocalDateTime startOfLastMonth = now.minusMonths(1).toLocalDate().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfLastMonth = now.minusMonths(1).toLocalDate()
                .withDayOfMonth(now.minusMonths(1).toLocalDate().lengthOfMonth())
                .atTime(23, 59, 59);
        BigDecimal lastMonthRevenue = orderService.getRevenueByDateRange(startOfLastMonth, endOfLastMonth);
        if (lastMonthRevenue == null) {
            lastMonthRevenue = BigDecimal.ZERO;
        }
        // Tính tỷ lệ tăng trưởng doanh thu
        double revenueGrowthRate = 0.0;
        if (lastMonthRevenue.compareTo(BigDecimal.ZERO) > 0) {
            revenueGrowthRate = (monthRevenue.subtract(lastMonthRevenue).doubleValue() / lastMonthRevenue.doubleValue()) * 100;
        }
        stats.put("revenueGrowthRate", revenueGrowthRate);
        return stats;
    }


    private Map<String, Object> getUserStats() {
        Map<String, Object> stats = new HashMap<>();
        // Tổng số người dùng
        long totalUsers = userService.countUser();
        stats.put("totalUsers", totalUsers);
        // Số người dùng tháng trước
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneMonthAgo = now.minusMonths(1);
        long lastMonthUsers = userService.countUsersBefore(oneMonthAgo);
        // Số người dùng mới trong tháng này
        long newUsersThisMonth = totalUsers - lastMonthUsers;
        // Tỷ lệ tăng trưởng người dùng
        double userGrowthRate = 0.0;
        if (lastMonthUsers > 0) {
            userGrowthRate = ((double) newUsersThisMonth / lastMonthUsers) * 100;
        }
        stats.put("userGrowthRate", userGrowthRate);
        return stats;
    }


    private Map<String, Object> getChartData() {
        Map<String, Object> chartData = new HashMap<>();
        LocalDate now = LocalDate.now();
        List<String> chartLabels = new ArrayList<>();
        List<Long> chartSalesData = new ArrayList<>();
        List<Double> chartRevenueData = new ArrayList<>();
        List<Long> chartUserData = new ArrayList<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'00:00:00.000'Z'");
        // Mặc định 10 ngày gần nhất
        int days = 10;
        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = now.minusDays(i);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(23, 59, 59);
            // Định dạng ngày cho biểu đồ
            chartLabels.add(date.format(dateFormatter));
            // Số lượng đơn hàng trong ngày
            long orderCount = orderService.countOrdersByDateRange(startOfDay, endOfDay);
            chartSalesData.add(orderCount);
            // Doanh thu trong ngày (đơn vị: triệu đồng)
            BigDecimal revenue = orderService.getRevenueByDateRange(startOfDay, endOfDay);
            double revenueInMillions = revenue.doubleValue() / 1000000;
            chartRevenueData.add(revenueInMillions);
            // Số người dùng mới trong ngày
            long newUsers = userService.countUsersByDateRange(startOfDay, endOfDay);
            chartUserData.add(newUsers);
        }
        chartData.put("chartLabels", chartLabels);
        chartData.put("chartSalesData", chartSalesData);
        chartData.put("chartRevenueData", chartRevenueData);
        chartData.put("chartUserData", chartUserData);
        return chartData;
    }


    private List<Map<String, Object>> getTopSellingProducts() {
        // Mặc định lấy theo tháng
        LocalDateTime startDate = LocalDateTime.now().minusMonths(1);
        LocalDateTime endDate = LocalDateTime.now();
        // Lấy top 5 sản phẩm bán chạy
        Pageable pageable = PageRequest.of(0, 5, Sort.by(Sort.Direction.DESC, "quantitySold"));
        List<Product> topProducts = productService.getTopSellingProductsByDateRange(startDate, endDate, pageable);
        return topProducts.stream().map(product -> {
            Map<String, Object> productMap = new HashMap<>();
            productMap.put("id", product.getId());
            productMap.put("title", product.getTitle());
            productMap.put("imageProduct", product.getImageProduct());
            productMap.put("price", product.getPrice());
            productMap.put("quantitySold", product.getQuantitySold());
            BigDecimal revenue = BigDecimal.valueOf(product.getPrice()).multiply(new BigDecimal(product.getQuantitySold()));
            if (product.getDiscount() > 0) {
                BigDecimal discountRate = BigDecimal.valueOf(product.getDiscount()).divide(BigDecimal.valueOf(100));
                BigDecimal discountAmount = revenue.multiply(discountRate);
                revenue = revenue.subtract(discountAmount);
            }
            productMap.put("revenue", revenue);
            return productMap;
        }).collect(Collectors.toList());
    }


    private List<Map<String, Object>> getTopSpendingUsers() {
        // Mặc định lấy theo tất cả
        LocalDateTime startDate = LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.now();
        // Lấy top 5 khách hàng chi tiêu nhiều nhất
        List<Object[]> topUsers = orderService.getTopSpendingUsers(startDate, endDate, 5);
        return topUsers.stream().map(userArray -> {
            Map<String, Object> userMap = new HashMap<>();
            User user = (User) userArray[0];
            BigDecimal totalSpending = (BigDecimal) userArray[1];
            userMap.put("id", user.getId());
            userMap.put("fullName", user.getFullName());
            userMap.put("email", user.getEmail());
            userMap.put("totalSpending", totalSpending);
            return userMap;
        }).collect(Collectors.toList());
    }


}
