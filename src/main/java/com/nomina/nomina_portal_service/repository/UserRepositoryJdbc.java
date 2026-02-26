package com.nomina.nomina_portal_service.repository;

import com.nomina.nomina_portal_service.model.User;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepositoryJdbc {
	private static final RowMapper<User> USER_ROW_MAPPER = new RowMapper<>() {
		@Override
		public User mapRow(ResultSet rs, int rowNum) throws SQLException {
			User user = new User();
			user.setId(rs.getLong("id"));
			user.setUsername(rs.getString("username"));
			user.setPasswordHash(rs.getString("password_hash"));
			user.setSuperUser(rs.getBoolean("is_super_user"));
			user.setAdminUser(rs.getBoolean("is_admin_user"));
			user.setActiveUser(rs.getBoolean("is_active_user"));
			return user;
		}
	};

	private final NamedParameterJdbcTemplate jdbcTemplate;

	public UserRepositoryJdbc(NamedParameterJdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public Optional<User> findByUsername(String username) {
		String sql = """
			SELECT id, username, password_hash, is_super_user, is_admin_user, is_active_user
			FROM users
			WHERE username = :username
			""";
		MapSqlParameterSource params = new MapSqlParameterSource("username", username);
		return jdbcTemplate.query(sql, params, USER_ROW_MAPPER).stream().findFirst();
	}

	public Optional<User> findById(long id) {
		String sql = """
			SELECT id, username, password_hash, is_super_user, is_admin_user, is_active_user
			FROM users
			WHERE id = :id
			""";
		MapSqlParameterSource params = new MapSqlParameterSource("id", id);
		return jdbcTemplate.query(sql, params, USER_ROW_MAPPER).stream().findFirst();
	}

	public boolean existsByUsername(String username) {
		String sql = "SELECT COUNT(1) FROM users WHERE username = :username";
		Map<String, Object> params = Map.of("username", username);
		Integer count = jdbcTemplate.queryForObject(sql, params, Integer.class);
		return count != null && count > 0;
	}

	public User insert(User user) {
		String sql = """
			INSERT INTO users (username, password_hash, is_super_user, is_admin_user, is_active_user)
			VALUES (:username, :password_hash, :is_super_user, :is_admin_user, :is_active_user)
			""";
		MapSqlParameterSource params = new MapSqlParameterSource()
			.addValue("username", user.getUsername())
			.addValue("password_hash", user.getPasswordHash())
			.addValue("is_super_user", user.isSuperUser())
			.addValue("is_admin_user", user.isAdminUser())
			.addValue("is_active_user", user.isActiveUser());
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(sql, params, keyHolder, new String[] {"id"});
		Number key = keyHolder.getKey();
		if (key != null) {
			user.setId(key.longValue());
		}
		return user;
	}
}
