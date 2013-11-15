package com.resgain.dragon.filter;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.zip.GZIPOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.resgain.dragon.util.ConfigUtil;
import com.resgain.dragon.util.JSONUtil;
import com.resgain.dragon.util.RequestUtil;

/**
 * Action处理中常用方法
 * @author gyl
 */
public class ActionUtil
{
	private static Logger logger = LoggerFactory.getLogger(ActionUtil.class);

	private static final String ACTION_EXT = ConfigUtil.getValue("action.extension", ".do");
	private static final String ROOT_PATH = new File(ActionUtil.class.getResource("/").getFile()).getParentFile().getParentFile().getPath();
	// new File(ActionUtil.class.getProtectionDomain().getCodeSource().getLocation().getFile()).getParentFile().getParent() File.separatorChar;

	static {
		try {
			logger.debug("新增Velocity初始属性:{}---->{}", RuntimeConstants.FILE_RESOURCE_LOADER_PATH, ROOT_PATH);
			Velocity.addProperty(RuntimeConstants.FILE_RESOURCE_LOADER_PATH, ROOT_PATH);
			Properties p = new Properties();
			p.load(ActionUtil.class.getResourceAsStream("/velocity.properties"));
			for (Entry<Object, Object> entry : p.entrySet()) {
				logger.debug("新增Velocity初始属性:{}---->{}", entry.getKey(), entry.getValue());
				Velocity.addProperty((String) entry.getKey(), (String) entry.getValue());
			}
			Velocity.init();
		} catch (Exception e) {
			logger.error("velocity初始化错误", e);
		}
	}

	// 判断请求是否需要处理
	public static boolean isProcessUrl(HttpServletRequest request) {
		String uri = request.getServletPath();
		return (uri.indexOf('.') < 0 || uri.endsWith(ACTION_EXT));
    }

	public static String getActionName(HttpServletRequest request) {
		String ret = request.getServletPath();
		if (ret.endsWith(ACTION_EXT))
			ret = ret.replace(ACTION_EXT, "");
		return ret;
	}

	/**
	 * 输出json格式消息及数据
	 * @param request
	 * @param response
	 * @param success
	 * @param msg
	 * @param data
	 * @throws IOException
	 */
	public static void outJsonMsg(HttpServletRequest request, HttpServletResponse response, boolean success, String msg, Object data) throws IOException {
		Map<String, Object> o = new HashMap<String, Object>();
		o.put("success", success);
		o.put("message", msg);
		o.put("data", data);
		outJson(request, response, o);
	}

	/**
	 * 输出json格式数据
	 * @param request
	 * @param response
	 * @param obj
	 * @throws IOException
	 */
	public static void outJson(HttpServletRequest request, HttpServletResponse response, Object obj) throws IOException {
		response.setLocale(new Locale(new String("zh"), new String("CN")));
		response.setCharacterEncoding("UTF-8");
		response.setContentType("text/plain; charset=utf-8");
		outStr(request, response, JSONUtil.toJson(obj));
	}

	/**
	 * 输出指定的内容
	 * @param request
	 * @param response
	 * @param str
	 * @throws IOException
	 */
	public static void outStr(HttpServletRequest request, HttpServletResponse response, String str) throws IOException {
		if (RequestUtil.isSupportGzip(request)) {
			response.addHeader("Content-Encoding", "gzip");
			response.addHeader("Vary", "Accept-Encoding");
			ServletOutputStream out = response.getOutputStream();
			GZIPOutputStream gzipStream = new GZIPOutputStream(out);
			OutputStreamWriter ow = new OutputStreamWriter(gzipStream, "UTF-8");
			ow.append(str);
			ow.flush();
			ow.close();
			gzipStream.flush();
			gzipStream.finish();
			gzipStream.close();
			out.close();
		} else {
			ServletOutputStream out = response.getOutputStream();
			OutputStreamWriter ow = new OutputStreamWriter(out, "UTF-8");
			ow.append(str);
			ow.flush();
			ow.close();
			out.close();
		}
	}

	public static void renderOut(HttpServletRequest request, HttpServletResponse response, String finalLocation, VelocityContext context) throws IOException {
		if (finalLocation.startsWith(ROOT_PATH))
			finalLocation = finalLocation.substring(ROOT_PATH.length());
		Template template = Velocity.getTemplate(finalLocation, "UTF-8");
		if (RequestUtil.isSupportGzip(request)) {
			response.addHeader("Content-Encoding", "gzip");
			response.addHeader("Vary", "Accept-Encoding");
			ServletOutputStream out = response.getOutputStream();
			GZIPOutputStream gzipStream = new GZIPOutputStream(out);
			OutputStreamWriter ow = new OutputStreamWriter(gzipStream, "UTF-8");
			template.merge(context, ow);
			ow.flush();
			ow.close();
			gzipStream.flush();
			gzipStream.finish();
			gzipStream.close();
			out.close();
		} else {
			ServletOutputStream out = response.getOutputStream();
			OutputStreamWriter ow = new OutputStreamWriter(out, "UTF-8");
			template.merge(context, ow);
			ow.flush();
			ow.close();
			out.close();
		}
	}
}
