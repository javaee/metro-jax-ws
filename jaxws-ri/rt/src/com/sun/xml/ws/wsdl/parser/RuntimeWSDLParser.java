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

package com.sun.xml.ws.wsdl.parser;
import com.sun.xml.ws.model.ParameterBinding;
import com.sun.xml.ws.server.DocInfo;
import com.sun.xml.ws.server.DocInfo.DOC_TYPE;
import com.sun.xml.ws.streaming.XMLStreamReaderFactory;
import com.sun.xml.ws.streaming.XMLStreamReaderUtil;
import com.sun.xml.ws.streaming.TidyXMLStreamReader;
import com.sun.xml.ws.util.xml.XmlUtil;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.WebServiceException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.Closeable;
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

    /*
     * Fills DocInfo with Document type(WSDL, or schema),
     * Service Name, Port Type name, targetNamespace for the document.
     * 
     * Don't follow imports
     */
    public static void fillDocInfo(DocInfo docInfo, QName serviceName,
        QName portTypeName) throws XMLStreamException {

        RuntimeWSDLParser parser = new RuntimeWSDLParser(null);
        InputSource source = new InputSource(docInfo.getDoc());

        XMLStreamReader reader = createReader(source);
        try {
            XMLStreamReaderUtil.nextElementContent(reader);

            if(reader.getName().equals(WSDLConstants.QNAME_SCHEMA)){
                docInfo.setDocType(DOC_TYPE.SCHEMA);
                String tns = ParserUtil.getMandatoryNonEmptyAttribute(reader, WSDLConstants.ATTR_TNS);
                docInfo.setTargetNamespace(tns);
                return;
            }else if (reader.getName().equals(WSDLConstants.QNAME_DEFINITIONS)) {
                docInfo.setDocType(DOC_TYPE.WSDL);
                String tns = ParserUtil.getMandatoryNonEmptyAttribute(reader, WSDLConstants.ATTR_TNS);
                parser.targetNamespace = tns;
                docInfo.setTargetNamespace(tns);
            }else{
                docInfo.setDocType(DOC_TYPE.OTHER);
                return;
            }

            while (XMLStreamReaderUtil.nextElementContent(reader) !=
                    XMLStreamConstants.END_ELEMENT) {
                 if(reader.getEventType() == XMLStreamConstants.END_DOCUMENT)
                    break;

                QName name = reader.getName();
                if (WSDLConstants.QNAME_PORT_TYPE.equals(name)) {
                    String pn = ParserUtil.getMandatoryNonEmptyAttribute(reader, WSDLConstants.ATTR_NAME);
                    if (portTypeName != null) {
                        if(portTypeName.getLocalPart().equals(pn) && portTypeName.getNamespaceURI().equals(docInfo.getTargetNamespace())) {
                            docInfo.setHavingPortType(true);
                        }
                    }
                    XMLStreamReaderUtil.skipElement(reader);
                } else if (WSDLConstants.QNAME_SERVICE.equals(name)) {
                    String sn = ParserUtil.getMandatoryNonEmptyAttribute(reader, WSDLConstants.ATTR_NAME);
                    QName sqn = new QName(docInfo.getTargetNamespace(), sn);
                    if(serviceName.equals(sqn)) {
                        parser.parseService(reader);
                        docInfo.setService(parser.wsdlDoc.getService(sqn));
                        if(reader.getEventType() != XMLStreamConstants.END_ELEMENT)
                            XMLStreamReaderUtil.next(reader);
                    } else {
                        XMLStreamReaderUtil.skipElement(reader);
                    }
                } else{
                    XMLStreamReaderUtil.skipElement(reader);
                }
            }
        } finally {
            reader.close();
        }
    }

    private RuntimeWSDLParser(EntityResolver resolver) {
        this.resolver = resolver;
    }

    /**
     * Make sure to return a "fresh" reader each time it is called because
     * more than one active reader may be needed within a single thread
     * to parse a WSDL file.
     */
    private static XMLStreamReader createReader(InputSource source) {
        // Char stream available?
        if (source.getCharacterStream() != null) {
            Reader reader = source.getCharacterStream();
            return new TidyXMLStreamReader(XMLStreamReaderFactory.createFreshXMLStreamReader(source.getSystemId(), reader), reader);
        }

        // Byte stream available?
        if (source.getByteStream() != null) {
            InputStream stream = source.getByteStream();
            return new TidyXMLStreamReader(XMLStreamReaderFactory.createFreshXMLStreamReader(source.getSystemId(), stream), stream);
        }

        // Otherwise, open URI
        try {
            InputStream stream = new URL(source.getSystemId()).openStream();
            return new TidyXMLStreamReader(XMLStreamReaderFactory.createFreshXMLStreamReader(source.getSystemId(), stream), stream);
        } catch (IOException e) {
            throw new WebServiceException(e);
        }
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
            } else if(WSDLConstants.QNAME_MESSAGE.equals(name)){
                parseMessage(reader);
            } else if(WSDLConstants.QNAME_PORT_TYPE.equals(name)){
                parsePortType(reader);
            } else if (WSDLConstants.QNAME_BINDING.equals(name)) {
                parseBinding(reader);
            } else if (WSDLConstants.QNAME_SERVICE.equals(name)) {
                parseService(reader);
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
                if(reader.getEventType() != XMLStreamConstants.END_ELEMENT)
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
        binding.setWsdlDocument(wsdlDoc);
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
            }else if(WSDLConstants.QNAME_OUTPUT.equals(name)){
                parseOutputBinding(reader, bindingOp);
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
                goToEnd(reader);
            }else if((SOAPConstants.QNAME_HEADER.equals(name) || SOAPConstants.QNAME_SOAP12HEADER.equals(name))){
                parseSOAPHeaderBinding(reader, bindingOp.getInputParts());
            }else if(MIMEConstants.QNAME_MULTIPART_RELATED.equals(name)){
                parseMimeMultipartBinding(reader, bindingOp.getInputParts(), bindingOp.getOutputMimeTypes());
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
                goToEnd(reader);
            }else if((SOAPConstants.QNAME_HEADER.equals(name) || SOAPConstants.QNAME_SOAP12HEADER.equals(name))){
                parseSOAPHeaderBinding(reader, bindingOp.getOutputParts());
            }else if(MIMEConstants.QNAME_MULTIPART_RELATED.equals(name)){
                parseMimeMultipartBinding(reader, bindingOp.getOutputParts(), bindingOp.getOutputMimeTypes());
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
    private static boolean parseSOAPBodyBinding(XMLStreamReader reader, Map<String, ParameterBinding> parts){
        String partsString = reader.getAttributeValue(null, "parts");
        if(partsString != null){
            List<String> partsList = XmlUtil.parseTokenList(partsString);
            if(partsList.isEmpty()){
                parts.put(" ", ParameterBinding.BODY);
            }else{
                for(String part:partsList){
                    parts.put(part, ParameterBinding.BODY);
                }
            }
            return true;
        }
        return false;
    }

    private static void parseSOAPHeaderBinding(XMLStreamReader reader, Map<String,ParameterBinding> parts){
        String part = reader.getAttributeValue(null, "part");
        //if(part == null| part.equals("")||message == null || message.equals("")){
        if(part == null| part.equals("")){
            return;
        }

        //lets not worry about message attribute for now, probably additional headers wont be there
        //String message = reader.getAttributeValue(null, "message");
        //QName msgName = ParserUtil.getQName(reader, message);
        parts.put(part, ParameterBinding.HEADER);
        goToEnd(reader);
    }


    private static void parseMimeMultipartBinding(XMLStreamReader reader, Map<String, ParameterBinding> parts,
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

    private static void parseMIMEPart(XMLStreamReader reader, Map<String,ParameterBinding> parts,
                                      Map<String,String> mimeTypes) {
        boolean bodyFound = false;
        while (XMLStreamReaderUtil.nextElementContent(reader) != XMLStreamConstants.END_ELEMENT) {
            QName name = reader.getName();
            if(SOAPConstants.QNAME_BODY.equals(name) && !bodyFound){
                bodyFound = true;
                parseSOAPBodyBinding(reader, parts);
                XMLStreamReaderUtil.next(reader);
            }else if(SOAPConstants.QNAME_HEADER.equals(name)){
                bodyFound = true;
                parseSOAPHeaderBinding(reader, parts);
                XMLStreamReaderUtil.next(reader);
            }else if(MIMEConstants.QNAME_CONTENT.equals(name)){
                String part = reader.getAttributeValue(null, "part");
                String type = reader.getAttributeValue(null, "type");
                if((part == null) || (type == null)){
                    XMLStreamReaderUtil.skipElement(reader);
                    continue;
                }
                ParameterBinding sb = ParameterBinding.createAttachment(type);
                parts.put(part, sb);
                //mimeTypes.put(part, type);
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
        while (XMLStreamReaderUtil.nextElementContent(reader) != XMLStreamConstants.END_ELEMENT){
            XMLStreamReaderUtil.skipElement(reader);
        }
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
            }else{
                XMLStreamReaderUtil.skipElement(reader);
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

        QName operationQName = new QName(portType.getName().getNamespaceURI(), operationName);
        PortTypeOperation operation = new PortTypeOperation(operationQName);
        String parameterOrder = ParserUtil.getAttribute(reader, "parameterOrder");
        operation.setParameterOrder(parameterOrder);
        portType.put(operationName, operation);
        while (XMLStreamReaderUtil.nextElementContent(reader) != XMLStreamConstants.END_ELEMENT) {
            QName name = reader.getName();
            if(name.equals(WSDLConstants.QNAME_INPUT)){
                parsePortTypeOperationInput(reader, operation);
            }else if(name.equals(WSDLConstants.QNAME_OUTPUT)){
                parsePortTypeOperationOutput(reader, operation);
            }else{
                XMLStreamReaderUtil.skipElement(reader);
            }
        }
    }

    private void parsePortTypeOperationInput(XMLStreamReader reader, PortTypeOperation operation) {
        String msg = ParserUtil.getAttribute(reader, "message");
        QName msgName = ParserUtil.getQName(reader, msg);
        operation.setInputMessage(msgName);
        goToEnd(reader);
    }

    private void parsePortTypeOperationOutput(XMLStreamReader reader, PortTypeOperation operation) {
        String msg = ParserUtil.getAttribute(reader, "message");
        QName msgName = ParserUtil.getQName(reader, msg);
        operation.setOutputMessage(msgName);
        goToEnd(reader);
    }

    private void parseMessage(XMLStreamReader reader) {
        String msgName = ParserUtil.getMandatoryNonEmptyAttribute(reader, WSDLConstants.ATTR_NAME);
        Message msg = new Message(new QName(targetNamespace, msgName));
        while (XMLStreamReaderUtil.nextElementContent(reader) != XMLStreamConstants.END_ELEMENT) {
            QName name = reader.getName();
            if (WSDLConstants.QNAME_PART.equals(name)) {
                String part = ParserUtil.getMandatoryNonEmptyAttribute(reader, WSDLConstants.ATTR_NAME);
//                String desc = null;
//                int index = reader.getAttributeCount();
//                for (int i = 0; i < index; i++) {
//                    if (reader.getAttributeName(i).equals("element") || reader.getAttributeName(i).equals("type")) {
//                        desc = reader.getAttributeValue(i);
//                        break;
//                    }
//                }
//                if (desc == null)
//                    continue;
                msg.add(part);
                if(reader.getEventType() != XMLStreamConstants.END_ELEMENT)
                    goToEnd(reader);
            }else{
                XMLStreamReaderUtil.skipElement(reader);
            }
        }
        wsdlDoc.addMessage(msg);
        if(reader.getEventType() != XMLStreamConstants.END_ELEMENT)
            goToEnd(reader);
    }

    private static void goToEnd(XMLStreamReader reader){
        while (XMLStreamReaderUtil.nextElementContent(reader) != XMLStreamConstants.END_ELEMENT){
            XMLStreamReaderUtil.skipElement(reader);
        }
    }

}
