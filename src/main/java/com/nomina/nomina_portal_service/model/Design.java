package com.nomina.nomina_portal_service.model;

import java.time.LocalDate;
import java.util.UUID;

public record Design(
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
	Long createdByUser,
	String createdByUsername,
	LocalDate dateOfCreation,
	String currentStatus
) {
}
