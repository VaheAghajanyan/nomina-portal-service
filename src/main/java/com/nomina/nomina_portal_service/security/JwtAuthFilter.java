package com.nomina.nomina_portal_service.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomina.nomina_portal_service.exception.ApiError;
import com.nomina.nomina_portal_service.service.JwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class JwtAuthFilter extends OncePerRequestFilter {
	private final JwtService jwtService;
	private final ObjectMapper objectMapper;
	private static final String LOGIN_PATH = "/auth/login";

	public JwtAuthFilter(JwtService jwtService, ObjectMapper objectMapper) {
		this.jwtService = jwtService;
		this.objectMapper = objectMapper;
	}

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {
		String header = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (header == null || !header.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		String token = header.substring(7);
		try {
			Claims claims = jwtService.parseToken(token);
			Boolean isActiveUser = claims.get("is_active_user", Boolean.class);
			if (Boolean.FALSE.equals(isActiveUser)) {
				writeError(response, HttpStatus.FORBIDDEN, "User is inactive.");
				return;
			}

			List<SimpleGrantedAuthority> authorities = new ArrayList<>();
			authorities.add(new SimpleGrantedAuthority("USER"));
			Boolean isAdminUser = claims.get("is_admin_user", Boolean.class);
			Boolean isSuperUser = claims.get("is_super_user", Boolean.class);
			if (Boolean.TRUE.equals(isAdminUser)) {
				authorities.add(new SimpleGrantedAuthority("ADMIN"));
			}
			if (Boolean.TRUE.equals(isSuperUser)) {
				authorities.add(new SimpleGrantedAuthority("SUPER"));
			}

			String username = claims.get("username", String.class);
			UsernamePasswordAuthenticationToken authentication =
				new UsernamePasswordAuthenticationToken(username, null, authorities);
			SecurityContextHolder.getContext().setAuthentication(authentication);
		} catch (JwtException ex) {
			writeError(response, HttpStatus.UNAUTHORIZED, "Invalid or expired token.");
			return;
		}

		filterChain.doFilter(request, response);
	}

	@Override
	protected boolean shouldNotFilter(HttpServletRequest request) {
		String path = request.getRequestURI();
		return LOGIN_PATH.equals(path) || (LOGIN_PATH + "/").equals(path);
	}

	private void writeError(HttpServletResponse response, HttpStatus status, String message) throws IOException {
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		objectMapper.writeValue(response.getOutputStream(), new ApiError(status.getReasonPhrase(), message));
	}
}
