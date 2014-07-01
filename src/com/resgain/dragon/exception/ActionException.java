package com.resgain.dragon.exception;

public class ActionException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	private int code; // 出错代码

	public ActionException(int code, String message) {
		super(message);
		this.code = code;
	}

	public ActionException(String message) {
		super(message);
		this.code = 500;
	}

	public int getCode() {
		return code;
	}
}
