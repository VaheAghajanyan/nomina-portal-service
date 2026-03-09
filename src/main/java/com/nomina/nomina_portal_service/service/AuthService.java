package com.nomina.nomina_portal_service.service;

import com.nomina.nomina_portal_service.dto.AuthResponse;
import com.nomina.nomina_portal_service.dto.LoginRequest;
import com.nomina.nomina_portal_service.dto.RefreshRequest;
import com.nomina.nomina_portal_service.dto.RegisterRequest;
import com.nomina.nomina_portal_service.dto.UserResponse;
import com.nomina.nomina_portal_service.exception.ConflictException;
import com.nomina.nomina_portal_service.exception.ForbiddenException;
import com.nomina.nomina_portal_service.exception.UnauthorizedException;
import com.nomina.nomina_portal_service.model.User;
import com.nomina.nomina_portal_service.repository.UserRepositoryJdbc;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
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

		return buildAuthResponse(user);
	}

	public AuthResponse refresh(RefreshRequest request) {
		Claims claims;
		try {
			claims = jwtService.parseToken(request.getAccessToken());
		} catch (JwtException ex) {
			throw new UnauthorizedException("Invalid or expired token.");
		}

		Boolean isActiveUser = claims.get("is_active_user", Boolean.class);
		if (Boolean.FALSE.equals(isActiveUser)) {
			throw new ForbiddenException("User is inactive.");
		}

		String subject = claims.getSubject();
		long userId;
		try {
			userId = Long.parseLong(subject);
		} catch (NumberFormatException ex) {
			throw new UnauthorizedException("Invalid token subject.");
		}

		User user = new User();
		user.setId(userId);
		user.setUsername(claims.get("username", String.class));
		user.setSuperUser(Boolean.TRUE.equals(claims.get("is_super_user", Boolean.class)));
		user.setAdminUser(Boolean.TRUE.equals(claims.get("is_admin_user", Boolean.class)));
		user.setActiveUser(!Boolean.FALSE.equals(isActiveUser));
		return buildAuthResponse(user);
	}

	private AuthResponse buildAuthResponse(User user) {
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
