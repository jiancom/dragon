package com.resgain.dragon.filter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Action相关的上下文环境信息
 * @author gyl
 */
final public class ActionContext
{
    private static ThreadLocal<HttpServletRequest> requestThreadLocal = new ThreadLocal<HttpServletRequest>();
    private static ThreadLocal<HttpServletResponse> responseThreadLocal = new ThreadLocal<HttpServletResponse>();

    static void setRequestResponse(HttpServletRequest request, HttpServletResponse response)
    {
        requestThreadLocal.set(request);
        responseThreadLocal.set(response);
    }

    public static HttpServletRequest getHttpServletRequest()
    {
        return requestThreadLocal.get();
    }

    public static HttpServletResponse getHttpServletResponse()
    {
        return responseThreadLocal.get();
    }
}
