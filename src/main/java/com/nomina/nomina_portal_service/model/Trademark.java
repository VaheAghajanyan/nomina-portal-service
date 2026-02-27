package com.nomina.nomina_portal_service.model;

import java.time.LocalDate;
import java.util.UUID;

public record Trademark(
	UUID id,
	String trademarkName,
	String markImage,
	String markType,
	String status,
	String jurisdiction,
	String applicationNumber,
	LocalDate applicationDate,
	String registrationNumber,
	LocalDate registrationDate,
	String ownerName,
	String ownerAddress,
	String classes,
	String goodsServicesText,
	String priorityNumber,
	LocalDate priorityDate,
	String priorityCountry,
	LocalDate renewalDate,
	String gracePeriodEnd,
	String oppositionDeadline,
	String proofOfUseDeadline,
	String interimDeadline,
	String lastAction,
	String legalEvents,
	String opposition,
	String cancellation,
	String litigation,
	String license,
	String assignment,
	String responsibleAttorney,
	String representative,
	String contact,
	String notes,
	Long createdByUser,
	String createdByUsername,
	LocalDate dateOfCreation,
	String currentStatus
) {
}
