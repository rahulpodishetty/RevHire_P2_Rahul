package com.rev.app.rest;

import com.rev.app.dto.*;
import com.rev.app.service.IAuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final IAuthService authService;

    @Value("${app.jwtExpirationMs:86400000}")
    private int jwtExpirationMs;

    public AuthController(IAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(
            @Valid @RequestBody LoginDto loginDto,
            HttpServletResponse response) {
        try {
            JwtResponseDto jwt = authService.login(loginDto);

            // Set HttpOnly cookie so the browser sends it on every page navigation
            Cookie cookie = new Cookie("jwtToken", jwt.getToken());
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(jwtExpirationMs / 1000); // seconds
            response.addCookie(cookie);

            return ResponseEntity.ok(jwt);

        } catch (BadCredentialsException | LockedException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid email or password. Please try again."));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Login failed: " + ex.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwtToken", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return ResponseEntity.ok(Map.of("message", "Logged out successfully"));
    }

    @PostMapping("/register/seeker")
    public ResponseEntity<?> registerSeeker(
            @Valid @RequestBody RegisterJobSeekerDto registerDto) {
        try {
            return ResponseEntity.ok(authService.registerJobSeeker(registerDto));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", ex.getMessage()));
        }
    }

    @PostMapping("/register/employer")
    public ResponseEntity<?> registerEmployer(
            @Valid @RequestBody RegisterEmployerDto registerDto) {
        try {
            return ResponseEntity.ok(authService.registerEmployer(registerDto));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", ex.getMessage()));
        }
    }

    /**
     * Handle @Valid failures inside this REST controller so they return JSON,
     * not the HTML 500 page rendered by GlobalExceptionHandler.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return ResponseEntity.badRequest().body(Map.of("message", errors));
    }
}
