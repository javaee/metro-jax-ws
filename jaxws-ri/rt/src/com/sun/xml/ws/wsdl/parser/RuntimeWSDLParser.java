/**
 * $Id: RuntimeWSDLParser.java,v 1.7 2005-08-17 20:43:13 vivekp Exp $
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
import com.sun.xml.ws.encoding.soap.SOAP12Constants;
import org.xml.sax.InputSource;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.soap.SOAPBinding;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class RuntimeWSDLParser {

    public static WSDLDocument parse(URL wsdlLoc) throws Exception{
        WSDLDocument wsdlDoc = new WSDLDocument();
        WSDLParserContext parserContext = new WSDLParserContext(wsdlDoc);
        parserContext.setOriginalWsdlURL(wsdlLoc);
        InputSource source = new InputSource(wsdlLoc.openStream());
        parseWSDL(source, parserContext);
        return wsdlDoc;
    }

    public static WSDLDocument parse(InputStream is) throws Exception {
        WSDLDocument wsdlDoc = new WSDLDocument();
        WSDLParserContext parserContext = new WSDLParserContext(wsdlDoc);
        InputSource source = new InputSource(is);
        System.out.println("system id: "+source.getSystemId());
        parserContext.setOriginalWsdlURL(new URL(source.getSystemId()));
        try {
            parseWSDL(source, parserContext);

        } catch (XMLStreamException e) {
            throw new Exception("wsdl.xmlReader", e);
        }
        return wsdlDoc;
    }

    private static XMLStreamReader createReader(InputStream is) {
        return XMLStreamReaderFactory.createXMLStreamReader(is, true);
    }

    private static void parseWSDL(InputSource source, WSDLParserContext parserContext) throws XMLStreamException {
        XMLStreamReader reader = createReader(source.getByteStream());
        XMLStreamReaderUtil.nextElementContent(reader);

        //wsdl:definition
        if (!reader.getName().equals(WSDLConstants.QNAME_DEFINITIONS)) {
            ParserUtil.failWithFullName("runtime.parser.wsdl.invalidElement", reader);
        }

        //get the targetNamespace of the service
        String tns = ParserUtil.getMandatoryNonEmptyAttribute(reader, WSDLConstants.ATTR_TNS);

        parserContext.pushContext(source.getSystemId(), tns);
         while (XMLStreamReaderUtil.nextElementContent(reader) !=
                XMLStreamConstants.END_ELEMENT) {
             if(reader.getEventType() == XMLStreamConstants.END_DOCUMENT)
                break;
            QName name = reader.getName();
            if (WSDLConstants.QNAME_IMPORT.equals(name)) {
                parseImport(reader, parserContext);
                XMLStreamReaderUtil.next(reader);
            }else if (WSDLConstants.QNAME_BINDING.equals(name)) {
                parseBinding(reader, parserContext);
                XMLStreamReaderUtil.next(reader);
            } else if (WSDLConstants.QNAME_SERVICE.equals(name)) {
                parseService(reader, parserContext);
                XMLStreamReaderUtil.next(reader);
            } else{
                XMLStreamReaderUtil.skipElement(reader);
            }
        }
        parserContext.popContext();
        reader.close();
    }

    private static void parseService(XMLStreamReader reader, WSDLParserContext parserContext) {
        String serviceName = ParserUtil.getMandatoryNonEmptyAttribute(reader, WSDLConstants.ATTR_NAME);
        Service service = new Service(new QName(parserContext.getTargetNamespace(), serviceName));
        while (XMLStreamReaderUtil.nextElementContent(reader) != XMLStreamConstants.END_ELEMENT) {
            QName name = reader.getName();
            if(WSDLConstants.QNAME_PORT.equals(name)){
                parsePort(reader, service);
                XMLStreamReaderUtil.next(reader);
            }else{
                XMLStreamReaderUtil.skipElement(reader);
            }
        }
        parserContext.getWsdlDocument().addService(service);
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

    private static void parseBinding(XMLStreamReader reader, WSDLParserContext parserContext) {
        String bindingName = ParserUtil.getMandatoryNonEmptyAttribute(reader, "name");
        String portTypeName = ParserUtil.getMandatoryNonEmptyAttribute(reader, "type");
        if((bindingName == null) || (portTypeName == null)){
            //TODO: throw exception?
            //skip wsdl:binding element for now
            XMLStreamReaderUtil.skipElement(reader);
            return;
        }
        Binding binding = new Binding(new QName(parserContext.getTargetNamespace(), bindingName),
                ParserUtil.getQName(reader, portTypeName));

        parserContext.getWsdlDocument().addBinding(binding);

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

    protected static void parseImport(XMLStreamReader reader, WSDLParserContext parserContext) {
        String importLocation =
                ParserUtil.getMandatoryNonEmptyAttribute(reader, WSDLConstants.ATTR_LOCATION);

        URI temp = null;
        URL importURL = null;
        try {
            temp = new URI(importLocation);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

        if (temp.isAbsolute()) {
            try {
                importURL = temp.toURL();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        } else {
            String wsdlPath = parserContext.getOriginalWsdlURL().getPath();
            int index = wsdlPath.lastIndexOf("/");
            String base = wsdlPath.substring(0, index);

            URI owsdlLoc = null;
            String host = null;
            int port = 0;

            try {
                owsdlLoc = new URI(parserContext.getOriginalWsdlURL().toExternalForm());
                host = owsdlLoc.getHost();
                port = owsdlLoc.getPort();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

            if (owsdlLoc != null) {
                File file = null;
                if (owsdlLoc.getScheme().equals("file")) {
                    String path = owsdlLoc.getPath();
                    if (path != null) {
                        file = new File(path);
                        File parent = file.getParentFile();
                        if (parent.isDirectory()) {
                            try {
                                String absPath = parent.getCanonicalPath();
                                String importString = "file:/" +
                                    absPath + "/" + importLocation;
                                importURL = new URL(importString);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //todo://needs to be update for http imports
                } else if (owsdlLoc.getScheme().equals("http"))
                    try {
                        importURL = new URL("http://" + host + ":" + port + base + "/" + importLocation);
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
            }
        }
        try {
            if(parserContext.isImportedWSDL(importURL.toExternalForm()))
                return;
            parserContext.addImportedWSDL(importURL.toExternalForm());

            parseWSDL(new InputSource(importURL.openStream()), parserContext);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void parsePortType(XMLStreamReader reader, WSDLParserContext parserContext) {
        String portTypeName = ParserUtil.getMandatoryNonEmptyAttribute(reader, WSDLConstants.ATTR_NAME);
        if(portTypeName == null){
            //TODO: throw exception?
            //skip wsdl:portType element for now
            XMLStreamReaderUtil.skipElement(reader);
            return;
        }
        PortType portType = new PortType(new QName(parserContext.getTargetNamespace(), portTypeName));
        parserContext.getWsdlDocument().addPortType(portType);
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

    private void parseMessage(XMLStreamReader reader, WSDLParserContext parserContext) {
        String msgName = ParserUtil.getMandatoryNonEmptyAttribute(reader, WSDLConstants.ATTR_NAME);
        Message msg = new Message(new QName(parserContext.getTargetNamespace(), msgName));
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
        parserContext.getWsdlDocument().addMessage(msg);
    }


}
