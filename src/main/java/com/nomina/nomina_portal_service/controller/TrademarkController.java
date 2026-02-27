package com.nomina.nomina_portal_service.controller;

import com.nomina.nomina_portal_service.dto.TrademarkRequest;
import com.nomina.nomina_portal_service.dto.TrademarkResponse;
import com.nomina.nomina_portal_service.service.TrademarkService;
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
@RequestMapping("/trademarks")
public class TrademarkController {
	private final TrademarkService trademarkService;

	public TrademarkController(TrademarkService trademarkService) {
		this.trademarkService = trademarkService;
	}

	@GetMapping
	public List<TrademarkResponse> getAll() {
		return trademarkService.getAll();
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public TrademarkResponse create(@Valid @RequestBody TrademarkRequest request) {
		return trademarkService.create(request);
	}

	@PutMapping("/{id}")
	public TrademarkResponse update(@PathVariable UUID id, @Valid @RequestBody TrademarkRequest request) {
		return trademarkService.update(id, request);
	}

	@DeleteMapping
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@RequestBody List<UUID> ids) {
		trademarkService.delete(ids);
	}
}
