/*
 * $Id: WSDLPatcher.java,v 1.4 2005-06-06 17:29:42 jitu Exp $
 *
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.server;

import com.sun.xml.ws.streaming.XMLStreamReaderFactory;
import com.sun.xml.ws.streaming.XMLStreamReaderUtil;
import com.sun.xml.ws.wsdl.parser.WSDLConstants;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

public class WSDLPatcher {
    
    private static final String NS_XSD = "http://www.w3.org/2001/XMLSchema";
    private static final QName QNAME_SCHEMA = new QName(NS_XSD, "schema");
    private static final QName SCHEMA_INCLUDE_QNAME = new QName(NS_XSD, "include");
    private static final QName SCHEMA_IMPORT_QNAME = new QName(NS_XSD, "import");
    private static final QName ATTR_NAME_QNAME = new QName("", "name");
    private static final QName ATTR_TARGETNS_QNAME =
            new QName("", "targetNamespace");
    private static final QName WSDL_LOCATION_QNAME = new QName("", "location");
    private static final QName SCHEMA_LOCATION_QNAME =
            new QName("", "schemaLocation");
    
    private static final XMLEventFactory eventFactory =
            XMLEventFactory.newInstance();
    private static final XMLOutputFactory outputFactory = 
            XMLOutputFactory.newInstance();
    private static final XMLInputFactory inputFactory = 
            XMLInputFactory.newInstance();
    
    public static enum DOC_TYPE { WSDL, SCHEMA, OTHER };
    
    private String inPath;
    private String baseAddress;
    private RuntimeEndpointInfo targetEndpoint;
    private List<RuntimeEndpointInfo> endpoints;
    private DocContext docContext;
    
    /*
     * inPath - /WEB-INF/wsdl/xxx.wsdl
     * baseAddress - http://host:port/context/
     */
    public WSDLPatcher(String inPath, String baseAddress,
            RuntimeEndpointInfo targetEndpoint,
            List<RuntimeEndpointInfo> endpoints, DocContext context) {
        this.inPath = inPath;
        this.baseAddress = baseAddress;
        this.targetEndpoint = targetEndpoint;
        this.endpoints = endpoints;
        this.docContext = context;
    }
    
    /*
     * import, include, soap:address locations are patched
     */
    public void patchDoc(InputStream in, OutputStream out) {
        try {
            XMLEventReader reader = inputFactory.createXMLEventReader(in);
            XMLEventWriter writer = outputFactory.createXMLEventWriter(out);
            StartElement start = null;
            QName serviceName = null;
            QName portName = null;
            String targetNamespace = null;
            while(reader.hasNext()) {
                XMLEvent event = reader.nextEvent();
                if (event.isStartElement()) {
                    start = event.asStartElement();
                    QName name = start.getName();                    
                    if (name.equals(SCHEMA_INCLUDE_QNAME)) {
                        event = handleSchemaInclude(start);
                    } else if (name.equals(SCHEMA_IMPORT_QNAME)) {
                        event = handleSchemaImport(start);
                    } else if (name.equals(WSDLConstants.QNAME_IMPORT)) {
                        event = handleWSDLImport(start);
                    } else if (name.equals(WSDLConstants.NS_SOAP_BINDING_ADDRESS)) {
                        event = handleSoapAddress(serviceName, portName, start);
                    } else if (name.equals(WSDLConstants.QNAME_DEFINITIONS)) {
                        Attribute attr = start.getAttributeByName(ATTR_TARGETNS_QNAME);
                        if (attr != null) {
                            targetNamespace = attr.getValue();
                        }
                    } else if (name.equals(WSDLConstants.QNAME_SERVICE)) {
                        Attribute attr = start.getAttributeByName(ATTR_NAME_QNAME);
                        if (attr != null) {
                            serviceName = new QName(targetNamespace, attr.getValue());
                        }
                    } else if (name.equals(WSDLConstants.QNAME_PORT)) {
                        Attribute attr = start.getAttributeByName(ATTR_NAME_QNAME);
                        if (attr != null) {
                            portName = new QName(targetNamespace, attr.getValue());
                        }
                    }
                } else if (event.isEndElement()) {
                    start = null;
                }
                writer.add(event);
            }
            reader.close();
            writer.close();
        } catch (XMLStreamException e) {
            e.printStackTrace();
        }
    }
  
    /*
     * Returns true for relative imports
     */
    private boolean isPatchable(String value) {
        return !value.startsWith("/") && value.indexOf(':') == -1;
    }

    private String getAbsImportLocation(String relPath) {
        String path = docContext.getAbsolutePath(inPath, relPath);    
        return baseAddress+targetEndpoint.getUrlPattern()+"?"+
                targetEndpoint.getQueryString(path);
    }

    private XMLEvent patchImport(StartElement startElement, QName location)
    throws XMLStreamException {
        Attribute locationAttr = startElement.getAttributeByName(location);
        if (locationAttr == null) {
            return startElement;
        }
        
        List<Attribute> newAttrs = new ArrayList<Attribute>();
        Iterator i = startElement.getAttributes();
        while(i.hasNext()) {
            Attribute attr = (Attribute)i.next();
            String file = attr.getValue();
            if (attr.getName().equals(location)) {
                String value = attr.getValue();
                if (isPatchable(value)) {
                    value = getAbsImportLocation(value);
                    Attribute newAttr = eventFactory.createAttribute(
                        location, value);
                    newAttrs.add(newAttr);
                    continue;
                }
            } 
            newAttrs.add(attr);
        }
        XMLEvent event = eventFactory.createStartElement(
                startElement.getName().getPrefix(),
                startElement.getName().getNamespaceURI(),
                startElement.getName().getLocalPart(),
                newAttrs.iterator(),
                startElement.getNamespaces(), 
                startElement.getNamespaceContext());
        return event;
    }

    /*
     * <schema:import> element is patched with correct uri and
     * returns a new element
     */
    private XMLEvent handleSchemaImport(StartElement startElement)
    throws XMLStreamException {
        return patchImport(startElement, SCHEMA_LOCATION_QNAME);
    }

    /*
     * <schema:include> element is patched with correct uri and
     * returns a new element
     */
    private XMLEvent handleSchemaInclude(StartElement startElement)
    throws XMLStreamException {
        return patchImport(startElement, SCHEMA_LOCATION_QNAME);
    }

    /*
     * <wsdl:import> element is patched with correct uri and
     * returns a new element
     */
    private XMLEvent handleWSDLImport(StartElement startElement)
    throws XMLStreamException {
        return patchImport(startElement, WSDL_LOCATION_QNAME);
    }

    /*
     * <soap:address> element is patched with correct endpoint address and
     * returns a new element
     */
    private XMLEvent handleSoapAddress(QName service, QName port,
            StartElement startElement) throws XMLStreamException {

        List<Attribute> newAttrs = new ArrayList<Attribute>();
        Iterator i = startElement.getAttributes();
        while(i.hasNext()) {
            Attribute attr = (Attribute)i.next();
            String file = attr.getValue();
            if (attr.getName().equals(WSDL_LOCATION_QNAME)) {
                String value = getAddressLocation(service, port);
                if (value == null) {
                    return startElement;        // Not patching  
                }
                Attribute newAttr = eventFactory.createAttribute(
                        WSDL_LOCATION_QNAME, value);
                newAttrs.add(newAttr);
                continue;
            } 
            newAttrs.add(attr);
        }
        XMLEvent event = eventFactory.createStartElement(
                startElement.getName().getPrefix(),
                startElement.getName().getNamespaceURI(),
                startElement.getName().getLocalPart(),
                newAttrs.iterator(),
                startElement.getNamespaces(), 
                startElement.getNamespaceContext());
        return event;
    }
    
    /*
     * Utility method to find if the document is WSDL or Schema
     */
    public static DOC_TYPE getDocType(InputStream in) throws XMLStreamException {
        XMLStreamReader reader = XMLStreamReaderFactory.createXMLStreamReader(in, true);
        try {
            XMLStreamReaderUtil.nextElementContent(reader);
            if (reader.getName().equals(WSDLConstants.QNAME_DEFINITIONS)) {
                return DOC_TYPE.WSDL;
            } else if (reader.getName().equals(QNAME_SCHEMA)) {
                return DOC_TYPE.SCHEMA;
            } else {
                return DOC_TYPE.OTHER;
            }
        } finally {
            reader.close();
        }
    }
    
    /*
     * For the given service, port names it matches the correct endpoint and
     * reutrns its endpoint address
     */
    private String getAddressLocation(QName docServiceName, QName docPortName) {
        for(RuntimeEndpointInfo endpointInfo : endpoints) {
            QName serviceName = endpointInfo.getServiceName();
            QName portName = endpointInfo.getPortName();
            if (serviceName != null && portName != null
                    && docServiceName != null && docPortName != null
                    && serviceName.equals(docServiceName)
                    && docPortName.equals(portName)) {
                return baseAddress+endpointInfo.getUrlPattern();
            }
        }
        return null;
    }

}
    

