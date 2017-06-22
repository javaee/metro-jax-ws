/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2012-2017 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.db.sdo;

import commonj.sdo.DataObject;
import commonj.sdo.Property;
import commonj.sdo.Type;
import commonj.sdo.helper.TypeHelper;

import com.sun.xml.ws.spi.db.PropertyGetter;
import com.sun.xml.ws.spi.db.PropertySetter;

import javax.xml.namespace.QName;

import com.sun.xml.ws.spi.db.PropertyAccessor;
import com.sun.xml.ws.spi.db.WrapperAccessor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: giglee
 * Date: May 19, 2009
 * Time: 10:40:31 AM
 * To change this template use File | Settings | File Templates.
 */
public final class SDOWrapperAccessor extends WrapperAccessor {

    private SDOContextWrapper contextWrapper = null;
    private Class<?> contentClass;

    public SDOWrapperAccessor(SDOContextWrapper contextWrapper, Class<?> wrapperBean) {
        this.contextWrapper = contextWrapper;
        contentClass = wrapperBean;
        initBuilders();
    }

    protected void initBuilders() {
        HashMap<Object, PropertySetter> setByQName = new HashMap<Object, PropertySetter>();
        HashMap<Object, PropertyGetter> getByQName = new HashMap<Object, PropertyGetter>();
        HashMap<Object, PropertySetter> setByLocalpart = new HashMap<Object, PropertySetter>();
        HashMap<Object, PropertyGetter> getByLocalpart = new HashMap<Object, PropertyGetter>();

        HashSet<String> elementLocalNames = new HashSet<String>();

        TypeHelper helper = contextWrapper.getHelperContext().getTypeHelper();
        Type type = helper.getType(contentClass);

        @SuppressWarnings("unchecked")
        List<Property> properties = (List<Property>) type.getDeclaredProperties();
        for (Property p : properties) {
            QName qname = SDOUtils.getPropertyElementName(contextWrapper.getHelperContext(), p);
            SDOPropertyBuilder pBuilder = new SDOPropertyBuilder(qname, p.getType().getInstanceClass());
            setByQName.put(qname, pBuilder);
            getByQName.put(qname, pBuilder);
            setByLocalpart.put(qname.getLocalPart(), pBuilder);
            getByLocalpart.put(qname.getLocalPart(), pBuilder);
            if (elementLocalNames.contains(qname.getLocalPart())) {
                elementLocalNameCollision = true;
            } else {
                elementLocalNames.add(qname.getLocalPart());
            }
        }

        if (elementLocalNameCollision) {
            propertySetters = setByQName;
            propertyGetters = getByQName;

        } else {
            propertySetters = setByLocalpart;
            propertyGetters = getByLocalpart;
        }
    }

    /**
     * Knows the mapping of all the properties of a contaning type
     */
    static class SDOPropertyBuilder implements PropertyAccessor,
            PropertyGetter, PropertySetter {

        private QName qname;
        private Class type;

        public SDOPropertyBuilder(QName qname, Class type) {
            this.qname = qname;
            this.type = type;
        }

        @Override
        public Class getType() {
            return type;
        }

        @Override
        public <A> A getAnnotation(Class<A> annotationType) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }

        // get the property value from the wrapper intance
        @Override
        public Object get(Object instance) {
            if (instance instanceof DataObject) {
                DataObject wrapperBean = (DataObject) instance;
                Property p = wrapperBean.getInstanceProperty(qname.getLocalPart());
                if (p == null) {
                    throw new SDODatabindingException("Property not found: " + qname.getLocalPart());
                }
                Object o = wrapperBean.get(p);
                return SDOUtils.unwrapPrimitives(o);
            } else {
                throw new SDODatabindingException("Invalid SDO object: " + instance);
            }
        }

        // set the property of the instance to the value
        @Override
        public void set(Object instance, Object value) {
            if (instance instanceof DataObject) {
                DataObject wrapperBean = (DataObject) instance;
                Property p = wrapperBean.getInstanceProperty(qname.getLocalPart());
                if (p == null) {
                    throw new SDODatabindingException("Property not found: " + qname.getLocalPart());
                }
                wrapperBean.set(p, value);
            } else {
                throw new SDODatabindingException("Invalid SDO object: " + instance);

            }
        }
    }
}
