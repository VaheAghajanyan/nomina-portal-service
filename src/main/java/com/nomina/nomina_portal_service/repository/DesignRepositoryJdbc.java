package com.nomina.nomina_portal_service.repository;

import com.nomina.nomina_portal_service.model.Design;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class DesignRepositoryJdbc {
	private static final RowMapper<Design> DESIGN_ROW_MAPPER = DataClassRowMapper.newInstance(Design.class);
	private static final String SELECT_COLUMNS = """
		id, design_title, design_image, jurisdiction,
		application_number, filing_date,
		registration_number, registration_date,
		status, locarno_class,
		priority_number, priority_date, priority_country,
		owner, designer, applicant,
		product_indication, color_claim,
		renewal_periods, grace_period_end, publication_deferment_end,
		product_line, brand, license,
		responsible_attorney, representative, contact,
		notes,
		created_by_user, date_of_creation,
		current_status
		""";
	private static final String SELECT_COLUMNS_WITH_USERNAME = """
		d.id, d.design_title, d.design_image, d.jurisdiction,
		d.application_number, d.filing_date,
		d.registration_number, d.registration_date,
		d.status, d.locarno_class,
		d.priority_number, d.priority_date, d.priority_country,
		d.owner, d.designer, d.applicant,
		d.product_indication, d.color_claim,
		d.renewal_periods, d.grace_period_end, d.publication_deferment_end,
		d.product_line, d.brand, d.license,
		d.responsible_attorney, d.representative, d.contact,
		d.notes,
		d.created_by_user, u.username AS created_by_username, d.date_of_creation,
		d.current_status
		""";

	private final NamedParameterJdbcTemplate jdbcTemplate;

	public DesignRepositoryJdbc(NamedParameterJdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<Design> findAll() {
		String sql = """
			SELECT %s
			FROM designs d
			LEFT JOIN users u ON d.created_by_user = u.id
			ORDER BY d.date_of_creation DESC NULLS LAST, d.id
			""".formatted(SELECT_COLUMNS_WITH_USERNAME);
		return jdbcTemplate.query(sql, DESIGN_ROW_MAPPER);
	}

	public Optional<Design> findById(UUID id) {
		String sql = """
			SELECT %s
			FROM designs d
			LEFT JOIN users u ON d.created_by_user = u.id
			WHERE d.id = :id
			""".formatted(SELECT_COLUMNS_WITH_USERNAME);
		return jdbcTemplate.query(sql, new MapSqlParameterSource("id", id), DESIGN_ROW_MAPPER)
			.stream()
			.findFirst();
	}

	public Design insert(Design design) {
		String sql = """
			INSERT INTO designs (
				design_title, design_image, jurisdiction,
				application_number, filing_date,
				registration_number, registration_date,
				status, locarno_class,
				priority_number, priority_date, priority_country,
				owner, designer, applicant,
				product_indication, color_claim,
				renewal_periods, grace_period_end, publication_deferment_end,
				product_line, brand, license,
				responsible_attorney, representative, contact,
				notes,
				created_by_user, date_of_creation,
				current_status
			)
			VALUES (
				:designTitle, :designImage, :jurisdiction,
				:applicationNumber, :filingDate,
				:registrationNumber, :registrationDate,
				:status, :locarnoClass,
				:priorityNumber, :priorityDate, :priorityCountry,
				:owner, :designer, :applicant,
				:productIndication, :colorClaim,
				:renewalPeriods, :gracePeriodEnd, :publicationDefermentEnd,
				:productLine, :brand, :license,
				:responsibleAttorney, :representative, :contact,
				:notes,
				:createdByUser, :dateOfCreation,
				:currentStatus
			)
			RETURNING id
			""";
		UUID id = jdbcTemplate.queryForObject(sql, toParams(design, false), UUID.class);
		return findById(id).orElseThrow();
	}

	public Optional<Design> update(Design design) {
		String sql = """
			UPDATE designs
			SET
				design_title = :designTitle,
				design_image = :designImage,
				jurisdiction = :jurisdiction,
				application_number = :applicationNumber,
				filing_date = :filingDate,
				registration_number = :registrationNumber,
				registration_date = :registrationDate,
				status = :status,
				locarno_class = :locarnoClass,
				priority_number = :priorityNumber,
				priority_date = :priorityDate,
				priority_country = :priorityCountry,
				owner = :owner,
				designer = :designer,
				applicant = :applicant,
				product_indication = :productIndication,
				color_claim = :colorClaim,
				renewal_periods = :renewalPeriods,
				grace_period_end = :gracePeriodEnd,
				publication_deferment_end = :publicationDefermentEnd,
				product_line = :productLine,
				brand = :brand,
				license = :license,
				responsible_attorney = :responsibleAttorney,
				representative = :representative,
				contact = :contact,
				notes = :notes,
				current_status = :currentStatus
			WHERE id = :id
			""";

		int updated = jdbcTemplate.update(sql, toParams(design, true));
		if (updated == 0) {
			return Optional.empty();
		}
		return findById(design.id());
	}

	public boolean deleteById(UUID id) {
		String sql = "DELETE FROM designs WHERE id = :id";
		return jdbcTemplate.update(sql, new MapSqlParameterSource("id", id)) > 0;
	}

	public int deleteByIds(List<UUID> ids) {
		String sql = "DELETE FROM designs WHERE id IN (:ids)";
		return jdbcTemplate.update(sql, new MapSqlParameterSource("ids", ids));
	}

	private MapSqlParameterSource toParams(Design design, boolean includeId) {
		MapSqlParameterSource params = new MapSqlParameterSource()
			.addValue("designTitle", design.designTitle())
			.addValue("designImage", design.designImage())
			.addValue("jurisdiction", design.jurisdiction())
			.addValue("applicationNumber", design.applicationNumber())
			.addValue("filingDate", design.filingDate())
			.addValue("registrationNumber", design.registrationNumber())
			.addValue("registrationDate", design.registrationDate())
			.addValue("status", design.status())
			.addValue("locarnoClass", design.locarnoClass())
			.addValue("priorityNumber", design.priorityNumber())
			.addValue("priorityDate", design.priorityDate())
			.addValue("priorityCountry", design.priorityCountry())
			.addValue("owner", design.owner())
			.addValue("designer", design.designer())
			.addValue("applicant", design.applicant())
			.addValue("productIndication", design.productIndication())
			.addValue("colorClaim", design.colorClaim())
			.addValue("renewalPeriods", design.renewalPeriods())
			.addValue("gracePeriodEnd", design.gracePeriodEnd())
			.addValue("publicationDefermentEnd", design.publicationDefermentEnd())
			.addValue("productLine", design.productLine())
			.addValue("brand", design.brand())
			.addValue("license", design.license())
			.addValue("responsibleAttorney", design.responsibleAttorney())
			.addValue("representative", design.representative())
			.addValue("contact", design.contact())
			.addValue("notes", design.notes())
			.addValue("createdByUser", design.createdByUser())
			.addValue("dateOfCreation", design.dateOfCreation())
			.addValue("currentStatus", design.currentStatus());

		if (includeId) {
			params.addValue("id", design.id());
		}

		return params;
	}
}
