package com.nomina.nomina_portal_service.service;

import com.nomina.nomina_portal_service.dto.DesignRequest;
import com.nomina.nomina_portal_service.dto.DesignResponse;
import com.nomina.nomina_portal_service.exception.NotFoundException;
import com.nomina.nomina_portal_service.exception.UnauthorizedException;
import com.nomina.nomina_portal_service.model.Design;
import com.nomina.nomina_portal_service.repository.DesignRepositoryJdbc;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class DesignService {
	private final DesignRepositoryJdbc designRepository;
	private final ImageStorageService imageStorageService;

	public DesignService(DesignRepositoryJdbc designRepository, ImageStorageService imageStorageService) {
		this.designRepository = designRepository;
		this.imageStorageService = imageStorageService;
	}

	public List<DesignResponse> getAll() {
		return designRepository.findAll().stream()
			.map(this::toResponse)
			.toList();
	}

	public DesignResponse create(DesignRequest request) {
		String imagePath = imageStorageService.saveBase64Image(
			request.designImage(),
			request.designTitle(),
			request.registrationNumber()
		);
		Design design = toDesign(null, request, getCurrentUserId(), request.dateOfCreation(), imagePath);
		return toResponse(designRepository.insert(design));
	}

	public DesignResponse update(UUID id, DesignRequest request) {
		Design existing = designRepository.findById(id)
			.orElseThrow(() -> new NotFoundException("Design not found."));

		Design updated = toDesign(
			existing.id(),
			request,
			existing.createdByUser(),
			request.dateOfCreation(),
			imageStorageService.saveBase64Image(
				request.designImage(),
				request.designTitle(),
				request.registrationNumber()
			)
		);

		return designRepository.update(updated)
			.map(this::toResponse)
			.orElseThrow(() -> new NotFoundException("Design not found."));
	}

	public void delete(List<UUID> ids) {
		Set<UUID> uniqueIds = new LinkedHashSet<>(ids);
		if (uniqueIds.isEmpty()) {
			throw new NotFoundException("At least one design id is required.");
		}

		int deleted = designRepository.deleteByIds(List.copyOf(uniqueIds));
		if (deleted != uniqueIds.size()) {
			throw new NotFoundException("One or more designs were not found.");
		}
	}

	private Design toDesign(
		UUID id,
		DesignRequest request,
		Long createdByUser,
		LocalDate dateOfCreation,
		String imagePath
	) {
		return new Design(
			id,
			request.designTitle(),
			imagePath,
			request.jurisdiction(),
			request.applicationNumber(),
			request.filingDate(),
			request.registrationNumber(),
			request.registrationDate(),
			request.status(),
			request.locarnoClass(),
			request.priorityNumber(),
			request.priorityDate(),
			request.priorityCountry(),
			request.owner(),
			request.designer(),
			request.applicant(),
			request.productIndication(),
			request.colorClaim(),
			request.renewalPeriods(),
			request.gracePeriodEnd(),
			request.publicationDefermentEnd(),
			request.productLine(),
			request.brand(),
			request.license(),
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

	private DesignResponse toResponse(Design design) {
		return new DesignResponse(
			design.id(),
			design.designTitle(),
			design.designImage(),
			design.jurisdiction(),
			design.applicationNumber(),
			design.filingDate(),
			design.registrationNumber(),
			design.registrationDate(),
			design.status(),
			design.locarnoClass(),
			design.priorityNumber(),
			design.priorityDate(),
			design.priorityCountry(),
			design.owner(),
			design.designer(),
			design.applicant(),
			design.productIndication(),
			design.colorClaim(),
			design.renewalPeriods(),
			design.gracePeriodEnd(),
			design.publicationDefermentEnd(),
			design.productLine(),
			design.brand(),
			design.license(),
			design.responsibleAttorney(),
			design.representative(),
			design.contact(),
			design.notes(),
			design.createdByUsername(),
			design.dateOfCreation(),
			design.currentStatus()
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
