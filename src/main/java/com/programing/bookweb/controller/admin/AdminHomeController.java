package com.programing.bookweb.controller.admin;

import com.programing.bookweb.controller.BaseController;
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
import java.time.YearMonth;
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
            // Thông tin đơn hàng trong ngày
            Map<String, Object> dailyOrderStats = getDailyOrderStats();
            model.addAttribute("dailyOrderStats", dailyOrderStats);

            // Thông tin doanh thu tháng hiện tại
            Map<String, Object> revenueStats = getRevenueStats();
            model.addAttribute("revenueStats", revenueStats);

            // Thông tin người dùng
            Map<String, Object> userStats = getUserStats();
            model.addAttribute("userStats", userStats);

            // Dữ liệu biểu đồ 7 ngày gần nhất
            Map<String, Object> weeklyChartData = getWeeklyChartData();
            model.addAttribute("weeklyChartLabels", weeklyChartData.get("chartLabels"));
            model.addAttribute("weeklyChartSalesData", weeklyChartData.get("chartSalesData"));
            model.addAttribute("weeklyChartRevenueData", weeklyChartData.get("chartRevenueData"));

            // Dữ liệu biểu đồ tháng gần nhất
            Map<String, Object> monthlyChartData = getMonthlyChartData();
            model.addAttribute("monthlyChartLabels", monthlyChartData.get("chartLabels"));
            model.addAttribute("monthlyChartRevenueData", monthlyChartData.get("chartRevenueData"));
            model.addAttribute("monthlyChartUserData", monthlyChartData.get("chartUserData"));

            // 10 Sản phẩm bán chạy nhất
            List<Map<String, Object>> topSellingProducts = getTopSellingProducts(10);
            model.addAttribute("topSellingProducts", topSellingProducts != null ? topSellingProducts : new ArrayList<>());

            // 5 Khách hàng tiêu nhiều tiền nhất
            List<Map<String, Object>> topSpendingUsers = getTopSpendingUsers(5);
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


    private Map<String, Object> getDailyOrderStats() {
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
        stats.put("yesterdayOrderCount", yesterdayOrderCount);

        // Doanh thu hôm nay
        BigDecimal todayRevenue = orderService.getRevenueByDateRange(startOfDay, endOfDay);
        if (todayRevenue == null) {
            todayRevenue = BigDecimal.ZERO;
        }
        stats.put("todayRevenue", todayRevenue);

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

        // Doanh thu tháng này (từ đầu tháng đến thời điểm hiện tại)
        LocalDateTime startOfMonth = now.toLocalDate().withDayOfMonth(1).atStartOfDay();
        BigDecimal currentMonthRevenue = orderService.getRevenueByDateRange(startOfMonth, now);
        if (currentMonthRevenue == null) {
            currentMonthRevenue = BigDecimal.ZERO;
        }
        stats.put("currentMonthRevenue", currentMonthRevenue);

        // Doanh thu của một tháng trước đến hiện tại
        LocalDateTime oneMonthAgo = now.minusMonths(1);
        BigDecimal lastMonthToNowRevenue = orderService.getRevenueByDateRange(oneMonthAgo, now);
        if (lastMonthToNowRevenue == null) {
            lastMonthToNowRevenue = BigDecimal.ZERO;
        }
        stats.put("lastMonthToNowRevenue", lastMonthToNowRevenue);

        // Doanh thu tháng trước (toàn bộ tháng trước)
        LocalDateTime startOfLastMonth = now.minusMonths(1).toLocalDate().withDayOfMonth(1).atStartOfDay();
        LocalDateTime endOfLastMonth = startOfLastMonth.toLocalDate()
                .withDayOfMonth(startOfLastMonth.toLocalDate().lengthOfMonth())
                .atTime(23, 59, 59);
        BigDecimal lastMonthRevenue = orderService.getRevenueByDateRange(startOfLastMonth, endOfLastMonth);
        if (lastMonthRevenue == null) {
            lastMonthRevenue = BigDecimal.ZERO;
        }
        stats.put("lastMonthRevenue", lastMonthRevenue);

        // Tính tỷ lệ tăng trưởng doanh thu so với tháng trước
        double revenueGrowthRate = 0.0;
        if (lastMonthRevenue.compareTo(BigDecimal.ZERO) > 0) {
            revenueGrowthRate = (currentMonthRevenue.subtract(lastMonthRevenue).doubleValue() / lastMonthRevenue.doubleValue()) * 100;
        }
        stats.put("revenueGrowthRate", revenueGrowthRate);
        return stats;
    }


    private Map<String, Object> getUserStats() {
        Map<String, Object> stats = new HashMap<>();
        // Tổng số người dùng
        long totalUsers = userService.countUser();
        stats.put("totalUsers", totalUsers);

        // Số người dùng mới trong tháng này
        LocalDateTime now = LocalDateTime.now();
        //LocalDateTime startOfMonth = now.toLocalDate().withDayOfMonth(1).atStartOfDay();
        LocalDateTime oneWeekAgo = now.minusWeeks(1).toLocalDate().atStartOfDay();
        long newUsersThisMonth = userService.countUsersByDateRange(oneWeekAgo, now);
        stats.put("newUsersThisMonth", newUsersThisMonth);

        // Số người dùng tháng trước
        LocalDateTime oneMonthAgo = now.minusMonths(1);
        long lastMonthUsers = userService.countUsersBefore(oneMonthAgo);

        // Tỷ lệ tăng trưởng người dùng
        double userGrowthRate = 0.0;
        if (lastMonthUsers > 0) {
            userGrowthRate = ((double) newUsersThisMonth / lastMonthUsers) * 100;
        }
        stats.put("userGrowthRate", userGrowthRate);
        return stats;
    }


    private Map<String, Object> getWeeklyChartData() {
        Map<String, Object> chartData = new HashMap<>();
        LocalDate now = LocalDate.now();
        List<String> chartLabels = new ArrayList<>();
        List<Long> chartSalesData = new ArrayList<>();
        List<Double> chartRevenueData = new ArrayList<>();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM");

        // 7 ngày gần nhất
        for (int i = 6; i >= 0; i--) {
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
            double revenueInMillions = revenue != null ? revenue.doubleValue() / 1000000 : 0;
            chartRevenueData.add(revenueInMillions);
        }

        chartData.put("chartLabels", chartLabels);
        chartData.put("chartSalesData", chartSalesData);
        chartData.put("chartRevenueData", chartRevenueData);
        return chartData;
    }


    private Map<String, Object> getMonthlyChartData() {
        Map<String, Object> chartData = new HashMap<>();
        List<String> chartLabels = new ArrayList<>();
        List<Double> chartRevenueData = new ArrayList<>();
        List<Long> chartUserData = new ArrayList<>();

        LocalDate now = LocalDate.now();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("MM/yyyy");

        // 6 tháng gần nhất
        for (int i = 5; i >= 0; i--) {
            YearMonth yearMonth = YearMonth.from(now.minusMonths(i));
            LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
            LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);

            // Định dạng tháng cho biểu đồ
            chartLabels.add(yearMonth.format(monthFormatter));

            // Doanh thu trong tháng (đơn vị: triệu đồng)
            BigDecimal revenue = orderService.getRevenueByDateRange(startOfMonth, endOfMonth);
            double revenueInMillions = revenue != null ? revenue.doubleValue() / 1000000 : 0;
            chartRevenueData.add(revenueInMillions);

            // Số người dùng mới trong tháng
            long newUsers = userService.countUsersByDateRange(startOfMonth, endOfMonth);
            chartUserData.add(newUsers);
        }

        chartData.put("chartLabels", chartLabels);
        chartData.put("chartRevenueData", chartRevenueData);
        chartData.put("chartUserData", chartUserData);
        return chartData;
    }


    private List<Map<String, Object>> getTopSellingProducts(int limit) {
        // Mặc định lấy theo 3 tháng gần nhất
        LocalDateTime startDate = LocalDateTime.now().minusMonths(3);
        LocalDateTime endDate = LocalDateTime.now();

        // Lấy top N sản phẩm bán chạy
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "quantitySold"));
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


    private List<Map<String, Object>> getTopSpendingUsers(int limit) {
        // Mặc định lấy theo 6 tháng gần nhất
        LocalDateTime startDate = LocalDateTime.now().minusMonths(6);
        LocalDateTime endDate = LocalDateTime.now();

        // Lấy top N khách hàng chi tiêu nhiều nhất
        List<Object[]> topUsers = orderService.getTopSpendingUsers(startDate, endDate, limit);

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
