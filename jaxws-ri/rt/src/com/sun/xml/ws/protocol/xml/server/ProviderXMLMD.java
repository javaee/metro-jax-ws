/**
 * $Id: ProviderXMLMD.java,v 1.3 2005-07-28 00:24:36 jitu Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.protocol.xml.server;

import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;

import com.sun.pept.ept.MessageInfo;
import com.sun.pept.presentation.MessageStruct;
import com.sun.xml.ws.handler.LogicalMessageImpl;
import com.sun.xml.ws.encoding.soap.internal.InternalMessage;
import com.sun.xml.ws.encoding.jaxb.LogicalEPTFactory;
import com.sun.xml.ws.encoding.soap.SOAPEncoder;
import com.sun.xml.ws.encoding.xml.XMLMessage;
import com.sun.xml.ws.handler.XMLHandlerContext;
import com.sun.xml.ws.server.RuntimeContext;
import com.sun.xml.ws.server.RuntimeEndpointInfo;
import com.sun.xml.ws.server.provider.ProviderPeptTie;
import com.sun.xml.ws.util.MessageInfoUtil;
import java.io.ByteArrayInputStream;
import java.util.Map;
import javax.activation.DataSource;
import javax.xml.transform.stream.StreamSource;

/**
 * @author WS Development Team
 *
 */

public class ProviderXMLMD extends XMLMessageDispatcher {

    /*
     * Fill the parameters, method in MessageInfo for Provider interface.
     * invoke(Source, XMLHandlerContext) to Object[]
     * invoke(SOAPMessage, XMLHandlerContext) to Object[]
     */
    @Override
    protected void toMessageInfo(MessageInfo messageInfo, XMLHandlerContext context) {
        Object[] data = new Object[2];
        data[1] = null;
        RuntimeContext rtCtxt = MessageInfoUtil.getRuntimeContext(messageInfo);
        RuntimeEndpointInfo endpointInfo = rtCtxt.getRuntimeEndpointInfo();
        Provider provider = (Provider)endpointInfo.getImplementor();
        Class providerClass = provider.getClass();
        boolean isSource = isSource(providerClass);
        boolean isDataSource = isDataSource(providerClass);
        if (!(isSource || isDataSource)) {
            throw new UnsupportedOperationException(
                    "Endpoint should implement Provider<Source> or Provider<DataSource>");
        }
        Service.Mode mode = getServiceMode(providerClass);
        if (mode == Service.Mode.PAYLOAD && isDataSource) {
            throw new UnsupportedOperationException(
                    "Illeagal combination Mode.PAYLOAD and Provider<DataSource>");
        } 
        XMLMessage xmlMessage = context.getXMLMessage();
        try {
            if (isSource) {
                data[0] = xmlMessage.getSource();
            } else {
                data[0] = xmlMessage.getDataSource();
            }
        } catch(Exception e) {
            messageInfo.setResponseType(MessageStruct.UNCHECKED_EXCEPTION_RESPONSE);
            messageInfo.setResponse(e);
        }
        
        data[1] = null;
        messageInfo.setData(data);
        messageInfo.setMethod(ProviderPeptTie.invoke_Method);
    }

    /*
     * MessageInfo contains the endpoint invocation results. If the endpoint
     * returns a SOAPMessage, just set the object in HandlerContext. If the
     * endpoint returns a Source in Mode.MESSAGE, it is converted to SOAPMessage
     * and set in HandlerContext. If the endpoint returns a Source in
     * Mode.PAYLOAD, it is set in InternalMessage, and InternalMessage is set
     * in HandlerContext
     */
    @Override
    protected void setResponseInContext(MessageInfo messageInfo,
            XMLHandlerContext context) {
        Object obj = messageInfo.getResponse();
        RuntimeContext rtCtxt = MessageInfoUtil.getRuntimeContext(messageInfo);
        RuntimeEndpointInfo endpointInfo = rtCtxt.getRuntimeEndpointInfo();
        Provider provider = (Provider)endpointInfo.getImplementor();
        Class providerClass = provider.getClass();
        XMLMessage xmlMessage = null;
        if (messageInfo.getResponseType() == MessageInfo.NORMAL_RESPONSE) {
            xmlMessage = (obj instanceof DataSource) 
                ? new XMLMessage((DataSource)obj) 
                : new XMLMessage((Source)obj);
        } else {
            xmlMessage = new XMLMessage((Exception)obj);
        }
        context.setXMLMessage(xmlMessage);
        context.setInternalMessage(null);
    }

    /*
     * In this case, Oneway is known only after invoking the endpoint. For other
     * endpoints, the HTTP response code is sent before invoking the endpoint.
     * This is taken care here after invoking the endpoint.
     */
    @Override
    protected void invokeEndpoint(MessageInfo messageInfo, XMLHandlerContext hc) {
        super.invokeEndpoint(messageInfo, hc);
        if (isOneway(messageInfo)) {
            sendResponseOneway(messageInfo);
        }
    }

    /*
     * Is it PAYLOAD or MESSAGE ??
     */
    private static Service.Mode getServiceMode(Class c) {
        ServiceMode mode = (ServiceMode)c.getAnnotation(ServiceMode.class);
        if (mode == null) {
            return Service.Mode.PAYLOAD;
        }
        return mode.value();
    }

    /*
     * Is it Provider<Source> ?
     */
    private static boolean isSource(Class c) {
        try {
            c.getMethod("invoke",  Source.class, Map.class);
            return true;
        } catch(NoSuchMethodException ne) {
            // ignoring intentionally
        }
        return false;
    }

    /*
     * Is it Provider<DataSource> ?
     */
    private static boolean isDataSource(Class c) {
        try {
            c.getMethod("invoke",  DataSource.class, Map.class);
            return true;
        } catch(NoSuchMethodException ne) {
            // ignoring intentionally
        }
        return false;
    }

}
