/*
 * $Id: JAXRPCRuntimeInfoParser.java,v 1.11 2005-07-14 02:01:28 arungupta Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.transport.http.servlet;

import com.sun.xml.ws.server.RuntimeEndpointInfo;
import com.sun.xml.ws.server.ServerRtException;
import com.sun.xml.ws.streaming.XMLStreamReaderFactory;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.xml.namespace.QName;

import com.sun.xml.ws.streaming.Attributes;
import com.sun.xml.ws.streaming.XMLStreamReaderUtil;
import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;
import com.sun.xml.ws.binding.soap.SOAPBindingImpl;

import javax.xml.ws.handler.Handler;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * @author JAX-RPC Development Team
 */
public class JAXRPCRuntimeInfoParser {

    public JAXRPCRuntimeInfoParser(ClassLoader cl) {
        classLoader = cl;
    }

    public List<RuntimeEndpointInfo> parse(InputStream is) {
        try {
            XMLStreamReader reader =
                XMLStreamReaderFactory.createXMLStreamReader(is, true);
            XMLStreamReaderUtil.nextElementContent(reader);
            return parseEndpoints(reader);
        } catch (XMLStreamException e) {
            throw new ServerRtException("runtime.parser.xmlReader",
                new LocalizableExceptionAdapter(e));
        }
    }

    protected List<RuntimeEndpointInfo> parseEndpoints(XMLStreamReader reader)
    throws XMLStreamException {
        if (!reader.getName().equals(QNAME_ENDPOINTS)) {
            failWithFullName("runtime.parser.invalidElement", reader);
        }

        List<RuntimeEndpointInfo> endpoints = new ArrayList();

        Attributes attrs = XMLStreamReaderUtil.getAttributes(reader);
        String version = getMandatoryNonEmptyAttribute(reader, attrs, ATTR_VERSION);
        if (!version.equals(ATTRVALUE_VERSION_1_0)) {
            failWithLocalName("runtime.parser.invalidVersionNumber",
                reader, version);
        }

        while (XMLStreamReaderUtil.nextElementContent(reader) !=
            XMLStreamConstants.END_ELEMENT) {
            if (reader.getName().equals(QNAME_ENDPOINT)) {
                RuntimeEndpointInfo rei = new RuntimeEndpointInfo();
                attrs = XMLStreamReaderUtil.getAttributes(reader);
                rei.setName(getMandatoryNonEmptyAttribute(reader, attrs, ATTR_NAME));
                String implementationName =
                    getMandatoryNonEmptyAttribute(reader, attrs, ATTR_IMPLEMENTATION);
                rei.setImplementor(getImplementor(implementationName));
                String wsdlFile = getAttribute(attrs, ATTR_WSDL);
                if (wsdlFile != null &&
                        !wsdlFile.startsWith(JAXRPCContextListener.JAXWS_WSDL_DIR)) {
                    logger.warning("Ignoring wrong wsdl="+wsdlFile+". It should start with "
                            +JAXRPCContextListener.JAXWS_WSDL_DIR
                            +". Going to generate and publish a new WSDL.");
                    wsdlFile = null;
                }
                rei.setWSDLFileName(wsdlFile);
                rei.setServiceName(getQNameAttribute(attrs, ATTR_SERVICE));
                rei.setPortName(getQNameAttribute(attrs, ATTR_PORT));

                //get enable-mtom attribute value
                String mtom = getAttribute(attrs, ATTR_ENABLE_MTOM);                
                rei.setMtomEnabled((mtom != null)?Boolean.valueOf(mtom):false);

                //get bindingId
                String bindingId = getAttribute(attrs, ATTR_BINDING);
                //if bindingId is null default to SOAP 1.1
                if(bindingId == null){
                    rei.setBinding(new SOAPBindingImpl(SOAPBinding.SOAP11HTTP_BINDING));
                }else if(bindingId.equals(SOAPBinding.SOAP11HTTP_BINDING) ||
                        bindingId.equals(SOAPBinding.SOAP12HTTP_BINDING)){
                    rei.setBinding(new SOAPBindingImpl(bindingId));
                }
                rei.setMtomEnabled((mtom != null)?Boolean.valueOf(mtom):false);

                rei.setUrlPattern(
                    getMandatoryNonEmptyAttribute(reader, attrs, ATTR_URL_PATTERN));
                setHandlersAndRoles(rei, reader);
                ensureNoContent(reader);
                endpoints.add(rei);
            } else {
                failWithLocalName("runtime.parser.invalidElement", reader);
            }
        }

        reader.close();

        return endpoints;
    }

    protected String getAttribute(Attributes attrs, String name) {
        String value = attrs.getValue(name);
        if (value != null) {
            value = value.trim();
        }
        return value;
    }

