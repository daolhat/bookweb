package com.programing.bookweb.repository;

import com.programing.bookweb.dto.TopUserDTO;
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

    Page<Order> findByUserOrderByCreatedAtDesc(User user, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate")
    Page<Order> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

    @Query("SELECT SUM(o.totalPrice) FROM Order o where  o.status = 'DELIVERED'")
    BigDecimal sumTotalPrice();

    Long countByUser(User user);

    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT SUM(o.totalPrice) FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate AND o.status = 'DELIVERED'")
    BigDecimal sumTotalPriceByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT o.user as user, SUM(o.totalPrice) as totalSpending FROM Order o WHERE o.createdAt BETWEEN :startDate AND :endDate AND o.status = 'DELIVERED' GROUP BY o.user ORDER BY totalSpending DESC")
    List<Object[]> findTopSpendingUsers(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, Pageable pageable);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.createdAt BETWEEN :startDate AND :endDate")
    Page<Order> findByStatusAndCreatedAtBetween(
            @Param("status") OrderStatus status,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            Pageable pageable);

    @Query("SELECT o FROM Order o WHERE o.code = :search OR o.receiver LIKE %:search% OR o.customerPhone = :search")
    Page<Order> findByCodeOrReceiverContainingOrCustomerPhone(@Param("search") String search, Pageable pageable);

    @Query("""
        SELECT new com.programing.bookweb.dto.TopUserDTO(u.id, u.fullName, u.email, u.phoneNumber, SUM(o.totalPrice)) 
        FROM User u 
        JOIN u.orders o 
        WHERE o.status = 'DELIVERED' 
        GROUP BY u.id 
        ORDER BY SUM(o.totalPrice) DESC
        """)
    List<TopUserDTO> findTopUsers(Pageable pageable);


}
