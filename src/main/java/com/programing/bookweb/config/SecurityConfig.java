//package com.programing.bookweb.config;
//
//import com.programing.bookweb.entity.User;
//import com.programing.bookweb.repository.UserRepository;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
//import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.io.IOException;
//import java.time.LocalDateTime;
//import java.util.Optional;
//
//@Configuration
//@EnableWebSecurity
//public class SecurityConfig {
//
//    @Autowired
//    private UserDetailsService userDetailsService;
//
//    @Autowired
//    private UserRepository userRepository;
//
//    @Bean
//    public static PasswordEncoder passwordEncoder(){
//        return new BCryptPasswordEncoder();
//    }
//
//    @Bean
//    public AuthenticationSuccessHandler authenticationSuccessHandler() {
//        return new AuthenticationSuccessHandler() {
//            @Override
//            @Transactional
//            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
//                                                Authentication authentication) throws IOException, ServletException {
//                try {
//                    if (authentication != null && authentication.getPrincipal() instanceof User) {
//                        User user = (User) authentication.getPrincipal();
//
//                        if (user.getId() != null) {
//                            Optional<User> dbUser = userRepository.findById(user.getId());
//                            if (dbUser.isPresent()) {
//                                User updatedUser = dbUser.get();
//                                updatedUser.setLastLogin(LocalDateTime.now());
//                                userRepository.save(updatedUser);
//                            }
//                        }
//                    }
//                } catch (Exception e) {
//                    throw new RuntimeException(e.getMessage());
//                }
//
//                // Kiểm tra nếu có tham số chuyển hướng checkout
//                boolean hasCheckoutRedirect = request.getSession().getAttribute("checkoutRedirect") != null;
//                if (hasCheckoutRedirect) {
//                    request.getSession().removeAttribute("checkoutRedirect");
//                    response.sendRedirect("/cart/checkout");
//                } else {
//                    response.sendRedirect("/");
//                }
//            }
//        };
//    }
//
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
//        http.csrf(csrf -> csrf.disable())
//                .authorizeHttpRequests(authorize ->
//                        authorize
//                                .requestMatchers("/dashboard/**").hasRole("ADMIN")
//                                .requestMatchers("/cart/**","/profile/**","/order/**","/contact/**")
//                                .authenticated()
//                                .anyRequest().permitAll()
//                ).formLogin(
//                        form -> form
//                                .loginPage("/login")
//                                .loginProcessingUrl("/login")
//                                .successHandler(authenticationSuccessHandler())
//                                .failureUrl("/login?error=true")
//                                .permitAll()
//                ).logout(
//                        logout -> logout
//                                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
//                                .permitAll()
//                                .logoutSuccessUrl("/")
//                                .invalidateHttpSession(false)
//                                .clearAuthentication(true)
//                )
////                .sessionManagement(session -> session
////                        .sessionFixation().newSession()
////                        .maximumSessions(1)
////                        .maxSessionsPreventsLogin(false)
////                )
//                .exceptionHandling(
//                        exceptionHandling -> exceptionHandling
//                                .accessDeniedHandler((request, response, accessDeniedException) -> {
//                                    if (request.isUserInRole("USER")) {
//                                        response.sendRedirect("/404");
//                                    } else {
//                                        response.sendRedirect("/404");
//                                    }
//                                })
//                )
//        ;
//        return http.build();
//    }
//
//    @Autowired
//    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
//        auth
//                .userDetailsService(userDetailsService)
//                .passwordEncoder(passwordEncoder());
//    }
//
//}
package com.programing.bookweb.config;

