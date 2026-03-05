package com.nomina.nomina_portal_service.dto;

import java.time.LocalDate;

public record NotificationResponse(
	Long id,
	String type,
	Long userId,
	String body,
	LocalDate createdAt,
	LocalDate deadlineDate,
	Integer deadlineType,
	Boolean seen
) {
}
