/*
 * $Id: XMLLogicalMessageImpl.java,v 1.4 2005-08-10 01:51:29 jitu Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */
package com.sun.xml.ws.handler;

import javax.xml.bind.JAXBContext;
import javax.xml.ws.LogicalMessage;
import javax.xml.transform.Source;
import com.sun.xml.ws.encoding.xml.XMLMessage;

/**
 * Implementation of LogicalMessage that is used in the
 * XML/HTTP binding. It is similar to LogicalMessageImpl
 * except that the context object passed in is an
 * {@link XMLHandlerContext} rather than a {@link HandlerContext}.
 *
 * @see LogicalMessageImpl
 * @see XMLHandlerContext
 * @see XMLLogicalMessageContextImpl
 *
 * @author WS Development Team
 */
public class XMLLogicalMessageImpl implements LogicalMessage {

    private XMLHandlerContext ctxt;

    public XMLLogicalMessageImpl(XMLHandlerContext ctxt) {
        this.ctxt = ctxt;
    }

    /*
     * Gets the source from XMLMessage. XMLMessage gives a copy of existing
     * data
     */
    public Source getPayload() {
        XMLMessage xmlMessage = ctxt.getXMLMessage();
        return xmlMessage.getPayload();
    }

    /*
     * Sets the Source as payload in XMLMessage
     */
    public void setPayload(Source source) {
        XMLMessage xmlMessage = ctxt.getXMLMessage();
        xmlMessage.setPayload(source);
    }

    /*
     * Gets XMLMessage data as JAXB bean
     */
    public Object getPayload(JAXBContext jaxbContext) {
        XMLMessage xmlMessage = ctxt.getXMLMessage();
        return xmlMessage.getPayload(jaxbContext);
    }

    /*
     * Sets JAXB bean into XMLMessage
     */
    public void setPayload(Object bean, JAXBContext jaxbContext) {
        XMLMessage xmlMessage = ctxt.getXMLMessage();
        xmlMessage.setPayload(bean, jaxbContext);
    }

    public XMLHandlerContext getHandlerContext() {
        return ctxt;
    }

}
