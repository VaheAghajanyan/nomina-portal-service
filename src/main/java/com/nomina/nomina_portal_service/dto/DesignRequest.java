package com.nomina.nomina_portal_service.dto;

import java.time.LocalDate;

public record DesignRequest(
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
	LocalDate dateOfCreation,
	String currentStatus
) {
}
