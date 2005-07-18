/*
 * $Id: JAXBBeanInfo.java,v 1.2 2005-07-18 16:52:10 kohlert Exp $
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
 * @author WS Development Team
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
