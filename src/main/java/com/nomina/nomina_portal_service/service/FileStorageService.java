package com.nomina.nomina_portal_service.service;

import com.nomina.nomina_portal_service.dto.StoredFileResponse;
import com.nomina.nomina_portal_service.model.StoredFile;
import com.nomina.nomina_portal_service.repository.FileRepositoryJdbc;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
public class FileStorageService {
	private static final Set<String> ALLOWED_EXTENSIONS = Set.of("pdf", "csv");

	private final FileRepositoryJdbc fileRepository;
	private final Path storageBasePath;

	public FileStorageService(FileRepositoryJdbc fileRepository, @Value("${storage.base-path}") String storageBasePath) {
		this.fileRepository = fileRepository;
		this.storageBasePath = Paths.get(storageBasePath);
	}

	public List<StoredFileResponse> replaceFiles(UUID id, List<MultipartFile> files) {
		if (id == null) {
			throw new ResponseStatusException(BAD_REQUEST, "Id is required.");
		}
		if (files == null || files.isEmpty()) {
			throw new ResponseStatusException(BAD_REQUEST, "At least one file is required.");
		}

		deleteStoredFilesById(id);

		Path filesDir = storageBasePath.resolve("files");
		try {
			Files.createDirectories(filesDir);
		} catch (Exception ex) {
			throw new IllegalStateException("Unable to create files directory.", ex);
		}

		List<StoredFileResponse> responses = new ArrayList<>();
		for (MultipartFile file : files) {
			if (file == null || file.isEmpty()) {
				continue;
			}

			String originalName = file.getOriginalFilename();
			String extension = resolveExtension(originalName);
			if (!ALLOWED_EXTENSIONS.contains(extension)) {
				throw new ResponseStatusException(BAD_REQUEST, "Only PDF and CSV files are allowed.");
			}

			String uniqueName = UUID.randomUUID() + "." + extension;
			Path destination = filesDir.resolve(uniqueName);
			try {
				Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
			} catch (Exception ex) {
				throw new IllegalStateException("Unable to store file.", ex);
			}

			String filePath = "files\\" + uniqueName;
			fileRepository.insert(new StoredFile(id, uniqueName, filePath));
			responses.add(new StoredFileResponse(uniqueName, filePath));
		}

		if (responses.isEmpty()) {
			throw new ResponseStatusException(BAD_REQUEST, "At least one non-empty file is required.");
		}
		return responses;
	}

	public List<StoredFileResponse> getFiles(UUID id) {
		return fileRepository.findById(id).stream()
			.map(file -> new StoredFileResponse(file.fileName(), file.filePath()))
			.toList();
	}

	public void deleteStoredFilesByEntityIds(List<UUID> ids) {
		if (ids == null || ids.isEmpty()) {
			return;
		}
		for (UUID id : ids) {
			deleteStoredFilesById(id);
		}
	}

	private void deleteStoredFilesById(UUID id) {
		List<StoredFile> existing = fileRepository.findById(id);
		for (StoredFile file : existing) {
			deletePhysicalFile(file.filePath());
		}
		fileRepository.deleteById(id);
	}

	private void deletePhysicalFile(String relativePath) {
		if (relativePath == null || relativePath.isBlank()) {
			return;
		}
		Path resolved = storageBasePath.resolve(relativePath.replace("\\", "/"));
		try {
			Files.deleteIfExists(resolved);
		} catch (Exception ignored) {
			// Keep DB cleanup flow resilient even if physical deletion fails.
		}
	}

	private String resolveExtension(String filename) {
		if (filename == null || filename.isBlank()) {
			throw new ResponseStatusException(BAD_REQUEST, "Invalid file name.");
		}
		int dot = filename.lastIndexOf('.');
		if (dot <= 0 || dot == filename.length() - 1) {
			throw new ResponseStatusException(BAD_REQUEST, "File must have an extension.");
		}
		return filename.substring(dot + 1).toLowerCase(Locale.ROOT);
	}
}
