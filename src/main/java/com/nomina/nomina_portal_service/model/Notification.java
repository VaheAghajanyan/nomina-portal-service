package com.nomina.nomina_portal_service.model;

import java.time.LocalDate;

public record Notification(
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
