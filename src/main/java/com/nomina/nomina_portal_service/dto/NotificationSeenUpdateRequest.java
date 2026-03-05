package com.nomina.nomina_portal_service.dto;

import jakarta.validation.constraints.NotNull;

public class NotificationSeenUpdateRequest {
	@NotNull
	private Long id;

	@NotNull
	private Boolean seen;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Boolean getSeen() {
		return seen;
	}

	public void setSeen(Boolean seen) {
		this.seen = seen;
	}
}
