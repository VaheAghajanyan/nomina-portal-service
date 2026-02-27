package com.nomina.nomina_portal_service.service;

import com.nomina.nomina_portal_service.dto.TrademarkRequest;
import com.nomina.nomina_portal_service.dto.TrademarkResponse;
import com.nomina.nomina_portal_service.exception.NotFoundException;
import com.nomina.nomina_portal_service.exception.UnauthorizedException;
import com.nomina.nomina_portal_service.model.Trademark;
import com.nomina.nomina_portal_service.repository.TrademarkRepositoryJdbc;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class TrademarkService {
	private final TrademarkRepositoryJdbc trademarkRepository;
	private final ImageStorageService imageStorageService;

	public TrademarkService(TrademarkRepositoryJdbc trademarkRepository, ImageStorageService imageStorageService) {
		this.trademarkRepository = trademarkRepository;
		this.imageStorageService = imageStorageService;
	}

	public List<TrademarkResponse> getAll() {
		return trademarkRepository.findAll().stream()
			.map(this::toResponse)
			.toList();
	}

	public TrademarkResponse create(TrademarkRequest request) {
		String imagePath = imageStorageService.saveBase64Image(
			request.markImage(),
			request.trademarkName(),
			request.registrationNumber()
		);
		Trademark trademark = toTrademark(null, request, getCurrentUserId(), request.dateOfCreation(), imagePath);
		return toResponse(trademarkRepository.insert(trademark));
	}

	public TrademarkResponse update(UUID id, TrademarkRequest request) {
		Trademark existing = trademarkRepository.findById(id)
			.orElseThrow(() -> new NotFoundException("Trademark not found."));

		Trademark updated = toTrademark(
			existing.id(),
			request,
			existing.createdByUser(),
			request.dateOfCreation(),
			imageStorageService.saveBase64Image(
				request.markImage(),
				request.trademarkName(),
				request.registrationNumber()
			)
		);

		return trademarkRepository.update(updated)
			.map(this::toResponse)
			.orElseThrow(() -> new NotFoundException("Trademark not found."));
	}

	public void delete(List<UUID> ids) {
		Set<UUID> uniqueIds = new LinkedHashSet<>(ids);
		if (uniqueIds.isEmpty()) {
			throw new NotFoundException("At least one trademark id is required.");
		}

		int deleted = trademarkRepository.deleteByIds(List.copyOf(uniqueIds));
		if (deleted != uniqueIds.size()) {
			throw new NotFoundException("One or more trademarks were not found.");
		}
	}

	private Trademark toTrademark(
		UUID id,
		TrademarkRequest request,
		Long createdByUser,
		LocalDate dateOfCreation,
		String imagePath
	) {
		return new Trademark(
			id,
			request.trademarkName(),
			imagePath,
			request.markType(),
			request.status(),
			request.jurisdiction(),
			request.applicationNumber(),
			request.applicationDate(),
			request.registrationNumber(),
			request.registrationDate(),
			request.ownerName(),
			request.ownerAddress(),
			request.classes(),
			request.goodsServicesText(),
			request.priorityNumber(),
			request.priorityDate(),
			request.priorityCountry(),
			request.renewalDate(),
			request.gracePeriodEnd(),
			request.oppositionDeadline(),
			request.proofOfUseDeadline(),
			request.interimDeadline(),
			request.lastAction(),
			request.legalEvents(),
			request.opposition(),
			request.cancellation(),
			request.litigation(),
			request.license(),
			request.assignment(),
			request.responsibleAttorney(),
			request.representative(),
			request.contact(),
			request.notes(),
			createdByUser,
			null,
			dateOfCreation,
			request.currentStatus()
		);
	}

	private TrademarkResponse toResponse(Trademark trademark) {
		return new TrademarkResponse(
			trademark.id(),
			trademark.trademarkName(),
			trademark.markImage(),
			trademark.markType(),
			trademark.status(),
			trademark.jurisdiction(),
			trademark.applicationNumber(),
			trademark.applicationDate(),
			trademark.registrationNumber(),
			trademark.registrationDate(),
			trademark.ownerName(),
			trademark.ownerAddress(),
			trademark.classes(),
			trademark.goodsServicesText(),
			trademark.priorityNumber(),
			trademark.priorityDate(),
			trademark.priorityCountry(),
			trademark.renewalDate(),
			trademark.gracePeriodEnd(),
			trademark.oppositionDeadline(),
			trademark.proofOfUseDeadline(),
			trademark.interimDeadline(),
			trademark.lastAction(),
			trademark.legalEvents(),
			trademark.opposition(),
			trademark.cancellation(),
			trademark.litigation(),
			trademark.license(),
			trademark.assignment(),
			trademark.responsibleAttorney(),
			trademark.representative(),
			trademark.contact(),
			trademark.notes(),
			trademark.createdByUsername(),
			trademark.dateOfCreation(),
			trademark.currentStatus()
		);
	}

	private long getCurrentUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null || !authentication.isAuthenticated()) {
			throw new UnauthorizedException("Authentication required.");
		}

		Object details = authentication.getDetails();
		if (details instanceof Number number) {
			return number.longValue();
		}
		if (details instanceof String value) {
			try {
				return Long.parseLong(value);
			} catch (NumberFormatException ignored) {
				// Fall through.
			}
		}

		throw new UnauthorizedException("Unable to resolve current user from token.");
	}
}
