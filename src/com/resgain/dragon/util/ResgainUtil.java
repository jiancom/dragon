package com.resgain.dragon.util;

import java.io.File;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ContextLoader;

import com.resgain.dragon.filter.ActionContext;

/**
 * 常用的工具方法类
 * @author memphis.guo
 */
public class ResgainUtil
{

	private static Logger logger = LoggerFactory.getLogger(ResgainUtil.class);

    /**
     * 取得当前系统日期，主要是保证在多个服务器之间获取的日期是一致的-暂时采用new Date
     * @return
     */
    public static Date getToday() {
        return new Date();
    }

    public static String getDateTimeString(Date date)
    {
        return convert(date, ConfigUtil.getValue("datetime-format", "yyyy-MM-dd HH:mm:ss"));
    }

    public static String getDateString(Date date)
    {
        return convert(date, ConfigUtil.getValue("date-format", "yyyy-MM-dd"));
    }

    public static String getTimeString(Date date)
    {
        return convert(date, ConfigUtil.getValue("time-format", "HH:mm:ss"));
    }

	public static String convert(Date date, String style)
    {
    	if(date==null || style==null)
    		return null;
    	return new SimpleDateFormat(style).format(date);
    }

	public static boolean isNS(String str)
	{
	    if (str == null || str.trim().length() < 1)
	        return true;
	    return false;
	}

	public static String str(String... str)
	{
	    StringBuilder sb = new StringBuilder();
	    for (String s : str) {
	        sb.append(s);
	    }
	    return sb.toString();
	}

	public static String foraNumber(Number num, String parten)
	{
		if(parten==null)
			parten = "####.000";
		DecimalFormat df1 = new DecimalFormat(parten);
		return df1.format(num);
	}

	public static String getRootPath(){
		return ActionContext.getHttpServletRequest().getServletContext().getRealPath(String.valueOf("/"));
	}

	public static String getViewPath() {
		return getRootPath() + ConfigUtil.getValue("VIEW_PATH", File.separatorChar + "views");
	}

	@SuppressWarnings("unchecked")
	public static <T> T getBean(Class<T> cls) {
		return (T) ContextLoader.getCurrentWebApplicationContext().getBean(String.valueOf(cls.getSimpleName().charAt(0)).toLowerCase() + cls.getSimpleName().substring(1));
	}

	public static Object getBean(String beanId) {
		logger.debug("在spring中获取{}的bean。", beanId);
		if (ContextLoader.getCurrentWebApplicationContext().containsBean(beanId))
			return ContextLoader.getCurrentWebApplicationContext().getBean(beanId);
		return null;
	}

	public static String render(String content, Map<String, Object> context)
    {
		try {
			if (context == null)
				return content;
			VelocityContext velocityContext = new VelocityContext(context);
			StringWriter sw = new StringWriter();
			Velocity.evaluate(velocityContext, sw, "LOG", content);
			return sw.toString();
		} catch (Exception e) {
			logger.error("模板解析错误:{}", e);
			throw new RuntimeException("模板解析错误");
		}
    }
}