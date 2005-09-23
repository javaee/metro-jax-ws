/*
 * $Id: WSDLParser.java,v 1.5 2005-09-23 22:05:48 kohsuke Exp $
 */

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

package com.sun.tools.ws.wsdl.parser;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.sun.xml.ws.util.localization.LocalizableMessageFactory;
import com.sun.xml.ws.util.localization.Localizer;
import com.sun.tools.ws.util.xml.NullEntityResolver;
import com.sun.tools.ws.wsdl.document.Binding;
import com.sun.tools.ws.wsdl.document.BindingFault;
import com.sun.tools.ws.wsdl.document.BindingInput;
import com.sun.tools.ws.wsdl.document.BindingOperation;
import com.sun.tools.ws.wsdl.document.BindingOutput;
import com.sun.tools.ws.wsdl.document.Definitions;
import com.sun.tools.ws.wsdl.document.Documentation;
import com.sun.tools.ws.wsdl.document.Fault;
import com.sun.tools.ws.wsdl.document.Import;
import com.sun.tools.ws.wsdl.document.Input;
import com.sun.tools.ws.wsdl.document.Message;
import com.sun.tools.ws.wsdl.document.MessagePart;
import com.sun.tools.ws.wsdl.document.Operation;
import com.sun.tools.ws.wsdl.document.OperationStyle;
import com.sun.tools.ws.wsdl.document.Output;
import com.sun.tools.ws.wsdl.document.Port;
import com.sun.tools.ws.wsdl.document.PortType;
import com.sun.tools.ws.wsdl.document.Service;
import com.sun.tools.ws.wsdl.document.Types;
import com.sun.tools.ws.wsdl.document.WSDLConstants;
import com.sun.tools.ws.wsdl.document.WSDLDocument;
import com.sun.tools.ws.wsdl.document.schema.SchemaConstants;
import com.sun.tools.ws.wsdl.document.schema.SchemaKinds;
import com.sun.tools.ws.wsdl.framework.Entity;
import com.sun.tools.ws.wsdl.framework.Extensible;
import com.sun.tools.ws.wsdl.framework.ParseException;
import com.sun.tools.ws.wsdl.framework.ParserContext;
import com.sun.tools.ws.wsdl.framework.ParserListener;
import com.sun.tools.ws.util.xml.XmlUtil;

/**
 * A parser for WSDL documents.
 *
 * @author WS Development Team
 */
public class WSDLParser {

    public WSDLParser() {
        _extensionHandlers = new HashMap();
        hSet = new HashSet();

        // register handlers for default extensions
        register(new SOAPExtensionHandler());
        register(new HTTPExtensionHandler());
        register(new MIMEExtensionHandler());
        register(new SchemaExtensionHandler());
    }

    public void register(ExtensionHandler h) {
        _extensionHandlers.put(h.getNamespaceURI(), h);
        h.setExtensionHandlers(_extensionHandlers);
    }

    public void unregister(ExtensionHandler h) {
        _extensionHandlers.put(h.getNamespaceURI(), null);
        h.setExtensionHandlers(null);
    }

    public void unregister(String uri) {
        _extensionHandlers.put(uri, null);
    }

    public boolean getFollowImports() {
        return _followImports;
    }

    public void setFollowImports(boolean b) {
        _followImports = b;
    }

    public WSDLDocument getWSDLDocument(
        java.net.URL wsdlURL) {
        return getWSDLDocumentInternal(wsdlURL);
    }

    private WSDLDocument getWSDLDocumentInternal(java.net.URL wsdlURL) {

        InputStream wsdlInputStream = null;

        try {
            wsdlInputStream = new BufferedInputStream(wsdlURL.openStream());
        } catch (IOException e) {
            throw new ParseException("parsing.ioException",e);
        }
        InputSource wsdlDocumentSource = new InputSource(wsdlInputStream);
        setFollowImports(true);
        addParserListener(new ParserListener() {
            public void ignoringExtension(QName name, QName parent) {
                if (parent.equals(WSDLConstants.QNAME_TYPES)) {
                    // check for a schema element with the wrong namespace URI
                    if (name.getLocalPart().equals("schema")
                        && !name.getNamespaceURI().equals("")) {
                        warn(
                            "wsdlmodeler.warning.ignoringUnrecognizedSchemaExtension",
                            name.getNamespaceURI());
                    }
                }
            }

            public void doneParsingEntity(QName element, Entity entity) {
            }
        });

        WSDLDocument wsdlDoc = parse(wsdlDocumentSource, true);
        Iterator importedDocs = wsdlDoc.getDefinitions().imports();
        wsdlDoc = parseImportedDocuments(importedDocs, wsdlDoc);

        try {
            wsdlInputStream.close();
        } catch (IOException ioe) {
            throw new ParseException("parsing.ioException",ioe);
        }

        return wsdlDoc;
    }

