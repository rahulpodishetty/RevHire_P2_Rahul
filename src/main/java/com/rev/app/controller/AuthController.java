package com.rev.app.controller;

import com.rev.app.dto.*;
import com.rev.app.service.IAuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final IAuthService IAuthService;

    public AuthController(IAuthService IAuthService) {
        this.IAuthService = IAuthService;
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponseDto> authenticateUser(@Valid @RequestBody LoginDto loginDto) {
        return ResponseEntity.ok(IAuthService.login(loginDto));
    }

    @PostMapping("/register/seeker")
    public ResponseEntity<UserDto> registerSeeker(@Valid @RequestBody RegisterJobSeekerDto registerDto) {
        return ResponseEntity.ok(IAuthService.registerJobSeeker(registerDto));
    }

    @PostMapping("/register/employer")
    public ResponseEntity<UserDto> registerEmployer(@Valid @RequestBody RegisterEmployerDto registerDto) {
        return ResponseEntity.ok(IAuthService.registerEmployer(registerDto));
    }
}
