package com.nomina.nomina_portal_service.controller;

import com.nomina.nomina_portal_service.dto.NotificationResponse;
import com.nomina.nomina_portal_service.dto.NotificationSeenUpdateRequest;
import com.nomina.nomina_portal_service.service.NotificationService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
	private final NotificationService notificationService;

	public NotificationController(NotificationService notificationService) {
		this.notificationService = notificationService;
	}

	@GetMapping
	public List<NotificationResponse> getAll(
		@RequestParam(required = false) Integer page,
		@RequestParam(required = false) Integer size
	) {
		return notificationService.getAll(page, size);
	}

	@PutMapping
	public NotificationResponse updateSeen(@Valid @RequestBody NotificationSeenUpdateRequest request) {
		return notificationService.updateSeen(request);
	}
}
