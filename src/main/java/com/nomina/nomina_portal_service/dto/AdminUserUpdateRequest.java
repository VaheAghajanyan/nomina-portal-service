package com.nomina.nomina_portal_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class AdminUserUpdateRequest {
	@NotNull
	private Long id;

	@NotBlank
	@Email(message = "Username must be a valid email address.")
	@Pattern(regexp = ".*\\..*", message = "Username must contain '.'")
	@Size(min = 3, max = 100)
	private String username;

	@Size(min = 8, max = 255)
	private String password;

	@JsonProperty("isSuperUser")
	@NotNull
	private Boolean isSuperUser;

	@JsonProperty("isActiveUser")
	@NotNull
	private Boolean isActiveUser;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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

	public void setIsSuperUser(Boolean superUser) {
		isSuperUser = superUser;
	}

	public Boolean getIsActiveUser() {
		return isActiveUser;
	}

	public void setIsActiveUser(Boolean activeUser) {
		isActiveUser = activeUser;
	}
}
