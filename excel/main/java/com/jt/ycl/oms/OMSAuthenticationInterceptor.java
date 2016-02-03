/**
 * 
 */
package com.jt.ycl.oms;

import java.io.PrintWriter;
import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.jt.ycl.oms.account.AccountInfo;
import com.jt.ycl.oms.auth.OMSPermission;
import com.jt.ycl.oms.auth.Permission;

/**
 * 在未登录前，任何访问url都跳转到login页面；登录成功后跳转至先前的url
 *
 */
public class OMSAuthenticationInterceptor implements HandlerInterceptor{

    /**  
     * 在业务处理器处理请求之前被调用  
     * 如果返回false  
     * 从当前的拦截器往回执行所有拦截器的afterCompletion(),再退出拦截器链 
     * 如果返回true  
     * 执行下一个拦截器,直到所有的拦截器都执行完毕  
     * 再执行被拦截的Controller  
     * 然后进入拦截器链,  
     * 从最后一个拦截器往回执行所有的postHandle()  
     * 接着再从最后一个拦截器往回执行所有的afterCompletion()  
     */    
    @Override    
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    	
    	HandlerMethod handlerMethod = (HandlerMethod) handler;
		Object controller = handlerMethod.getBean();
		//登录请求不需要拦截
		if(StringUtils.equalsIgnoreCase(controller.getClass().getName(), "com.jt.ycl.oms.account.OmsLoginController")){
			return true;
		}
		if(StringUtils.equalsIgnoreCase(controller.getClass().getName(), "com.jt.ycl.oms.sales.SalesLoginController")){
			return true;
		}
		boolean ajax = true;
		if(!(request.getHeader("Accept").indexOf("application/json") > -1 || (request.getHeader("X-Requested-With")!= null && request.getHeader("X-Requested-With").indexOf("XMLHttpRequest") > -1))) {
			ajax = false;
        }
		
		//检查是否需要登录。如果需要登录并且是ajax请求，则返回信息提示需要登录，如果不是ajax请求，则直接跳转到登录界面
		AccountInfo accountInfo = (AccountInfo)request.getSession().getAttribute("user");
		if(accountInfo != null){
			Method method = handlerMethod.getMethod();
			OMSPermission clazzAuth = AnnotationUtils.findAnnotation(controller.getClass(), OMSPermission.class);
			OMSPermission methodAuth = AnnotationUtils.findAnnotation(method, OMSPermission.class);
			if(methodAuth != null){
				Permission permission = methodAuth.permission();
				if(!accountInfo.getRole().getPermissions().contains(permission)){
					response.setStatus(HttpStatus.SC_UNAUTHORIZED);
					if(ajax){
						PrintWriter writer = response.getWriter();
						writer.write("无权访问");
						writer.flush();
					}else{
						response.sendRedirect(request.getContextPath() + "/authFailed");
					}
					return false;
				}
			}
			if(clazzAuth != null){
				Permission permission = clazzAuth.permission();
				if(!accountInfo.getRole().getPermissions().contains(permission)){
					response.setStatus(HttpStatus.SC_UNAUTHORIZED);
					if(ajax){
						PrintWriter writer = response.getWriter();
						writer.write("无权访问");
						writer.flush();
					}else{
						response.sendRedirect(request.getContextPath() + "/authFailed");
					}
					return false;
				}
			}
			return true;
		}
		response.setStatus(HttpStatus.SC_UNAUTHORIZED);
		if(!ajax) {
			response.sendRedirect(request.getContextPath() + "/");
        }else{
    		PrintWriter writer = response.getWriter();
			writer.write("请登录后操作");
			writer.flush();
			return false;
        }
		
		return false;
    }
        
    /** 
     * 在业务处理器处理请求执行完成后,生成视图之前执行的动作    
     * 可在modelAndView中加入数据，比如当前时间 
     */  
    @Override    
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, 
            ModelAndView modelAndView) throws Exception {     
    }    
    
    /**  
     * 在DispatcherServlet完全处理完请求后被调用,可用于清理资源等   
     * 当有拦截器抛出异常时,会从当前拦截器往回执行所有的拦截器的afterCompletion()  
     */    
    @Override    
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)    
            throws Exception {    
	}    
}