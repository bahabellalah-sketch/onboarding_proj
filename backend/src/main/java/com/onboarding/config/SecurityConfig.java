package com.onboarding.config;

import com.onboarding.service.JwtService;
import com.onboarding.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    
    @Autowired
    @Lazy
    private UserService userService;
    
    @Autowired
    private JwtService jwtService;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // Ant path matchers (not MVC): permitAll must apply to DELETE/PUT even when
            // Spring MVC has no handler yet; MvcRequestMatcher would fall through and return 403.
            .authorizeHttpRequests(auth -> auth
                // ====== PUBLIC endpoints (no auth required) ======
                .requestMatchers(
                        new AntPathRequestMatcher("/api/auth/login", "POST"),
                        new AntPathRequestMatcher("/api/auth/forgot-password", "POST"),
                        new AntPathRequestMatcher("/api/auth/reset-password", "POST"),
                        new AntPathRequestMatcher("/api/email-verification/verify", "GET"),
                        new AntPathRequestMatcher("/uploads/**")
                ).permitAll()
                // ====== Evaluation endpoint accessible for direct links ======
                .requestMatchers("/api/evaluations/**").permitAll()
                // ====== ADMIN-only endpoints ======
                .requestMatchers(new AntPathRequestMatcher("/api/users/**", "DELETE")).hasRole("ADMINISTRATEUR")
                .requestMatchers(new AntPathRequestMatcher("/api/reports/resolve/**")).hasRole("ADMINISTRATEUR")
                .requestMatchers(new AntPathRequestMatcher("/api/reports/pending/**")).hasRole("ADMINISTRATEUR")
                .requestMatchers(new AntPathRequestMatcher("/api/analytics/**")).hasAnyRole("ADMINISTRATEUR", "MANAGER")
                // ====== Authenticated endpoints (any logged-in user) ======
                .requestMatchers(
                        new AntPathRequestMatcher("/api/auth/logout", "POST"),
                        new AntPathRequestMatcher("/api/users/me", "GET"),
                        new AntPathRequestMatcher("/api/users/search", "GET"),
                        new AntPathRequestMatcher("/api/profiles/**"),
                        new AntPathRequestMatcher("/api/profile/**"),
                        new AntPathRequestMatcher("/api/parcours/**"),
                        new AntPathRequestMatcher("/api/etapes/**"),
                        new AntPathRequestMatcher("/api/assignments/**"),
                        new AntPathRequestMatcher("/api/notifications/**"),
                        new AntPathRequestMatcher("/api/documents/**"),
                        new AntPathRequestMatcher("/api/team-chat/**"),
                        new AntPathRequestMatcher("/api/reports/**")
                ).authenticated()
                // ====== Users management requires auth (further checked in controller) ======
                .requestMatchers("/api/users/**").authenticated()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public SecurityFilterChain evaluationFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/api/evaluations/**")
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.disable())
            .securityContext(securityContext -> securityContext.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            );
        
        return http.build();
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
    
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtService, userService);
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList(
                "http://localhost:*",
                "http://127.0.0.1:*",
                "http://192.168.*:*",
                "http://10.*:*"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