import com.programing.bookweb.entity.User;
import com.programing.bookweb.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.session.web.http.HeaderHttpSessionIdResolver;
import org.springframework.session.web.http.HttpSessionIdResolver;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsService userDetailsService;

    @Autowired
    private UserRepository userRepository;

    // Map with userID as key and a map of session attributes as value
    private final ConcurrentHashMap<Long, ConcurrentHashMap<String, Object>> userSessionStore = new ConcurrentHashMap<>();

    @Bean
    public static PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    @Bean
    public HttpSessionIdResolver httpSessionIdResolver() {
        return HeaderHttpSessionIdResolver.xAuthToken();
    }

    /**
     * Saves all session attributes to the user's session store
     */
    private void saveSessionAttributes(Long userId, HttpSession session) {
        if (userId == null || session == null) return;

        ConcurrentHashMap<String, Object> sessionData = new ConcurrentHashMap<>();
        Enumeration<String> attributeNames = session.getAttributeNames();

        while (attributeNames.hasMoreElements()) {
            String name = attributeNames.nextElement();
            // Skip transient attributes that shouldn't be persisted
            if (!name.startsWith("org.springframework") && !name.startsWith("SPRING_SECURITY")) {
                sessionData.put(name, session.getAttribute(name));
            }
        }

        userSessionStore.put(userId, sessionData);
    }

    /**
     * Restores all session attributes from the user's session store
     */
    private void restoreSessionAttributes(Long userId, HttpSession session) {
        if (userId == null || session == null) return;

        ConcurrentHashMap<String, Object> sessionData = userSessionStore.get(userId);
        if (sessionData != null) {
            sessionData.forEach(session::setAttribute);
        }
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            @Transactional
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                                Authentication authentication) throws IOException, ServletException {
                try {
                    if (authentication != null && authentication.getPrincipal() instanceof User) {
                        User user = (User) authentication.getPrincipal();
                        Long userId = user.getId();

                        if (userId != null) {
                            // Update last login time
                            Optional<User> dbUser = userRepository.findById(userId);
                            if (dbUser.isPresent()) {
                                User updatedUser = dbUser.get();
                                updatedUser.setLastLogin(LocalDateTime.now());
                                userRepository.save(updatedUser);
                            }

                            // Get current HTTP session
                            HttpSession session = request.getSession(true);

                            // Set basic user attributes in session
                            session.setAttribute("userId", userId);
                            session.setAttribute("username", user.getUsername());
                            session.setAttribute("userRole", user.getAuthorities().toString());

                            // Restore any previously saved session data for this user
                            restoreSessionAttributes(userId, session);
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Authentication success handling failed: " + e.getMessage(), e);
                }

                // Check if we need to redirect to checkout
                HttpSession session = request.getSession(false);
                if (session != null && session.getAttribute("checkoutRedirect") != null) {
                    session.removeAttribute("checkoutRedirect");
                    response.sendRedirect("/cart/checkout");
                } else {
                    response.sendRedirect("/");
                }
            }
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize ->
                        authorize
                                .requestMatchers("/dashboard/**").hasRole("ADMIN")
                                .requestMatchers("/cart/**","/profile/**","/order/**","/contact/**")
                                .authenticated()
                                .anyRequest().permitAll()
                ).formLogin(
                        form -> form
                                .loginPage("/login")
                                .loginProcessingUrl("/login")
                                .successHandler(authenticationSuccessHandler())
                                .failureUrl("/login?error=true")
                                .permitAll()
                ).logout(
                        logout -> logout
                                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                                .permitAll()
                                .logoutSuccessUrl("/")
                                .invalidateHttpSession(true) // Changed to true to ensure clean session on logout
                                .clearAuthentication(true)
                                .addLogoutHandler((request, response, authentication) -> {
                                    if (authentication != null && authentication.getPrincipal() instanceof User) {
                                        User user = (User) authentication.getPrincipal();
                                        Long userId = user.getId();

                                        if (userId != null) {
                                            // Save the current session state before invalidating it
                                            HttpSession session = request.getSession(false);
                                            if (session != null) {
                                                saveSessionAttributes(userId, session);
                                            }
                                        }
                                    }
                                })
                )
                .sessionManagement(session -> session
                        .sessionFixation().newSession()  // Create a new session on login to prevent fixation attacks
                        .maximumSessions(1)              // Limit to one session per user
                        .expiredUrl("/login?expired=true") // Redirect when previous session is expired
                )
                .exceptionHandling(
                        exceptionHandling -> exceptionHandling
                                .accessDeniedHandler((request, response, accessDeniedException) -> {
                                    response.sendRedirect("/404");
                                })
                )
        ;
        return http.build();
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
    }
}