    protected QName getQNameAttribute(Attributes attrs, String name) {
        String value = getAttribute(attrs, name);
        if (value == null || value.equals("")) {
            return null;
        } else {
            return QName.valueOf(value);
        }
    }

    protected String getNonEmptyAttribute(XMLStreamReader reader, Attributes attrs, String name) {
        String value = getAttribute(attrs, name);
        if (value != null && value.equals("")) {
            failWithLocalName(
                "runtime.parser.invalidAttributeValue",
                reader,
                name);
        }
        return value;
    }

    protected String getMandatoryAttribute(XMLStreamReader reader, Attributes attrs, String name) {
        String value = getAttribute(attrs, name);
        if (value == null) {
            failWithLocalName("runtime.parser.missing.attribute", reader, name);
        }
        return value;
    }

    protected String getMandatoryNonEmptyAttribute(XMLStreamReader reader, Attributes attributes,
        String name) {
        String value = getAttribute(attributes, name);
        if (value == null) {
            failWithLocalName("runtime.parser.missing.attribute", reader, name);
        } else if (value.equals("")) {
            failWithLocalName(
                "runtime.parser.invalidAttributeValue",
                reader,
                name);
        }
        return value;
    }

    /*
     * Parses the handler and role information and sets it
     * on the RuntimeEndpointInfo.
     *
     * todo: use code in HandlerAnnotationProcessor for this
     */
    protected void setHandlersAndRoles(RuntimeEndpointInfo rei,
        XMLStreamReader reader) {

        // first check for handler-chain element
        if (XMLStreamReaderUtil.nextElementContent(reader) ==
            XMLStreamConstants.END_ELEMENT ||
            !reader.getName().equals(QNAME_HANDLER_CHAIN)) {

            return;
        }

        List<Handler> handlerChain = new ArrayList<Handler>();
        Set<String> roles = new HashSet<String>();

        XMLStreamReaderUtil.nextElementContent(reader);
        if (reader.getName().equals(QNAME_HANDLER_CHAIN_NAME)) {
            skipTextElement(reader);
        }

        // process all <handler> elements
        while (reader.getName().equals(QNAME_HANDLER)) {
            Handler handler = null;
            Map<String, String> initParams = new HashMap<String, String>();
            Set<QName> headers = new HashSet<QName>();

            XMLStreamReaderUtil.nextContent(reader);
            if (reader.getName().equals(QNAME_HANDLER_NAME)) {
                skipTextElement(reader);
            }

            // handler class
            ensureProperName(reader, QNAME_HANDLER_CLASS);
            try {
                handler = (Handler) loadClass(
                    XMLStreamReaderUtil.getElementText(reader)).newInstance();
            } catch (InstantiationException ie){
                throw new RuntimeException(ie);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            XMLStreamReaderUtil.nextContent(reader);

            // init params
            while (reader.getName().equals(QNAME_HANDLER_PARAM)) {
                XMLStreamReaderUtil.nextContent(reader);

                // Skip <description> at either end in case users have
                // confused which schema we're using (don't want to fail
                // over this).
                if (reader.getLocalName().equals("description")) {
                    skipTextElement(reader);
                }
                
                ensureProperName(reader, QNAME_HANDLER_PARAM_NAME);
                String paramName = XMLStreamReaderUtil.getElementText(reader);

                XMLStreamReaderUtil.nextContent(reader);
                ensureProperName(reader, QNAME_HANDLER_PARAM_VALUE);
                String paramValue = XMLStreamReaderUtil.getElementText(reader);
                initParams.put(paramName, paramValue);

                XMLStreamReaderUtil.nextContent(reader); // past param-value
                
                // skip <description> if present
                if (reader.getLocalName().equals("description")) {
                    skipTextElement(reader);
                }
                XMLStreamReaderUtil.nextContent(reader); // past init-param
            }

            // headers (ignored)
            while (reader.getName().equals(QNAME_HANDLER_HEADER)) {
                skipTextElement(reader);
            }
            
            // roles (not stored per handler)
            while (reader.getName().equals(QNAME_HANDLER_ROLE)) {
                roles.add(XMLStreamReaderUtil.getElementText(reader));
                XMLStreamReaderUtil.nextContent(reader);
            }

            // finish handler info and add to chain
            if (!initParams.isEmpty()) {
                handler.init(initParams);
            }
            if (!headers.isEmpty()) {
                QName [] headerArray = new QName [headers.size()];
                int i = 0;
                for (QName header : headers) {
                    headerArray[i++] = header;
                }
            }
            handlerChain.add(handler);

            // move past </handler>
            XMLStreamReaderUtil.nextContent(reader);
        }

        rei.getBinding().setHandlerChain(handlerChain);
        if (!roles.isEmpty() &&
            rei.getBinding() instanceof SOAPBinding) {
            Set<URI> uriRoles = new HashSet<URI>(roles.size());
            ((SOAPBinding) rei.getBinding()).setRoles(uriRoles);
        }

        // move past </handler-chain>
        XMLStreamReaderUtil.nextContent(reader);
    }

    // utility method
    protected static void ensureProperName(XMLStreamReader reader,
        QName expectedName) {

        if (!reader.getName().equals(expectedName)) {
            failWithLocalName("runtime.parser.wrong.element", reader,
                expectedName.getLocalPart());
        }
    }

    // utility method for setHandlersAndRoles
    protected static void skipTextElement(XMLStreamReader reader) {
        XMLStreamReaderUtil.nextContent(reader);
        XMLStreamReaderUtil.nextElementContent(reader);
        XMLStreamReaderUtil.nextElementContent(reader);
    }
    
    protected static void ensureNoContent(XMLStreamReader reader) {
        if (reader.getEventType() != XMLStreamConstants.END_ELEMENT) {
            fail("runtime.parser.unexpectedContent", reader);
        }
    }

    protected static void fail(String key, XMLStreamReader reader) {
        logger.log(Level.SEVERE, key + reader.getLocation().getLineNumber());
        throw new ServerRtException(
            key,
            Integer.toString(reader.getLocation().getLineNumber()));
    }

    protected static void failWithFullName(String key, XMLStreamReader reader) {
        throw new ServerRtException(
            key,
            new Object[] {
                Integer.toString(reader.getLocation().getLineNumber()),
                reader.getName().toString()});
    }

    protected static void failWithLocalName(String key, XMLStreamReader reader) {
        throw new ServerRtException(
            key,
            new Object[] {
                Integer.toString(reader.getLocation().getLineNumber()),
                reader.getLocalName()});
    }

    protected static void failWithLocalName(
        String key,
        XMLStreamReader reader,
        String arg) {
        throw new ServerRtException(
            key,
            new Object[] {
                Integer.toString(reader.getLocation().getLineNumber()),
                reader.getLocalName(),
                arg });
    }

    protected Class loadClass(String name) {
        try {
            return Class.forName(name, true, classLoader);
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new ServerRtException(
                "runtime.parser.classNotFound",
                name);
        }
    }

    /*
     * Instantiates endpoint implementation
     */
    protected Object getImplementor(String name) {
        try {
            return Class.forName(name, true, classLoader).newInstance();
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new ServerRtException(
                "runtime.parser.classNotFound", name);
        } catch (InstantiationException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new ServerRtException(
                "error.implementorFactory.newInstanceFailed", name);
        } catch (IllegalAccessException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new ServerRtException(
                "error.implementorFactory.newInstanceFailed", name);
        }
    }

    protected ClassLoader classLoader;

    public static final String NS_RUNTIME =
        "http://java.sun.com/xml/ns/jax-ws/ri/runtime";

    public static final QName QNAME_ENDPOINTS =
        new QName(NS_RUNTIME, "endpoints");
    public static final QName QNAME_ENDPOINT =
        new QName(NS_RUNTIME, "endpoint");
    public static final QName QNAME_HANDLER_CHAIN =
        new QName(NS_RUNTIME, "handler-chain");
    public static final QName QNAME_HANDLER_CHAIN_NAME =
        new QName(NS_RUNTIME, "handler-chain-name");
    public static final QName QNAME_HANDLER =
        new QName(NS_RUNTIME, "handler");
    public static final QName QNAME_HANDLER_NAME =
        new QName(NS_RUNTIME, "handler-name");
    public static final QName QNAME_HANDLER_CLASS =
        new QName(NS_RUNTIME, "handler-class");
    public static final QName QNAME_HANDLER_PARAM =
        new QName(NS_RUNTIME, "init-param");
    public static final QName QNAME_HANDLER_PARAM_NAME =
        new QName(NS_RUNTIME, "param-name");
    public static final QName QNAME_HANDLER_PARAM_VALUE =
        new QName(NS_RUNTIME, "param-value");
    public static final QName QNAME_HANDLER_HEADER =
        new QName(NS_RUNTIME, "soap-header");
    public static final QName QNAME_HANDLER_ROLE =
        new QName(NS_RUNTIME, "soap-role");

    public static final String ATTR_VERSION = "version";
    public static final String ATTR_NAME = "name";
    public static final String ATTR_IMPLEMENTATION = "implementation";
    public static final String ATTR_WSDL = "wsdl";
    public static final String ATTR_SERVICE = "service";
    public static final String ATTR_PORT = "port";
    public static final String ATTR_URL_PATTERN = "urlpattern";
    public static final String ATTR_ENABLE_MTOM = "enable-mtom";
    public static final String ATTR_BINDING = "binding";

    public static final String ATTRVALUE_VERSION_1_0 = "2.0";
    private static final Logger logger =
        Logger.getLogger(
            com.sun.xml.ws.util.Constants.LoggingDomain + ".server.http");
}
