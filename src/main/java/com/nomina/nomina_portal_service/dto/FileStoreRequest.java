package com.nomina.nomina_portal_service.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class FileStoreRequest {
	@NotNull
	private UUID id;

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}
}
