package com.nomina.nomina_portal_service.service;

import com.nomina.nomina_portal_service.dto.AuthResponse;
import com.nomina.nomina_portal_service.dto.LoginRequest;
import com.nomina.nomina_portal_service.dto.RegisterRequest;
import com.nomina.nomina_portal_service.dto.UserResponse;
import com.nomina.nomina_portal_service.exception.ConflictException;
import com.nomina.nomina_portal_service.exception.ForbiddenException;
import com.nomina.nomina_portal_service.exception.UnauthorizedException;
import com.nomina.nomina_portal_service.model.User;
import com.nomina.nomina_portal_service.repository.UserRepositoryJdbc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
	private final UserRepositoryJdbc userRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtService jwtService;

	public AuthService(UserRepositoryJdbc userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
		this.jwtService = jwtService;
	}

	public UserResponse register(RegisterRequest request) {
		if (userRepository.existsByUsername(request.getUsername())) {
			throw new ConflictException("Username already exists.");
		}

		User user = new User();
		user.setUsername(request.getUsername());
		user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
		user.setSuperUser(Boolean.TRUE.equals(request.getIsSuperUser()));
		user.setAdminUser(Boolean.TRUE.equals(request.getIsAdminUser()));
		user.setActiveUser(request.getIsActiveUser() == null || request.getIsActiveUser());

		User created = userRepository.insert(user);
		return toUserResponse(created);
	}

	public AuthResponse login(LoginRequest request) {
		User user = userRepository.findByUsername(request.getUsername())
			.orElseThrow(() -> new UnauthorizedException("Invalid credentials."));

		if (!user.isActiveUser()) {
			throw new ForbiddenException("User is inactive.");
		}

		if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
			throw new UnauthorizedException("Invalid credentials.");
		}

		String token = jwtService.generateToken(user);
		AuthResponse response = new AuthResponse();
		response.setAccessToken(token);
		response.setTokenType("Bearer");
		response.setExpiresInSeconds(jwtService.getExpirationSeconds());
		response.setUser(toUserResponse(user));
		return response;
	}

	private UserResponse toUserResponse(User user) {
		UserResponse response = new UserResponse();
		response.setId(user.getId());
		response.setUsername(user.getUsername());
		response.setSuperUser(user.isSuperUser());
		response.setAdminUser(user.isAdminUser());
		response.setActiveUser(user.isActiveUser());
		return response;
	}
}
