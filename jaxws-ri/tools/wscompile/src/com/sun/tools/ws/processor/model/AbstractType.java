/*
 * $Id: AbstractType.java,v 1.3 2005-09-10 19:49:36 kohsuke Exp $
 */

/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */

package com.sun.tools.ws.processor.model;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.QName;

import com.sun.tools.ws.processor.model.java.JavaType;
import com.sun.xml.ws.util.NullIterator;

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
            return NullIterator.getInstance();
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
