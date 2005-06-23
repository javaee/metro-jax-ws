
package com.sun.xml.ws.transport.http.servlet;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

public class WebServiceContext {
    
    public static ThreadLocal servletContext = new ThreadLocal();
    public static ThreadLocal servletRequest = new ThreadLocal();
    
    public static ServletContext getServletContext() {
        return (ServletContext)servletContext.get();
    }
    
    public static void setServletContext(ServletContext context) {
        servletContext.set(context);
    }
    
    public static void setServletRequest(HttpServletRequest request) {
        servletRequest.set(request);
    }
    
    public static HttpServletRequest getServletRequest() {
        return (HttpServletRequest)servletRequest.get();
    }
    
}
