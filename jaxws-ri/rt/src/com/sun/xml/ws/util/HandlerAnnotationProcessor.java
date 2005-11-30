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
package com.sun.xml.ws.util;

import java.io.InputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.annotation.PostConstruct;

import javax.jws.HandlerChain;
import javax.jws.soap.SOAPMessageHandlers;
import javax.jws.WebService;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import javax.xml.namespace.QName;

import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;

import com.sun.xml.ws.handler.HandlerChainCaller;
import com.sun.xml.ws.streaming.XMLStreamReaderFactory;
import com.sun.xml.ws.streaming.XMLStreamReaderUtil;

/**
 * Used by client and server side to create handler information
 * from annotated class.
 *
 * @author JAX-WS Development Team
 */
public class HandlerAnnotationProcessor {

    private static final Logger logger = Logger.getLogger(
        com.sun.xml.ws.util.Constants.LoggingDomain + ".util");
    
    /*
     * Returns an object that stores handler lists and roles. Will
     * return null if there is no handler annotation.
     */
    public static HandlerAnnotationInfo buildHandlerInfo(Class clazz,
        QName serviceName, QName portName, String bindingId) {
        
        clazz = checkClass(clazz);
        HandlerChain handlerChain =
            (HandlerChain) clazz.getAnnotation(HandlerChain.class);
        if (handlerChain == null) {
            return null;
        }
        
        if (clazz.getAnnotation(SOAPMessageHandlers.class) != null) {
            throw new UtilException(
                "util.handler.cannot.combine.soapmessagehandlers");
        }
        InputStream iStream = getFileAsStream(clazz, handlerChain);
        XMLStreamReader reader =
            XMLStreamReaderFactory.createXMLStreamReader(iStream, true);
        XMLStreamReaderUtil.nextElementContent(reader);
        return parseHandlerFile(reader, clazz.getClassLoader(),
            serviceName, portName, bindingId);
    }
    
    static Class getClass(String className) {
        try {
            return Thread.currentThread().getContextClassLoader().loadClass(
                className);
        } catch (ClassNotFoundException e) {
            throw new UtilException("util.handler.class.not.found",
                new Object[] {className});
        }
    }
    
    static Class checkClass(Class clazz) {
        if (!clazz.isAnnotationPresent(WebService.class)) {
            throw new UtilException("util.handler.no.webservice.annotation",
                new Object[] {clazz.getCanonicalName()});
        }
        
        WebService webService =
            (WebService) clazz.getAnnotation(WebService.class);

        String ei = webService.endpointInterface();
        if (ei.length() > 0) {
            clazz = getClass(webService.endpointInterface());
            if (!clazz.isAnnotationPresent(WebService.class)) {
                throw new UtilException("util.handler.endpoint.interface.no.webservice",
                                    new Object[] {webService.endpointInterface()});
            }
        }
        return clazz;
    }
    
