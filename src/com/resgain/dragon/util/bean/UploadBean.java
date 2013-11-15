package com.resgain.dragon.util.bean;

import java.io.InputStream;

public final class UploadBean
{
    private String fieldName;	
    private String name;
    private String contentType;
    private String extName;
    private long szie;
    private InputStream lsInputStream;
    
    @SuppressWarnings("unused")
	private UploadBean(){}

	public UploadBean(String fieldName, String name, String contentType, long szie, InputStream lsInputStream) 
	{
		super();
		this.fieldName = fieldName;
		this.name = name;
		this.contentType = contentType;
		this.extName = (name.lastIndexOf('.') > 0)?name.substring(name.lastIndexOf('.')):"";
		this.szie = szie;
		this.lsInputStream = lsInputStream;		
	}

	public String getContentType()
	{
		return contentType;
	}

	public String getExtName()
	{
		return extName;
	}

	public String getFieldName()
	{
		return fieldName;
	}

	public InputStream getLsInputStream()
	{
		return lsInputStream;
	}

	public String getName()
	{
		return name;
	}

	public long getSzie()
	{
		return szie;
	}	
}
