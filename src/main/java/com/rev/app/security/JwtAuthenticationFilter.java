package com.rev.app.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomUserDetailsService customUserDetailsService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    /** Public endpoints that should skip JWT filtering entirely */
    private static final List<String> PUBLIC_PATHS = Arrays.asList(
            "/", "/index", "/login", "/register/**",
            "/css/**", "/js/**", "/images/**",
            "/error/**", "/error", "/favicon.ico",
            "/api/auth/**", "/api/auth/logout");

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider,
            CustomUserDetailsService customUserDetailsService) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();
        return PUBLIC_PATHS.stream().anyMatch(pattern -> pathMatcher.match(pattern, uri));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();

        try {
            String jwt = parseJwt(request);

            if (jwt != null && jwtTokenProvider.validateJwtToken(jwt)) {
                String username = jwtTokenProvider.getUserNameFromJwtToken(jwt);
                UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                logger.debug("Authenticated user: " + username
                        + " | authorities: " + userDetails.getAuthorities()
                        + " | URI: " + uri);
            } else {
                logger.debug("No valid JWT for URI: " + uri);
            }
        } catch (UsernameNotFoundException ex) {
            // User was deleted or email changed after the token was issued
            logger.warn("JWT references unknown user – clearing context. URI: " + uri
                    + " | " + ex.getMessage());
            SecurityContextHolder.clearContext();
        } catch (Exception e) {
            // Any other unexpected error – never block the filter chain
            logger.error("Cannot set user authentication for URI: " + uri, e);
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        // 1. Authorization header takes priority
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            logger.debug("Found JWT in Authorization header");
            return headerAuth.substring(7);
        }

        // 2. Fall back to cookie (for Thymeleaf page navigation)
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwtToken".equals(cookie.getName())
                        && StringUtils.hasText(cookie.getValue())) {
                    logger.debug("Found JWT in cookie");
                    return cookie.getValue();
                }
            }
        }

        return null;
    }
}
