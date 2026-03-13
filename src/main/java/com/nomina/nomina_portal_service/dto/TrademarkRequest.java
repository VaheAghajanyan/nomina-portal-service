package com.nomina.nomina_portal_service.dto;

import com.nomina.nomina_portal_service.model.MadridSystemItem;
import java.time.LocalDate;
import java.util.List;

public record TrademarkRequest(
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
	String publicationDate,
	LocalDate priorityDate,
	String priorityCountry,
	LocalDate renewalDate,
	String gracePeriodEnd,
	String oppositionDeadline,
	String proofOfUseDeadline,
	String actionDeadline,
	String lastAction,
	String basic,
	String opposition,
	String cancellation,
	String litigation,
	String license,
	String assignment,
	String responsibleAttorney,
	String representative,
	String contact,
	String notes,
	List<MadridSystemItem> madridSystem,
	LocalDate dateOfCreation,
	String neededAction
) {
}
