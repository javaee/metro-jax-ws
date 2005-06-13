/*
 * $Id: HandlerAnnotationProcessor.java,v 1.4 2005-06-13 19:10:25 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.util;

import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.jws.HandlerChain;
import javax.jws.soap.SOAPMessageHandlers;
import javax.jws.WebService;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

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

    /*
     * Returns an object that stores handler lists and roles. Will
     * return null if there is no handler annotation.
     */
    public static HandlerAnnotationInfo buildHandlerInfo(Class clazz) {
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
        return parseHandlerFile(reader, clazz);
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
    
    // see JAXRPCRuntimeInfoParser.setHandlersAndRoles
    static HandlerAnnotationInfo parseHandlerFile(XMLStreamReader reader,
        Class clazz) {
        
        HandlerAnnotationInfo info = new HandlerAnnotationInfo();
        
        List<Handler> handlerChain = new ArrayList<Handler>();
        Set<String> roles = new HashSet<String>();
        
        // skip <handler-config> and <handler-chain>
        String elementName = reader.getLocalName();
        while (elementName.equals("handler-config") ||
            elementName.equals("handler-chain")) {
            
            XMLStreamReaderUtil.nextElementContent(reader);
            elementName = reader.getLocalName();
        }

        // skip <handler-chain-name>
        if (reader.getLocalName().equals("handler-chain-name")) {
            skipTextElement(reader);
        }

        // process all <handler> elements
        while (reader.getLocalName().equals("handler")) {
            XMLStreamReaderUtil.nextContent(reader);
            Handler handler = null;
            Map<String, String> initParams = new HashMap<String, String>();
            
            // skip some elements that we don't use
            elementName = reader.getLocalName();
            while (elementName.equals("description") ||
                elementName.equals("display-name") ||
                elementName.equals("small-icon") ||
                elementName.equals("large-icon") ||
                elementName.equals("handler-name")) {
                
                skipTextElement(reader);
                elementName = reader.getLocalName();
            }

            // handler class
            ensureProperName(reader, "handler-class");
            try {
                handler = (Handler) loadClass(clazz,
                    XMLStreamReaderUtil.getElementText(reader)).newInstance();
            } catch (InstantiationException ie){
                throw new RuntimeException(ie);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            XMLStreamReaderUtil.nextElementContent(reader);

            while (!reader.getLocalName().equals("handler")) {
                if (reader.getLocalName().equals("init-param")) {
                    XMLStreamReaderUtil.nextContent(reader);
                    ensureProperName(reader, "param-name");
                    String paramName = XMLStreamReaderUtil.getElementText(reader);

                    XMLStreamReaderUtil.nextContent(reader);
                    ensureProperName(reader, "param-value");
                    String paramValue = XMLStreamReaderUtil.getElementText(reader);
                    initParams.put(paramName, paramValue);

                    XMLStreamReaderUtil.nextContent(reader); // past param-value
                    
                    // skip <description> if present
                    if (reader.getLocalName().equals("description")) {
                        skipTextElement(reader);
                    }
                    XMLStreamReaderUtil.nextContent(reader); // past init-param
                } else if (reader.getLocalName().equals("soap-header")) {
                    skipTextElement(reader);
                } else if (reader.getLocalName().equals("soap-role")) {
                    roles.add(XMLStreamReaderUtil.getElementText(reader));
                    XMLStreamReaderUtil.nextContent(reader);
                } else {
                    failWithLocalName("util.parser.wrong.element", reader,
                        "</handler>");
                }
            }
            
            // finish handler info and add to chain
            if (!initParams.isEmpty()) {
                handler.init(initParams);
            }
            handlerChain.add(handler);
            
            // move past </handler>
            XMLStreamReaderUtil.nextElementContent(reader);
        }

        info.setHandlers(handlerChain);
        info.setRoles(roles);
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
    
    static Class loadClass(Class clazz, String name) {
        try {
            return Class.forName(name, true, clazz.getClassLoader());
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

    static InputStream getFileAsStream(Class clazz, HandlerChain chain) {
        URL url = clazz.getResource(chain.file());
        if (url == null) {
            String tmp = clazz.getPackage().toString();
            tmp = tmp.replace('.', '/');
            tmp += "/" + chain.file();
            url = clazz.getResource(tmp);
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
}
