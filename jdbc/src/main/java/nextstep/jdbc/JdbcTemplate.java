package nextstep.jdbc;

import static nextstep.jdbc.Extractor.extractData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JdbcTemplate {

	private static final Logger log = LoggerFactory.getLogger(JdbcTemplate.class);

	private final DataSource dataSource;

	public JdbcTemplate(final DataSource dataSource) {
		this.dataSource = dataSource;
	}

	public <T> T queryForObject(final String sql, final Class<T> type, final Object... objects) {
		final StatementCallback<List<T>> statementCallback = statement -> extractData(type,
			statement.executeQuery());
		return JdbcTemplateUtils.singleResult(execute(sql, statementCallback, objects));
	}

	public int update(final String sql, final Object... objects) {
		final StatementCallback<Integer> statementCallback = PreparedStatement::executeUpdate;
		return execute(sql, statementCallback, objects);
	}

	private <T> T execute(final String sql, final StatementCallback<T> statementCallback, final Object... objects) {
		try (final Connection connection = dataSource.getConnection();
			 final PreparedStatement statement = connection.prepareStatement(sql)) {
			for (int i = 0; i < objects.length; i++) {
				statement.setObject(i + 1, objects[i]);
			}
			return statementCallback.doInStatement(statement);
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			throw new DataAccessException(e.getMessage(), e);
		}
	}

}
