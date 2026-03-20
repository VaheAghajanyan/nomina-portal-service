package com.nomina.nomina_portal_service.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomina.nomina_portal_service.model.MadridSystemItem;
import com.nomina.nomina_portal_service.model.Trademark;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class TrademarkRepositoryJdbc {
	private static final TypeReference<List<MadridSystemItem>> MADRID_SYSTEM_LIST_TYPE = new TypeReference<>() {
	};
	private static final String SELECT_COLUMNS = """
		id, trademark_name, mark_image, mark_type, status, jurisdiction,
		application_number, application_date, registration_number, registration_date,
		owner_name, owner_address, classes, goods_services_text,
		publication_date, priority_date, priority_country,
		renewal_date, grace_period_end, opposition_deadline, proof_of_use_deadline, action_deadline,
		last_action, basic, opposition, cancellation, litigation, license, assignment,
		responsible_attorney, representative, contact, notes, madrid_system,
		created_by_user, date_of_creation, needed_action
		""";
	private static final String SELECT_COLUMNS_WITH_USERNAME = """
		t.id, t.trademark_name, t.mark_image, t.mark_type, t.status, t.jurisdiction,
		t.application_number, t.application_date, t.registration_number, t.registration_date,
		t.owner_name, t.owner_address, t.classes, t.goods_services_text,
		t.publication_date, t.priority_date, t.priority_country,
		t.renewal_date, t.grace_period_end, t.opposition_deadline, t.proof_of_use_deadline, t.action_deadline,
		t.last_action, t.basic, t.opposition, t.cancellation, t.litigation, t.license, t.assignment,
		t.responsible_attorney, t.representative, t.contact, t.notes, t.madrid_system,
		t.created_by_user, u.username AS created_by_username, t.date_of_creation, t.needed_action
		""";

	private final NamedParameterJdbcTemplate jdbcTemplate;
	private final ObjectMapper objectMapper;

	public TrademarkRepositoryJdbc(NamedParameterJdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
		this.jdbcTemplate = jdbcTemplate;
		this.objectMapper = objectMapper;
	}

	public List<Trademark> findAll() {
		String sql = """
			SELECT %s
			FROM trademarks t
			LEFT JOIN users u ON t.created_by_user = u.id
			ORDER BY t.date_of_creation DESC NULLS LAST, t.id
			""".formatted(SELECT_COLUMNS_WITH_USERNAME);
		return jdbcTemplate.query(sql, (rs, rowNum) -> mapRow(rs));
	}

	public List<Trademark> findLatest(int limit) {
		String sql = """
			SELECT %s
			FROM trademarks t
			LEFT JOIN users u ON t.created_by_user = u.id
			ORDER BY t.date_of_creation DESC NULLS LAST, t.id
			LIMIT :limit
			""".formatted(SELECT_COLUMNS_WITH_USERNAME);
		MapSqlParameterSource params = new MapSqlParameterSource("limit", limit);
		return jdbcTemplate.query(sql, params, (rs, rowNum) -> mapRow(rs));
	}

	public Optional<Trademark> findById(UUID id) {
		String sql = """
			SELECT %s
			FROM trademarks t
			LEFT JOIN users u ON t.created_by_user = u.id
			WHERE t.id = :id
			""".formatted(SELECT_COLUMNS_WITH_USERNAME);
		return jdbcTemplate.query(sql, new MapSqlParameterSource("id", id), (rs, rowNum) -> mapRow(rs))
			.stream()
			.findFirst();
	}

	public Trademark insert(Trademark trademark) {
		String sql = """
			INSERT INTO trademarks (
				trademark_name, mark_image, mark_type, status, jurisdiction,
				application_number, application_date, registration_number, registration_date,
				owner_name, owner_address, classes, goods_services_text,
				publication_date, priority_date, priority_country,
				renewal_date, grace_period_end, opposition_deadline, proof_of_use_deadline, action_deadline,
				last_action, basic, opposition, cancellation, litigation, license, assignment,
				responsible_attorney, representative, contact, notes, madrid_system,
				created_by_user, date_of_creation, needed_action
			)
			VALUES (
				:trademarkName, :markImage, :markType, :status, :jurisdiction,
				:applicationNumber, :applicationDate, :registrationNumber, :registrationDate,
				:ownerName, :ownerAddress, :classes, :goodsServicesText,
				:publicationDate, :priorityDate, :priorityCountry,
				:renewalDate, :gracePeriodEnd, :oppositionDeadline, :proofOfUseDeadline, :actionDeadline,
				:lastAction, :basic, :opposition, :cancellation, :litigation, :license, :assignment,
				:responsibleAttorney, :representative, :contact, :notes, CAST(:madridSystem AS jsonb),
				:createdByUser, :dateOfCreation, :neededAction
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
				publication_date = :publicationDate,
				priority_date = :priorityDate,
				priority_country = :priorityCountry,
				renewal_date = :renewalDate,
				grace_period_end = :gracePeriodEnd,
				opposition_deadline = :oppositionDeadline,
				proof_of_use_deadline = :proofOfUseDeadline,
				action_deadline = :actionDeadline,
				last_action = :lastAction,
				basic = :basic,
				opposition = :opposition,
				cancellation = :cancellation,
				litigation = :litigation,
				license = :license,
				assignment = :assignment,
				responsible_attorney = :responsibleAttorney,
				representative = :representative,
				contact = :contact,
				notes = :notes,
				madrid_system = CAST(:madridSystem AS jsonb),
				needed_action = :neededAction
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
			.addValue("publicationDate", trademark.publicationDate())
			.addValue("priorityDate", trademark.priorityDate())
			.addValue("priorityCountry", trademark.priorityCountry())
			.addValue("renewalDate", trademark.renewalDate())
			.addValue("gracePeriodEnd", trademark.gracePeriodEnd())
			.addValue("oppositionDeadline", trademark.oppositionDeadline())
			.addValue("proofOfUseDeadline", trademark.proofOfUseDeadline())
			.addValue("actionDeadline", trademark.actionDeadline())
			.addValue("lastAction", trademark.lastAction())
			.addValue("basic", trademark.basic())
			.addValue("opposition", trademark.opposition())
			.addValue("cancellation", trademark.cancellation())
			.addValue("litigation", trademark.litigation())
			.addValue("license", trademark.license())
			.addValue("assignment", trademark.assignment())
			.addValue("responsibleAttorney", trademark.responsibleAttorney())
			.addValue("representative", trademark.representative())
			.addValue("contact", trademark.contact())
			.addValue("notes", trademark.notes())
			.addValue("madridSystem", toJsonbValue(trademark.madridSystem()))
			.addValue("createdByUser", trademark.createdByUser())
			.addValue("dateOfCreation", trademark.dateOfCreation())
			.addValue("neededAction", trademark.neededAction());

		if (includeId) {
			params.addValue("id", trademark.id());
		}

		return params;
	}

	private Trademark mapRow(ResultSet rs) throws SQLException {
		return new Trademark(
			rs.getObject("id", UUID.class),
			rs.getString("trademark_name"),
			rs.getString("mark_image"),
			rs.getString("mark_type"),
			rs.getString("status"),
			rs.getString("jurisdiction"),
			rs.getString("application_number"),
			rs.getObject("application_date", java.time.LocalDate.class),
			rs.getString("registration_number"),
			rs.getObject("registration_date", java.time.LocalDate.class),
			rs.getString("owner_name"),
			rs.getString("owner_address"),
			rs.getString("classes"),
			rs.getString("goods_services_text"),
			rs.getString("publication_date"),
			rs.getObject("priority_date", java.time.LocalDate.class),
			rs.getString("priority_country"),
			rs.getObject("renewal_date", java.time.LocalDate.class),
			rs.getString("grace_period_end"),
			rs.getString("opposition_deadline"),
			rs.getString("proof_of_use_deadline"),
			rs.getString("action_deadline"),
			rs.getString("last_action"),
			rs.getString("basic"),
			rs.getString("opposition"),
			rs.getString("cancellation"),
			rs.getString("litigation"),
			rs.getString("license"),
			rs.getString("assignment"),
			rs.getString("responsible_attorney"),
			rs.getString("representative"),
			rs.getString("contact"),
			rs.getString("notes"),
			parseMadridSystem(rs.getString("madrid_system")),
			rs.getObject("created_by_user", Long.class),
			rs.getString("created_by_username"),
			rs.getObject("date_of_creation", java.time.LocalDate.class),
			rs.getString("needed_action")
		);
	}

	private String toJsonbValue(List<MadridSystemItem> madridSystem) {
		if (madridSystem == null) {
			return null;
		}
		try {
			return objectMapper.writeValueAsString(madridSystem);
		} catch (JsonProcessingException ex) {
			throw new IllegalStateException("Failed to serialize madridSystem.", ex);
		}
	}

	private List<MadridSystemItem> parseMadridSystem(String json) {
		if (json == null) {
			return null;
		}
		try {
			return objectMapper.readValue(json, MADRID_SYSTEM_LIST_TYPE);
		} catch (JsonProcessingException ex) {
			throw new IllegalStateException("Failed to parse madrid_system JSON.", ex);
		}
	}
}
