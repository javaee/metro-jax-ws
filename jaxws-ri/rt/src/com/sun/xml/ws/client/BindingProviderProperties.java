/*
 * $Id: BindingProviderProperties.java,v 1.6 2005-07-28 21:56:53 spericas Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */

package com.sun.xml.ws.client;

public interface BindingProviderProperties {

    //legacy properties
    public static final String SERVICEIMPL_NAME = "serviceImplementationName";
    public static final String HOSTNAME_VERIFICATION_PROPERTY =
        "com.sun.xml.ws.client.http.HostnameVerificationProperty";
    public static final String HTTP_COOKIE_JAR =
        "com.sun.xml.ws.client.http.CookieJar";
    public static final String SECURITY_CONTEXT =
        "com.sun.xml.ws.security.context";
    public static final String HTTP_STATUS_CODE =
        "com.sun.xml.ws.client.http.HTTPStatusCode";

    public static final String REDIRECT_REQUEST_PROPERTY =
        "com.sun.xml.ws.client.http.RedirectRequestProperty";
    public static final String SET_ATTACHMENT_PROPERTY =
        "com.sun.xml.ws.attachment.SetAttachmentContext";
    public static final String GET_ATTACHMENT_PROPERTY =
        "com.sun.xml.ws.attachment.GetAttachmentContext";
    public static final String ONE_WAY_OPERATION =
        "com.sun.xml.ws.server.OneWayOperation";


    //JAXWS 2.0
    public static final String JAXWS_RUNTIME_CONTEXT =
        "com.sun.xml.ws.runtime.context";
    public static final String JAXWS_CONTEXT_PROPERTY =
        "com.sun.xml.ws.context.request";
    public static final String JAXWS_HANDLER_CONTEXT_PROPERTY =
        "com.sun.xml.ws.handler.context";
    public static final String JAXWS_RESPONSE_CONTEXT_PROPERTY =
        "com.sun.xml.ws.context.response";
    public static final String JAXWS_CLIENT_ASYNC_HANDLER =
        "com.sun.xml.ws.client.dispatch.asynchandler";
    public static final String JAXWS_CLIENT_HANDLE_PROPERTY =
        "com.sun.xml.ws.client.handle";
    public static final String JAXB_CONTEXT_PROPERTY =
        "com.sun.xml.ws.jaxbcontext";

    public static final String CLIENT_TRANSPORT_FACTORY = 
        "com.sun.xml.ws.client.ClientTransportFactory";
    
    public static final String JAXB_OUTPUTSTREAM =
        "com.sun.xml.bind.api.Bridge.outputStream";

    public static final String XML_ENCODING_VALUE = "xml.encoding";                 // deprecated
    public static final String ACCEPT_ENCODING_PROPERTY = "accept.encoding";
    
    public static final String CONTENT_TYPE_PROPERTY = "Content-Type";
    public static final String SOAP_ACTION_PROPERTY = "SOAPAction";
    public static final String ACCEPT_PROPERTY = "Accept";
    
    // FI + SOAP 1.1
    public static final String FAST_INFOSET_TYPE_SOAP11 =
        "application/fastinfoset";

    // FI + SOAP 1.2
    public static final String FAST_INFOSET_TYPE_SOAP12 =
        "application/soap+fastinfoset";

    // XML + XOP + SOAP 1.1
    public static final String XOP_SOAP11_XML_TYPE_VALUE = 
        "application/xop+xml;type=\"text/xml\"";
    
    // FI + XOP + SOAP 1.1
    public static final String XOP_SOAP11_FI_TYPE_VALUE  = 
        "application/xop+xml;type=\"application/fastinfoset\"";
    
    // XML + XOP + SOAP 1.2
    public static final String XOP_SOAP12_XML_TYPE_VALUE = 
        "application/xop+xml;type=\"application/soap+xml\"";
    
    // FI + XOP + SOAP 1.2
    public static final String XOP_SOAP12_FI_TYPE_VALUE  = 
        "application/xop+xml;type=\"application/soap+fastinfoset\"";
    
    public static final String XML_CONTENT_TYPE_VALUE = "text/xml";
    
    public static final String SOAP12_XML_CONTENT_TYPE_VALUE = "application/soap+xml";
    
    public static final String STANDARD_ACCEPT_VALUE =
        "application/xop+xml, text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2";
    
    public static final String SOAP12_XML_ACCEPT_VALUE =
        "application/soap+xml"+ ", " + STANDARD_ACCEPT_VALUE;
    
    public static final String XML_ACCEPT_VALUE =
        XML_CONTENT_TYPE_VALUE + ", " + STANDARD_ACCEPT_VALUE;
    
    public static final String XML_FI_ACCEPT_VALUE =
        FAST_INFOSET_TYPE_SOAP11 + ", " + XML_ACCEPT_VALUE;
    
    public static final String SOAP12_XML_FI_ACCEPT_VALUE =
        FAST_INFOSET_TYPE_SOAP12 + ", " + SOAP12_XML_ACCEPT_VALUE;
    
    public String DISPATCH_CONTEXT = "com.sun.xml.ws.client.dispatch.context";
    
    public static final String BINDING_ID_PROPERTY = "com.sun.xml.ws.binding";

    // Content negotiation property: values "none", "pessimistic" and "optimistic"
    public static final String CONTENT_NEGOTIATION_PROPERTY =
        "com.sun.xml.ws.client.ContentNegotiation";

}
