/*
 * $Id: LogicalEncoder.java,v 1.1 2005-05-23 22:28:41 bbissett Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */
package com.sun.xml.ws.encoding.jaxb;

import javax.xml.bind.JAXBContext;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;

/**
 * Payload is converted from one form to the other
 */
public interface LogicalEncoder {
    public DOMSource toDOMSource(JAXBBeanInfo beanInfo);
    public JAXBBeanInfo toJAXBBeanInfo(Source source, JAXBContext jaxbContext);
}
