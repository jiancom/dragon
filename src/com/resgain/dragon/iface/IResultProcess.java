package com.resgain.dragon.iface;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Action结果处理接口
 * @author memphis
 */
public interface IResultProcess
{
	public abstract void process(Map<String, Object> context, HttpServletRequest request, HttpServletResponse response);
}
