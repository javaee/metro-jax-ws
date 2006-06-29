/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */
package com.sun.xml.ws.handler;

import com.sun.xml.ws.spi.runtime.Invoker;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import javax.xml.ws.handler.LogicalMessageContext;
import com.sun.xml.ws.spi.runtime.MessageContext;

import com.sun.xml.ws.pept.ept.MessageInfo;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import com.sun.xml.ws.encoding.xml.XMLMessage;
import java.lang.reflect.Method;


/**
 * Version of {@link HandlerContext} for XML/HTTP binding that
 * only deals with logical messages.
 *
 * <p>Class has to defer information to HandlerContext so that properties
 * are shared between this and SOAPMessageContext.
 *
 * @see HandlerContext
 *
 * @author WS Development Team
 * @author WS Development Team
 */
public class XMLHandlerContext extends HandlerContext {

    private XMLMessage xmlMessage;
    private LogicalMessageContext logicalContext;
    private SHDXMLMessageContext shdXmlContext;

    public XMLHandlerContext(MessageInfo messageInfo,
            InternalMessage internalMessage,
            XMLMessage xmlMessage) {
        super(messageInfo, internalMessage);
        this.xmlMessage = xmlMessage;
    }

    public LogicalMessageContext getLogicalMessageContext() {
        if (logicalContext == null) {
            logicalContext = new XMLLogicalMessageContextImpl(this);
        }
        return logicalContext;
    }
    
    /**
     * @return Returns XMLMessage
     */
    public XMLMessage getXMLMessage() {
        return xmlMessage;
    }

    /**
     * @param xmlMessage The xmlMessage to set.
     */
    public void setXMLMessage(XMLMessage xmlMessage) {
        this.xmlMessage = xmlMessage;
    }
    
    public SHDXMLMessageContext getSHDXMLMessageContext() {
        if (shdXmlContext == null) {
            shdXmlContext = new SHDXMLMessageContext(this);
        }
        return shdXmlContext;
    }
    
    private static class SHDXMLMessageContext extends XMLLogicalMessageContextImpl implements com.sun.xml.ws.spi.runtime.MessageContext {

        XMLHandlerContext handlerCtxt;

        public SHDXMLMessageContext(XMLHandlerContext handlerCtxt) {
            super(handlerCtxt);
            this.handlerCtxt = handlerCtxt;
        }

        public String getBindingId() {
            return handlerCtxt.getBindingId();
        }

        public Method getMethod() {
            return handlerCtxt.getMethod();
        }

        public void setCanonicalization(String algorithm) {
            handlerCtxt.setCanonicalization(algorithm);
        }

        public Invoker getInvoker() {
            return handlerCtxt.getInvoker();
        }

        public boolean isMtomEnabled() {
            return false;
        }

    }

}
