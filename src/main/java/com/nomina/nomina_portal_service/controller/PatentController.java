package com.nomina.nomina_portal_service.controller;

import com.nomina.nomina_portal_service.dto.PatentRequest;
import com.nomina.nomina_portal_service.dto.PatentResponse;
import com.nomina.nomina_portal_service.service.PatentService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/patents")
public class PatentController {
	private final PatentService patentService;

	public PatentController(PatentService patentService) {
		this.patentService = patentService;
	}

	@GetMapping
	public List<PatentResponse> getAll() {
		return patentService.getAll();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public PatentResponse create(@Valid @RequestBody PatentRequest request) {
		return patentService.create(request);
	}

	@PutMapping("/{id}")
	public PatentResponse update(@PathVariable UUID id, @Valid @RequestBody PatentRequest request) {
		return patentService.update(id, request);
	}

	@DeleteMapping
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@RequestBody List<UUID> ids) {
		patentService.delete(ids);
	}
}
