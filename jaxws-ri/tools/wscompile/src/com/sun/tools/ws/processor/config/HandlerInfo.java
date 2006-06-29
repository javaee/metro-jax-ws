/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
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
