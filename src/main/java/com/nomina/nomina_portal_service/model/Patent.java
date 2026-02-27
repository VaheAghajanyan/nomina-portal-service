package com.nomina.nomina_portal_service.model;

import java.time.LocalDate;
import java.util.UUID;

public record Patent(
	UUID id,
	String title,
	String patentType,
	String jurisdiction,
	String applicationNumber,
	LocalDate filingDate,
	String publicationNumber,
	LocalDate publicationDate,
	String patentNumber,
	LocalDate grantDate,
	String status,
	String priorityNumber,
	LocalDate priorityDate,
	String priorityCountry,
	String inventors,
	String inventorCountry,
	String applicant,
	String assignee,
	String ipcCpc,
	LocalDate examinationRequestDeadline,
	LocalDate officeActionDeadline,
	LocalDate grantFeeDeadline,
	LocalDate validationDeadlines,
	LocalDate annuityDueDates,
	LocalDate lapseDate,
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
