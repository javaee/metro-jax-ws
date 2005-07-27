/**
 * $Id: EPTFactoryFactoryBase.java,v 1.9 2005-07-27 18:50:04 jitu Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.server;

import com.sun.pept.ept.EPTFactory;
import com.sun.pept.ept.MessageInfo;
import com.sun.pept.presentation.TargetFinder;
import com.sun.pept.protocol.MessageDispatcher;
import com.sun.xml.ws.encoding.internal.InternalEncoder;
import com.sun.xml.ws.encoding.jaxb.LogicalEncoder;
import com.sun.xml.ws.encoding.soap.SOAPDecoder;
import com.sun.xml.ws.encoding.soap.SOAPEncoder;
import com.sun.xml.ws.encoding.soap.ServerEncoderDecoder;
import com.sun.xml.ws.server.provider.ProviderMessageDispatcher;
import com.sun.xml.ws.spi.runtime.MessageContext;
import javax.xml.ws.Provider;
import javax.xml.ws.soap.SOAPBinding;

import com.sun.xml.ws.util.MessageInfoUtil;
import com.sun.xml.ws.server.provider.ProviderPeptTie;
import com.sun.xml.ws.server.provider.ProviderSED;
import com.sun.xml.ws.binding.BindingImpl;
import com.sun.xml.ws.protocol.soap.server.SOAPMessageDispatcher;
import com.sun.xml.ws.encoding.soap.server.SOAP12XMLDecoder;
import com.sun.xml.ws.encoding.soap.server.SOAP12XMLEncoder;
import com.sun.xml.ws.encoding.soap.server.SOAPXMLDecoder;
import com.sun.xml.ws.encoding.soap.server.SOAPXMLEncoder;
import com.sun.xml.ws.encoding.xml.XMLDecoder;
import com.sun.xml.ws.encoding.xml.XMLEncoder;
import com.sun.xml.ws.protocol.xml.server.ProviderXMLMD;
import javax.xml.ws.http.HTTPBinding;

/**
 * factory for creating the appropriate EPTFactory given the BindingId from the EndpointInfo
 * in the RuntimeContext of the MessageInfo
 * Based on MessageInfo data(Binding, Implementor) it selects one the static EPTFactories 
 * using Binding information
 * Has a static EPTFactory object for each particular binding. 
 * EPTFactories are reused for all the requests.
 * Has static provider EPTFactory objects. 
 * The provider EPTFactories are reused for all the requests.
 * The factories reuse encoder, decoder, message dispatcher objects since these objects 
 * are Stateless. They are reused for all the requests.
 *
 * @author WS Development Team
 */
public abstract class EPTFactoryFactoryBase {

    public static final ProviderMessageDispatcher providerMessageDispatcher =
        new ProviderMessageDispatcher();
    public static final LogicalEncoder logicalEncoder = new LogicalEncoderImpl();
    public static final SOAPEncoder soap11Encoder = new SOAPXMLEncoder();
    public static final SOAPDecoder soap11Decoder = new SOAPXMLDecoder();
    public static final SOAPEncoder soap12Encoder = new SOAP12XMLEncoder();
    public static final SOAPDecoder soap12Decoder = new SOAP12XMLDecoder();
    
    public static final XMLEncoder xmlEncoder = null; //new XMLEncoder();
    public static final XMLDecoder xmlDecoder = null; //new XMLDecoder();

    public static final SOAPMessageDispatcher soap11MessageDispatcher =
        new SOAPMessageDispatcher();
    public static final MessageDispatcher providerXmlMD =
        new ProviderXMLMD();
    public static final InternalEncoder internalSED = new ServerEncoderDecoder();
    public static final InternalEncoder providerSED = new ProviderSED();
    public static final TargetFinder providerTargetFinder =
            new TargetFinderImpl(new ProviderPeptTie());
    public static final TargetFinder targetFinder =
            new TargetFinderImpl(new PeptTie());

    public static final EPTFactory providerSoap11 =
            new EPTFactoryBase(soap11Encoder, soap11Decoder,
                logicalEncoder, providerSED, providerTargetFinder,
                providerMessageDispatcher);

    public static final EPTFactory providerSoap12 =
            new EPTFactoryBase(soap12Encoder, soap12Decoder,
                logicalEncoder, providerSED, providerTargetFinder,
                providerMessageDispatcher);

    public static final EPTFactory soap11 =
            new EPTFactoryBase(
                soap11Encoder, soap11Decoder,
                logicalEncoder, internalSED, targetFinder,
                soap11MessageDispatcher);

    public static final EPTFactory soap12 =
            new EPTFactoryBase(
                soap12Encoder, soap12Decoder,
                logicalEncoder, internalSED, targetFinder,
                soap11MessageDispatcher);
    
    public static final EPTFactory providerXml =
        new XMLEPTFactoryImpl(
            xmlEncoder, xmlDecoder,
            logicalEncoder, providerSED, providerTargetFinder,
            providerXmlMD);

    public static boolean isEncodeFast(MessageContext context) {
        boolean fast = false;
/*
        SOAPMessage message = ((SOAPMessageContext)context).getMessage();
        MimeHeaders headers = message.getMimeHeaders();
        String[] values = headers.getHeader("Accept");
        if (values != null) {
            for(int i=0; i < values.length; i++) {
//System.out.println("****** Accept="+values[i]);
                if (values[i].indexOf("application/fastsoap") != -1) {
                    fast = true;
                    break;
                }
            }
        }
*/
        return fast;
    }

    public static boolean isDecodeFast(MessageContext context) {
        boolean fast = false;
/*
        SOAPMessage message = ((SOAPMessageContext)context).getMessage();
        MimeHeaders headers = message.getMimeHeaders();
        String[] values = headers.getHeader("Content-Type");
        if (values != null) {
            for(int i=0; i < values.length; i++) {
//System.out.println("****** Content-Type="+values[i]);
                if (values[i].indexOf("application/fastsoap") != -1) {
                    fast = true;
                    break;
                }
            }
        }
*/
        return fast;
    }
    

    
    /**
     * Choose correct EPTFactory. MessageInfo contains all the needed
     * information like Binding, WSConnection to make that decision.
     * @param mi the MessageInfo object to obtain the BindingID from.
     * @return returns the appropriate EPTFactory for the BindingID in the mi
     */
    public static EPTFactory getEPTFactory(MessageInfo mi) {
        RuntimeContext rtCtxt = MessageInfoUtil.getRuntimeContext(mi);
        RuntimeEndpointInfo endpointInfo = rtCtxt.getRuntimeEndpointInfo();
        String bindingId = ((BindingImpl)endpointInfo.getBinding()).getBindingId();
        if(bindingId.equals(SOAPBinding.SOAP11HTTP_BINDING)){
            if (endpointInfo.getImplementor() instanceof Provider) {
                return providerSoap11;
            } else {
                return soap11;
            }
        }else if(bindingId.equals(SOAPBinding.SOAP12HTTP_BINDING)){
            if (endpointInfo.getImplementor() instanceof Provider) {
                return providerSoap12;
            } else {
                return soap12;
            }
        } else if(bindingId.equals(HTTPBinding.HTTP_BINDING)){
            if (endpointInfo.getImplementor() instanceof Provider) {
                return providerXml;
            }
        }
        return null;
    }
}
