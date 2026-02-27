package com.nomina.nomina_portal_service.repository;

import com.nomina.nomina_portal_service.model.Trademark;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class TrademarkRepositoryJdbc {
	private static final RowMapper<Trademark> TRADEMARK_ROW_MAPPER = DataClassRowMapper.newInstance(Trademark.class);
	private static final String SELECT_COLUMNS = """
		id, trademark_name, mark_image, mark_type, status, jurisdiction,
		application_number, application_date, registration_number, registration_date,
		owner_name, owner_address, classes, goods_services_text,
		priority_number, priority_date, priority_country,
		renewal_date, grace_period_end, opposition_deadline, proof_of_use_deadline, interim_deadline,
		last_action, legal_events, opposition, cancellation, litigation, license, assignment,
		responsible_attorney, representative, contact, notes,
		created_by_user, date_of_creation, current_status
		""";
	private static final String SELECT_COLUMNS_WITH_USERNAME = """
		t.id, t.trademark_name, t.mark_image, t.mark_type, t.status, t.jurisdiction,
		t.application_number, t.application_date, t.registration_number, t.registration_date,
		t.owner_name, t.owner_address, t.classes, t.goods_services_text,
		t.priority_number, t.priority_date, t.priority_country,
		t.renewal_date, t.grace_period_end, t.opposition_deadline, t.proof_of_use_deadline, t.interim_deadline,
		t.last_action, t.legal_events, t.opposition, t.cancellation, t.litigation, t.license, t.assignment,
		t.responsible_attorney, t.representative, t.contact, t.notes,
		t.created_by_user, u.username AS created_by_username, t.date_of_creation, t.current_status
		""";

	private final NamedParameterJdbcTemplate jdbcTemplate;

	public TrademarkRepositoryJdbc(NamedParameterJdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<Trademark> findAll() {
		String sql = """
			SELECT %s
			FROM trademarks t
			LEFT JOIN users u ON t.created_by_user = u.id
			ORDER BY t.date_of_creation DESC NULLS LAST, t.id
			""".formatted(SELECT_COLUMNS_WITH_USERNAME);
		return jdbcTemplate.query(sql, TRADEMARK_ROW_MAPPER);
	}

	public Optional<Trademark> findById(UUID id) {
		String sql = """
			SELECT %s
			FROM trademarks t
			LEFT JOIN users u ON t.created_by_user = u.id
			WHERE t.id = :id
			""".formatted(SELECT_COLUMNS_WITH_USERNAME);
		return jdbcTemplate.query(sql, new MapSqlParameterSource("id", id), TRADEMARK_ROW_MAPPER)
			.stream()
			.findFirst();
	}

	public Trademark insert(Trademark trademark) {
		String sql = """
			INSERT INTO trademarks (
				trademark_name, mark_image, mark_type, status, jurisdiction,
				application_number, application_date, registration_number, registration_date,
				owner_name, owner_address, classes, goods_services_text,
				priority_number, priority_date, priority_country,
				renewal_date, grace_period_end, opposition_deadline, proof_of_use_deadline, interim_deadline,
				last_action, legal_events, opposition, cancellation, litigation, license, assignment,
				responsible_attorney, representative, contact, notes,
				created_by_user, date_of_creation, current_status
			)
			VALUES (
				:trademarkName, :markImage, :markType, :status, :jurisdiction,
				:applicationNumber, :applicationDate, :registrationNumber, :registrationDate,
				:ownerName, :ownerAddress, :classes, :goodsServicesText,
				:priorityNumber, :priorityDate, :priorityCountry,
				:renewalDate, :gracePeriodEnd, :oppositionDeadline, :proofOfUseDeadline, :interimDeadline,
				:lastAction, :legalEvents, :opposition, :cancellation, :litigation, :license, :assignment,
				:responsibleAttorney, :representative, :contact, :notes,
				:createdByUser, :dateOfCreation, :currentStatus
			)
			RETURNING id
			""";
		UUID id = jdbcTemplate.queryForObject(sql, toParams(trademark, false), UUID.class);
		return findById(id).orElseThrow();
	}

	public Optional<Trademark> update(Trademark trademark) {
		String sql = """
			UPDATE trademarks
			SET
				trademark_name = :trademarkName,
				mark_image = :markImage,
				mark_type = :markType,
				status = :status,
				jurisdiction = :jurisdiction,
				application_number = :applicationNumber,
				application_date = :applicationDate,
				registration_number = :registrationNumber,
				registration_date = :registrationDate,
				owner_name = :ownerName,
				owner_address = :ownerAddress,
				classes = :classes,
				goods_services_text = :goodsServicesText,
				priority_number = :priorityNumber,
				priority_date = :priorityDate,
				priority_country = :priorityCountry,
				renewal_date = :renewalDate,
				grace_period_end = :gracePeriodEnd,
				opposition_deadline = :oppositionDeadline,
				proof_of_use_deadline = :proofOfUseDeadline,
				interim_deadline = :interimDeadline,
				last_action = :lastAction,
				legal_events = :legalEvents,
				opposition = :opposition,
				cancellation = :cancellation,
				litigation = :litigation,
				license = :license,
				assignment = :assignment,
				responsible_attorney = :responsibleAttorney,
				representative = :representative,
				contact = :contact,
				notes = :notes,
				current_status = :currentStatus
			WHERE id = :id
			""";

		int updated = jdbcTemplate.update(sql, toParams(trademark, true));
		if (updated == 0) {
			return Optional.empty();
		}
		return findById(trademark.id());
	}

	public boolean deleteById(UUID id) {
		String sql = "DELETE FROM trademarks WHERE id = :id";
		return jdbcTemplate.update(sql, new MapSqlParameterSource("id", id)) > 0;
	}

	public int deleteByIds(List<UUID> ids) {
		String sql = "DELETE FROM trademarks WHERE id IN (:ids)";
		return jdbcTemplate.update(sql, new MapSqlParameterSource("ids", ids));
	}

	private MapSqlParameterSource toParams(Trademark trademark, boolean includeId) {
		MapSqlParameterSource params = new MapSqlParameterSource()
			.addValue("trademarkName", trademark.trademarkName())
			.addValue("markImage", trademark.markImage())
			.addValue("markType", trademark.markType())
			.addValue("status", trademark.status())
			.addValue("jurisdiction", trademark.jurisdiction())
			.addValue("applicationNumber", trademark.applicationNumber())
			.addValue("applicationDate", trademark.applicationDate())
			.addValue("registrationNumber", trademark.registrationNumber())
			.addValue("registrationDate", trademark.registrationDate())
			.addValue("ownerName", trademark.ownerName())
			.addValue("ownerAddress", trademark.ownerAddress())
			.addValue("classes", trademark.classes())
			.addValue("goodsServicesText", trademark.goodsServicesText())
			.addValue("priorityNumber", trademark.priorityNumber())
			.addValue("priorityDate", trademark.priorityDate())
			.addValue("priorityCountry", trademark.priorityCountry())
			.addValue("renewalDate", trademark.renewalDate())
			.addValue("gracePeriodEnd", trademark.gracePeriodEnd())
			.addValue("oppositionDeadline", trademark.oppositionDeadline())
			.addValue("proofOfUseDeadline", trademark.proofOfUseDeadline())
			.addValue("interimDeadline", trademark.interimDeadline())
			.addValue("lastAction", trademark.lastAction())
			.addValue("legalEvents", trademark.legalEvents())
			.addValue("opposition", trademark.opposition())
			.addValue("cancellation", trademark.cancellation())
			.addValue("litigation", trademark.litigation())
			.addValue("license", trademark.license())
			.addValue("assignment", trademark.assignment())
			.addValue("responsibleAttorney", trademark.responsibleAttorney())
			.addValue("representative", trademark.representative())
			.addValue("contact", trademark.contact())
			.addValue("notes", trademark.notes())
			.addValue("createdByUser", trademark.createdByUser())
			.addValue("dateOfCreation", trademark.dateOfCreation())
			.addValue("currentStatus", trademark.currentStatus());

		if (includeId) {
			params.addValue("id", trademark.id());
		}

		return params;
	}
}
