package com.resgain.dragon.filter.result;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.resgain.dragon.exception.KnowException;
import com.resgain.dragon.filter.ActionUtil;
import com.resgain.dragon.iface.IResultProcess;
import com.resgain.dragon.util.ConfigUtil;
import com.resgain.dragon.util.ResgainUtil;

/**
 * 输出数据与模板渲染结果
 * @author gyl
 */
public class Template implements IResultProcess
{
	private static Logger logger = LoggerFactory.getLogger(Template.class);

	private String tname;
	private VelocityContext context;

	public Template(String tname) {
		this.tname = tname;
		context = new VelocityContext();
	}

	/**
	 * 获取输出实例，根据请求和规则找指定的模板
	 * @return
	 */
	public static Template getInstance() {
		return new Template(null);
	}

	/**
	 * 获取输出实例，指定模板文件名
	 * @return
	 */
	public static Template getInstance(String tname) {
		return new Template(tname);
	}

	/**
	 * 新增模板数据变量
	 * @param key
	 * @param value
	 * @return
	 */
	public Template put(String key, Object value) {
		context.put(key, value);
		logger.debug("上下文环境新增{}\t:\t{}", key, value);
		return this;
	}

	/**
	 * 根据请求路径获取模板的实际文件名
	 * @param path
	 * @return
	 */
	public String getTemplate(String path) {
		String tpa = tname;
		if (tname == null) {
			if (path.endsWith("/"))
				tpa = "/index";
			else if ((path.indexOf(ConfigUtil.getCMSplit()) > 0))
				tpa = path.replace(ConfigUtil.getCMSplit(), "-");
			else
				tpa = path;
		} else if (!tname.startsWith("/")) {
			tpa = path.substring(0, path.lastIndexOf('/')) + tname;
		}
		String f = ResgainUtil.getViewPath() + tpa.replace('/', File.separatorChar) + ".vm";
		if (!new File(f).exists()) {
			logger.error("模板文件{}不存在。", f);
			throw new KnowException("程序中设定的相关资源不存在！");
		}
		return f;
	}

	@Override
	public void process(Map<String, Object> context1, HttpServletRequest request, HttpServletResponse response) {
		String f = getTemplate(ActionUtil.getActionName(request));
		try {
			for (Entry<String, Object> entry : context1.entrySet()) {
				if (!context.containsKey(entry.getKey()))
					context.put(entry.getKey(), entry.getValue());
			}
			ActionUtil.renderOut(request, response, f, context);
		} catch (Exception e) {
			logger.error("模板文件{}渲染输出出错", f, e);
			throw new KnowException("模板文件渲染错误");
		}
	}
}