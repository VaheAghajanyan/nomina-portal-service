package com.nomina.nomina_portal_service.service;

import com.nomina.nomina_portal_service.dto.AdminUserListItemResponse;
import com.nomina.nomina_portal_service.dto.AdminUserUpdateRequest;
import com.nomina.nomina_portal_service.exception.ConflictException;
import com.nomina.nomina_portal_service.exception.NotFoundException;
import com.nomina.nomina_portal_service.model.User;
import com.nomina.nomina_portal_service.repository.UserRepositoryJdbc;
import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserAdminService {
	private final UserRepositoryJdbc userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserAdminService(UserRepositoryJdbc userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public List<AdminUserListItemResponse> getAllUsers() {
		return userRepository.findAll().stream()
			.map(this::toListItemResponse)
			.toList();
	}

	public AdminUserListItemResponse updateUser(AdminUserUpdateRequest request) {
		User existing = userRepository.findById(request.getId())
			.orElseThrow(() -> new NotFoundException("User not found."));

		if (userRepository.existsByUsernameAndIdNot(request.getUsername(), request.getId())) {
			throw new ConflictException("Username already exists.");
		}

		User userToUpdate = new User();
		userToUpdate.setId(existing.getId());
		userToUpdate.setUsername(request.getUsername());
		userToUpdate.setPasswordHash(
			request.getPassword() == null
				? existing.getPasswordHash()
				: passwordEncoder.encode(request.getPassword())
		);
		userToUpdate.setSuperUser(Boolean.TRUE.equals(request.getIsSuperUser()));
		userToUpdate.setAdminUser(existing.isAdminUser());
		userToUpdate.setActiveUser(Boolean.TRUE.equals(request.getIsActiveUser()));

		User updated = userRepository.update(userToUpdate)
			.orElseThrow(() -> new NotFoundException("User not found."));
		return toListItemResponse(updated);
	}

	private AdminUserListItemResponse toListItemResponse(User user) {
		AdminUserListItemResponse response = new AdminUserListItemResponse();
		response.setId(user.getId());
		response.setUsername(user.getUsername());
		response.setSuperUser(user.isSuperUser());
		response.setActiveUser(user.isActiveUser());
		return response;
	}
}
