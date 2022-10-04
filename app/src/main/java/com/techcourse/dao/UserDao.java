package com.techcourse.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.techcourse.domain.User;

import nextstep.jdbc.JdbcTemplate;

public class UserDao {

	private static final Logger log = LoggerFactory.getLogger(UserDao.class);

	private final JdbcTemplate jdbcTemplate;

	public UserDao(final JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public UserDao(final DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	public void insert(final User user) {
		final var sql = "insert into users (account, password, email) values (?, ?, ?)";

		Connection conn = null;
		PreparedStatement pstmt = null;
		try {
			conn = jdbcTemplate.getConnection();
			pstmt = conn.prepareStatement(sql);

			log.debug("query : {}", sql);

			pstmt.setString(1, user.getAccount());
			pstmt.setString(2, user.getPassword());
			pstmt.setString(3, user.getEmail());
			pstmt.executeUpdate();
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		} finally {
			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (SQLException ignored) {
			}

			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException ignored) {
			}
		}
	}

	public void update(final User user) {
		final var sql = "UPDATE users SET account = ?, password = ?, email = ? WHERE id = ?";
		jdbcTemplate.execute(sql, user.getAccount(), user.getPassword(), user.getEmail(), user.getId());
	}

	public List<User> findAll() {
		final var sql = "SELECT * FROM users";
		return jdbcTemplate.query(sql, User.class);
	}

	public User findById(final Long id) {
		final var sql = "select id, account, password, email from users where id = ?";

		Connection conn = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			conn = jdbcTemplate.getConnection();
			pstmt = conn.prepareStatement(sql);
			pstmt.setLong(1, id);
			rs = pstmt.executeQuery();

			log.debug("query : {}", sql);

			if (rs.next()) {
				return new User(
					rs.getLong(1),
					rs.getString(2),
					rs.getString(3),
					rs.getString(4));
			}
			return null;
		} catch (SQLException e) {
			log.error(e.getMessage(), e);
			throw new RuntimeException(e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
			} catch (SQLException ignored) {
			}

			try {
				if (pstmt != null) {
					pstmt.close();
				}
			} catch (SQLException ignored) {
			}

			try {
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException ignored) {
			}
		}
	}

	public User findByAccount(final String account) {
		final var sql = "SELECT * FROM users WHERE account = ?";
		return jdbcTemplate.queryForObject(sql, User.class, account);
	}
}
