package com.nomina.nomina_portal_service.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class NotificationEmailTemplateService {
	private static final DateTimeFormatter DEADLINE_FORMATTER = DateTimeFormatter.ofPattern("MMMM d, uuuu");

	public String render(
		String assetType,
		String assetName,
		String registrationNumber,
		String ownerName,
		LocalDate deadlineDate,
		int daysLeft
	) {
		String template = loadTemplate();
		String safeAssetType = defaultValue(assetType, "Asset");
		String safeAssetName = defaultValue(assetName, "N/A");
		String safeRegistrationNumber = defaultValue(registrationNumber, "N/A");
		String safeOwnerName = defaultValue(ownerName, "N/A");
		String safeDeadlineDate = deadlineDate != null ? deadlineDate.format(DEADLINE_FORMATTER) : "N/A";
		String safeDaysLeft = Integer.toString(daysLeft);

		return template
			.replace("{{ALERT_TITLE}}", "Renewal deadline approaching.")
			.replace("{{ALERT_TEXT}}", "Your " + safeAssetType.toLowerCase() + " registration requires renewal to maintain legal protection.")
			.replace("{{HEADLINE}}", "Your " + safeAssetType.toLowerCase() + " is due for renewal")
			.replace("{{INTRO_TEXT}}", "Nomina has detected that one of your registered intellectual property assets is approaching its renewal deadline. Please review the details below and initiate the renewal process before the expiration date to avoid losing protection.")
			.replace("{{ASSET_TYPE}}", escapeHtml(safeAssetType))
			.replace("{{STATUS_LABEL}}", "Renewal Required")
			.replace("{{REGISTRATION_NUMBER}}", escapeHtml(safeRegistrationNumber))
			.replace("{{ASSET_NAME}}", escapeHtml(safeAssetName))
			.replace("{{OWNER_NAME}}", escapeHtml(safeOwnerName))
			.replace("{{DEADLINE_DATE}}", escapeHtml(safeDeadlineDate))
			.replace("{{DAYS_LEFT}}", safeDaysLeft);
	}

	private String loadTemplate() {
		Path rootTemplate = Path.of("notification.html");
		if (Files.exists(rootTemplate)) {
			try {
				return Files.readString(rootTemplate, StandardCharsets.UTF_8);
			} catch (IOException ex) {
				throw new IllegalStateException("Unable to read notification.html from project root.", ex);
			}
		}

		ClassPathResource resource = new ClassPathResource("notification.html");
		if (resource.exists()) {
			try (var inputStream = resource.getInputStream()) {
				return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
			} catch (IOException ex) {
				throw new IllegalStateException("Unable to read notification.html from classpath.", ex);
			}
		}

		throw new IllegalStateException("notification.html template was not found.");
	}

	private String defaultValue(String value, String fallback) {
		return value == null || value.isBlank() ? fallback : value;
	}

	private String escapeHtml(String value) {
		return value
			.replace("&", "&amp;")
			.replace("<", "&lt;")
			.replace(">", "&gt;")
			.replace("\"", "&quot;")
			.replace("'", "&#39;");
	}
}
