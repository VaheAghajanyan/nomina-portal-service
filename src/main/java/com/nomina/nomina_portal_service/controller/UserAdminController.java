package com.nomina.nomina_portal_service.controller;

import com.nomina.nomina_portal_service.dto.AdminUserListItemResponse;
import com.nomina.nomina_portal_service.dto.AdminUserUpdateRequest;
import com.nomina.nomina_portal_service.service.UserAdminService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserAdminController {
	private final UserAdminService userAdminService;

	public UserAdminController(UserAdminService userAdminService) {
		this.userAdminService = userAdminService;
	}

	@GetMapping
	public List<AdminUserListItemResponse> getAllUsers() {
		return userAdminService.getAllUsers();
	}

	@PutMapping
	public AdminUserListItemResponse updateUser(@Valid @RequestBody AdminUserUpdateRequest request) {
		return userAdminService.updateUser(request);
	}
}
