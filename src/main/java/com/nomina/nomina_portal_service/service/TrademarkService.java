package com.nomina.nomina_portal_service.service;

import com.nomina.nomina_portal_service.dto.TrademarkRequest;
import com.nomina.nomina_portal_service.dto.TrademarkResponse;
import com.nomina.nomina_portal_service.exception.NotFoundException;
import com.nomina.nomina_portal_service.exception.UnauthorizedException;
import com.nomina.nomina_portal_service.model.Design;
import com.nomina.nomina_portal_service.model.LatestAddedRecord;
import com.nomina.nomina_portal_service.model.Patent;
import com.nomina.nomina_portal_service.model.Trademark;
import com.nomina.nomina_portal_service.repository.DesignRepositoryJdbc;
import com.nomina.nomina_portal_service.repository.PatentRepositoryJdbc;
import com.nomina.nomina_portal_service.repository.TrademarkRepositoryJdbc;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class TrademarkService {
	private static final int LATEST_LIMIT = 10;
	private final TrademarkRepositoryJdbc trademarkRepository;
	private final PatentRepositoryJdbc patentRepository;
	private final DesignRepositoryJdbc designRepository;
	private final ImageStorageService imageStorageService;

	public TrademarkService(
		TrademarkRepositoryJdbc trademarkRepository,
		PatentRepositoryJdbc patentRepository,
		DesignRepositoryJdbc designRepository,
		ImageStorageService imageStorageService
	) {
		this.trademarkRepository = trademarkRepository;
		this.patentRepository = patentRepository;
		this.designRepository = designRepository;
		this.imageStorageService = imageStorageService;
	}

	public List<TrademarkResponse> getAll() {
		return trademarkRepository.findAll().stream()
			.map(this::toResponse)
			.toList();
	}

	public List<LatestAddedRecord> getLatestAdded() {
		List<LatestAddedRecord> combined = new ArrayList<>();
		combined.addAll(
			trademarkRepository.findLatest(LATEST_LIMIT).stream()
				.map(this::toLatestAddedRecord)
				.toList()
		);
		combined.addAll(
			patentRepository.findLatest(LATEST_LIMIT).stream()
				.map(this::toLatestAddedRecord)
				.toList()
		);
		combined.addAll(
			designRepository.findLatest(LATEST_LIMIT).stream()
				.map(this::toLatestAddedRecord)
				.toList()
		);

		return combined.stream()
			.sorted(
				Comparator
					.comparing(LatestAddedRecord::dateOfCreation, Comparator.nullsLast(Comparator.reverseOrder()))
					.thenComparing(LatestAddedRecord::id, Comparator.nullsLast(Comparator.reverseOrder()))
			)
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
			request.publicationDate(),
			request.priorityDate(),
			request.priorityCountry(),
			request.renewalDate(),
			request.gracePeriodEnd(),
			request.oppositionDeadline(),
			request.proofOfUseDeadline(),
			request.actionDeadline(),
			request.lastAction(),
			request.basic(),
			request.opposition(),
			request.cancellation(),
			request.litigation(),
			request.license(),
			request.assignment(),
			request.responsibleAttorney(),
			request.representative(),
			request.contact(),
			request.notes(),
			request.madridSystem(),
			createdByUser,
			null,
			dateOfCreation,
			request.neededAction()
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
			trademark.publicationDate(),
			trademark.priorityDate(),
			trademark.priorityCountry(),
			trademark.renewalDate(),
			trademark.gracePeriodEnd(),
			trademark.oppositionDeadline(),
			trademark.proofOfUseDeadline(),
			trademark.actionDeadline(),
			trademark.lastAction(),
			trademark.basic(),
			trademark.opposition(),
			trademark.cancellation(),
			trademark.litigation(),
			trademark.license(),
			trademark.assignment(),
			trademark.responsibleAttorney(),
			trademark.representative(),
			trademark.contact(),
			trademark.notes(),
			trademark.madridSystem(),
			trademark.createdByUsername(),
			trademark.dateOfCreation(),
			trademark.neededAction()
		);
	}

	private LatestAddedRecord toLatestAddedRecord(Trademark trademark) {
		return new LatestAddedRecord(
			trademark.id(),
			trademark.trademarkName(),
			trademark.applicationNumber(),
			trademark.markImage(),
			trademark.createdByUser(),
			trademark.dateOfCreation(),
			trademark.status(),
			trademark.madridSystem()
		);
	}

	private LatestAddedRecord toLatestAddedRecord(Patent patent) {
		return new LatestAddedRecord(
			patent.id(),
			patent.title(),
			patent.applicationNumber(),
			null,
			patent.createdByUser(),
			patent.dateOfCreation(),
			patent.status(),
			patent.madridSystem()
		);
	}

	private LatestAddedRecord toLatestAddedRecord(Design design) {
		return new LatestAddedRecord(
			design.id(),
			design.designTitle(),
			design.applicationNumber(),
			design.designImage(),
			design.createdByUser(),
			design.dateOfCreation(),
			design.status(),
			design.madridSystem()
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
