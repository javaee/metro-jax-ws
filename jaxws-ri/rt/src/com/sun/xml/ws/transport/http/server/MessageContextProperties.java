/*
 * $Id: MessageContextProperties.java,v 1.3 2005-08-29 19:37:32 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.transport.http.server;

/**
 *
 * @author WS Development Team
 */
public interface MessageContextProperties {

    public static final String SERVLET_CONTEXT =
        "com.sun.xml.w.server.http.ServletContext";
    public static final String HTTP_SERVLET_REQUEST =
        "com.sun.xml.ws.server.http.HttpServletRequest";
    public static final String HTTP_SERVLET_RESPONSE =
        "com.sun.xml.ws.server.http.HttpServletResponse";
    public static final String IMPLEMENTOR =
        "com.sun.xml.ws.server.http.Implementor";
    public static final String ONE_WAY_OPERATION =
        "com.sun.xml.ws.server.OneWayOperation";
    public static final String CLIENT_BAD_REQUEST =
        "com.sun.xml.ws.server.http.ClientBadRequest";
    public static final String CLIENT_INVALID_CONTENT_TYPE =
        "com.sun.xml.ws.server.http.ClientInvalidContentType";
}
