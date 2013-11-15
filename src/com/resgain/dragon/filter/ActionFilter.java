package com.resgain.dragon.filter;

import java.beans.Expression;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.resgain.dragon.bean.KnowException;
import com.resgain.dragon.iface.IResultProcess;
import com.resgain.dragon.util.ClassUtil;
import com.resgain.dragon.util.UploadUtil;
import com.resgain.dragon.util.bean.MethodAndParameter;

/**
 * Action过滤器，框架程序的入口
 * @author memphis.guo
 */
public class ActionFilter implements Filter
{
    private static Logger logger = LoggerFactory.getLogger(ActionFilter.class);

	public void init(FilterConfig fileterConfig) throws ServletException {
	}

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException
	{
		if (request instanceof HttpServletRequest) {
			HttpServletRequest hr = (HttpServletRequest) request;
			HttpServletResponse hp = (HttpServletResponse) response;
			String actionName = ActionUtil.getActionName(hr);
			if (!"/".equals(actionName) && ActionUtil.isProcessUrl(hr)) { // 如果是满足条件的动态请求
				long s = System.currentTimeMillis();
				ActionContext.setRequestResponse(hr, hp); // 设置request, response
				execute(actionName, hr, hp); // 执行动态请求
				logger.debug("{}请求执行完毕，耗时:{}", hr.getRequestURL(), (System.currentTimeMillis() - s));
				return;
			}
		}
		chain.doFilter(request, response);
	}

	public void destroy() {
	}

	private void execute(String actionName, HttpServletRequest request, HttpServletResponse response)
    {
		try {
			BeanMethod bm = ActionFinder.getAction(actionName.replace('/', '.'));
			if (bm == null) {
				ActionUtil.outJsonMsg(request, response, false, "没找到对应的Action处理类!", null);
				return;
			}
			MethodAndParameter pms = ClassUtil.getMethodParamNames(bm.getBean().getClass().getName(), bm.getMethod(), request.getMethod());
			if (pms == null)
				throw new KnowException("无效的请求:" + request.getRequestURL());
			List<Object> params = new ArrayList<Object>();
			Map<String, Object> context = UploadUtil.getParameter(request);
			if (pms.getParameter().size() > 0) {
				for (Entry<String, String> entry : pms.getParameter().entrySet()) {
					params.add(ClassUtil.getValue(entry.getKey(), entry.getValue(), context));
				}
			}
			Object ret = new Expression(bm.getBean(), bm.getMethod(), params.toArray()).getValue();
			if (ret instanceof IResultProcess)
				((IResultProcess) ret).process(context, request, response);
			else
				ActionUtil.outJson(request, response, ret);
		} catch (Exception e) {
			logger.error("响应请求发生错误", e);
			try {
				ActionUtil.outJsonMsg(request, response, false, e.getLocalizedMessage(), null);
			} catch (IOException e1) {
			}
		}
	}
}