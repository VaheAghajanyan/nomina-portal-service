package com.nomina.nomina_portal_service.service;

import com.nomina.nomina_portal_service.dto.PatentRequest;
import com.nomina.nomina_portal_service.dto.PatentResponse;
import com.nomina.nomina_portal_service.exception.NotFoundException;
import com.nomina.nomina_portal_service.exception.UnauthorizedException;
import com.nomina.nomina_portal_service.model.Patent;
import com.nomina.nomina_portal_service.repository.PatentRepositoryJdbc;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class PatentService {
	private final PatentRepositoryJdbc patentRepository;

	public PatentService(PatentRepositoryJdbc patentRepository) {
		this.patentRepository = patentRepository;
	}

	public List<PatentResponse> getAll() {
		return patentRepository.findAll().stream()
			.map(this::toResponse)
			.toList();
	}

	public PatentResponse create(PatentRequest request) {
		Patent patent = toPatent(null, request, getCurrentUserId(), request.dateOfCreation());
		return toResponse(patentRepository.insert(patent));
	}

	public PatentResponse update(UUID id, PatentRequest request) {
		Patent existing = patentRepository.findById(id)
			.orElseThrow(() -> new NotFoundException("Patent not found."));

		Patent updated = toPatent(
			existing.id(),
			request,
			existing.createdByUser(),
			request.dateOfCreation()
		);

		return patentRepository.update(updated)
			.map(this::toResponse)
			.orElseThrow(() -> new NotFoundException("Patent not found."));
	}

	public void delete(List<UUID> ids) {
		Set<UUID> uniqueIds = new LinkedHashSet<>(ids);
		if (uniqueIds.isEmpty()) {
			throw new NotFoundException("At least one patent id is required.");
		}

		int deleted = patentRepository.deleteByIds(List.copyOf(uniqueIds));
		if (deleted != uniqueIds.size()) {
			throw new NotFoundException("One or more patents were not found.");
		}
	}

	private Patent toPatent(UUID id, PatentRequest request, Long createdByUser, LocalDate dateOfCreation) {
		return new Patent(
			id,
			request.title(),
			request.patentType(),
			request.jurisdiction(),
			request.applicationNumber(),
			request.filingDate(),
			request.publicationNumber(),
			request.publicationDate(),
			request.patentNumber(),
			request.grantDate(),
			request.status(),
			request.priorityNumber(),
			request.priorityDate(),
			request.priorityCountry(),
			request.inventors(),
			request.inventorCountry(),
			request.applicant(),
			request.assignee(),
			request.ipcCpc(),
			request.examinationRequestDeadline(),
			request.officeActionDeadline(),
			request.grantFeeDeadline(),
			request.validationDeadlines(),
			request.annuityDueDates(),
			request.lapseDate(),
			request.license(),
			request.responsibleAttorney(),
			request.representative(),
			request.contact(),
			request.notes(),
			request.madridSystem(),
			createdByUser,
			null,
			dateOfCreation,
			request.currentStatus()
		);
	}

	private PatentResponse toResponse(Patent patent) {
		return new PatentResponse(
			patent.id(),
			patent.title(),
			patent.patentType(),
			patent.jurisdiction(),
			patent.applicationNumber(),
			patent.filingDate(),
			patent.publicationNumber(),
			patent.publicationDate(),
			patent.patentNumber(),
			patent.grantDate(),
			patent.status(),
			patent.priorityNumber(),
			patent.priorityDate(),
			patent.priorityCountry(),
			patent.inventors(),
			patent.inventorCountry(),
			patent.applicant(),
			patent.assignee(),
			patent.ipcCpc(),
			patent.examinationRequestDeadline(),
			patent.officeActionDeadline(),
			patent.grantFeeDeadline(),
			patent.validationDeadlines(),
			patent.annuityDueDates(),
			patent.lapseDate(),
			patent.license(),
			patent.responsibleAttorney(),
			patent.representative(),
			patent.contact(),
			patent.notes(),
			patent.madridSystem(),
			patent.createdByUsername(),
			patent.dateOfCreation(),
			patent.currentStatus()
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
