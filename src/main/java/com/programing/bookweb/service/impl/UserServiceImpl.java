package com.programing.bookweb.service.impl;

import com.programing.bookweb.entity.Role;
import com.programing.bookweb.entity.User;
import com.programing.bookweb.repository.RoleRepository;
import com.programing.bookweb.repository.UserRepository;
import com.programing.bookweb.service.IUserService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class UserServiceImpl implements IUserService {

    private static final String DEFAULT_ROLE = "USER";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;


    @Override
    public Page<User> getAllUserPage(Pageable pageable) {
        Role role = roleRepository.findByName(DEFAULT_ROLE)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vai trò có tên: " + DEFAULT_ROLE));
        return userRepository.findByRoles(role, pageable);
    }

    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }


    @Override
    public void updateUser(User user) {
        userRepository.save(user);
    }

    @Override
    public void changePassword(User user, String newPassword) {
        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("Mật khẩu phải có độ dài ừ 8 ký tự trở lên!");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)){
            throw new RuntimeException("Người dùng có id: " + userId + " không tồn tại!");
        }
        userRepository.deleteById(userId);
    }

    @Override
    @Transactional
    public boolean registerUser(User user) {
        if(userRepository.existsByEmail(user.getEmail())){
            throw new IllegalArgumentException("Email đã được đăng ký!");
        }

        if(userRepository.existsByPhoneNumber(user.getPhoneNumber())){
            throw new IllegalArgumentException("Số đện thoại đã được đăng ký!");
        }
        Role userRole = null;
        try {
            Optional<Role> roleOptional = roleRepository.findByName(DEFAULT_ROLE);

            if (roleOptional.isPresent()) {
                userRole = roleOptional.get();
            } else {
                userRole = new Role();
                userRole.setName(DEFAULT_ROLE);
                userRole = roleRepository.save(userRole);
            }
        } catch (Exception e) {
            userRole = new Role();
            userRole.setName(DEFAULT_ROLE);
            userRole = roleRepository.save(userRole);
        }
        // Ensure user has proper values set
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(true);
        user.setCreatedAt(LocalDateTime.now());

        user.setAddress("Không có");
        user.setGender("Không");
        user.setAvatar("avatardefault.jpg");
        user.setBirthday(LocalDate.of(2000, 1, 1));
        // Initialize collections
        if (user.getRoles() == null) {
            user.setRoles(new HashSet<>());
        }
        // Add the role
        if (userRole != null) {
            user.addRole(userRole);
        } else {
            return false;
        }
        try {
            User savedUser = userRepository.save(user);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void saveUser(User user) {
        userRepository.save(user);
    }

    @Override
    public Long countUser() {
        Role role = roleRepository.findByName(DEFAULT_ROLE)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vai trò có tên: " + DEFAULT_ROLE));
        return userRepository.countByRoles(role);
    }

    @Override
    public long countUsersBefore(LocalDateTime date) {
        return userRepository.countByCreatedAtBefore(date);
    }

    @Override
    public long countUsersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return userRepository.countByCreatedAtBetween(startDate, endDate);
    }

    @Override
    public List<User> getLatestUsers(Pageable pageable) {
        return userRepository.findByOrderByCreatedAtDesc(pageable);
    }

    @Override
    public Page<User> getUserSearch(String search, Pageable pageable) {
        Role role = roleRepository.findByName(DEFAULT_ROLE)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy vai trò có tên: " + DEFAULT_ROLE));
        if (search != null && !search.trim().isEmpty()) {
            String searchNew = search.trim();
            return userRepository.findByIdOrFullNameContainingOrPhoneNumber(searchNew, role, pageable);
        }
        return getAllUserPage(pageable);
    }

}
