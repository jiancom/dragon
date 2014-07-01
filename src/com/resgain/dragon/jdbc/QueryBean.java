package com.resgain.dragon.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.resgain.dragon.exception.KnowException;

public class QueryBean
{
	private static Logger logger = LoggerFactory.getLogger(QueryBean.class);

	protected Connection conn;

	public QueryBean(Connection conn) {
		this.conn = conn;
		try {
			this.conn.setAutoCommit(false);
		} catch (SQLException e) {
			logger.error("设置链接为非自动提交失败。", e);
		}
	}

	// 去掉了public 避免调用者不能手动提交
	void submit() throws Exception {
		if (conn != null && !conn.isClosed() && !conn.getAutoCommit())
			conn.commit();
	}

	// 去掉了public 调用者不能手动回滚
	void rollback() {
		try {
			if (!conn.getAutoCommit())
				conn.rollback();
		} catch (Exception e) {
			logger.error("提交失败{}", e);
		}
	}

	public int execSql(String sql, Object... param) {
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(sql);
			logger.debug("执行SQL语句{}相关参数{}", sql, param);
			setParam(stmt, param);
			int ret = stmt.executeUpdate();
			return ret;
		} catch (Exception e) {
			logger.error("执行SQL:{}发生异常", sql, e);
			throw new KnowException(e.getMessage());
		} finally {
			clearRS(stmt, null);
		}
	}

	public <T extends Object> T query(final String sql, final Object param[], final JdbcCallback<T> handler) {
		PreparedStatement stmt = null;
		ResultSet result = null;
		try {
			stmt = conn.prepareStatement(sql);
			logger.debug("执行SQL语句{}相关参数{}", sql, param);
			setParam(stmt, param);
			return handler.processRow(stmt.executeQuery());
		} catch (Exception e) {
			logger.error("执行SQL:{}发生异常", sql, e);
			throw new KnowException(e.getMessage());
		} finally {
			clearRS(stmt, result);
		}
	}

	public List<Map<String, Object>> query(String lsSql, Object... param) {
		return (List<Map<String, Object>>) query(lsSql, param, new JdbcCallback<List<Map<String, Object>>>() {
			public List<Map<String, Object>> processRow(ResultSet rs) throws Exception {
				List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
				while (rs.next()) {
					ResultSetMetaData rsmd = rs.getMetaData();
					Map<String, Object> map = new HashMap<String, Object>();
					for (int col = 1; col <= rsmd.getColumnCount(); col++) {
						map.put(rsmd.getColumnLabel(col), rs.getObject(col));
					}
					ret.add(map);
				}
				return ret;
			}
		});
	}

	public Map<String, Object> getMap(String lsSql, Object... param) {
		List<Map<String, Object>> tmp = query(lsSql, param);
		return tmp.size() > 0 ? tmp.get(0) : null;
	}

	public int getCount(String sql, Object... param) {
		Map<String, Object> tmp = getMap(getCountSQL(sql), param);
		int rows = (tmp == null ? 0 : new Integer(tmp.get("T_COUNT").toString()).intValue());
		return rows;
	}

	public void execBatch(String piSql, List<Object[]> piData) {
		PreparedStatement stmt = null;
		try {
			stmt = conn.prepareStatement(piSql);
			for (int i = 0; i < piData.size(); i++) {
				setParam(stmt, piData.get(i));
				stmt.addBatch();
			}
			stmt.executeBatch();
		} catch (Exception e) {
			logger.error("批量数据更新发生异常", e);
			throw new KnowException(e.getMessage());
		} finally {
			clearRS(stmt, null);
		}
	}

	final void endQuery() {
		try {
			if (conn != null) {
				conn.close();
				conn = null;
			}
		} catch (Exception exception) {
			logger.error("关闭链接发生异常", exception);
		}
	}

	private void clearRS(PreparedStatement stmt, ResultSet rs) {
		try {
			if (rs != null) {
				rs.close();
				rs = null;
			}
			if (stmt != null) {
				stmt.close();
				stmt = null;
			}
		} catch (Exception e) {
			logger.error("清除rs/stmt发生异常", e);
		}
	}

	private void setParam(PreparedStatement stmt, Object... param) throws Exception {
		if (param != null) {
			for (int i = 0; i < param.length; i++) {
				if (param[i] instanceof Date)
					stmt.setTimestamp(i + 1, new Timestamp(((Date) param[i]).getTime()));
				else if (param[i] instanceof Integer)
					stmt.setInt(i + 1, ((Integer) param[i]).intValue());
				else if (param[i] instanceof Long)
					stmt.setLong(i + 1, ((Long) param[i]).longValue());
				else if (param[i] instanceof Double)
					stmt.setDouble(i + 1, ((Double) param[i]).doubleValue());
				else if (param[i] instanceof Boolean)
					stmt.setInt(i + 1, ((Boolean) param[i]) ? 1 : 0);
				else if (param[i] == null)
					stmt.setString(i + 1, null);
				else
					stmt.setString(i + 1, param[i].toString().trim());
			}
		}
	}

	private String getCountSQL(final String sql) {
		String tmp = "select count(*) as T_COUNT " + sql.substring(sql.toLowerCase().indexOf(" from"), sql.length());
		if (tmp.toLowerCase().lastIndexOf(" order ") > 0)
			tmp = tmp.substring(0, tmp.toLowerCase().lastIndexOf(" order "));
		return tmp;
	}
}