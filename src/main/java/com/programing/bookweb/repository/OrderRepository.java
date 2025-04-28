package com.programing.bookweb.repository;

import com.programing.bookweb.entity.Order;
import com.programing.bookweb.entity.User;
import com.programing.bookweb.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserOrderByCreatedAtDesc(User user);

    Page<Order> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    Page<Order> findByStatus(String status, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    List<Order> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT SUM(o.totalPrice) FROM Order o where  o.status = 'DELIVERED'")
    BigDecimal sumTotalPrice();

    Long countByUser(User user);

    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate AND o.status = 'DELIVERED'")
    BigDecimal sumTotalPriceByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT o.user as user, SUM(o.totalPrice) as totalSpending FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate AND o.status = 'DELIVERED' GROUP BY o.user ORDER BY totalSpending DESC")
    List<Object[]> findTopSpendingUsers(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

    List<Order> findByStatus(OrderStatus status, Pageable pageable);

}
