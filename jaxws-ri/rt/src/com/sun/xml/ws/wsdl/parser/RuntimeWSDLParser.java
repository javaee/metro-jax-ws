/**
 * $Id: RuntimeWSDLParser.java,v 1.11 2005-08-17 23:43:50 kohsuke Exp $
 */

/**
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.wsdl.parser;

import com.sun.xml.ws.model.soap.SOAPBlock;
import com.sun.xml.ws.streaming.XMLStreamReaderFactory;
import com.sun.xml.ws.streaming.XMLStreamReaderUtil;
import com.sun.xml.ws.util.xml.XmlUtil;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.soap.SOAPBinding;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

public class RuntimeWSDLParser {
    private final WSDLDocument wsdlDoc = new WSDLDocument();
    /**
     * Target namespace URI of the WSDL that we are currently parsing.
     */
    private String targetNamespace;
    /**
     * System IDs of WSDLs that are already read.
     */
    private final Set<String> importedWSDLs = new HashSet<String>();
    /**
     * Must not be null.
     */
    private final EntityResolver resolver;

    public static WSDLDocument parse(URL wsdlLoc, EntityResolver resolver) throws IOException, XMLStreamException, SAXException {
        assert resolver!=null;
        RuntimeWSDLParser parser = new RuntimeWSDLParser(resolver);
        parser.parseWSDL(wsdlLoc);
        return parser.wsdlDoc;
    }

    private RuntimeWSDLParser(EntityResolver resolver) {
        this.resolver = resolver;
    }

    private static XMLStreamReader createReader(InputSource source) {
        return XMLStreamReaderFactory.createXMLStreamReader(source,true);
    }

    private void parseWSDL(URL wsdlLoc) throws XMLStreamException, IOException, SAXException {

//        String systemId = wsdlLoc.toExternalForm();
//        InputSource source = resolver.resolveEntity(null,systemId);
//        if(source==null)
//            source = new InputSource(systemId);

        InputSource source = resolver.resolveEntity(null,wsdlLoc.toExternalForm());
        if(source==null)
            source = new InputSource(wsdlLoc.toExternalForm());  // default resolution
        else
            if(source.getSystemId()==null)
                // ideally entity resolvers should be giving us the system ID for the resource
                // (or otherwise we won't be able to resolve references within this imported WSDL correctly),
                // but if none is given, the system ID before the entity resolution is better than nothing.
                source.setSystemId(wsdlLoc.toExternalForm());

        // avoid processing the same WSDL twice.
        if(!importedWSDLs.add(source.getSystemId()))
            return;


        XMLStreamReader reader = createReader(source);
        XMLStreamReaderUtil.nextElementContent(reader);

        //wsdl:definition
        if (!reader.getName().equals(WSDLConstants.QNAME_DEFINITIONS)) {
            ParserUtil.failWithFullName("runtime.parser.wsdl.invalidElement", reader);
        }

        //get the targetNamespace of the service
        String tns = ParserUtil.getMandatoryNonEmptyAttribute(reader, WSDLConstants.ATTR_TNS);

        final String oldTargetNamespace = targetNamespace;
        targetNamespace = tns;

        while (XMLStreamReaderUtil.nextElementContent(reader) !=
                XMLStreamConstants.END_ELEMENT) {
             if(reader.getEventType() == XMLStreamConstants.END_DOCUMENT)
                break;
            QName name = reader.getName();
            if (WSDLConstants.QNAME_IMPORT.equals(name)) {
                parseImport(wsdlLoc, reader);
                XMLStreamReaderUtil.next(reader);
            }else if (WSDLConstants.QNAME_BINDING.equals(name)) {
                parseBinding(reader);
                XMLStreamReaderUtil.next(reader);
            } else if (WSDLConstants.QNAME_SERVICE.equals(name)) {
                parseService(reader);
                XMLStreamReaderUtil.next(reader);
            } else{
                XMLStreamReaderUtil.skipElement(reader);
            }
        }
        targetNamespace = oldTargetNamespace;
        reader.close();
    }

    private void parseService(XMLStreamReader reader) {
        String serviceName = ParserUtil.getMandatoryNonEmptyAttribute(reader, WSDLConstants.ATTR_NAME);
        Service service = new Service(new QName(targetNamespace, serviceName));
        while (XMLStreamReaderUtil.nextElementContent(reader) != XMLStreamConstants.END_ELEMENT) {
            QName name = reader.getName();
            if(WSDLConstants.QNAME_PORT.equals(name)){
                parsePort(reader, service);
                XMLStreamReaderUtil.next(reader);
            }else{
                XMLStreamReaderUtil.skipElement(reader);
            }
        }
        wsdlDoc.addService(service);
    }

    private static void parsePort(XMLStreamReader reader, Service service) {
        String portName = ParserUtil.getMandatoryNonEmptyAttribute(reader, WSDLConstants.ATTR_NAME);
        String binding = ParserUtil.getMandatoryNonEmptyAttribute(reader, "binding");
        QName bindingName = ParserUtil.getQName(reader, binding);
        String location = null;
        while (XMLStreamReaderUtil.nextElementContent(reader) != XMLStreamConstants.END_ELEMENT) {
            QName name = reader.getName();
            if(SOAPConstants.QNAME_ADDRESS.equals(name)||SOAPConstants.QNAME_SOAP12ADDRESS.equals(name)){
                location = ParserUtil.getMandatoryNonEmptyAttribute(reader, WSDLConstants.ATTR_LOCATION);
                XMLStreamReaderUtil.next(reader);
            }else{
                XMLStreamReaderUtil.skipElement(reader);
            }
        }
        QName portQName = new QName(service.getName().getNamespaceURI(), portName);
        service.put(portQName, new Port(portQName, bindingName, location));
    }

    private void parseBinding(XMLStreamReader reader) {
        String bindingName = ParserUtil.getMandatoryNonEmptyAttribute(reader, "name");
        String portTypeName = ParserUtil.getMandatoryNonEmptyAttribute(reader, "type");
        if((bindingName == null) || (portTypeName == null)){
            //TODO: throw exception?
            //skip wsdl:binding element for now
            XMLStreamReaderUtil.skipElement(reader);
            return;
        }
        Binding binding = new Binding(new QName(targetNamespace, bindingName),
                ParserUtil.getQName(reader, portTypeName));

        wsdlDoc.addBinding(binding);

        while (XMLStreamReaderUtil.nextElementContent(reader) != XMLStreamConstants.END_ELEMENT) {
            QName name = reader.getName();
            if (WSDLConstants.NS_SOAP_BINDING.equals(name)) {
                binding.setBindingId(SOAPBinding.SOAP11HTTP_BINDING);
                XMLStreamReaderUtil.next(reader);
            } else if (WSDLConstants.NS_SOAP12_BINDING.equals(name)) {
                binding.setBindingId(SOAPBinding.SOAP12HTTP_BINDING);
                XMLStreamReaderUtil.next(reader);
            } else if (WSDLConstants.QNAME_OPERATION.equals(name)) {
                parseBindingOperation(reader, binding);
                XMLStreamReaderUtil.next(reader);
            }else{
               XMLStreamReaderUtil.skipElement(reader);
            }
        }
    }

    private static void parseBindingOperation(XMLStreamReader reader, Binding binding) {
        String bindingOpName = ParserUtil.getMandatoryNonEmptyAttribute(reader, "name");
        if(bindingOpName == null){
            //TODO: throw exception?
            //skip wsdl:binding element for now
            XMLStreamReaderUtil.skipElement(reader);
            return;
        }

        BindingOperation bindingOp = new BindingOperation(bindingOpName);
        binding.put(bindingOpName, bindingOp);

        while (XMLStreamReaderUtil.nextElementContent(reader) != XMLStreamConstants.END_ELEMENT) {
            QName name = reader.getName();
            if (WSDLConstants.QNAME_INPUT.equals(name)) {
                parseInputBinding(reader, bindingOp);
                XMLStreamReaderUtil.next(reader);
            }else if(WSDLConstants.QNAME_OUTPUT.equals(name)){
                parseOutputBinding(reader, bindingOp);
                XMLStreamReaderUtil.next(reader);
            }else{
                XMLStreamReaderUtil.skipElement(reader);
            }
        }
    }

    private static void parseInputBinding(XMLStreamReader reader, BindingOperation bindingOp) {
        boolean bodyFound = false;
        while (XMLStreamReaderUtil.nextElementContent(reader) != XMLStreamConstants.END_ELEMENT) {
            QName name = reader.getName();
            if((SOAPConstants.QNAME_BODY.equals(name) || SOAPConstants.QNAME_SOAP12BODY.equals(name)) && !bodyFound){
                bodyFound = true;
                bindingOp.setInputExplicitBodyParts(parseSOAPBodyBinding(reader, bindingOp.getInputParts()));
                XMLStreamReaderUtil.next(reader);
            }else if(MIMEConstants.QNAME_MULTIPART_RELATED.equals(name)){
                parseMimeMultipartBinding(reader, bindingOp.getInputParts(), bindingOp.getOutputMimeTypes());
                XMLStreamReaderUtil.next(reader);
            }else{
                XMLStreamReaderUtil.skipElement(reader);
            }

        }
    }

    private static void parseOutputBinding(XMLStreamReader reader, BindingOperation bindingOp) {
        boolean bodyFound = false;
        while (XMLStreamReaderUtil.nextElementContent(reader) != XMLStreamConstants.END_ELEMENT) {
            QName name = reader.getName();
            if((SOAPConstants.QNAME_BODY.equals(name) || SOAPConstants.QNAME_SOAP12BODY.equals(name)) && !bodyFound){
                bodyFound = true;
                bindingOp.setOutputExplicitBodyParts(parseSOAPBodyBinding(reader, bindingOp.getOutputParts()));
                XMLStreamReaderUtil.next(reader);
            }else if(MIMEConstants.QNAME_MULTIPART_RELATED.equals(name)){
                parseMimeMultipartBinding(reader, bindingOp.getOutputParts(), bindingOp.getOutputMimeTypes());
                XMLStreamReaderUtil.next(reader);
            }else{
                XMLStreamReaderUtil.skipElement(reader);
            }

        }
    }

    /**
     *
     * @param reader
     * @param parts
     * @return
     * Returns true if body has explicit parts declaration
     */
    private static boolean parseSOAPBodyBinding(XMLStreamReader reader, Map<String, SOAPBlock> parts){
        String partsString = reader.getAttributeValue(null, "parts");
        if(partsString != null){
            List<String> partsList = XmlUtil.parseTokenList(partsString);
            if(partsList.isEmpty()){
                parts.put(" ", SOAPBlock.BODY);
            }else{
                for(String part:partsList){
                    parts.put(part, SOAPBlock.BODY);
                }
            }
            return true;
        }
        return false;
    }

