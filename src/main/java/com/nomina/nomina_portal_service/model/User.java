package com.nomina.nomina_portal_service.model;

public class User {
	private long id;
	private String username;
	private String passwordHash;
	private boolean isSuperUser;
	private boolean isAdminUser;
	private boolean isActiveUser;

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

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public boolean isSuperUser() {
		return isSuperUser;
	}

	public void setSuperUser(boolean superUser) {
		isSuperUser = superUser;
	}

	public boolean isAdminUser() {
		return isAdminUser;
	}

	public void setAdminUser(boolean adminUser) {
		isAdminUser = adminUser;
	}

	public boolean isActiveUser() {
		return isActiveUser;
	}

	public void setActiveUser(boolean activeUser) {
		isActiveUser = activeUser;
	}
}
