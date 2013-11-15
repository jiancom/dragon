package com.resgain.dragon.jdbc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BaseService extends ResgainService
{
	@Override
	protected abstract String getDsname();

	protected List<Map<String, Object>> queryTop(final String sql, final int top, final Object... args) {
		final List<Map<String, Object>> ret = new ArrayList<Map<String, Object>>();
		getDBean().query(sql, args, new JdbcCallback<List<Map<String, Object>>>() {
			public List<Map<String, Object>> processRow(ResultSet rs) throws Exception {
				int currentRow = 0;
				ResultSetMetaData rsmd = rs.getMetaData();
				while (rs.next() && currentRow < top) {
					Map<String, Object> map = new HashMap<String, Object>();
					for (int col = 1; col <= rsmd.getColumnCount(); col++) {
						int type = rsmd.getColumnType(col);
						if (type == java.sql.Types.DATE || type == java.sql.Types.TIME || type == java.sql.Types.TIMESTAMP)
							map.put(rsmd.getColumnLabel(col).toLowerCase(), rs.getObject(col));
						else
							map.put(rsmd.getColumnLabel(col).toLowerCase(), rs.getString(col));
					}
					ret.add(map);
					currentRow++;
				}
				return null;
			}
		});
		return ret;
	}
}