//    private void parseSOAPHeaderBinding(XMLStreamReader reader, Map<String,SOAPBlock> parts){
//        String part = reader.getAttributeValue(null, "part");
//        String message = reader.getAttributeValue(null, "message");
//        if(part == null| part.equals("")||message == null || message.equals("")){
//            //TODO: throw exception?
//            XMLStreamReaderUtil.skipElement(reader);
//        }
//        QName msgName = ParserUtil.getQName(reader, message);
//        parts.put(new Part(part, msgName), SOAPBlock.HEADER);
//    }


    private static void parseMimeMultipartBinding(XMLStreamReader reader, Map<String, SOAPBlock> parts,
                                                  Map<String, String> mimeTypes) {
        while (XMLStreamReaderUtil.nextElementContent(reader) != XMLStreamConstants.END_ELEMENT) {
            QName name = reader.getName();
            if(MIMEConstants.QNAME_PART.equals(name)){
                parseMIMEPart(reader, parts, mimeTypes);
            }else{
                XMLStreamReaderUtil.skipElement(reader);
            }
        }
    }

    private static void parseMIMEPart(XMLStreamReader reader, Map<String,SOAPBlock> parts,
                                      Map<String,String> mimeTypes) {
        boolean bodyFound = false;
        while (XMLStreamReaderUtil.nextElementContent(reader) != XMLStreamConstants.END_ELEMENT) {
            QName name = reader.getName();
            if(SOAPConstants.QNAME_BODY.equals(name) && !bodyFound){
                bodyFound = true;
                parseSOAPBodyBinding(reader, parts);
                XMLStreamReaderUtil.next(reader);
            }else if(MIMEConstants.QNAME_CONTENT.equals(name)){
                String part = reader.getAttributeValue(null, "part");
                String type = reader.getAttributeValue(null, "type");
                if((part == null) || (type == null)){
                    XMLStreamReaderUtil.skipElement(reader);
                    continue;
                }
                parts.put(part, SOAPBlock.MIME);
                mimeTypes.put(part, type);
                XMLStreamReaderUtil.next(reader);
            }else{
                XMLStreamReaderUtil.skipElement(reader);
            }
        }
    }

    protected void parseImport(URL baseURL, XMLStreamReader reader) throws IOException, SAXException, XMLStreamException {
        // expand to the absolute URL of the imported WSDL.
        String importLocation =
                ParserUtil.getMandatoryNonEmptyAttribute(reader, WSDLConstants.ATTR_LOCATION);
        URL importURL = new URL(baseURL,importLocation);

        parseWSDL(importURL);
    }

    private void parsePortType(XMLStreamReader reader) {
        String portTypeName = ParserUtil.getMandatoryNonEmptyAttribute(reader, WSDLConstants.ATTR_NAME);
        if(portTypeName == null){
            //TODO: throw exception?
            //skip wsdl:portType element for now
            XMLStreamReaderUtil.skipElement(reader);
            return;
        }
        PortType portType = new PortType(new QName(targetNamespace, portTypeName));
        wsdlDoc.addPortType(portType);
        while (XMLStreamReaderUtil.nextElementContent(reader) != XMLStreamConstants.END_ELEMENT) {
            QName name = reader.getName();
            if(WSDLConstants.QNAME_OPERATION.equals(name)){
                parsePortTypeOperation(reader, portType);
            }
        }
    }

    private void parsePortTypeOperation(XMLStreamReader reader, PortType portType) {
        String operationName = ParserUtil.getMandatoryNonEmptyAttribute(reader, WSDLConstants.ATTR_NAME);
        if(operationName == null){
            //TODO: throw exception?
            //skip wsdl:portType element for now
            XMLStreamReaderUtil.skipElement(reader);
            return;
        }

        QName operationQName = ParserUtil.getQName(reader, operationName);
        PortTypeOperation operation = new PortTypeOperation(operationQName);
        String parameterOrder = ParserUtil.getMandatoryNonEmptyAttribute(reader, "parameterOrder");
        operation.setParameterOrder(parameterOrder);
        portType.put(operationQName, operation);
    }

    private void parseMessage(XMLStreamReader reader) {
        String msgName = ParserUtil.getMandatoryNonEmptyAttribute(reader, WSDLConstants.ATTR_NAME);
        Message msg = new Message(new QName(targetNamespace, msgName));
        while (XMLStreamReaderUtil.nextElementContent(reader) != XMLStreamConstants.END_ELEMENT) {
            QName name = reader.getName();
            if (WSDLConstants.QNAME_PART.equals(name)) {
                String part = ParserUtil.getMandatoryNonEmptyAttribute(reader, WSDLConstants.ATTR_NAME);
                String desc = null;
                int index = reader.getAttributeCount();
                for (int i = 0; i < index; i++) {
                    if (reader.getAttributeName(i).equals("element") || reader.getAttributeName(i).equals("type")) {
                        desc = reader.getAttributeValue(i);
                        break;
                    }
                }
                if (desc == null)
                    continue;
                msg.put(part, ParserUtil.getQName(reader, desc));
            }
        }
        wsdlDoc.addMessage(msg);
    }
}
