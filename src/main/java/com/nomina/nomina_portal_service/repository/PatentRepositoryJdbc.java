package com.nomina.nomina_portal_service.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nomina.nomina_portal_service.model.MadridSystemItem;
import com.nomina.nomina_portal_service.model.Patent;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class PatentRepositoryJdbc {
	private static final TypeReference<List<MadridSystemItem>> MADRID_SYSTEM_LIST_TYPE = new TypeReference<>() {
	};
	private static final String SELECT_COLUMNS = """
		id, title, patent_type, jurisdiction,
		application_number, filing_date,
		publication_number, publication_date,
		patent_number, grant_date,
		status,
		priority_number, priority_date, priority_country,
		inventors, inventor_country, applicant, assignee,
		ipc_cpc,
		examination_request_deadline, office_action_deadline, grant_fee_deadline, validation_deadlines, annuity_due_dates, lapse_date,
		license,
		responsible_attorney, representative, contact,
		notes, madrid_system,
		created_by_user, date_of_creation,
		current_status
		""";
	private static final String SELECT_COLUMNS_WITH_USERNAME = """
		p.id, p.title, p.patent_type, p.jurisdiction,
		p.application_number, p.filing_date,
		p.publication_number, p.publication_date,
		p.patent_number, p.grant_date,
		p.status,
		p.priority_number, p.priority_date, p.priority_country,
		p.inventors, p.inventor_country, p.applicant, p.assignee,
		p.ipc_cpc,
		p.examination_request_deadline, p.office_action_deadline, p.grant_fee_deadline, p.validation_deadlines, p.annuity_due_dates, p.lapse_date,
		p.license,
		p.responsible_attorney, p.representative, p.contact,
		p.notes, p.madrid_system,
		p.created_by_user, u.username AS created_by_username, p.date_of_creation,
		p.current_status
		""";

	private final NamedParameterJdbcTemplate jdbcTemplate;
	private final ObjectMapper objectMapper;

	public PatentRepositoryJdbc(NamedParameterJdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
		this.jdbcTemplate = jdbcTemplate;
		this.objectMapper = objectMapper;
	}

	public List<Patent> findAll() {
		String sql = """
			SELECT %s
			FROM patents p
			LEFT JOIN users u ON p.created_by_user = u.id
			ORDER BY p.date_of_creation DESC NULLS LAST, p.id
			""".formatted(SELECT_COLUMNS_WITH_USERNAME);
		return jdbcTemplate.query(sql, (rs, rowNum) -> mapRow(rs));
	}

	public List<Patent> findLatest(int limit) {
		String sql = """
			SELECT %s
			FROM patents p
			LEFT JOIN users u ON p.created_by_user = u.id
			ORDER BY p.date_of_creation DESC NULLS LAST, p.id
			LIMIT :limit
			""".formatted(SELECT_COLUMNS_WITH_USERNAME);
		MapSqlParameterSource params = new MapSqlParameterSource("limit", limit);
		return jdbcTemplate.query(sql, params, (rs, rowNum) -> mapRow(rs));
	}

	public Optional<Patent> findById(UUID id) {
		String sql = """
			SELECT %s
			FROM patents p
			LEFT JOIN users u ON p.created_by_user = u.id
			WHERE p.id = :id
			""".formatted(SELECT_COLUMNS_WITH_USERNAME);
		return jdbcTemplate.query(sql, new MapSqlParameterSource("id", id), (rs, rowNum) -> mapRow(rs))
			.stream()
			.findFirst();
	}

	public Patent insert(Patent patent) {
		String sql = """
			INSERT INTO patents (
				title, patent_type, jurisdiction,
				application_number, filing_date,
				publication_number, publication_date,
				patent_number, grant_date,
				status,
				priority_number, priority_date, priority_country,
				inventors, inventor_country, applicant, assignee,
				ipc_cpc,
				examination_request_deadline, office_action_deadline, grant_fee_deadline, validation_deadlines, annuity_due_dates, lapse_date,
				license,
				responsible_attorney, representative, contact,
				notes, madrid_system,
				created_by_user, date_of_creation,
				current_status
			)
			VALUES (
				:title, :patentType, :jurisdiction,
				:applicationNumber, :filingDate,
				:publicationNumber, :publicationDate,
				:patentNumber, :grantDate,
				:status,
				:priorityNumber, :priorityDate, :priorityCountry,
				:inventors, :inventorCountry, :applicant, :assignee,
				:ipcCpc,
				:examinationRequestDeadline, :officeActionDeadline, :grantFeeDeadline, :validationDeadlines, :annuityDueDates, :lapseDate,
				:license,
				:responsibleAttorney, :representative, :contact,
				:notes, CAST(:madridSystem AS jsonb),
				:createdByUser, :dateOfCreation,
				:currentStatus
			)
			RETURNING id
			""";
		UUID id = jdbcTemplate.queryForObject(sql, toParams(patent, false), UUID.class);
		return findById(id).orElseThrow();
	}

	public Optional<Patent> update(Patent patent) {
		String sql = """
			UPDATE patents
			SET
				title = :title,
				patent_type = :patentType,
				jurisdiction = :jurisdiction,
				application_number = :applicationNumber,
				filing_date = :filingDate,
				publication_number = :publicationNumber,
				publication_date = :publicationDate,
				patent_number = :patentNumber,
				grant_date = :grantDate,
				status = :status,
				priority_number = :priorityNumber,
				priority_date = :priorityDate,
				priority_country = :priorityCountry,
				inventors = :inventors,
				inventor_country = :inventorCountry,
				applicant = :applicant,
				assignee = :assignee,
				ipc_cpc = :ipcCpc,
				examination_request_deadline = :examinationRequestDeadline,
				office_action_deadline = :officeActionDeadline,
				grant_fee_deadline = :grantFeeDeadline,
				validation_deadlines = :validationDeadlines,
				annuity_due_dates = :annuityDueDates,
				lapse_date = :lapseDate,
				license = :license,
				responsible_attorney = :responsibleAttorney,
				representative = :representative,
				contact = :contact,
				notes = :notes,
				madrid_system = CAST(:madridSystem AS jsonb),
				current_status = :currentStatus
			WHERE id = :id
			""";

		int updated = jdbcTemplate.update(sql, toParams(patent, true));
		if (updated == 0) {
			return Optional.empty();
		}
		return findById(patent.id());
	}

	public boolean deleteById(UUID id) {
		String sql = "DELETE FROM patents WHERE id = :id";
		return jdbcTemplate.update(sql, new MapSqlParameterSource("id", id)) > 0;
	}

	public int deleteByIds(List<UUID> ids) {
		String sql = "DELETE FROM patents WHERE id IN (:ids)";
		return jdbcTemplate.update(sql, new MapSqlParameterSource("ids", ids));
	}

	private MapSqlParameterSource toParams(Patent patent, boolean includeId) {
		MapSqlParameterSource params = new MapSqlParameterSource()
			.addValue("title", patent.title())
			.addValue("patentType", patent.patentType())
			.addValue("jurisdiction", patent.jurisdiction())
			.addValue("applicationNumber", patent.applicationNumber())
			.addValue("filingDate", patent.filingDate())
			.addValue("publicationNumber", patent.publicationNumber())
			.addValue("publicationDate", patent.publicationDate())
			.addValue("patentNumber", patent.patentNumber())
			.addValue("grantDate", patent.grantDate())
			.addValue("status", patent.status())
			.addValue("priorityNumber", patent.priorityNumber())
			.addValue("priorityDate", patent.priorityDate())
			.addValue("priorityCountry", patent.priorityCountry())
			.addValue("inventors", patent.inventors())
			.addValue("inventorCountry", patent.inventorCountry())
			.addValue("applicant", patent.applicant())
			.addValue("assignee", patent.assignee())
			.addValue("ipcCpc", patent.ipcCpc())
			.addValue("examinationRequestDeadline", patent.examinationRequestDeadline())
			.addValue("officeActionDeadline", patent.officeActionDeadline())
			.addValue("grantFeeDeadline", patent.grantFeeDeadline())
			.addValue("validationDeadlines", patent.validationDeadlines())
			.addValue("annuityDueDates", patent.annuityDueDates())
			.addValue("lapseDate", patent.lapseDate())
			.addValue("license", patent.license())
			.addValue("responsibleAttorney", patent.responsibleAttorney())
			.addValue("representative", patent.representative())
			.addValue("contact", patent.contact())
			.addValue("notes", patent.notes())
			.addValue("madridSystem", toJsonbValue(patent.madridSystem()))
			.addValue("createdByUser", patent.createdByUser())
			.addValue("dateOfCreation", patent.dateOfCreation())
			.addValue("currentStatus", patent.currentStatus());

		if (includeId) {
			params.addValue("id", patent.id());
		}

		return params;
	}

	private Patent mapRow(ResultSet rs) throws SQLException {
		return new Patent(
			rs.getObject("id", UUID.class),
			rs.getString("title"),
			rs.getString("patent_type"),
			rs.getString("jurisdiction"),
			rs.getString("application_number"),
			rs.getObject("filing_date", java.time.LocalDate.class),
			rs.getString("publication_number"),
			rs.getObject("publication_date", java.time.LocalDate.class),
			rs.getString("patent_number"),
			rs.getObject("grant_date", java.time.LocalDate.class),
			rs.getString("status"),
			rs.getString("priority_number"),
			rs.getObject("priority_date", java.time.LocalDate.class),
			rs.getString("priority_country"),
			rs.getString("inventors"),
			rs.getString("inventor_country"),
			rs.getString("applicant"),
			rs.getString("assignee"),
			rs.getString("ipc_cpc"),
			rs.getObject("examination_request_deadline", java.time.LocalDate.class),
			rs.getObject("office_action_deadline", java.time.LocalDate.class),
			rs.getObject("grant_fee_deadline", java.time.LocalDate.class),
			rs.getObject("validation_deadlines", java.time.LocalDate.class),
			rs.getObject("annuity_due_dates", java.time.LocalDate.class),
			rs.getObject("lapse_date", java.time.LocalDate.class),
			rs.getString("license"),
			rs.getString("responsible_attorney"),
			rs.getString("representative"),
			rs.getString("contact"),
			rs.getString("notes"),
			parseMadridSystem(rs.getString("madrid_system")),
			rs.getObject("created_by_user", Long.class),
			rs.getString("created_by_username"),
			rs.getObject("date_of_creation", java.time.LocalDate.class),
			rs.getString("current_status")
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
