/*
 * $Id: BindingProviderProperties.java,v 1.4 2005-07-19 18:10:01 arungupta Exp $
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

    String CLIENT_TRANSPORT_FACTORY = "com.sun.xml.ws.client.ClientTransportFactory";

    String XMLFAST_ENCODING_PROPERTY = "xml.fast.encoding";     // deprecated
    String XML_ENCODING_VALUE = "xml.encoding";                 // deprecated
    String FAST_ENCODING_VALUE = "fast.encoding";               // deprecated
    String ACCEPT_ENCODING_PROPERTY = "accept.encoding";
    String CONTENT_TYPE_PROPERTY = "Content-Type";
    String ACCEPT_PROPERTY = "Accept";
    String FAST_CONTENT_TYPE_VALUE = "application/fastsoap";    // deprecated
    String XML_CONTENT_TYPE_VALUE = "text/xml";
    String STANDARD_ACCEPT_VALUE = "application/xop+xml, text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2";
    String SOAP12_XML_ACCEPT_VALUE = "application/soap+xml"+ ", " + STANDARD_ACCEPT_VALUE;
    String XML_ACCEPT_VALUE = XML_CONTENT_TYPE_VALUE + ", " + STANDARD_ACCEPT_VALUE;
    String FAST_ACCEPT_VALUE = FAST_CONTENT_TYPE_VALUE + ", " + STANDARD_ACCEPT_VALUE;      // deprecated
    String SOAP_ACTION_PROPERTY = "SOAPAction";

    public String DISPATCH_CONTEXT = "com.sun.xml.ws.client.dispatch.context";

    // -- FI constants ---------------------------------------------------

    /**
     * A string-valued property "none", "pessimistic" and "optimistic"
     * Used for Fast Infoset content negotiation
     */
    public static final String CONTENT_NEGOTIATION_PROPERTY =
        "com.sun.xml.rpc.client.ContentNegotiation";

    // FI MIME using SOAP 1.1
    public static final String FAST_INFOSET_TYPE_SOAP11 =
        "application/fastinfoset";

    // FI MIME using SOAP 1.2
    public static final String FAST_INFOSET_TYPE_SOAP12 =
        "application/soap+fastinfoset";

    // HTTP accept for FI and XML supporting both SOAP 1.1 and SOAP 1.2
    public static final String FAST_INFOSET_ACCEPT_VALUE =
        FAST_INFOSET_TYPE_SOAP11 + ", " + FAST_INFOSET_TYPE_SOAP12 +
        ", text/xml, application/soap+xml, text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2";
    
    public static final String BINDING_ID_PROPERTY = "com.sun.xml.ws.binding";
}
