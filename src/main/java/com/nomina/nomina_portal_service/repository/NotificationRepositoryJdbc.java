package com.nomina.nomina_portal_service.repository;

import com.nomina.nomina_portal_service.model.Notification;
import java.time.LocalDate;
import java.util.Map;
import java.util.List;
import java.util.Optional;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class NotificationRepositoryJdbc {
	private static final RowMapper<Notification> NOTIFICATION_ROW_MAPPER = DataClassRowMapper.newInstance(Notification.class);
	private final NamedParameterJdbcTemplate jdbcTemplate;

	public NotificationRepositoryJdbc(NamedParameterJdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<Notification> findPage(int limit, int offset) {
		String sql = """
			SELECT id, type, user_id, body, created_at, deadline_date, deadline_type, seen
			FROM notifications
			ORDER BY created_at DESC NULLS LAST, id DESC
			LIMIT :limit OFFSET :offset
			""";
		MapSqlParameterSource params = new MapSqlParameterSource()
			.addValue("limit", limit)
			.addValue("offset", offset);
		return jdbcTemplate.query(sql, params, NOTIFICATION_ROW_MAPPER);
	}

	public boolean existsDeadlineNotification(
		String type,
		long userId,
		String body,
		LocalDate createdAt,
		LocalDate deadlineDate,
		int deadlineType
	) {
		String sql = """
			SELECT COUNT(1)
			FROM notifications
			WHERE type = :type
			  AND user_id = :user_id
			  AND body = :body
			  AND created_at = :created_at
			  AND deadline_date = :deadline_date
			  AND deadline_type = :deadline_type
			""";
		Map<String, Object> params = Map.of(
			"type", type,
			"user_id", userId,
			"body", body,
			"created_at", createdAt,
			"deadline_date", deadlineDate,
			"deadline_type", deadlineType
		);
		Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
		return count != null && count > 0;
	}

	public Notification insert(Notification notification) {
		String sql = """
			INSERT INTO notifications (type, user_id, body, created_at, deadline_date, deadline_type, seen)
			VALUES (:type, :user_id, :body, :created_at, :deadline_date, :deadline_type, :seen)
			RETURNING id
			""";
		MapSqlParameterSource params = new MapSqlParameterSource()
			.addValue("type", notification.type())
			.addValue("user_id", notification.userId())
			.addValue("body", notification.body())
			.addValue("created_at", notification.createdAt())
			.addValue("deadline_date", notification.deadlineDate())
			.addValue("deadline_type", notification.deadlineType())
			.addValue("seen", notification.seen() != null ? notification.seen() : Boolean.FALSE);
		Long id = jdbcTemplate.queryForObject(sql, params, Long.class);
		return new Notification(
			id,
			notification.type(),
			notification.userId(),
			notification.body(),
			notification.createdAt(),
			notification.deadlineDate(),
			notification.deadlineType(),
			notification.seen() != null ? notification.seen() : Boolean.FALSE
		);
	}

	public Optional<Notification> updateSeen(long id, boolean seen) {
		String updateSql = "UPDATE notifications SET seen = :seen WHERE id = :id";
		MapSqlParameterSource updateParams = new MapSqlParameterSource()
			.addValue("id", id)
			.addValue("seen", seen);
		int updated = jdbcTemplate.update(updateSql, updateParams);
		if (updated == 0) {
			return Optional.empty();
		}

		String selectSql = """
			SELECT id, type, user_id, body, created_at, deadline_date, deadline_type, seen
			FROM notifications
			WHERE id = :id
			""";
		return jdbcTemplate.query(selectSql, new MapSqlParameterSource("id", id), NOTIFICATION_ROW_MAPPER)
			.stream()
			.findFirst();
	}
}
