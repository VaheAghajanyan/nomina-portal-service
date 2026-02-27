package com.nomina.nomina_portal_service.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomina.nomina_portal_service.exception.ApiError;
import com.nomina.nomina_portal_service.service.JwtService;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
	private final ObjectMapper objectMapper;

	public SecurityConfig(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(
		HttpSecurity http,
		JwtAuthFilter jwtAuthFilter
	) throws Exception {
		http
			.csrf(csrf -> csrf.disable())
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.authorizeHttpRequests(auth -> auth
				.requestMatchers(HttpMethod.POST, "/auth/register").hasAuthority("ADMIN")
				.requestMatchers(HttpMethod.POST, "/trademarks/**").hasAuthority("SUPER")
				.requestMatchers(HttpMethod.PUT, "/trademarks/**").hasAuthority("SUPER")
				.requestMatchers(HttpMethod.DELETE, "/trademarks/**").hasAuthority("SUPER")
				.requestMatchers(HttpMethod.POST, "/patents/**").hasAuthority("SUPER")
				.requestMatchers(HttpMethod.PUT, "/patents/**").hasAuthority("SUPER")
				.requestMatchers(HttpMethod.DELETE, "/patents/**").hasAuthority("SUPER")
				.requestMatchers(HttpMethod.POST, "/designs/**").hasAuthority("SUPER")
				.requestMatchers(HttpMethod.PUT, "/designs/**").hasAuthority("SUPER")
				.requestMatchers(HttpMethod.DELETE, "/designs/**").hasAuthority("SUPER")
				.requestMatchers("/auth/**").permitAll()
				.anyRequest().authenticated()
			)
			.addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
			.exceptionHandling(ex -> ex
				.authenticationEntryPoint((request, response, authException) ->
					writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized", "Authentication required.")
				)
				.accessDeniedHandler((request, response, accessDeniedException) ->
					writeError(response, HttpServletResponse.SC_FORBIDDEN, "Forbidden", "Access is denied.")
				)
			);

		return http.build();
	}

	@Bean
	public JwtAuthFilter jwtAuthFilter(JwtService jwtService, ObjectMapper objectMapper) {
		return new JwtAuthFilter(jwtService, objectMapper);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	private void writeError(HttpServletResponse response, int status, String error, String message) throws IOException {
		response.setStatus(status);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		objectMapper.writeValue(response.getOutputStream(), new ApiError(error, message));
	}
}
