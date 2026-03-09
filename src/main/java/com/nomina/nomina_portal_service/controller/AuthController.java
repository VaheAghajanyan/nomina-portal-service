package com.nomina.nomina_portal_service.controller;

import com.nomina.nomina_portal_service.dto.AuthResponse;
import com.nomina.nomina_portal_service.dto.LoginRequest;
import com.nomina.nomina_portal_service.dto.RefreshRequest;
import com.nomina.nomina_portal_service.dto.RegisterRequest;
import com.nomina.nomina_portal_service.dto.UserResponse;
import com.nomina.nomina_portal_service.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
	private final AuthService authService;

	public AuthController(AuthService authService) {
		this.authService = authService;
	}

	@PostMapping("/register")
	public UserResponse register(@Valid @RequestBody RegisterRequest request) {
		return authService.register(request);
	}

	@PostMapping("/login")
	public AuthResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}

	@PostMapping("/refresh")
	public AuthResponse refresh(@Valid @RequestBody RefreshRequest request) {
		return authService.refresh(request);
	}
}
