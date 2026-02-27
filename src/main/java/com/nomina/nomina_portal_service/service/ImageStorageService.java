package com.nomina.nomina_portal_service.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ImageStorageService {
	private final Path storageBasePath;

	public ImageStorageService(@Value("${storage.base-path}") String storageBasePath) {
		this.storageBasePath = Paths.get(storageBasePath);
	}

	public String saveBase64Image(String base64Image, String namePart, String registrationNumber) {
		if (base64Image == null || base64Image.isBlank()) {
			return null;
		}

		try {
			String payload = extractBase64Payload(base64Image);
			byte[] imageBytes = Base64.getDecoder().decode(payload);
			String extension = resolveExtension(base64Image, imageBytes);

			Path imagesDir = storageBasePath.resolve("images");
			Files.createDirectories(imagesDir);

			String fileName = buildFileName(namePart, registrationNumber, extension);
			Path filePath = imagesDir.resolve(fileName);
			if (Files.exists(filePath)) {
				filePath = imagesDir.resolve(buildFileName(
					namePart,
					registrationNumber + "_" + System.currentTimeMillis(),
					extension
				));
			}

			Files.write(filePath, imageBytes, StandardOpenOption.CREATE_NEW);
			return "images\\" + filePath.getFileName();
		} catch (Exception ignored) {
			return null;
		}
	}

	private String extractBase64Payload(String raw) {
		int commaIndex = raw.indexOf(',');
		if (raw.startsWith("data:") && commaIndex > -1) {
			return raw.substring(commaIndex + 1);
		}
		return raw;
	}

	private String buildFileName(String namePart, String registrationNumber, String extension) {
		String left = sanitize(namePart);
		String right = sanitize(registrationNumber);
		String merged = left + right;
		if (merged.isBlank()) {
			return "image_" + System.currentTimeMillis() + "." + extension;
		}
		if (merged.length() > 150) {
			merged = merged.substring(0, 150);
		}
		return merged + "." + extension;
	}

	private String sanitize(String value) {
		if (value == null) {
			return "";
		}
		return value.replaceAll("[^a-zA-Z0-9._-]", "_");
	}

	private String resolveExtension(String rawBase64, byte[] imageBytes) {
		String lowered = rawBase64.toLowerCase();
		if (lowered.startsWith("data:image/png")) {
			return "png";
		}
		if (lowered.startsWith("data:image/jpeg") || lowered.startsWith("data:image/jpg")) {
			return "jpg";
		}

		if (isPng(imageBytes)) {
			return "png";
		}
		if (isJpeg(imageBytes)) {
			return "jpg";
		}

		return "png";
	}

	private boolean isPng(byte[] bytes) {
		return bytes.length > 8
			&& (bytes[0] & 0xFF) == 0x89
			&& bytes[1] == 0x50
			&& bytes[2] == 0x4E
			&& bytes[3] == 0x47
			&& bytes[4] == 0x0D
			&& bytes[5] == 0x0A
			&& bytes[6] == 0x1A
			&& bytes[7] == 0x0A;
	}

	private boolean isJpeg(byte[] bytes) {
		return bytes.length > 3
			&& (bytes[0] & 0xFF) == 0xFF
			&& (bytes[1] & 0xFF) == 0xD8
			&& (bytes[2] & 0xFF) == 0xFF;
	}
}
