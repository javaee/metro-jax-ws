/**
 * Servlet transport for the JAX-WS RI.
 *
 * <p>
 * This package glues together the servlet API and
 * {@link com.sun.xml.ws.api.server the JAX-WS hosting API}.
 *
 * <h2>Compatibility</h2>
 * <p>
 * {@link WSServlet} and {@link WSServletContextListener} class names
 * show up in the user appliation, so we need to be careful in changing them.
 *
 * <b>Other parts of the code, including actual definitions of the above classes,
 * are subject to change without notice.</b>
 */
package com.sun.xml.ws.transport.http.servlet;
