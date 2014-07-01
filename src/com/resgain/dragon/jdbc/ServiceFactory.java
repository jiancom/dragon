package com.resgain.dragon.jdbc;

import net.sf.cglib.proxy.Enhancer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.resgain.dragon.exception.KnowException;

public class ServiceFactory
{
	private static Logger logger = LoggerFactory.getLogger(ServiceFactory.class);

	@SuppressWarnings("unchecked")
	public static <T extends ResgainService> T getInstance(Class<T> clazz) {
		try {
			ResgainService beans = new ResgainService(clazz.newInstance());
			Enhancer enhancer = new Enhancer();
			enhancer.setSuperclass(clazz);
			enhancer.setCallback(beans);
			T ret = (T) enhancer.create();
			return ret;
		} catch (Exception e) {
			logger.error("创建service出现异常:", e);
			throw new KnowException("创建service出现异常:" + e.getMessage());
		}
	}

}
