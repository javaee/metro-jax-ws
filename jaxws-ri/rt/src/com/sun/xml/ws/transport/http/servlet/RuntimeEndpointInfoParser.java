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

package com.sun.xml.ws.transport.http.servlet;

import com.sun.xml.ws.handler.HandlerChainsModel;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.jws.WebService;

import javax.xml.namespace.QName;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.soap.SOAPBinding;

import com.sun.xml.ws.binding.BindingImpl;
import com.sun.xml.ws.binding.soap.SOAPBindingImpl;
import com.sun.xml.ws.modeler.RuntimeModeler;
import com.sun.xml.ws.server.RuntimeEndpointInfo;
import com.sun.xml.ws.server.ServerRtException;
import com.sun.xml.ws.spi.runtime.Binding;
import com.sun.xml.ws.streaming.Attributes;
import com.sun.xml.ws.streaming.XMLStreamReaderFactory;
import com.sun.xml.ws.streaming.XMLStreamReaderUtil;
import com.sun.xml.ws.util.HandlerAnnotationInfo;

/**
 * @author WS Development Team
 */
public class RuntimeEndpointInfoParser {

    public RuntimeEndpointInfoParser(ClassLoader cl) {
        classLoader = cl;
    }

    public List<RuntimeEndpointInfo> parse(InputStream is) {
        try {
            XMLStreamReader reader =
                XMLStreamReaderFactory.createXMLStreamReader(is, true);
            XMLStreamReaderUtil.nextElementContent(reader);
            return parseEndpoints(reader);
        } catch (XMLStreamException e) {
            throw new ServerRtException("runtime.parser.xmlReader",e);
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
                Class implementorClass = getImplementorClass(implementationName);
                rei.setImplementorClass(implementorClass);
                rei.setImplementor(getImplementor(implementorClass));
                rei.verifyImplementorClass();
                
                String wsdlFile = processWsdlLocation(attrs, rei);
                rei.setWSDLFileName(wsdlFile);
                rei.setServiceName(getQNameAttribute(attrs, ATTR_SERVICE));
                rei.doServiceNameProcessing();
                rei.setPortName(getQNameAttribute(attrs, ATTR_PORT));
                rei.doPortNameProcessing();
                //set Binding using DD, annotation, or default one(in that order)
                String bindingId = getAttribute(attrs, ATTR_BINDING);
                Binding binding = BindingImpl.getBinding(bindingId,
                    implementorClass, rei.getServiceName(), true);
                rei.setBinding(binding);

                //get enable-mtom attribute value
                String mtom = getAttribute(attrs, ATTR_ENABLE_MTOM);

                String mtomThreshold = getAttribute(attrs, ATTR_MTOM_THRESHOLD_VALUE);
                if(mtomThreshold != null){
                    int mtomThresholdValue = Integer.valueOf(mtomThreshold);
                    rei.setMtomThreshold(mtomThresholdValue);
                }

                if(rei.getBinding() instanceof SOAPBindingImpl){
                    SOAPBinding sb = (SOAPBinding)rei.getBinding();
                    if (mtom != null) {
                        sb.setMTOMEnabled(Boolean.valueOf(mtom));
                    }
                }

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
    
    private String processWsdlLocation(Attributes attrs, RuntimeEndpointInfo rei) {
        String wsdlFile = getAttribute(attrs, ATTR_WSDL);
        if (wsdlFile == null) {
            wsdlFile = rei.getWsdlLocation();
        }
        if (wsdlFile != null) {
            if (!wsdlFile.startsWith(WSServletContextListener.JAXWS_WSDL_DD_DIR)) {
                throw new ServerRtException("runtime.parser.wrong.wsdl.location", wsdlFile);
            }
        }
        if (wsdlFile == null) {
            logger.info("wsdl cannot be found from DD or annotation. Will generate and publish a new WSDL for SEI endpoints.");
        }
        return wsdlFile;
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
     */
    protected void setHandlersAndRoles(RuntimeEndpointInfo rei,
        XMLStreamReader reader) {

        if (XMLStreamReaderUtil.nextElementContent(reader) ==
            XMLStreamConstants.END_ELEMENT ||
            !reader.getName().equals(
            HandlerChainsModel.QNAME_HANDLER_CHAINS)) {

            return;
        }

        QName serviceName = rei.getServiceName();
        HandlerAnnotationInfo handlerInfo =
            HandlerChainsModel.parseHandlerFile(reader, classLoader,
            serviceName, rei.getPortName(),
            ((BindingImpl) rei.getBinding()).getActualBindingId());

        rei.getBinding().setHandlerChain(handlerInfo.getHandlers());
        if (rei.getBinding() instanceof SOAPBinding) {
            ((SOAPBinding) rei.getBinding()).setRoles(handlerInfo.getRoles());
        }

        // move past </handler-chains>
        XMLStreamReaderUtil.nextContent(reader);
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
     * Gets endpoint implementation class
     */
    protected Class getImplementorClass(String name) {
        try {
            return Class.forName(name, true, classLoader);
        } catch (ClassNotFoundException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new ServerRtException(
                "runtime.parser.classNotFound", name);
        }
    }

    /*
     * Instantiates endpoint implementation
     */
    protected Object getImplementor(Class cl) {
        try {
            return cl.newInstance();
        } catch (InstantiationException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new ServerRtException(
                "error.implementorFactory.newInstanceFailed", cl.getName());
        } catch (IllegalAccessException e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new ServerRtException(
                "error.implementorFactory.newInstanceFailed", cl.getName());
        }
    }

    protected ClassLoader classLoader;

    public static final String NS_RUNTIME =
        "http://java.sun.com/xml/ns/jax-ws/ri/runtime";
    
    public static final QName QNAME_ENDPOINTS =
        new QName(NS_RUNTIME, "endpoints");
    public static final QName QNAME_ENDPOINT =
        new QName(NS_RUNTIME, "endpoint");
    
    public static final String ATTR_VERSION = "version";
    public static final String ATTR_NAME = "name";
    public static final String ATTR_IMPLEMENTATION = "implementation";
    public static final String ATTR_WSDL = "wsdl";
    public static final String ATTR_SERVICE = "service";
    public static final String ATTR_PORT = "port";
    public static final String ATTR_URL_PATTERN = "url-pattern";
    public static final String ATTR_ENABLE_MTOM = "enable-mtom";
    public static final String ATTR_MTOM_THRESHOLD_VALUE = "mtom-threshold-value";
    public static final String ATTR_BINDING = "binding";

    public static final String ATTRVALUE_VERSION_1_0 = "2.0";
    private static final Logger logger =
        Logger.getLogger(
            com.sun.xml.ws.util.Constants.LoggingDomain + ".server.http");
}
