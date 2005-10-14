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

package com.sun.xml.ws.client;

import com.sun.xml.ws.developer.JAXWSProperties;

public interface BindingProviderProperties extends JAXWSProperties{

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
    public static final String JAXWS_CLIENT_ASYNC_RESPONSE_CONTEXT =
        "com.sun.xml.ws.client.dispatch.async.response.context";
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

    // XML + XOP + SOAP 1.2
    public static final String XOP_SOAP12_XML_TYPE_VALUE =
        "application/xop+xml;type=\"application/soap+xml\"";

    public static final String XML_CONTENT_TYPE_VALUE = "text/xml";

    public static final String SOAP12_XML_CONTENT_TYPE_VALUE = "application/soap+xml";

    public static final String STANDARD_ACCEPT_VALUE =
        "application/xop+xml, text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2";

    public static final String SOAP12_XML_ACCEPT_VALUE =
        "application/soap+xml" + ", " + STANDARD_ACCEPT_VALUE;

    public static final String XML_ACCEPT_VALUE =
        XML_CONTENT_TYPE_VALUE + ", " + STANDARD_ACCEPT_VALUE;

    public static final String XML_FI_ACCEPT_VALUE =
        FAST_INFOSET_TYPE_SOAP11 + ", " + XML_ACCEPT_VALUE;

    public static final String SOAP12_XML_FI_ACCEPT_VALUE =
        FAST_INFOSET_TYPE_SOAP12 + ", " + SOAP12_XML_ACCEPT_VALUE;

    public String DISPATCH_CONTEXT = "com.sun.xml.ws.client.dispatch.context";

    public static final String BINDING_ID_PROPERTY = "com.sun.xml.ws.binding";

//    // Content negotiation property: values "none", "pessimistic" and "optimistic"
//    public static final String CONTENT_NEGOTIATION_PROPERTY =
//        "com.sun.xml.ws.client.ContentNegotiation";

}
