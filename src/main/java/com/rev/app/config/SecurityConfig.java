package com.rev.app.config;

import com.rev.app.security.CustomUserDetailsService;
import com.rev.app.security.JwtAuthenticationEntryPoint;
import com.rev.app.security.JwtAuthenticationFilter;
import jakarta.servlet.http.Cookie;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    public SecurityConfig(CustomUserDetailsService userDetailsService,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
        this.userDetailsService = userDetailsService;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Explicitly build AuthenticationManager with our DaoAuthenticationProvider
     * (which has BCryptPasswordEncoder). Using
     * AuthenticationConfiguration.getAuthenticationManager()
     * can resolve to a provider built by Spring autoconfiguration that may NOT use
     * our explicit PasswordEncoder bean, causing BadCredentialsException on login.
     */
    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(List.of(authenticationProvider()));
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            Cookie cookie = new Cookie("jwtToken", "");
                            cookie.setHttpOnly(true);
                            cookie.setPath("/");
                            cookie.setMaxAge(0);
                            response.addCookie(cookie);
                            response.sendRedirect("/login?error=access_denied");
                        }))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/index", "/login", "/register/**",
                                "/css/**", "/js/**", "/images/**",
                                "/error/**", "/error", "/favicon.ico",
                                "/api/auth/**", "/uploads/**")
                        .permitAll()
                        .requestMatchers("/seeker/**").hasRole("SEEKER")
                        .requestMatchers("/employer/**").hasRole("EMPLOYER")
                        .requestMatchers("/api/seeker/**").hasRole("SEEKER")
                        .requestMatchers("/api/employer/**").hasRole("EMPLOYER")
                        .anyRequest().authenticated());

        http.authenticationProvider(authenticationProvider());
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
