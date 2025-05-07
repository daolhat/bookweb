package com.programing.bookweb.repository;

import com.programing.bookweb.entity.Role;
import com.programing.bookweb.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
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

    @Query("SELECT u FROM User u WHERE (CAST(u.id AS string) = :search OR u.fullName LIKE %:search% OR u.phoneNumber = :search OR u.email = :search) AND :role MEMBER OF u.roles")
    Page<User> findByIdOrFullNameContainingOrPhoneNumber(@Param("search") String search, @Param("role") Role role, Pageable pageable);
}
