package com.resgain.dragon.jdbc;

import java.sql.ResultSet;

public interface JdbcCallback<T>
{
	T processRow(ResultSet rs) throws Exception;
}
