package com.resgain.dragon.util;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestUtil
{
    private static Logger logger = LoggerFactory.getLogger(RequestUtil.class);

    public static String getIpAddr(HttpServletRequest request)
    {
        return request.getHeader("x-forwarded-for") == null ? (request.getHeader("Proxy-Client-IP") == null ? (request.getHeader("WL-Proxy-Client-IP") == null ? request.getRemoteAddr() : request.getHeader("WL-Proxy-Client-IP")) : request.getHeader("Proxy-Client-IP")) : request
                .getHeader("x-forwarded-for");
    }

    /**
	 * 判断客户端是否支持GZIP压缩
	 * @param req
	 */
	@SuppressWarnings("rawtypes")
	public static boolean isSupportGzip(HttpServletRequest req) {
		boolean supportsGzip = false;
		Enumeration e = ((HttpServletRequest) req).getHeaders("Accept-Encoding");
		while (e.hasMoreElements()) {
			String name = (String) e.nextElement();
			if (name.matches("(?i).*gzip.*")) {
				supportsGzip = true;
				logger.debug("发起请求的浏览器支持压缩...");
				break;
			}
		}
		return supportsGzip;
	}

}
