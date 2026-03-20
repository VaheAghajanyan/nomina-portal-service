package com.nomina.nomina_portal_service.model;

import java.util.UUID;

public record StoredFile(
	UUID id,
	String fileName,
	String filePath
) {
}
