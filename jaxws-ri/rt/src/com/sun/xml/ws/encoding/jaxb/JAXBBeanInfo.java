/*
 * $Id: JAXBBeanInfo.java,v 1.1 2005-05-23 22:28:40 bbissett Exp $
 */

/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.encoding.jaxb;

import javax.xml.bind.JAXBContext;

/*
 * BodyBlock may contain JAXBBeanInfo object. This object holds
 * a bean object and a JAXBContext. Runtime invokes JAXB API with the given
 * JAXBContext to marshall this bean.
 *
 * @author JAX-RPC Development Team
 */
public class JAXBBeanInfo {
    private Object jaxbBean;
    private JAXBContext jaxbContext;

    public JAXBBeanInfo(Object payload, JAXBContext jaxbContext) {
        this.jaxbBean = payload;
        this.jaxbContext = jaxbContext;
    }

    public Object getBean() {
        return jaxbBean;
    }

    public JAXBContext getJAXBContext() {
        return jaxbContext;
    }
}
