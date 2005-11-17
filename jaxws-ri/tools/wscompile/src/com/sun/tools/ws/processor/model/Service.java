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

import com.sun.tools.ws.processor.model.java.JavaInterface;

import javax.xml.namespace.QName;
import java.util.*;

/**
 *
 * @author WS Development Team
 */
public class Service extends ModelObject {

    public Service() {}

    public Service(QName name, JavaInterface javaInterface) {
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
