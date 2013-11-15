package com.resgain.dragon.jdbc;

import java.sql.Connection;
import java.util.Hashtable;
import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidDataSource;
import com.resgain.dragon.util.IniUtil;

public class DataSourceFactory
{
    private static Logger logger = LoggerFactory.getLogger(DataSourceFactory.class);

	private static final Hashtable<String, DruidDataSource> dataSouce = new Hashtable<String, DruidDataSource>();

	private static final String USER = "jdbc.username";
	private static final String PASSWORD = "jdbc.password";
	private static final String URL = "jdbc.url";
	private static final String DRIVER = "jdbc.driver";
	private static final int maxConnections = 30;
	private static final int minConnections = 3;

	static {
		try {
			Hashtable<String, LinkedHashMap<String, String>> tmp = IniUtil.getTitle(DataSourceFactory.class.getResourceAsStream("/datasource.ini"));
			for (String key : tmp.keySet()) {
				LinkedHashMap<String, String> config = tmp.get(key);
				logger.info("开始创建datasource【{}】，驱动：{} URL:{} 用户名:{}", key, config.get(DRIVER), config.get(URL), config.get(USER));
				DruidDataSource ds = new DruidDataSource();
				ds.setDriverClassName(config.get(DRIVER));
				ds.setUrl(config.get(URL));
				ds.setUsername(config.get(USER));
				ds.setPassword(config.get(PASSWORD));
				ds.setMaxActive(maxConnections);
				ds.setMinIdle(minConnections);
				dataSouce.put(key, ds);
			}
		} catch (Exception e) {
			logger.error("create datasource err:", e);
		}
	}

	// static {
	// try {
	// Hashtable<String, LinkedHashMap<String, String>> tmp = IniUtil.getTitle(DataSourceFactory.class.getResourceAsStream("/datasource.ini"));
	// for (String key : tmp.keySet()) {
	// LinkedHashMap<String, String> config = tmp.get(key);
	// logger.info("开始创建datasource【{}】，驱动：{} URL:{} 用户名:{}", key, config.get(DRIVER), config.get(URL), config.get(USER));
	// BoneCPDataSource ds = new BoneCPDataSource();
	// ds.setDriverClass(config.get(DRIVER));
	// ds.setJdbcUrl(config.get(URL));
	// ds.setUsername(config.get(USER));
	// ds.setPassword(config.get(PASSWORD));
	// ds.setMaxConnectionsPerPartition(maxConnections);
	// ds.setMinConnectionsPerPartition(minConnections);
	// dataSouce.put(key, ds);
	// }
	// } catch (Exception e) {
	// logger.error("create datasource err:", e);
	// }
	// }

	public static Connection getConnection(String name) throws Exception {
		return dataSouce.get(name).getConnection();
	}
}
