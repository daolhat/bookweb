package com.programing.bookweb.service;

import com.programing.bookweb.dto.CartDTO;
import com.programing.bookweb.dto.TopUserDTO;
import com.programing.bookweb.dto.UserOrder;
import com.programing.bookweb.entity.Order;
import com.programing.bookweb.entity.User;
import com.programing.bookweb.enums.OrderStatus;
import com.programing.bookweb.enums.PaymentMethod;
import com.programing.bookweb.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface IOrderService {

    Page<Order> getAllOrdersByUserPage(User user, Pageable pageable);

    Long countOrder();

    Order getOrderById(Long orderId);

    Order createOrder(User user, CartDTO cart, UserOrder userOrder, PaymentMethod paymentMethod);

    void deleteOrder(Order order);

    BigDecimal getTotalRevenue();

    Page<Order> getAllOrders(Pageable pageable);

    void cancelOrder(Order order);

    Long countOrderByUser(User user);

    void setProcessingOrder(Order order);

    void setDeliveringOrder(Order order);

    void setDeliveredOrder(Order order);

    void setPaidOrder(Order order);

    long countOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    BigDecimal getRevenueByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    Page<Order> getOrdersByStatus(OrderStatus status, Pageable pageable);

    Page<Order> getOrdersBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<Order> getOrdersByStatusAndBetween(OrderStatus status, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<Order> getOrderSearch(String search, Pageable pageable);

    List<TopUserDTO> getTopUsers();
}
