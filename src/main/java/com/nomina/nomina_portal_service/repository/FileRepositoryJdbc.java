package com.nomina.nomina_portal_service.repository;

import com.nomina.nomina_portal_service.model.StoredFile;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.DataClassRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class FileRepositoryJdbc {
	private static final RowMapper<StoredFile> STORED_FILE_ROW_MAPPER = DataClassRowMapper.newInstance(StoredFile.class);
	private final NamedParameterJdbcTemplate jdbcTemplate;

	public FileRepositoryJdbc(NamedParameterJdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<StoredFile> findById(UUID id) {
		String sql = """
			SELECT id, file_name, file_path
			FROM files
			WHERE id = :id
			ORDER BY file_name
			""";
		return jdbcTemplate.query(sql, new MapSqlParameterSource("id", id), STORED_FILE_ROW_MAPPER);
	}

	public void insert(StoredFile file) {
		String sql = """
			INSERT INTO files (id, file_name, file_path)
			VALUES (:id, :fileName, :filePath)
			""";
		MapSqlParameterSource params = new MapSqlParameterSource()
			.addValue("id", file.id())
			.addValue("fileName", file.fileName())
			.addValue("filePath", file.filePath());
		jdbcTemplate.update(sql, params);
	}

	public int deleteById(UUID id) {
		String sql = "DELETE FROM files WHERE id = :id";
		return jdbcTemplate.update(sql, new MapSqlParameterSource("id", id));
	}

	public int deleteByIds(List<UUID> ids) {
		if (ids == null || ids.isEmpty()) {
			return 0;
		}
		String sql = "DELETE FROM files WHERE id IN (:ids)";
		return jdbcTemplate.update(sql, new MapSqlParameterSource("ids", ids));
	}
}
