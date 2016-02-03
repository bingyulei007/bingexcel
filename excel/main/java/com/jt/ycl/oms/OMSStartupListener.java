/**
 * 
 */
package com.jt.ycl.oms;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * @author wuqh
 *
 */
public class OMSStartupListener implements ServletContextListener {

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		sce.getServletContext().setAttribute("buildVersion", "201512001717");
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
	}
}
