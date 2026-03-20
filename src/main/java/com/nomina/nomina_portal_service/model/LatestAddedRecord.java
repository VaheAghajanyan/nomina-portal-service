package com.nomina.nomina_portal_service.model;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record LatestAddedRecord(
	UUID id,
	String name,
	String applicationNumber,
	String image,
	Long createdByUser,
	LocalDate dateOfCreation,
	String status,
	List<MadridSystemItem> madridSystem
) {
}
