package com.rev.app.rest;

import com.rev.app.dto.*;
import com.rev.app.service.IAuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final IAuthService authService;

    public AuthController(IAuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponseDto> authenticateUser(@Valid @RequestBody LoginDto loginDto,
            HttpServletResponse response) {
        JwtResponseDto jwtResponse = authService.login(loginDto);

        // Set JWT as HttpOnly cookie so browser sends it automatically on every request
        Cookie cookie = new Cookie("token", jwtResponse.getToken());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(86400); // 24 hours
        // SameSite=Lax is set via header since Cookie API doesn't support it directly
        response.addCookie(cookie);
        response.setHeader("Set-Cookie",
                "token=" + jwtResponse.getToken()
                        + "; Path=/; Max-Age=86400; HttpOnly; SameSite=Lax");

        return ResponseEntity.ok(jwtResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        // Clear the JWT cookie
        Cookie cookie = new Cookie("token", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // Expire immediately
        response.addCookie(cookie);
        response.setHeader("Set-Cookie",
                "token=; Path=/; Max-Age=0; HttpOnly; SameSite=Lax");

        return ResponseEntity.ok("{\"message\":\"Logged out successfully\"}");
    }

    @PostMapping("/register/seeker")
    public ResponseEntity<UserDto> registerSeeker(@Valid @RequestBody RegisterJobSeekerDto registerDto) {
        return ResponseEntity.ok(authService.registerJobSeeker(registerDto));
    }

    @PostMapping("/register/employer")
    public ResponseEntity<UserDto> registerEmployer(@Valid @RequestBody RegisterEmployerDto registerDto) {
        return ResponseEntity.ok(authService.registerEmployer(registerDto));
    }
}
