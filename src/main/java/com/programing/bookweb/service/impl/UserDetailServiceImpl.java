package com.programing.bookweb.service.impl;


import com.programing.bookweb.entity.Role;
import com.programing.bookweb.entity.User;
import com.programing.bookweb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserDetailServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
//        User user = userRepository.findByEmail(identifier);
//        if (user == null) {
//            user = userRepository.findByPhoneNumber(identifier);
//        }
//        if (user == null) {
//            throw new UsernameNotFoundException("Không tìm thấy người dùng với thông tin: " + identifier);
//        }
//        return user;

        try {
            if (identifier == null || identifier.trim().isEmpty()) {
                throw new UsernameNotFoundException("Identifier cannot be empty");
            }
            User user = userRepository.findByEmail(identifier);

            if (user == null) {
                user = userRepository.findByPhoneNumber(identifier);
            }

            if (user == null) {
                throw new UsernameNotFoundException("Không tìm thấy người dùng với thông tin: " + identifier);
            }
            user.getRoles().size();

            if (user.getRoles() == null) {
                user.setRoles(new HashSet<>());
            }

            return user;
        } catch (UsernameNotFoundException e) {
            throw e;
        } catch (Exception e) {
            throw new UsernameNotFoundException("Lỗi xác thực: " + e.getMessage());
        }

    }

    private Collection<? extends GrantedAuthority> mapRolesToAuthorities(Collection<Role> roles){
//        Collection<? extends GrantedAuthority> mapRoles = roles.stream()
//                .map(role -> new SimpleGrantedAuthority(role.getName()))
//                .collect(Collectors.toList());
//        return mapRoles;

        if (roles == null) {
            return new HashSet<>();
        }

        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }

}
