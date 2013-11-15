package com.resgain.dragon.filter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import jodd.io.findfile.ClassScanner;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.web.context.ContextLoader;

import com.resgain.dragon.util.ConfigUtil;
import com.resgain.dragon.util.ResgainUtil;

/**
 * 查找到相关的Action类，并把其注册到spring factory由spring进行管理
 * @author gyl
 */
public class ActionFinder
{
	private final static String ACTION_POSTFIX = "Action";
	private final static List<String> ACTION_PACKAGE = new ArrayList<String>();
	private final static String ACTION_PREFIX = "action";

	private static Logger logger = LoggerFactory.getLogger(ActionFinder.class);

	static {
		ACTION_PACKAGE.add("com.resgain.base.action");
		for (String ar : ConfigUtil.getValue("ActionRoot", "action").split(";")) {
			if (!StringUtils.isBlank(ar)) {
				logger.debug("新增Action根：{}", ar);
				ACTION_PACKAGE.add(ar);
			}
		}

		ClassScanner cs = new ClassScanner() {
			DefaultListableBeanFactory acf = (DefaultListableBeanFactory) ContextLoader.getCurrentWebApplicationContext().getAutowireCapableBeanFactory();
			@Override
			protected void onEntry(EntryData entryData) throws Exception {
				String name = entryData.getName();
				String path = getActionPath(name);
				logger.debug("找到类{}, 路径{}", name, path);
				if (path != null) {
					Class<?> clazz = Class.forName(name);
					acf.registerBeanDefinition(path, BeanDefinitionBuilder.genericBeanDefinition(clazz).getBeanDefinition());
					logger.info("注册Action类:{}到spring factory中:{}", name, path);
				}
			}
		};
		String path = new File(ActionFinder.class.getResource("/").getFile()).getPath();
		// String path = ActionFinder.class.getProtectionDomain().getCodeSource().getLocation().getFile();
		cs.scan(new File(path));
	}

	private static String getActionPath(String name) {
		if (name.endsWith(ACTION_POSTFIX)) {
			String n = name.substring(0, name.length() - ACTION_POSTFIX.length());
			for (String ap : ACTION_PACKAGE) {
				if (n.startsWith(ap))
					return ACTION_PREFIX + n.substring(ap.length()).toLowerCase();
			}
		}
		return null;
	}

	public static BeanMethod getAction(String name) {
		String beanId = ACTION_PREFIX + name;
		Object bean = null;
		String methodName = "exec";
		if (beanId.indexOf('!') > 0) {
			String t[] = name.split("!");
			beanId = ACTION_PREFIX + t[0];
			methodName = t[1];
		}
		bean = ResgainUtil.getBean(beanId);
		if (bean == null) {
			bean = ResgainUtil.getBean(beanId.substring(0, beanId.lastIndexOf('.')));
			if (bean != null)
				methodName = beanId.substring(beanId.lastIndexOf('.') + 1);
		}
		if (bean == null)
			bean = ResgainUtil.getBean("action.default");
		if (bean != null)
			return new BeanMethod(bean, methodName);
		return null;
	}
}

class BeanMethod
{
	private Object bean;
	private String method;

	public BeanMethod(Object bean, String method) {
		super();
		this.bean = bean;
		this.method = method;
	}

	public Object getBean() {
		return bean;
	}

	public String getMethod() {
		return method;
	}
}