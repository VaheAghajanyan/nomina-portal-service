package com.nomina.nomina_portal_service.service;

import com.nomina.nomina_portal_service.dto.NotificationResponse;
import com.nomina.nomina_portal_service.dto.NotificationSeenUpdateRequest;
import com.nomina.nomina_portal_service.exception.NotFoundException;
import com.nomina.nomina_portal_service.model.Notification;
import com.nomina.nomina_portal_service.repository.NotificationRepositoryJdbc;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {
	private final NotificationRepositoryJdbc notificationRepository;

	public NotificationService(NotificationRepositoryJdbc notificationRepository) {
		this.notificationRepository = notificationRepository;
	}

	public List<NotificationResponse> getAll() {
		return getAll(null, null);
	}

	public List<NotificationResponse> getAll(Integer page, Integer size) {
		int resolvedPage = page == null ? 0 : Math.max(page, 0);
		int resolvedSize = size == null ? 20 : Math.max(size, 1);
		int offset = resolvedPage * resolvedSize;

		return notificationRepository.findPage(resolvedSize, offset).stream()
			.map(this::toResponse)
			.toList();
	}

	public NotificationResponse updateSeen(NotificationSeenUpdateRequest request) {
		Notification updated = notificationRepository.updateSeen(request.getId(), request.getSeen())
			.orElseThrow(() -> new NotFoundException("Notification not found."));
		return toResponse(updated);
	}

	private NotificationResponse toResponse(Notification notification) {
		return new NotificationResponse(
			notification.id(),
			notification.type(),
			notification.userId(),
			notification.body(),
			notification.createdAt(),
			notification.deadlineDate(),
			notification.deadlineType(),
			notification.seen()
		);
	}
}
