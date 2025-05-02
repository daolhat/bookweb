package com.programing.bookweb.repository;

import com.programing.bookweb.entity.Order;
import com.programing.bookweb.entity.Role;
import com.programing.bookweb.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Page<User> findByRoles(Role roles, Pageable pageable);

    List<User> findByRoles(Role roles);

    Page<User> findAllByOrderByCreatedAtAsc(Pageable pageable);

    List<User> findByOrderByCreatedAtDesc(Pageable pageable);

    User findByEmail(String email);

    User findByPhoneNumber(String phoneNumber);

    boolean existsByEmail(String email);

    boolean existsByPhoneNumber(String phoneNumber);

    long countByCreatedAtBefore(LocalDateTime date);

    long countByCreatedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

//    @Query("SELECT o FROM Order o WHERE o.fullName LIKE %:search% OR o.phoneNumber = :search AND o.role = :role")
//    Page<User> findByIdOrFullNameContainingOrPhoneNumber(@Param("search") String search, @Param("role") Role role, Pageable pageable);
}
