/*
 * $Id: XMLLogicalMessageImpl.java,v 1.3 2005-08-10 01:36:45 jitu Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */
package com.sun.xml.ws.handler;

import javax.xml.bind.JAXBContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.LogicalMessage;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;

import com.sun.pept.ept.MessageInfo;
import com.sun.xml.ws.encoding.jaxb.JAXBBeanInfo;
import com.sun.xml.ws.encoding.jaxb.LogicalEPTFactory;
import com.sun.xml.ws.encoding.jaxb.LogicalEncoder;
import com.sun.xml.ws.encoding.soap.internal.BodyBlock;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import com.sun.xml.ws.util.xml.XmlUtil;
import com.sun.xml.ws.encoding.xml.XMLEPTFactory;
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
     *
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
     *
     */
    public Object getPayload(JAXBContext jaxbContext) {
        XMLMessage xmlMessage = ctxt.getXMLMessage();
        return xmlMessage.getPayload(jaxbContext);
    }

    /*
     *
     */
    public void setPayload(Object bean, JAXBContext jaxbContext) {
        XMLMessage xmlMessage = ctxt.getXMLMessage();
        xmlMessage.setPayload(bean, jaxbContext);
    }

    public XMLHandlerContext getHandlerContext() {
        return ctxt;
    }

}
