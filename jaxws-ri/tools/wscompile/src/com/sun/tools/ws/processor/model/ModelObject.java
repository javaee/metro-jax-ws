/*
 * $Id: ModelObject.java,v 1.4 2005-10-14 21:57:45 vivekp Exp $
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

import com.sun.xml.ws.util.NullIterator;

/**
 *
 * @author WS Development Team
 */
public abstract class ModelObject {

    public abstract void accept(ModelVisitor visitor) throws Exception;

    public Object getProperty(String key) {
        if (_properties == null) {
            return null;
        }
        return _properties.get(key);
    }

    public void setProperty(String key, Object value) {
        if (value == null) {
            removeProperty(key);
            return;
        }

        if (_properties == null) {
            _properties = new HashMap();
        }
        _properties.put(key, value);
    }

    public void removeProperty(String key) {
        if (_properties != null) {
            _properties.remove(key);
        }
    }

    public Iterator getProperties() {
        if (_properties == null) {
            return NullIterator.getInstance();
        } else {
            return _properties.keySet().iterator();
        }
    }

    /* serialization */
    public Map getPropertiesMap() {
        return _properties;
    }

    /* serialization */
    public void setPropertiesMap(Map m) {
        _properties = m;
    }

    public String getJavaDoc() {
        return javaDoc;
    }

    public void setJavaDoc(String javaDoc) {
        this.javaDoc = javaDoc;
    }

    private String javaDoc;
    private Map _properties;
}
