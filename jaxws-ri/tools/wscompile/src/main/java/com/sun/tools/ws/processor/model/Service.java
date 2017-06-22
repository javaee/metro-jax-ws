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

import com.sun.tools.ws.processor.model.java.JavaInterface;
import com.sun.tools.ws.wsdl.framework.Entity;

import javax.xml.namespace.QName;
import java.util.*;

/**
 *
 * @author WS Development Team
 */
public class Service extends ModelObject {

    public Service(Entity entity) {
        super(entity);
    }

    public Service(QName name, JavaInterface javaInterface, Entity entity) {
        super(entity);
        this.name = name;
        this.javaInterface = javaInterface;
    }

    public QName getName() {
        return name;
    }

    public void setName(QName n) {
        name = n;
    }

    public void addPort(Port port) {
        if (portsByName.containsKey(port.getName())) {
            throw new ModelException("model.uniqueness");
        }
        ports.add(port);
        portsByName.put(port.getName(), port);
    }


    public Port getPortByName(QName n) {
        if (portsByName.size() != ports.size()) {
            initializePortsByName();
        }
        return (Port) portsByName.get(n);
    }

    /* serialization */
    public List<Port> getPorts() {
        return ports;
    }

    /* serialization */
    public void setPorts(List<Port> m) {
        ports = m;
//        initializePortsByName();
    }

    private void initializePortsByName() {
        portsByName = new HashMap();
        if (ports != null) {
            for (Iterator iter = ports.iterator(); iter.hasNext();) {
                Port port = (Port) iter.next();
                if (port.getName() != null &&
                    portsByName.containsKey(port.getName())) {

                    throw new ModelException("model.uniqueness");
                }
                portsByName.put(port.getName(), port);
            }
        }
    }

    public JavaInterface getJavaIntf() {
        return getJavaInterface();
    }

    public JavaInterface getJavaInterface() {
        return javaInterface;
    }

    public void setJavaInterface(JavaInterface i) {
        javaInterface = i;
    }

    public void accept(ModelVisitor visitor) throws Exception {
        visitor.visit(this);
    }

    private QName name;
    private List<Port> ports = new ArrayList();
    private Map<QName, Port> portsByName = new HashMap<QName, Port>();
    private JavaInterface javaInterface;
}
