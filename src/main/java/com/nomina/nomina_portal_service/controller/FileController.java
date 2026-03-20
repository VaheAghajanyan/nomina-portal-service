package com.nomina.nomina_portal_service.controller;

import com.nomina.nomina_portal_service.dto.FileStoreRequest;
import com.nomina.nomina_portal_service.dto.StoredFileResponse;
import com.nomina.nomina_portal_service.service.FileStorageService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/files")
public class FileController {
	private final FileStorageService fileStorageService;

	public FileController(FileStorageService fileStorageService) {
		this.fileStorageService = fileStorageService;
	}

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@ResponseStatus(HttpStatus.CREATED)
	public List<StoredFileResponse> uploadFiles(
		@Valid @RequestPart("request") FileStoreRequest request,
		@RequestPart("files") List<MultipartFile> files
	) {
		return fileStorageService.replaceFiles(request.getId(), files);
	}

	@GetMapping("/{id}")
	public List<StoredFileResponse> getFiles(@PathVariable UUID id) {
		return fileStorageService.getFiles(id);
	}
}
