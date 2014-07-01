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
		if (StringUtils.countMatches(beanId, ConfigUtil.getCMSplit())==1 && beanId.indexOf(ConfigUtil.getCMSplit()) > 0) { //如果有定义分割符号则以定义的分割符号区分类名和方法名，否则就用默认的方法名
			methodName = beanId.substring(beanId.lastIndexOf(ConfigUtil.getCMSplit())+1);
			beanId = beanId.substring(0, beanId.lastIndexOf(ConfigUtil.getCMSplit()));
		}
		bean = ResgainUtil.getBean(beanId);
		if (bean == null)
			bean = ResgainUtil.getBean(ConfigUtil.getValue("default.action", "action.default"));
		return (bean != null)? new BeanMethod(bean, methodName): null;
	}

	public static boolean isPackage(String s){
		for (String pack : ACTION_PACKAGE) {
			if(Package.getPackage(pack+s)!=null)
				return true;
		}
		return false;
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