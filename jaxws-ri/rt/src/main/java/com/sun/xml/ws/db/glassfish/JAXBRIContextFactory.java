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

package com.sun.xml.ws.db.glassfish;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.lang.reflect.Type;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import com.sun.xml.bind.api.TypeReference;
import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.bind.api.CompositeStructure;
import com.sun.xml.bind.v2.ContextFactory;
import com.sun.xml.bind.v2.model.annotation.RuntimeAnnotationReader;
import com.sun.xml.bind.v2.runtime.MarshallerImpl;
import com.sun.xml.ws.developer.JAXBContextFactory;
import com.sun.xml.ws.spi.db.BindingContext;
import com.sun.xml.ws.spi.db.BindingContextFactory;
import com.sun.xml.ws.spi.db.BindingInfo;
import com.sun.xml.ws.spi.db.DatabindingException;
import com.sun.xml.ws.spi.db.TypeInfo;
import com.sun.xml.ws.spi.db.WrapperComposite;
import java.util.Arrays;

/**
 * JAXBRIContextFactory
 *
 * @author shih-chang.chen@oracle.com
 */
public class JAXBRIContextFactory extends BindingContextFactory {

    @Override
    public BindingContext newContext(JAXBContext context) {
        return new JAXBRIContextWrapper((JAXBRIContext) context, null);
    }

    @Override
    public BindingContext newContext(BindingInfo bi) {
        Class[] classes = bi.contentClasses().toArray(new Class[bi.contentClasses().size()]);
        for (int i = 0; i < classes.length; i++) {
            if (WrapperComposite.class.equals(classes[i])) {
                classes[i] = CompositeStructure.class;
            }
        }
        Map<TypeInfo, TypeReference> typeInfoMappings = typeInfoMappings(bi.typeInfos());
        Map<Class, Class> subclassReplacements = bi.subclassReplacements();
        String defaultNamespaceRemap = bi.getDefaultNamespace();
        Boolean c14nSupport = (Boolean) bi.properties().get("c14nSupport");
        RuntimeAnnotationReader ar = (RuntimeAnnotationReader) bi.properties().get("com.sun.xml.bind.v2.model.annotation.RuntimeAnnotationReader");
        JAXBContextFactory jaxbContextFactory = (JAXBContextFactory) bi.properties().get(JAXBContextFactory.class.getName());
        try {
            JAXBRIContext context = (jaxbContextFactory != null)
                    ? jaxbContextFactory.createJAXBContext(
                    bi.getSEIModel(),
                    toList(classes),
                    toList(typeInfoMappings.values()))
                    : ContextFactory.createContext(
                    classes, typeInfoMappings.values(),
                    subclassReplacements, defaultNamespaceRemap,
                    (c14nSupport != null) ? c14nSupport : false,
                    ar, false, false, false);
            return new JAXBRIContextWrapper(context, typeInfoMappings);
        } catch (Exception e) {
            throw new DatabindingException(e);
        }
    }

    private <T> List<T> toList(T[] a) {
        List<T> l = new ArrayList<T>();
        l.addAll(Arrays.asList(a));
        return l;
    }

    private <T> List<T> toList(Collection<T> col) {
        if (col instanceof List) {
            return (List<T>) col;
        }
        List<T> l = new ArrayList<T>();
        l.addAll(col);
        return l;
    }

    private Map<TypeInfo, TypeReference> typeInfoMappings(Collection<TypeInfo> typeInfos) {
        Map<TypeInfo, TypeReference> map = new java.util.HashMap<TypeInfo, TypeReference>();
        for (TypeInfo ti : typeInfos) {
            Type type = WrapperComposite.class.equals(ti.type) ? CompositeStructure.class : ti.type;
            TypeReference tr = new TypeReference(ti.tagName, type, ti.annotations);
            map.put(ti, tr);
        }
        return map;
    }

    @Override
    protected BindingContext getContext(Marshaller m) {
        return newContext(((MarshallerImpl) m).getContext());
    }

    @Override
    protected boolean isFor(String str) {
        return (str.equals("glassfish.jaxb")
                || str.equals(this.getClass().getName())
                || str.equals("com.sun.xml.bind.v2.runtime"));
    }
}
