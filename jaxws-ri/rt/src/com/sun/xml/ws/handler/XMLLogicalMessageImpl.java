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
        xmlMessage = new XMLMessage(source, xmlMessage.getAttachments(), xmlMessage.useFastInfoset());
        ctxt.setXMLMessage(xmlMessage);
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
        xmlMessage = new XMLMessage(bean, jaxbContext, xmlMessage.getAttachments(), xmlMessage.useFastInfoset());
        ctxt.setXMLMessage(xmlMessage);
    }

    public XMLHandlerContext getHandlerContext() {
        return ctxt;
    }

}
