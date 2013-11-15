package com.resgain.dragon.util;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 框架用到的基础配置类
 * @author memphis.guo
 */
public class ConfigUtil
{
    private static Logger logger = LoggerFactory.getLogger(ConfigUtil.class);
    private static Properties p = new Properties();

    static {
        try {
            p.load(ConfigUtil.class.getResourceAsStream("/resgain.properties"));
        } catch (Exception e) {
            logger.warn("resgain框架配置文件[{}]不存在", "resgain.properties");
		}
    }

    /**
     * 取得配置值
     * @param key
     * @param 如果配置不存在则使用指定的值
     * @return
     */
    public static String getValue(String key, String def)
    {
        if(p.containsKey(key))
            return p.getProperty(key);
        return def;
    }
}