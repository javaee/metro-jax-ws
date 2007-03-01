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

package com.sun.tools.ws.processor.model;

import com.sun.tools.ws.wsdl.framework.Entity;
import com.sun.tools.ws.wscompile.ErrorReceiver;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.xml.sax.Locator;

/**
 *
 * @author WS Development Team
 */
public abstract class ModelObject {
    public abstract void accept(ModelVisitor visitor) throws Exception;

    private final Entity entity;
    protected ErrorReceiver errorReceiver;

    protected ModelObject(Entity entity) {
        this.entity = entity;
    }

    public void setErrorReceiver(ErrorReceiver errorReceiver) {
        this.errorReceiver = errorReceiver;
    }

    public Entity getEntity() {
        return entity;
    }

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
            return Collections.emptyList().iterator();
        } else {
            return _properties.keySet().iterator();
        }
    }

    public Locator getLocator(){
        return entity.getLocator();
    }

    public Map getPropertiesMap() {
        return _properties;
    }

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
