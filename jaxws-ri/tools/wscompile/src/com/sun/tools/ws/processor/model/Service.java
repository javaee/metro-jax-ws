/*
 * $Id: Service.java,v 1.3 2005-08-04 21:53:23 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
