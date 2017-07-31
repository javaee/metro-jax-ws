/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.tools.ws.processor.model;

import com.sun.tools.ws.processor.model.java.JavaType;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 *
 * @author WS Development Team
 */
public abstract class AbstractType {

    protected AbstractType() {}

    protected AbstractType(QName name) {
        this(name, null, null);
    }

    protected AbstractType(QName name, String version) {
        this(name, null, version);
    }

    protected AbstractType(QName name, JavaType javaType) {
        this(name, javaType, null);
    }

    protected AbstractType(QName name, JavaType javaType, String version) {
        this.name = name;
        this.javaType = javaType;
        this.version = version;
    }

    public QName getName() {
        return name;
    }

    public void setName(QName name) {
        this.name = name;
    }

    public JavaType getJavaType() {
        return javaType;
    }

    public void setJavaType(JavaType javaType) {
        this.javaType = javaType;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public boolean isNillable() {
        return false;
    }

    public boolean isSOAPType() {
        return false;
    }

    public boolean isLiteralType() {
        return false;
    }

    public Object getProperty(String key) {
        if (properties == null) {
            return null;
        }
        return properties.get(key);
    }

    public void setProperty(String key, Object value) {
        if (value == null) {
            removeProperty(key);
            return;
        }

        if (properties == null) {
            properties = new HashMap();
        }
        properties.put(key, value);
    }

    public void removeProperty(String key) {
        if (properties != null) {
            properties.remove(key);
        }
    }

    public Iterator getProperties() {
        if (properties == null) {
            return Collections.emptyList().iterator();
        } else {
            return properties.keySet().iterator();
        }
    }

    /* serialization */
    public Map getPropertiesMap() {
        return properties;
    }

    /* serialization */
    public void setPropertiesMap(Map m) {
        properties = m;
    }

    private QName name;
    private JavaType javaType;
    private String version = null;
    private Map properties;
}
