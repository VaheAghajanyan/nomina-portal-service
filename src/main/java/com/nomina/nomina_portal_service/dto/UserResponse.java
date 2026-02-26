package com.nomina.nomina_portal_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserResponse {
	private long id;
	private String username;

	@JsonProperty("isSuperUser")
	private boolean superUser;

	@JsonProperty("isAdminUser")
	private boolean adminUser;

	@JsonProperty("isActiveUser")
	private boolean activeUser;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public boolean isSuperUser() {
		return superUser;
	}

	public void setSuperUser(boolean superUser) {
		this.superUser = superUser;
	}

	public boolean isAdminUser() {
		return adminUser;
	}

	public void setAdminUser(boolean adminUser) {
		this.adminUser = adminUser;
	}

	public boolean isActiveUser() {
		return activeUser;
	}

	public void setActiveUser(boolean activeUser) {
		this.activeUser = activeUser;
	}
}
