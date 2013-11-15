package com.resgain.dragon.filter.result;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.resgain.dragon.filter.ActionUtil;
import com.resgain.dragon.iface.IResultProcess;

/**
 * 输出数据与模板渲染结果
 * @author gyl
 */
public class MsgResult implements IResultProcess
{
	private static Logger logger = LoggerFactory.getLogger(MsgResult.class);

	private boolean success;
	private String msg;
	private Object data;

	public MsgResult(boolean success, String msg, Object data) {
		this.success = success;
		this.msg = msg;
		this.data = data;
	}

	public static MsgResult success() {
		return new MsgResult(true, "操作成功", null);
	}

	public static MsgResult success(String msg) {
		return new MsgResult(true, msg, null);
	}

	public static MsgResult success(String msg, Object data) {
		return new MsgResult(true, msg, data);
	}

	public static MsgResult error() {
		return new MsgResult(false, "操作失败", null);
	}

	public static MsgResult error(String msg) {
		return new MsgResult(false, msg, null);
	}

	public static MsgResult result(boolean success) {
		return success ? success() : error();
	}

	public static MsgResult result(boolean success, Object data) {
		return success ? success("操作成功", data) : error();
	}

	@Override
	public void process(Map<String, Object> context, HttpServletRequest request, HttpServletResponse response) {
		try {
			ActionUtil.outJsonMsg(request, response, success, msg, data);
		} catch (IOException e) {
			logger.error("结果输出错误:{}", e);
		}
	}
}