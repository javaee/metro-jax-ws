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
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import javax.jws.HandlerChain;
import javax.jws.soap.SOAPMessageHandlers;
import javax.jws.WebService;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import com.sun.xml.ws.handler.HandlerChainsModel;
import javax.xml.namespace.QName;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;

import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.Handler;

import com.sun.xml.ws.handler.HandlerChainCaller;
import com.sun.xml.ws.streaming.XMLStreamReaderFactory;
import com.sun.xml.ws.streaming.XMLStreamReaderUtil;

/**
 * <p>Used by client and server side to create handler information
 * from annotated class. The public methods all return a
 * HandlerChainInfo that contains the handlers and role information
 * needed at runtime.
 * 
 * <p>All of the handler chain descriptors follow the same schema,
 * whether they are wsdl customizations, handler files specified
 * by an annotation, or are included in the sun-jaxws.xml file.
 * So this class is used for all handler xml information. The
 * two public entry points are
 * {@link HandlerAnnotationProcessor#buildHandlerInfo}, called
 * when you have an annotated class that points to a file, and
 * {@link HandlerAnnotationProcessor#parseHandlerFile}, called
 * when the file reader already exists.
 *
 * <p>The methods in the class are static so that it may called
 * from the runtime statically.
 *
 * @see com.sun.xml.ws.util.HandlerAnnotationInfo
 *
 * @author JAX-WS Development Team
 */
public class HandlerAnnotationProcessor {

    private static final Logger logger = Logger.getLogger(
        com.sun.xml.ws.util.Constants.LoggingDomain + ".util");
    
    /**
     * <p>This method is called by
     * {@link com.sun.xml.ws.client.ServiceContextBuilder} and
     * {@link com.sun.xml.ws.server.RuntimeEndpointInfo} when
     * they have an annotated class.
     *
     * <p>If there is no handler chain annotation on the class,
     * this method will return null. Otherwise it will load the
     * class and call the parseHandlerFile method to read the
     * information.
     *
     * @return A HandlerAnnotationInfo object that stores the
     * handlers and roles. Will return null if the class passed
     * in has no handler chain annotation.
     */
    public static HandlerAnnotationInfo buildHandlerInfo(Class clazz,
        QName serviceName, QName portName, String bindingId) {
        
//        clazz = checkClass(clazz);
        HandlerChain handlerChain =
            (HandlerChain) clazz.getAnnotation(HandlerChain.class);
        if (handlerChain == null) {
            clazz = getSEI(clazz);
            if (clazz != null)
            handlerChain =
                (HandlerChain) clazz.getAnnotation(HandlerChain.class);  
            if (handlerChain == null)
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
        return HandlerChainsModel.parseHandlerFile(reader, clazz.getClassLoader(),
            serviceName, portName, bindingId);
    }
    
    public static HandlerChainsModel buildHandlerChainsModel(final Class clazz) {
        if(clazz == null) {
            return null;
        }
        HandlerChain handlerChain =
            (HandlerChain) clazz.getAnnotation(HandlerChain.class);
        if(handlerChain == null)
            return null;
        InputStream iStream = getFileAsStream(clazz, handlerChain);
        XMLStreamReader reader =
            XMLStreamReaderFactory.createXMLStreamReader(iStream, true);
        XMLStreamReaderUtil.nextElementContent(reader);
        return HandlerChainsModel.parseHandlerConfigFile(clazz, reader);
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
    
    static Class getSEI(Class clazz) {
        if (Provider.class.isAssignableFrom(clazz)) {
            //No SEI for Provider Implementation
            return null;
        }
        if (Service.class.isAssignableFrom(clazz)) {
            //No SEI for Service class
            return null;
        }
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
            return clazz;
        }
        return null;
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

    
}