    private WSDLDocument parseImportedDocuments(
        Iterator imports,
        WSDLDocument wsdlDoc) {

        Definitions wsdlDefinitions = wsdlDoc.getDefinitions();

        for (Iterator iter = imports; iter.hasNext();) {
            Import mport = (Import) iter.next();

            try {
                WSDLDocument importDoc =
                    getWSDLDocumentInternal(new URL(mport.getLocation()));
                Definitions definitions = importDoc.getDefinitions();
                for (Iterator siter = definitions.services();
                    siter.hasNext();
                    ) {
                    wsdlDefinitions.addServiceOveride((Service) siter.next());
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
                //To change body of catch statement use Options | File Templates.
            }
        }
        return wsdlDoc;
    }

    public void addParserListener(ParserListener l) {
        if (_listeners == null) {
            _listeners = new ArrayList();
        }
        _listeners.add(l);
    }

    public void removeParserListener(ParserListener l) {
        if (_listeners == null) {
            return;
        }
        _listeners.remove(l);
    }

    public WSDLDocument parse(InputSource source) {
        return parse(source, false);
    }

    public WSDLDocument parse(InputSource source, boolean useWSIBasicProfile) {
        this._useWSIBasicProfile = useWSIBasicProfile;
        _messageFactory =
            new LocalizableMessageFactory("com.sun.tools.ws.resources.wsdl");
        _localizer = new Localizer();

        WSDLDocument document = new WSDLDocument();
        document.setSystemId(source.getSystemId());
        ParserContext context = new ParserContext(document, _listeners);
        context.setFollowImports(_followImports);
        document.setDefinitions(parseDefinitions(context, source, null));
        return document;
    }

    protected Definitions parseDefinitions(
        ParserContext context,
        InputSource source,
        String expectedTargetNamespaceURI) {
        //bug fix: 4856674
        context.pushWSDLLocation();
        context.setWSDLLocation(source.getSystemId());
        Definitions definitions =
            parseDefinitionsNoImport(
                context,
                source,
                expectedTargetNamespaceURI);
        processImports(context, source, definitions);
        //bug fix: 4856674
        context.popWSDLLocation();
        return definitions;
    }

    protected void processImports(
        ParserContext context,
        InputSource source,
        Definitions definitions) {
        for (Iterator iter = definitions.imports(); iter.hasNext();) {
            Import i = (Import) iter.next();
            String location = i.getLocation();
            //bug fix: 4857762, add adjustedLocation to teh importDocuments and ignore if it
            //exists, to avoid duplicates
            if (location != null) {
                // NOTE - here we would really benefit from a URI class!
                String adjustedLocation =
                    source.getSystemId() == null
                        ? (context.getDocument().getSystemId() == null
                            ? location
                            : Util.processSystemIdWithBase(
                                context.getDocument().getSystemId(),
                                location))
                        : Util.processSystemIdWithBase(
                            source.getSystemId(),
                            location);

                try {
                    if (!context
                        .getDocument()
                        .isImportedDocument(adjustedLocation)) {
                        context.getDocument().addImportedEntity(
                            parseDefinitions(
                                context,
                                new InputSource(adjustedLocation),
                                i.getNamespace()));
                        context.getDocument().addImportedDocument(
                            adjustedLocation);
                    }
                } catch (ParseException e) {
                    // hardly the cleanest solution, but it should work in practice
                    if (e.getKey().equals("parsing.incorrectRootElement")) {
                        if (_useWSIBasicProfile) {
                            warn("warning.wsi.r2001", adjustedLocation);
                        }
                        // try to parse the document as an XSD one!
                        try {
                            SchemaParser parser = new SchemaParser();
                            context.getDocument().addImportedEntity(
                                parser.parseSchema(
                                    context,
                                    new InputSource(adjustedLocation),
                                    i.getNamespace()));
                        } catch (ParseException e2) {
                            if (e2
                                .getKey()
                                .equals("parsing.incorrectRootElement")) {
                                Util.fail(
                                    "parsing.unknownImportedDocumentType",
                                    location);
                            } else {
                                // a genuine parsing exception
                                throw e2;
                            }
                        }
                    } else {
                        // a genuine parsing exception
                        throw e;
                    }
                }
            }
        }
    }

    protected Definitions parseDefinitionsNoImport(
        ParserContext context,
        InputSource source,
        String expectedTargetNamespaceURI) {
        try {
            DocumentBuilderFactory builderFactory =
                DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(true);
            builderFactory.setValidating(false);
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            builder.setErrorHandler(new ErrorHandler() {
                public void error(SAXParseException e)
                    throws SAXParseException {
                    throw e;
                }

                public void fatalError(SAXParseException e)
                    throws SAXParseException {
                    throw e;
                }

                public void warning(SAXParseException err)
                    throws SAXParseException {
                    // do nothing
                }
            });
            builder.setEntityResolver(new NullEntityResolver());

            try {
                Document document = builder.parse(source);
                return parseDefinitionsNoImport(
                    context,
                    document,
                    expectedTargetNamespaceURI);
            } catch (IOException e) {
                if (source.getSystemId() != null) {
                    throw new ParseException(
                        "parsing.ioExceptionWithSystemId",
                        source.getSystemId(),e);
                } else {
                    throw new ParseException("parsing.ioException",e);
                }
            } catch (SAXException e) {
                if (source.getSystemId() != null) {
                    throw new ParseException(
                        "parsing.saxExceptionWithSystemId",
                        source.getSystemId(),
                        e);
                } else {
                    throw new ParseException("parsing.saxException",e);
                }
            }
        } catch (ParserConfigurationException e) {
            throw new ParseException("parsing.parserConfigException",e);
        } catch (FactoryConfigurationError e) {
            throw new ParseException("parsing.factoryConfigException",e);
        }
    }

    protected Definitions parseDefinitionsNoImport(
        ParserContext context,
        Document doc,
        String expectedTargetNamespaceURI) {
        _targetNamespaceURI = null;
        Element root = doc.getDocumentElement();
        Util.verifyTagNSRootElement(root, WSDLConstants.QNAME_DEFINITIONS);
        return parseDefinitionsNoImport(
            context,
            root,
            expectedTargetNamespaceURI);
    }

    protected Definitions parseDefinitionsNoImport(
        ParserContext context,
        Element e,
        String expectedTargetNamespaceURI) {
        context.push();
        context.registerNamespaces(e);

        Definitions definitions = new Definitions(context.getDocument());
        String name = XmlUtil.getAttributeOrNull(e, Constants.ATTR_NAME);
        definitions.setName(name);

        _targetNamespaceURI =
            XmlUtil.getAttributeOrNull(e, Constants.ATTR_TARGET_NAMESPACE);

//        if (expectedTargetNamespaceURI != null
//            && !expectedTargetNamespaceURI.equals(_targetNamespaceURI)
//            && _useWSIBasicProfile) {
//            warn(
//                "warning.wsi.r2002",
//                new Object[] {
//                    _targetNamespaceURI,
//                    expectedTargetNamespaceURI });
//        }

        /*
        // I commented this out because it conflicts with the interpretation of the
        // <wsdl:import/> element given by the soapbuilders in their round 3 tests.
        // In particular, the namespace mentioned by a <wsdl:import/> doesn't have to
        // be the target namespace of the WSDL, it could be the target namespace of
        // a schema within that WSDL document, or even a target namespace in some
        // WSDL or schema imported by that document!
        if (expectedTargetNamespaceURI != null &&
             !expectedTargetNamespaceURI.equals(_targetNamespaceURI)) {
            throw new ValidationException("validation.incorrectTargetNamespace", new Object[] { _targetNamespaceURI, expectedTargetNamespaceURI });
        }
        */

        definitions.setTargetNamespaceURI(_targetNamespaceURI);

        boolean gotDocumentation = false;
        boolean gotTypes = false;

        for (Iterator iter = XmlUtil.getAllChildren(e); iter.hasNext();) {
            Element e2 = Util.nextElement(iter);
            if (e2 == null)
                break;

            if (XmlUtil.matchesTagNS(e2, WSDLConstants.QNAME_DOCUMENTATION)) {
                if (gotDocumentation) {
                    Util.fail(
                        "parsing.onlyOneDocumentationAllowed",
                        e.getLocalName());
                }
                gotDocumentation = true;
                definitions.setDocumentation(getDocumentationFor(e2));
            } else if (XmlUtil.matchesTagNS(e2, WSDLConstants.QNAME_TYPES)) {
                if (gotTypes) {
                    Util.fail(
                        "parsing.onlyOneTypesAllowed",
                        Constants.TAG_DEFINITIONS);
                }
                //add all the wsdl:type elements to latter make a list of all the schema elements
                // that will be needed to create jaxb model
                addSchemaElements(e2);

                //definitions.setTypes(parseTypes(context, definitions, e2));
            } else if (XmlUtil.matchesTagNS(e2, WSDLConstants.QNAME_MESSAGE)) {
                Message message = parseMessage(context, definitions, e2);
                definitions.add(message);
            } else if (
                XmlUtil.matchesTagNS(e2, WSDLConstants.QNAME_PORT_TYPE)) {
                PortType portType = parsePortType(context, definitions, e2);
                definitions.add(portType);
            } else if (XmlUtil.matchesTagNS(e2, WSDLConstants.QNAME_BINDING)) {
                Binding binding = parseBinding(context, definitions, e2);
                definitions.add(binding);
            } else if (XmlUtil.matchesTagNS(e2, WSDLConstants.QNAME_SERVICE)) {
                Service service = parseService(context, definitions, e2);
                definitions.add(service);
            } else if (XmlUtil.matchesTagNS(e2, WSDLConstants.QNAME_IMPORT)) {
                definitions.add(parseImport(context, definitions, e2));
            } else if (
                (_useWSIBasicProfile)
                    && (XmlUtil.matchesTagNS(e2, SchemaConstants.QNAME_IMPORT))) {
                warn("warning.wsi.r2003");
            } else {
                // possible extensibility element -- must live outside the WSDL namespace
                checkNotWsdlElement(e2);
                if (!handleExtension(context, definitions, e2)) {
                    checkNotWsdlRequired(e2);
                }
            }
        }

        context.pop();
        context.fireDoneParsingEntity(
            WSDLConstants.QNAME_DEFINITIONS,
            definitions);
        return definitions;
    }

    protected Message parseMessage(
        ParserContext context,
        Definitions definitions,
        Element e) {
        context.push();
        context.registerNamespaces(e);
        Message message = new Message(definitions);
        String name = Util.getRequiredAttribute(e, Constants.ATTR_NAME);
        message.setName(name);

        boolean gotDocumentation = false;

        for (Iterator iter = XmlUtil.getAllChildren(e); iter.hasNext();) {
            Element e2 = Util.nextElement(iter);
            if (e2 == null)
                break;

            if (XmlUtil.matchesTagNS(e2, WSDLConstants.QNAME_DOCUMENTATION)) {
                if (gotDocumentation) {
                    Util.fail(
                        "parsing.onlyOneDocumentationAllowed",
                        e.getLocalName());
                }
                gotDocumentation = true;
                message.setDocumentation(getDocumentationFor(e2));
            } else if (XmlUtil.matchesTagNS(e2, WSDLConstants.QNAME_PART)) {
                MessagePart part = parseMessagePart(context, e2);
                message.add(part);
            } else {
                Util.fail(
                    "parsing.invalidElement",
                    e2.getTagName(),
                    e2.getNamespaceURI());
            }
        }

        context.pop();
        context.fireDoneParsingEntity(WSDLConstants.QNAME_MESSAGE, message);
        return message;
    }

    protected MessagePart parseMessagePart(ParserContext context, Element e) {
        context.push();
        context.registerNamespaces(e);
        MessagePart part = new MessagePart();
        String partName = Util.getRequiredAttribute(e, Constants.ATTR_NAME);
        part.setName(partName);

        String elementAttr =
            XmlUtil.getAttributeOrNull(e, Constants.ATTR_ELEMENT);
        String typeAttr = XmlUtil.getAttributeOrNull(e, Constants.ATTR_TYPE);

        if (elementAttr != null) {
            if (typeAttr != null) {
                Util.fail("parsing.onlyOneOfElementOrTypeRequired", partName);
            }

            part.setDescriptor(context.translateQualifiedName(elementAttr));
            part.setDescriptorKind(SchemaKinds.XSD_ELEMENT);
        } else if (typeAttr != null) {
            part.setDescriptor(context.translateQualifiedName(typeAttr));
            part.setDescriptorKind(SchemaKinds.XSD_TYPE);
        } else {
            // XXX-NOTE - this is wrong; for extensibility purposes,
            // any attribute can be specified on a <part> element, so
            // we need to put an extensibility hook here
            Util.fail("parsing.elementOrTypeRequired", partName);
        }

        context.pop();
        context.fireDoneParsingEntity(WSDLConstants.QNAME_PART, part);
        return part;
    }

    protected PortType parsePortType(
        ParserContext context,
        Definitions definitions,
        Element e) {
        context.push();
        context.registerNamespaces(e);
        PortType portType = new PortType(definitions);
        String name = Util.getRequiredAttribute(e, Constants.ATTR_NAME);
        portType.setName(name);

        boolean gotDocumentation = false;

        for (Iterator iter = XmlUtil.getAllChildren(e); iter.hasNext();) {
            Element e2 = Util.nextElement(iter);
            if (e2 == null)
                break;

            if (XmlUtil.matchesTagNS(e2, WSDLConstants.QNAME_DOCUMENTATION)) {
                if (gotDocumentation) {
                    Util.fail(
                        "parsing.onlyOneDocumentationAllowed",
                        e.getLocalName());
                }
                gotDocumentation = true;
                portType.setDocumentation(getDocumentationFor(e2));
            } else if (
                XmlUtil.matchesTagNS(e2, WSDLConstants.QNAME_OPERATION)) {
                Operation op = parsePortTypeOperation(context, e2);
                portType.add(op);
            } else {
                // possible extensibility element -- must live outside the WSDL namespace
                checkNotWsdlElement(e2);
                if (!handleExtension(context, portType, e2)) {
                    checkNotWsdlRequired(e2);
                }
            }/*else {
                Util.fail(
                    "parsing.invalidElement",
                    e2.getTagName(),
                    e2.getNamespaceURI());
            }*/
        }

        context.pop();
        context.fireDoneParsingEntity(WSDLConstants.QNAME_PORT_TYPE, portType);
        return portType;
    }

    protected Operation parsePortTypeOperation(
        ParserContext context,
        Element e) {
        context.push();
        context.registerNamespaces(e);

        Operation operation = new Operation();
        String name = Util.getRequiredAttribute(e, Constants.ATTR_NAME);
        operation.setName(name);
        String parameterOrderAttr =
            XmlUtil.getAttributeOrNull(e, Constants.ATTR_PARAMETER_ORDER);
        operation.setParameterOrder(parameterOrderAttr);

        boolean gotDocumentation = false;

        boolean gotInput = false;
        boolean gotOutput = false;
        boolean gotFault = false;
        boolean inputBeforeOutput = false;

        for (Iterator iter = XmlUtil.getAllChildren(e); iter.hasNext();) {
            Element e2 = Util.nextElement(iter);
            if (e2 == null)
                break;

            if (XmlUtil.matchesTagNS(e2, WSDLConstants.QNAME_DOCUMENTATION)) {
                if (gotDocumentation) {
                    Util.fail(
                        "parsing.onlyOneDocumentationAllowed",
                        e.getLocalName());
                }
                gotDocumentation = true;
                operation.setDocumentation(getDocumentationFor(e2));
            } else if (XmlUtil.matchesTagNS(e2, WSDLConstants.QNAME_INPUT)) {
                if (gotInput) {
                    Util.fail(
                        "parsing.tooManyElements",
                        new Object[] {
                            Constants.TAG_INPUT,
                            Constants.TAG_OPERATION,
                            name });
                }

                context.push();
                context.registerNamespaces(e2);
                Input input = new Input();
                String messageAttr =
                    Util.getRequiredAttribute(e2, Constants.ATTR_MESSAGE);
                input.setMessage(context.translateQualifiedName(messageAttr));
                String nameAttr =
                    XmlUtil.getAttributeOrNull(e2, Constants.ATTR_NAME);
                input.setName(nameAttr);
                operation.setInput(input);
                gotInput = true;
                if (gotOutput) {
                    inputBeforeOutput = false;
                }

                // verify that there is at most one child element and it is a documentation element
                boolean gotDocumentation2 = false;
                for (Iterator iter2 = XmlUtil.getAllChildren(e2);
                    iter2.hasNext();
                    ) {
                    Element e3 = Util.nextElement(iter2);
                    if (e3 == null)
                        break;

                    if (XmlUtil
                        .matchesTagNS(e3, WSDLConstants.QNAME_DOCUMENTATION)) {
                        if (gotDocumentation2) {
                            Util.fail(
                                "parsing.onlyOneDocumentationAllowed",
                                e.getLocalName());
                        }
                        gotDocumentation2 = true;
                        input.setDocumentation(getDocumentationFor(e3));
                    } else {
                        Util.fail(
                            "parsing.invalidElement",
                            e3.getTagName(),
                            e3.getNamespaceURI());
                    }
                }
                context.pop();
            } else if (XmlUtil.matchesTagNS(e2, WSDLConstants.QNAME_OUTPUT)) {
                if (gotOutput) {
                    Util.fail(
                        "parsing.tooManyElements",
                        new Object[] {
                            Constants.TAG_OUTPUT,
                            Constants.TAG_OPERATION,
                            name });
                }

                context.push();
                context.registerNamespaces(e2);
                Output output = new Output();
                String messageAttr =
                    Util.getRequiredAttribute(e2, Constants.ATTR_MESSAGE);
                output.setMessage(context.translateQualifiedName(messageAttr));
                String nameAttr =
                    XmlUtil.getAttributeOrNull(e2, Constants.ATTR_NAME);
                output.setName(nameAttr);
                operation.setOutput(output);
                gotOutput = true;
                if (gotInput) {
                    inputBeforeOutput = true;
                }

                // verify that there is at most one child element and it is a documentation element
                boolean gotDocumentation2 = false;
                for (Iterator iter2 = XmlUtil.getAllChildren(e2);
                    iter2.hasNext();
                    ) {
                    Element e3 = Util.nextElement(iter2);
                    if (e3 == null)
                        break;

                    if (XmlUtil
                        .matchesTagNS(e3, WSDLConstants.QNAME_DOCUMENTATION)) {
                        if (gotDocumentation2) {
                            Util.fail(
                                "parsing.onlyOneDocumentationAllowed",
                                e.getLocalName());
                        }
                        gotDocumentation2 = true;
                        output.setDocumentation(getDocumentationFor(e3));
                    } else {
                        Util.fail(
                            "parsing.invalidElement",
                            e3.getTagName(),
                            e3.getNamespaceURI());
                    }
                }
                context.pop();
            } else if (XmlUtil.matchesTagNS(e2, WSDLConstants.QNAME_FAULT)) {
                context.push();
                context.registerNamespaces(e2);
                Fault fault = new Fault();
                String messageAttr =
                    Util.getRequiredAttribute(e2, Constants.ATTR_MESSAGE);
                fault.setMessage(context.translateQualifiedName(messageAttr));
                String nameAttr =
                    XmlUtil.getAttributeOrNull(e2, Constants.ATTR_NAME);
                fault.setName(nameAttr);
                operation.addFault(fault);
                gotFault = true;

                // verify that there is at most one child element and it is a documentation element
                boolean gotDocumentation2 = false;
                for (Iterator iter2 = XmlUtil.getAllChildren(e2);
                    iter2.hasNext();
                    ) {
                    Element e3 = Util.nextElement(iter2);
                    if (e3 == null)
                        break;

                    if (XmlUtil
                        .matchesTagNS(e3, WSDLConstants.QNAME_DOCUMENTATION)) {
                        if (gotDocumentation2) {
                            Util.fail(
                                "parsing.onlyOneDocumentationAllowed",
                                e.getLocalName());
                        }
                        gotDocumentation2 = true;
                        fault.setDocumentation(getDocumentationFor(e3));
                    } else {
                        // possible extensibility element -- must live outside the WSDL namespace
                        checkNotWsdlElement(e3);
                        if (!handleExtension(context, fault, e3)) {
                            checkNotWsdlRequired(e3);
                        }
                    }/*else {
                        Util.fail(
                            "parsing.invalidElement",
                            e3.getTagName(),
                            e3.getNamespaceURI());
                    }*/
                }
                context.pop();
            } else {
                // possible extensibility element -- must live outside the WSDL namespace
                checkNotWsdlElement(e2);
                if (!handleExtension(context, operation, e2)) {
                    checkNotWsdlRequired(e2);
                }
            }/*else {
                Util.fail(
                    "parsing.invalidElement",
                    e2.getTagName(),
                    e2.getNamespaceURI());
            }*/
        }

        if (gotInput && !gotOutput && !gotFault) {
            operation.setStyle(OperationStyle.ONE_WAY);
        } else if (gotInput && gotOutput && inputBeforeOutput) {
            operation.setStyle(OperationStyle.REQUEST_RESPONSE);
        } else if (gotInput && gotOutput && !inputBeforeOutput) {
            operation.setStyle(OperationStyle.SOLICIT_RESPONSE);
        } else if (gotOutput && !gotInput && !gotFault) {
            operation.setStyle(OperationStyle.NOTIFICATION);
        } else {
            Util.fail("parsing.invalidOperationStyle", name);
        }

        context.pop();
        context.fireDoneParsingEntity(WSDLConstants.QNAME_OPERATION, operation);
        return operation;
    }

    protected Binding parseBinding(
        ParserContext context,
        Definitions definitions,
        Element e) {
        context.push();
        context.registerNamespaces(e);
        Binding binding = new Binding(definitions);
        String name = Util.getRequiredAttribute(e, Constants.ATTR_NAME);
        binding.setName(name);
        String typeAttr = Util.getRequiredAttribute(e, Constants.ATTR_TYPE);
        binding.setPortType(context.translateQualifiedName(typeAttr));

        boolean gotDocumentation = false;

        for (Iterator iter = XmlUtil.getAllChildren(e); iter.hasNext();) {
            Element e2 = Util.nextElement(iter);
            if (e2 == null)
                break;

            if (XmlUtil.matchesTagNS(e2, WSDLConstants.QNAME_DOCUMENTATION)) {
                if (gotDocumentation) {
                    Util.fail(
                        "parsing.onlyOneDocumentationAllowed",
                        e.getLocalName());
                }
                gotDocumentation = true;
                binding.setDocumentation(getDocumentationFor(e2));
            } else if (
                XmlUtil.matchesTagNS(e2, WSDLConstants.QNAME_OPERATION)) {
                BindingOperation op = parseBindingOperation(context, e2);
                binding.add(op);
            } else {
                // possible extensibility element -- must live outside the WSDL namespace
                checkNotWsdlElement(e2);
                if (!handleExtension(context, binding, e2)) {
                    checkNotWsdlRequired(e2);
                }
            }
        }

        context.pop();
        context.fireDoneParsingEntity(WSDLConstants.QNAME_BINDING, binding);
        return binding;
    }

    protected BindingOperation parseBindingOperation(
        ParserContext context,
        Element e) {
        context.push();
        context.registerNamespaces(e);
        BindingOperation operation = new BindingOperation();
        String name = Util.getRequiredAttribute(e, Constants.ATTR_NAME);
        operation.setName(name);

        boolean gotDocumentation = false;

        boolean gotInput = false;
        boolean gotOutput = false;
        boolean gotFault = false;
        boolean inputBeforeOutput = false;

        for (Iterator iter = XmlUtil.getAllChildren(e); iter.hasNext();) {
            Element e2 = Util.nextElement(iter);
            if (e2 == null)
                break;
            if (XmlUtil.matchesTagNS(e2, WSDLConstants.QNAME_DOCUMENTATION)) {
                if (gotDocumentation) {
                    Util.fail(
                        "parsing.onlyOneDocumentationAllowed",
                        e.getLocalName());
                }
                gotDocumentation = true;
                operation.setDocumentation(getDocumentationFor(e2));
            } else if (XmlUtil.matchesTagNS(e2, WSDLConstants.QNAME_INPUT)) {
                if (gotInput) {
                    Util.fail(
                        "parsing.tooManyElements",
                        new Object[] {
                            Constants.TAG_INPUT,
                            Constants.TAG_OPERATION,
                            name });
                }

                /* Here we check for the use scenario */
                Iterator itere2 = XmlUtil.getAllChildren(e2);
                Element ee = Util.nextElement(itere2);
                if (hSet.isEmpty()) {
                    hSet.add(ee.getAttribute("use"));
                } else {
                    /* this codition will happen when the wsdl used has a mixture of
                       literal and encoded style */
                    if (!hSet.contains(ee.getAttribute("use"))
                        && (ee.getAttribute("use") != "")) {
                        hSet.add(ee.getAttribute("use"));
                    }
                }

                context.push();
                context.registerNamespaces(e2);
                BindingInput input = new BindingInput();
                String nameAttr =
                    XmlUtil.getAttributeOrNull(e2, Constants.ATTR_NAME);
                input.setName(nameAttr);
                operation.setInput(input);
                gotInput = true;
                if (gotOutput) {
                    inputBeforeOutput = false;
                }

                // verify that there is at most one child element and it is a documentation element
                boolean gotDocumentation2 = false;
                for (Iterator iter2 = XmlUtil.getAllChildren(e2);
                    iter2.hasNext();
                    ) {
                    Element e3 = Util.nextElement(iter2);
                    if (e3 == null)
                        break;

                    if (XmlUtil
                        .matchesTagNS(e3, WSDLConstants.QNAME_DOCUMENTATION)) {
                        if (gotDocumentation2) {
                            Util.fail(
                                "parsing.onlyOneDocumentationAllowed",
                                e.getLocalName());
                        }
                        gotDocumentation2 = true;
                        input.setDocumentation(getDocumentationFor(e3));
                    } else {
                        // possible extensibility element -- must live outside the WSDL namespace
                        checkNotWsdlElement(e3);
                        if (!handleExtension(context, input, e3)) {
                            checkNotWsdlRequired(e3);
                        }
                    }
                }
                context.pop();
            } else if (XmlUtil.matchesTagNS(e2, WSDLConstants.QNAME_OUTPUT)) {
                if (gotOutput) {
                    Util.fail(
                        "parsing.tooManyElements",
                        new Object[] {
                            Constants.TAG_OUTPUT,
                            Constants.TAG_OPERATION,
                            name });
                }

                context.push();
                context.registerNamespaces(e2);
                BindingOutput output = new BindingOutput();
                String nameAttr =
                    XmlUtil.getAttributeOrNull(e2, Constants.ATTR_NAME);
                output.setName(nameAttr);
                operation.setOutput(output);
                gotOutput = true;
                if (gotInput) {
                    inputBeforeOutput = true;
                }

                // verify that there is at most one child element and it is a documentation element
                boolean gotDocumentation2 = false;
                for (Iterator iter2 = XmlUtil.getAllChildren(e2);
                    iter2.hasNext();
                    ) {

                    Element e3 = Util.nextElement(iter2);
                    if (e3 == null)
                        break;

                    if (XmlUtil
                        .matchesTagNS(e3, WSDLConstants.QNAME_DOCUMENTATION)) {
                        if (gotDocumentation2) {
                            Util.fail(
                                "parsing.onlyOneDocumentationAllowed",
                                e.getLocalName());
                        }
                        gotDocumentation2 = true;
                        output.setDocumentation(getDocumentationFor(e3));
                    } else {
                        // possible extensibility element -- must live outside the WSDL namespace
                        checkNotWsdlElement(e3);
                        if (!handleExtension(context, output, e3)) {
                            checkNotWsdlRequired(e3);
                        }
                    }
                }
                context.pop();
            } else if (XmlUtil.matchesTagNS(e2, WSDLConstants.QNAME_FAULT)) {
                context.push();
                context.registerNamespaces(e2);
                BindingFault fault = new BindingFault();
                String nameAttr =
                    Util.getRequiredAttribute(e2, Constants.ATTR_NAME);
                fault.setName(nameAttr);
                operation.addFault(fault);
                gotFault = true;

                // verify that there is at most one child element and it is a documentation element
                boolean gotDocumentation2 = false;
                for (Iterator iter2 = XmlUtil.getAllChildren(e2);
                    iter2.hasNext();
                    ) {
                    Element e3 = Util.nextElement(iter2);
                    if (e3 == null)
                        break;

                    if (XmlUtil
                        .matchesTagNS(e3, WSDLConstants.QNAME_DOCUMENTATION)) {
                        if (gotDocumentation2) {
                            Util.fail(
                                "parsing.onlyOneDocumentationAllowed",
                                e.getLocalName());
                        }
                        gotDocumentation2 = true;
                        fault.setDocumentation(getDocumentationFor(e3));
                    } else {
                        // possible extensibility element -- must live outside the WSDL namespace
                        checkNotWsdlElement(e3);
                        if (!handleExtension(context, fault, e3)) {
                            checkNotWsdlRequired(e3);
                        }
                    }
                }
                context.pop();
            } else {
                // possible extensibility element -- must live outside the WSDL namespace
                checkNotWsdlElement(e2);
                if (!handleExtension(context, operation, e2)) {
                    checkNotWsdlRequired(e2);
                }
            }
        }

        if (gotInput && !gotOutput && !gotFault) {
            operation.setStyle(OperationStyle.ONE_WAY);
        } else if (gotInput && gotOutput && inputBeforeOutput) {
            operation.setStyle(OperationStyle.REQUEST_RESPONSE);
        } else if (gotInput && gotOutput && !inputBeforeOutput) {
            operation.setStyle(OperationStyle.SOLICIT_RESPONSE);
        } else if (gotOutput && !gotInput && !gotFault) {
            operation.setStyle(OperationStyle.NOTIFICATION);
        } else {
            Util.fail("parsing.invalidOperationStyle", name);
        }

        context.pop();
        context.fireDoneParsingEntity(WSDLConstants.QNAME_OPERATION, operation);
        return operation;
    }

    protected Import parseImport(
        ParserContext context,
        Definitions definitions,
        Element e) {
        context.push();
        context.registerNamespaces(e);
        Import anImport = new Import();
        String namespace =
            Util.getRequiredAttribute(e, Constants.ATTR_NAMESPACE);
        anImport.setNamespace(namespace);
        String location = Util.getRequiredAttribute(e, Constants.ATTR_LOCATION);
        anImport.setLocation(location);

        // according to the schema in the WSDL 1.1 spec, an import can have a documentation element
        boolean gotDocumentation = false;

        for (Iterator iter = XmlUtil.getAllChildren(e); iter.hasNext();) {
            Element e2 = Util.nextElement(iter);
            if (e2 == null)
                break;

            if (XmlUtil.matchesTagNS(e2, WSDLConstants.QNAME_DOCUMENTATION)) {
                if (gotDocumentation) {
                    Util.fail(
                        "parsing.onlyOneDocumentationAllowed",
                        e.getLocalName());
                }
                gotDocumentation = true;
                anImport.setDocumentation(getDocumentationFor(e2));
            } else {
                Util.fail(
                    "parsing.invalidElement",
                    e2.getTagName(),
                    e2.getNamespaceURI());
            }
        }
        context.pop();
        context.fireDoneParsingEntity(WSDLConstants.QNAME_IMPORT, anImport);
        return anImport;
    }

    protected Service parseService(
        ParserContext context,
        Definitions definitions,
        Element e) {
        context.push();
        context.registerNamespaces(e);
        Service service = new Service(definitions);
        String name = Util.getRequiredAttribute(e, Constants.ATTR_NAME);
        service.setName(name);

        boolean gotDocumentation = false;

        for (Iterator iter = XmlUtil.getAllChildren(e); iter.hasNext();) {
            Element e2 = Util.nextElement(iter);
            if (e2 == null)
                break;

            if (XmlUtil.matchesTagNS(e2, WSDLConstants.QNAME_DOCUMENTATION)) {
                if (gotDocumentation) {
                    Util.fail(
                        "parsing.onlyOneDocumentationAllowed",
                        e.getLocalName());
                }
                gotDocumentation = true;
                service.setDocumentation(getDocumentationFor(e2));
            } else if (XmlUtil.matchesTagNS(e2, WSDLConstants.QNAME_PORT)) {
                Port port = parsePort(context, definitions, e2);
                service.add(port);
            } else {
                // possible extensibility element -- must live outside the WSDL namespace
                checkNotWsdlElement(e2);
                if (!handleExtension(context, service, e2)) {
                    checkNotWsdlRequired(e2);
                }
            }
        }

        context.pop();
        context.fireDoneParsingEntity(WSDLConstants.QNAME_SERVICE, service);
        return service;
    }

    protected Port parsePort(
        ParserContext context,
        Definitions definitions,
        Element e) {
        context.push();
        context.registerNamespaces(e);

        Port port = new Port(definitions);
        String name = Util.getRequiredAttribute(e, Constants.ATTR_NAME);
        port.setName(name);

        String bindingAttr =
            Util.getRequiredAttribute(e, Constants.ATTR_BINDING);
        port.setBinding(context.translateQualifiedName(bindingAttr));

        boolean gotDocumentation = false;

        for (Iterator iter = XmlUtil.getAllChildren(e); iter.hasNext();) {
            Element e2 = Util.nextElement(iter);
            if (e2 == null)
                break;

            if (XmlUtil.matchesTagNS(e2, WSDLConstants.QNAME_DOCUMENTATION)) {
                if (gotDocumentation) {
                    Util.fail(
                        "parsing.onlyOneDocumentationAllowed",
                        e.getLocalName());
                }
                gotDocumentation = true;
                port.setDocumentation(getDocumentationFor(e2));
            } else {
                // possible extensibility element -- must live outside the WSDL namespace
                checkNotWsdlElement(e2);
                if (!handleExtension(context, port, e2)) {
                    checkNotWsdlRequired(e2);
                }
            }
        }

        context.pop();
        context.fireDoneParsingEntity(WSDLConstants.QNAME_PORT, port);
        return port;
    }

    protected Types parseTypes(
        ParserContext context,
        Definitions definitions,
        Element e) {
        context.push();
        context.registerNamespaces(e);
        Types types = new Types();

        boolean gotDocumentation = false;

        for (Iterator iter = XmlUtil.getAllChildren(e); iter.hasNext();) {
            Element e2 = Util.nextElement(iter);
            if (e2 == null)
                break;

            if (XmlUtil.matchesTagNS(e2, WSDLConstants.QNAME_DOCUMENTATION)) {
                if (gotDocumentation) {
                    Util.fail(
                        "parsing.onlyOneDocumentationAllowed",
                        e.getLocalName());
                }
                gotDocumentation = true;
                types.setDocumentation(getDocumentationFor(e2));
            } //bug fix 4854004
            else if (
                (_useWSIBasicProfile)
                    && (XmlUtil.matchesTagNS(e2, SchemaConstants.QNAME_IMPORT))) {
                warn("warning.wsi.r2003");
            } else {
                // possible extensibility element -- must live outside the WSDL namespace
                checkNotWsdlElement(e2);
                try {
                    if (!handleExtension(context, types, e2)) {
                        checkNotWsdlRequired(e2);
                    }
                } catch (ParseException pe) {
                    if (pe.getKey().equals("parsing.incorrectRootElement")) {
                        if (_useWSIBasicProfile) {
                            warn("warning.wsi.r2004");
                        }
                        throw pe;
                    }
                }
            }
        }

        context.pop();
        context.fireDoneParsingEntity(WSDLConstants.QNAME_TYPES, types);
        return types;
    }

    private List _elements = new ArrayList();

    public void addSchemaElements(Element typesElement){
        for (Iterator iter = XmlUtil.getAllChildren(typesElement); iter.hasNext();) {
            Element e = Util.nextElement(iter);
            if (e == null)
                break;

            if (XmlUtil.matchesTagNS(e, SchemaConstants.QNAME_SCHEMA)) {
                _elements.add(e);
            } else {
                // possible extensibility element -- must live outside the WSDL namespace
                checkNotWsdlElement(e);
            }
        }
    }

    public List getSchemaElements(){
        return _elements;
    }

    protected boolean handleExtension(
        ParserContext context,
        Extensible entity,
        Element e) {
        ExtensionHandler h =
            (ExtensionHandler) _extensionHandlers.get(e.getNamespaceURI());
        if (h == null) {
            context.fireIgnoringExtension(
                new QName(e.getNamespaceURI(), e.getLocalName()),
                ((Entity) entity).getElementName());
            return false;
        } else {
            return h.doHandleExtension(context, entity, e);
        }
    }

    protected void checkNotWsdlElement(Element e) {
        // possible extensibility element -- must live outside the WSDL namespace
        if (e.getNamespaceURI().equals(Constants.NS_WSDL))
            Util.fail("parsing.invalidWsdlElement", e.getTagName());
    }

    protected void checkNotWsdlRequired(Element e) {
        // check the wsdl:required attribute, fail if set to "true"
        String required =
            XmlUtil.getAttributeNSOrNull(
                e,
                Constants.ATTR_REQUIRED,
                Constants.NS_WSDL);
        if (required != null && required.equals(Constants.TRUE)) {
            Util.fail(
                "parsing.requiredExtensibilityElement",
                e.getTagName(),
                e.getNamespaceURI());
        }
    }

    protected Documentation getDocumentationFor(Element e) {
        String s = XmlUtil.getTextForNode(e);
        if (s == null) {
            return null;
        } else {
            return new Documentation(s);
        }
    }

    protected void error(String key) {
        System.err.println(
            _localizer.localize(_messageFactory.getMessage(key)));
    }

    public HashSet getUse() {
        return hSet;
    }

    protected void warn(String key) {
        System.err.println(
            _localizer.localize(_messageFactory.getMessage(key)));
    }

    protected void warn(String key, String arg) {
        System.err.println(
            _localizer.localize(_messageFactory.getMessage(key, arg)));
    }

    protected void warn(String key, Object[] args) {
        System.err.println(
            _localizer.localize(_messageFactory.getMessage(key, args)));
    }

    private boolean _followImports;
    private String _targetNamespaceURI;
    private Map _extensionHandlers;
    private ArrayList _listeners;
    private boolean _useWSIBasicProfile = false;
    private LocalizableMessageFactory _messageFactory = null;
    private Localizer _localizer;
    private HashSet hSet = null;
}
