package com.resgain.dragon.jdbc;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import net.sf.cglib.proxy.InvocationHandler;

public class ResgainService implements InvocationHandler
{
	private QueryBean queryBean;

	protected QueryBean getDBean()
	{
		return queryBean;
	}

	private ResgainService obj;

	public ResgainService(){}

	public ResgainService(Object o)
	{
	    if(o instanceof ResgainService)
	        this.obj = (ResgainService)o;
	}

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
		obj.queryBean = new QueryBean(DataSourceFactory.getConnection(obj.getDsname()));
    	Object r = null;
		try {
			r = method.invoke(obj, args);
		} catch (Exception e) {
			obj.getDBean().rollback();
			if(e instanceof InvocationTargetException)
				throw ((InvocationTargetException) e).getTargetException();
			else
				throw e;
		} finally {
			try {
				obj.getDBean().submit();
			} catch (Exception e) {
				obj.getDBean().rollback();
			}
			obj.getDBean().endQuery();
			obj.queryBean = null;
		}
		return r;
	}

	protected String getDsname() {
		return "default";
	}
}