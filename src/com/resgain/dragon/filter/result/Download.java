package com.resgain.dragon.filter.result;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.resgain.dragon.exception.ActionException;
import com.resgain.dragon.iface.IResultProcess;

public class Download implements IResultProcess
{
	private static Logger logger = LoggerFactory.getLogger(Download.class);

	private String fileName;
	private String contentType;
	private long size;
	private InputStream is;

	public Download() {
	}

	public Download(String fileName, String contentType, long size, InputStream is) {
		this.contentType = contentType;
		this.fileName = fileName;
		this.is = is;
		this.size = size;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getContentType() {
		if (contentType == null || contentType.trim().length() < 1)
			contentType = "application/octet-stream";
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public InputStream getIs() {
		return is;
	}

	public void setIs(InputStream is) {
		this.is = is;
	}

	@Override
	public void process(Map<String, Object> context, HttpServletRequest request, HttpServletResponse response) {
		response.reset();
		response.setContentType(getContentType());
		if (getFileName() != null)
			response.setHeader("Content-Disposition", "attachment; filename=" + getFileName());
		if (getSize() > 0)
			response.setHeader("Content-Length", String.valueOf(getSize()));
		ServletOutputStream ouputStream;
		try {
			ouputStream = response.getOutputStream();
			copy(is, ouputStream);
		} catch (IOException e) {
			logger.error("读取或输出文件错误：", e);
			throw new ActionException("读取或输出文件错误！");
		}
	}

	private static boolean copy(InputStream in1, OutputStream out1) {
		try {
			byte[] bytes = new byte[4 * 1024];
			int c;
			while ((c = in1.read(bytes)) != -1) {
				out1.write(bytes, 0, c);
			}
			in1.close();
			out1.close();
			return true; // if success then return true
		}

		catch (Exception e) {
			return false;
		} finally {
			try {
				out1.flush();
				in1.close();
				out1.close();
			} catch (Throwable t) {
			}
		}
	}
}