    public static HandlerAnnotationInfo parseHandlerFile(XMLStreamReader reader,
        ClassLoader classLoader, QName serviceName, QName portName,
        String bindingId) {
        
        HandlerAnnotationInfo info = new HandlerAnnotationInfo();
        
        XMLStreamReaderUtil.nextElementContent(reader);
        
        List<Handler> handlerChain = new ArrayList<Handler>();
        Set<String> roles = new HashSet<String>();

        while (reader.getName().equals(QNAME_HANDLER_CHAIN)) {
        
            XMLStreamReaderUtil.nextElementContent(reader);
            
            if (reader.getName().equals(QNAME_CHAIN_PORT_PATTERN)) {
                if (portName == null) {
                    logger.warning("handler chain sepcified for port " +
                        "but port QName passed to parser is null");
                }
                boolean parseChain = JAXWSUtils.matchQNames(portName,
                    XMLStreamReaderUtil.getElementQName(reader));
                if (!parseChain) {
                    skipChain(reader);
                    continue;
                }
                XMLStreamReaderUtil.nextElementContent(reader);
            } else if (reader.getName().equals(QNAME_CHAIN_PROTOCOL_BINDING)) {
                if (bindingId == null) {
                    logger.warning("handler chain sepcified for bindingId " +
                        "but bindingId passed to parser is null");
                }
                String bindingList = XMLStreamReaderUtil.getElementText(reader);
                boolean skipThisChain = true;
                if (bindingId.equals(HTTPBinding.HTTP_BINDING) &&
                    bindingList.indexOf(PROTOCOL_XML_TOKEN) != -1) {
                    skipThisChain = false;
                } else if (bindingId.equals(SOAPBinding.SOAP11HTTP_BINDING) &&
                    bindingList.indexOf(PROTOCOL_SOAP11_TOKEN) != -1) {
                    skipThisChain = false;
                } else if (bindingId.equals(SOAPBinding.SOAP12HTTP_BINDING) &&
                    bindingList.indexOf(PROTOCOL_SOAP12_TOKEN) != -1) {
                    skipThisChain = false;
                }

                if (skipThisChain) {
                    skipChain(reader);
                    continue;
                }
                XMLStreamReaderUtil.nextElementContent(reader);
            } else if (reader.getName().equals(QNAME_CHAIN_SERVICE_PATTERN)) {
                if (serviceName == null) {
                    logger.warning("handler chain sepcified for service " +
                        "but service QName passed to parser is null");
                }
                boolean parseChain = JAXWSUtils.matchQNames(
                    serviceName,
                    XMLStreamReaderUtil.getElementQName(reader));
                if (!parseChain) {
                    skipChain(reader);
                    continue;
                }
                XMLStreamReaderUtil.nextElementContent(reader);
            }

            // process all <handler> elements
            while (reader.getName().equals(QNAME_HANDLER)) {
                Handler handler = null;

                XMLStreamReaderUtil.nextContent(reader);
                if (reader.getName().equals(QNAME_HANDLER_NAME)) {
                    skipTextElement(reader);
                }

                // handler class
                ensureProperName(reader, QNAME_HANDLER_CLASS);
                try {
                    handler = (Handler) loadClass(classLoader,
                        XMLStreamReaderUtil.getElementText(reader)).newInstance();
                } catch (InstantiationException ie){
                    throw new RuntimeException(ie);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                XMLStreamReaderUtil.nextContent(reader);

                // init params (ignored)
                while (reader.getName().equals(QNAME_HANDLER_PARAM)) {
                    skipInitParamElement(reader);
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

                // call @PostConstruct method on handler if present
                for (Method method : handler.getClass().getMethods()) {
                    if (method.getAnnotation(PostConstruct.class) == null) {
                        continue;
                    }
                    try {
                        method.invoke(handler, new Object [0]);
			break;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
                
                handlerChain.add(handler);

                // move past </handler>
                ensureProperName(reader, QNAME_HANDLER);
                XMLStreamReaderUtil.nextContent(reader);
            }
            
            // move past </handler-chain>
            ensureProperName(reader, QNAME_HANDLER_CHAIN);
            XMLStreamReaderUtil.nextContent(reader);
        }
        
        info.setHandlers(handlerChain);
        Set<URI> uriRoles = new HashSet<URI>(roles.size());
        try {
            for (String role : roles) {
                uriRoles.add(new URI(role));
            }
        } catch (URISyntaxException e) {
            throw new UtilException(e.getMessage());
        }
        info.setRoles(uriRoles);
        return info;
    }
    
    static void ensureProperName(XMLStreamReader reader, String expectedName) {
        if (!reader.getLocalName().equals(expectedName)) {
            failWithLocalName("util.parser.wrong.element", reader,
                expectedName);
        }
    }
    
    static void failWithLocalName(String key,
        XMLStreamReader reader, String arg) {
        throw new UtilException(key,
            new Object[] {
                Integer.toString(reader.getLocation().getLineNumber()),
                reader.getLocalName(),
                arg });
    }
    
    static Class loadClass(ClassLoader loader, String name) {
        try {
            return Class.forName(name, true, loader);
        } catch (ClassNotFoundException e) {
            throw new UtilException(
                "util.handler.class.not.found",
                name);
        }
    }
    
    static void skipTextElement(XMLStreamReader reader) {
        XMLStreamReaderUtil.nextContent(reader);
        XMLStreamReaderUtil.nextElementContent(reader);
        XMLStreamReaderUtil.nextElementContent(reader);
    }
    
    static void skipInitParamElement(XMLStreamReader reader) {
        int state;
        do {
            state = XMLStreamReaderUtil.nextContent(reader);
        } while (state != XMLStreamReader.END_ELEMENT ||
            !reader.getName().equals(QNAME_HANDLER_PARAM));
        XMLStreamReaderUtil.nextElementContent(reader);
    }

    static void skipChain(XMLStreamReader reader) {
        while (XMLStreamReaderUtil.nextContent(reader) !=
            XMLStreamConstants.END_ELEMENT ||
            !reader.getName().equals(QNAME_HANDLER_CHAIN)) {}
        XMLStreamReaderUtil.nextElementContent(reader);
    }
    
    static void ensureProperName(XMLStreamReader reader,
        QName expectedName) {

        if (!reader.getName().equals(expectedName)) {
            failWithLocalName("util.parser.wrong.element", reader,
                expectedName.getLocalPart());
        }
    }

    static InputStream getFileAsStream(Class clazz, HandlerChain chain) {
        URL url = clazz.getResource(chain.file());
        if (url == null) {
            url = Thread.currentThread().getContextClassLoader().
                getResource(chain.file());
        }
        if (url == null) {
            String tmp = clazz.getPackage().getName();
            tmp = tmp.replace('.', '/');
            tmp += "/" + chain.file();
            url =
                Thread.currentThread().getContextClassLoader().getResource(tmp);
        }
        if (url == null) {
            throw new UtilException("util.failed.to.find.handlerchain.file",
                new Object[] {clazz.getName(), chain.file()});
        }
        try {
            return url.openStream();
        } catch (IOException e) {
            throw new UtilException("util.failed.to.parse.handlerchain.file",
                new Object[] {clazz.getName(), chain.file()});
        }
    }

    public static final String NS_109 =
        "http://java.sun.com/xml/ns/javaee";

    public static final String PROTOCOL_SOAP11_TOKEN = "##SOAP11_HTTP";
    public static final String PROTOCOL_SOAP12_TOKEN = "##SOAP12_HTTP";
    public static final String PROTOCOL_XML_TOKEN = "##XML_HTTP";
    
    public static final QName QNAME_CHAIN_PORT_PATTERN =
        new QName(NS_109, "port-name-pattern");
    public static final QName QNAME_CHAIN_PROTOCOL_BINDING =
        new QName(NS_109, "protocol-bindings");
    public static final QName QNAME_CHAIN_SERVICE_PATTERN =
        new QName(NS_109, "service-name-pattern");
    public static final QName QNAME_HANDLER_CHAIN =
        new QName(NS_109, "handler-chain");
    public static final QName QNAME_HANDLER_CHAINS =
        new QName(NS_109, "handler-chains");
    public static final QName QNAME_HANDLER =
        new QName(NS_109, "handler");
    public static final QName QNAME_HANDLER_NAME =
        new QName(NS_109, "handler-name");
    public static final QName QNAME_HANDLER_CLASS =
        new QName(NS_109, "handler-class");
    public static final QName QNAME_HANDLER_PARAM =
        new QName(NS_109, "init-param");
    public static final QName QNAME_HANDLER_PARAM_NAME =
        new QName(NS_109, "param-name");
    public static final QName QNAME_HANDLER_PARAM_VALUE =
        new QName(NS_109, "param-value");
    public static final QName QNAME_HANDLER_HEADER =
        new QName(NS_109, "soap-header");
    public static final QName QNAME_HANDLER_ROLE =
        new QName(NS_109, "soap-role");

}
