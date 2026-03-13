package com.nomina.nomina_portal_service.dto;

import com.nomina.nomina_portal_service.model.MadridSystemItem;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record DesignResponse(
	UUID id,
	String designTitle,
	String designImage,
	String jurisdiction,
	String applicationNumber,
	LocalDate filingDate,
	String registrationNumber,
	LocalDate registrationDate,
	String status,
	String locarnoClass,
	String priorityNumber,
	LocalDate priorityDate,
	String priorityCountry,
	String owner,
	String designer,
	String applicant,
	String productIndication,
	String colorClaim,
	String renewalPeriods,
	LocalDate gracePeriodEnd,
	LocalDate publicationDefermentEnd,
	String productLine,
	String brand,
	String license,
	String responsibleAttorney,
	String representative,
	String contact,
	String notes,
	List<MadridSystemItem> madridSystem,
	String createdByUser,
	LocalDate dateOfCreation,
	String currentStatus
) {
}
