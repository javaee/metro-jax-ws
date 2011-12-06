/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

import com.sun.tools.ws.processor.model.jaxb.JAXBModel;
import com.sun.tools.ws.wsdl.framework.Entity;

import javax.xml.namespace.QName;
import java.util.*;

/**
 * The model is used to represent the entire Web Service.  The JAX-WS ProcessorActions can process
 * this Model to generate Java artifacts such as the service interface.
 *
 * @author WS Development Team
 */
public class Model extends ModelObject {

    public Model(Entity entity) {
        super(entity);
    }

    public Model(QName name, Entity entity) {
        super(entity);
        this.name = name;
    }

    public QName getName() {
        return name;
    }

    public void setName(QName n) {
        name = n;
    }

    public String getTargetNamespaceURI() {
        return targetNamespace;
    }

    public void setTargetNamespaceURI(String s) {
        targetNamespace = s;
    }

    public void addService(Service service) {
        if (servicesByName.containsKey(service.getName())) {
            throw new ModelException("model.uniqueness");
        }
        services.add(service);
        servicesByName.put(service.getName(), service);
    }

    public Service getServiceByName(QName name) {
        if (servicesByName.size() != services.size()) {
            initializeServicesByName();
        }
        return (Service)servicesByName.get(name);
    }

    /* serialization */
    public List<Service> getServices() {
        return services;
    }

    /* serialization */
    public void setServices(List<Service> l) {
        services = l;
    }

    private void initializeServicesByName() {
        servicesByName = new HashMap();
        if (services != null) {
            for (Service service : services) {
                if (service.getName() != null &&
                    servicesByName.containsKey(service.getName())) {

                    throw new ModelException("model.uniqueness");
                }
                servicesByName.put(service.getName(), service);
            }
        }
    }

    public void addExtraType(AbstractType type) {
        extraTypes.add(type);
    }

    public Iterator getExtraTypes() {
        return extraTypes.iterator();
    }

    /* serialization */
    public Set<AbstractType> getExtraTypesSet() {
        return extraTypes;
    }

    /* serialization */
    public void setExtraTypesSet(Set<AbstractType> s) {
        extraTypes = s;
    }


    public void accept(ModelVisitor visitor) throws Exception {
        visitor.visit(this);
    }

    /**
     * @return the source version
     */
    public String getSource() {
        return source;
    }

    /**
     * @param string
     */
    public void setSource(String string) {
        source = string;
    }

    public void setJAXBModel(JAXBModel jaxBModel) {
        this.jaxBModel = jaxBModel;
    }

    public JAXBModel getJAXBModel() {
        return jaxBModel;
    }

    private QName name;
    private String targetNamespace;
    private List<Service> services = new ArrayList<Service>();
    private Map<QName, Service> servicesByName = new HashMap<QName, Service>();
    private Set<AbstractType> extraTypes = new HashSet<AbstractType>();
    private String source;
    private JAXBModel jaxBModel = null;
}
