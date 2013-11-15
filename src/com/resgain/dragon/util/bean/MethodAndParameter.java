package com.resgain.dragon.util.bean;

import java.util.Map;

import javassist.CtMethod;


public class MethodAndParameter {
	private CtMethod cm;
	private Map<String, String> parameter;

	public MethodAndParameter(CtMethod cm, Map<String, String> parameter) {
		super();
		this.cm = cm;
		this.parameter = parameter;
	}
	
	public CtMethod getCm()
	{
		return cm;
	}
	public Map<String, String> getParameter()
	{
		return parameter;
	}
}