package com.programing.bookweb.service;

import com.programing.bookweb.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

public interface IUserService  {
    Page<User> getAllUserPage(Pageable pageable);

    User getUserById(Long userId);

    void updateUser(User user);

    void changePassword(User user,String newPassword);

    void deleteUser(Long userId);

    boolean registerUser(User user);

    void saveUser(User user);

    Long countUser();

    long countUsersBefore(LocalDateTime date);

    long countUsersByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    List<User> getLatestUsers(Pageable pageable);

    Page<User> getUserSearch(String search, Pageable pageable);

}
