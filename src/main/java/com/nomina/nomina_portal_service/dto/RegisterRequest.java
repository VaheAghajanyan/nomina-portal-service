package com.nomina.nomina_portal_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RegisterRequest {
	@NotBlank
	@Size(min = 3, max = 100)
	private String username;

	@NotBlank
	@Size(min = 8, max = 255)
	private String password;

	@JsonProperty("isSuperUser")
	private Boolean isSuperUser;

	@JsonProperty("isAdminUser")
	private Boolean isAdminUser;

	@JsonProperty("isActiveUser")
	private Boolean isActiveUser;

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Boolean getIsSuperUser() {
		return isSuperUser;
	}

	public void setIsSuperUser(Boolean isSuperUser) {
		this.isSuperUser = isSuperUser;
	}

	public Boolean getIsAdminUser() {
		return isAdminUser;
	}

	public void setIsAdminUser(Boolean isAdminUser) {
		this.isAdminUser = isAdminUser;
	}

	public Boolean getIsActiveUser() {
		return isActiveUser;
	}

	public void setIsActiveUser(Boolean isActiveUser) {
		this.isActiveUser = isActiveUser;
	}
}
