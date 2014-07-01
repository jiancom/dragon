package com.resgain.dragon.util;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class SqlConfig
{
	private static Logger logger = LoggerFactory.getLogger(SqlConfig.class);

	private static Map<String, String> sqlCacheMap = new HashMap<String, String>();
	static {
		File path = new File(new File(SqlConfig.class.getResource("/").getFile()).getPath() + ConfigUtil.getValue("SQL_PATH", "/sql"));
		if (path.isDirectory()) {
			Collection<File> files = FileUtils.listFiles(path, FileFilterUtils.suffixFileFilter(".sql"), TrueFileFilter.INSTANCE);
			for (File file : files) {
				sqlCacheMap.putAll(parse(file));
			}
		}

	}

	private static Map<String, String> parse(File file) {
		Map<String, String> ret = new HashMap<String, String>();
		try {
			Pattern p = Pattern.compile("(?m)^--([\\w]*)[\\S\\ \\t]*(?s)(.*?)(;|\\n{2,}|\\z)");
			Matcher m = p.matcher(FileUtils.readFileToString(file, "utf-8"));
			String prefix = file.getName().substring(0, file.getName().indexOf('.') + 1);
			while (m.find()) {
				ret.put(prefix + m.group(1), m.group(2));
			}
		} catch (IOException e) {
			logger.error("读取SQL配置文件[{}]出错", file.getPath(), e);
		}
		return ret;
	}

	public static String getSql(String key) {
		return sqlCacheMap.get(key);
	}
}
