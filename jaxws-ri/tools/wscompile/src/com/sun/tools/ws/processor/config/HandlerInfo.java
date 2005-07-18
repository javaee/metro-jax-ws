/*
 * $Id: HandlerInfo.java,v 1.2 2005-07-18 18:13:55 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.processor.config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;

/**
 *
 * @author WS Development Team
 */
public class HandlerInfo {

    public HandlerInfo() {
        properties = new HashMap();
        headerNames = new HashSet();
    }

    public String getHandlerClassName() {
        return handlerClassName;
    }

    public void setHandlerClassName(String s) {
        handlerClassName = s;
    }

    public Map getProperties() {
        return properties;
    }

    /* serialization */
    public void setProperties(Map m) {
        properties = m;
    }

    public void addHeaderName(QName name) {
        headerNames.add(name);
    }

    public Set getHeaderNames() {
        return headerNames;
    }

    /* serialization */
    public void setHeaderNames(Set s) {
        headerNames = s;
    }

    private String handlerClassName;
    private Map properties;
    private Set headerNames;
}
