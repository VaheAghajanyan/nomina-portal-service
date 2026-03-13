package com.nomina.nomina_portal_service.dto;

import com.nomina.nomina_portal_service.model.MadridSystemItem;
import java.time.LocalDate;
import java.util.List;

public record PatentRequest(
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
	List<MadridSystemItem> madridSystem,
	LocalDate dateOfCreation,
	String currentStatus
) {
}
