/*
 * $Id: WSDLModelerBase.java,v 1.2 2005-07-18 18:14:04 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.processor.modeler.wsdl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;

import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.sun.tools.ws.processor.ProcessorOptions;
import com.sun.tools.ws.processor.config.WSDLModelInfo;
import com.sun.tools.ws.processor.generator.Names;
import com.sun.tools.ws.processor.model.AbstractType;
import com.sun.tools.ws.processor.model.Block;
import com.sun.tools.ws.processor.model.Fault;
import com.sun.tools.ws.processor.model.Model;
import com.sun.tools.ws.processor.model.ModelException;
import com.sun.tools.ws.processor.model.ModelObject;
import com.sun.tools.ws.processor.model.ModelProperties;
import com.sun.tools.ws.processor.model.Operation;
import com.sun.tools.ws.processor.model.Parameter;
import com.sun.tools.ws.processor.model.Port;
import com.sun.tools.ws.processor.model.Request;
import com.sun.tools.ws.processor.model.Response;
import com.sun.tools.ws.processor.model.Service;
import com.sun.tools.ws.processor.model.java.JavaException;
import com.sun.tools.ws.processor.model.java.JavaInterface;
import com.sun.tools.ws.processor.model.java.JavaMethod;
import com.sun.tools.ws.processor.model.java.JavaParameter;
import com.sun.tools.ws.processor.model.java.JavaType;
import com.sun.tools.ws.processor.modeler.JavaSimpleTypeCreator;
import com.sun.tools.ws.processor.modeler.Modeler;
import com.sun.tools.ws.processor.modeler.ModelerException;
import com.sun.tools.ws.processor.util.ClassNameCollector;
import com.sun.tools.ws.processor.util.ProcessorEnvironment;
import com.sun.tools.ws.wsdl.document.Binding;
import com.sun.tools.ws.wsdl.document.BindingFault;
import com.sun.tools.ws.wsdl.document.BindingOperation;
import com.sun.tools.ws.wsdl.document.Documentation;
import com.sun.tools.ws.wsdl.document.Kinds;
import com.sun.tools.ws.wsdl.document.Message;
import com.sun.tools.ws.wsdl.document.MessagePart;
import com.sun.tools.ws.wsdl.document.OperationStyle;
import com.sun.tools.ws.wsdl.document.PortType;
import com.sun.tools.ws.wsdl.document.WSDLConstants;
import com.sun.tools.ws.wsdl.document.WSDLDocument;
import com.sun.tools.ws.wsdl.document.jaxrpc.JAXRPCBinding;
import com.sun.tools.ws.wsdl.document.mime.MIMEContent;
import com.sun.tools.ws.wsdl.document.mime.MIMEMultipartRelated;
import com.sun.tools.ws.wsdl.document.mime.MIMEPart;
import com.sun.tools.ws.wsdl.document.schema.SchemaKinds;
import com.sun.tools.ws.wsdl.document.soap.SOAPAddress;
import com.sun.tools.ws.wsdl.document.soap.SOAPBinding;
import com.sun.tools.ws.wsdl.document.soap.SOAPBody;
import com.sun.tools.ws.wsdl.document.soap.SOAPConstants;
import com.sun.tools.ws.wsdl.document.soap.SOAPFault;
import com.sun.tools.ws.wsdl.document.soap.SOAPHeader;
import com.sun.tools.ws.wsdl.document.soap.SOAPOperation;
import com.sun.tools.ws.wsdl.document.soap.SOAPStyle;
import com.sun.tools.ws.wsdl.document.soap.SOAPUse;
import com.sun.tools.ws.wsdl.framework.Entity;
import com.sun.tools.ws.wsdl.framework.Extensible;
import com.sun.tools.ws.wsdl.framework.Extension;
import com.sun.tools.ws.wsdl.framework.GloballyKnown;
import com.sun.tools.ws.wsdl.framework.NoSuchEntityException;
import com.sun.tools.ws.wsdl.framework.ParseException;
import com.sun.tools.ws.wsdl.framework.ParserListener;
import com.sun.tools.ws.wsdl.framework.ValidationException;
import com.sun.tools.ws.wsdl.parser.Constants;
import com.sun.tools.ws.wsdl.parser.SOAPEntityReferenceValidator;
import com.sun.tools.ws.wsdl.parser.Util;
import com.sun.tools.ws.wsdl.parser.WSDLParser;
import com.sun.xml.ws.util.localization.Localizable;
import com.sun.xml.ws.util.localization.LocalizableMessageFactory;
import com.sun.xml.ws.util.xml.XmlUtil;

/**
 *
 * @author WS Development Team
 *
 * Base class for WSDL->Model classes.
 */
public abstract class WSDLModelerBase implements Modeler {
    public WSDLModelerBase(WSDLModelInfo modelInfo, Properties options) {
        //init();
        _modelInfo = modelInfo;
        _options = options;
        _messageFactory =
            new LocalizableMessageFactory("com.sun.tools.ws.resources.modeler");
        _conflictingClassNames = null;
        _env = (ProcessorEnvironment)modelInfo.getParent().getEnvironment();
        hSet = null;
        reqResNames = new HashSet();
    }

    /*
     * Creates multiple versions of the SOAPWSDLConstants class
     * to use with different versions of SOAP.
     */
//    private void init() {
//        soap11WSDLConstants =
//            SOAPConstantsFactory.getSOAPWSDLConstants(SOAPVersion.SOAP_11);
//        soap12WSDLConstants =
//            SOAPConstantsFactory.getSOAPWSDLConstants(SOAPVersion.SOAP_12);
//    }

    protected WSDLParser createWSDLParser(){
        return new WSDLParser();
    }

    /**
     * Builds model from WSDL document. Model contains abstraction which is used by the
     * generators to generate the stub/tie/serializers etc. code.
     *
     * @see com.sun.xml.rpc.processor.modeler.Modeler#buildModel()
     */
    public Model buildModel() {
        try {

            parser = createWSDLParser();
            InputSource inputSource = new InputSource(_modelInfo.getLocation());
            parser.addParserListener(new ParserListener() {
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
            hSet = parser.getUse();
            useWSIBasicProfile =
                Boolean
                    .valueOf(
                        _options.getProperty(
                            ProcessorOptions.USE_WSI_BASIC_PROFILE))
                    .booleanValue();

            unwrap =
                Boolean
                    .valueOf(
                        _options.getProperty(
                            ProcessorOptions.UNWRAP_DOC_LITERAL_WRAPPERS,
                            "true"))
                    .booleanValue();
            strictCompliance =
                Boolean
                    .valueOf(
                        _options.getProperty(
                            ProcessorOptions.STRICT_COMPLIANCE))
                    .booleanValue();
            //          doNotUnwrap = strictCompliance || (useWSIBasicProfile && !unwrap);

            // Added parameters to tell WSDLParser so that it
            // can generate warnings when f:wsi is set to true
            // This is done to validate WSDL for wsi compliant
            //WSDLDocument document =
            document =
                parser.parse(inputSource, useWSIBasicProfile);
            document.validateLocally();

/*            literalOnly =
                useWSIBasicProfile
                    || Boolean
                        .valueOf(
                            _options.getProperty(
                                ProcessorOptions
                                    .USE_DOCUMENT_LITERAL_ENCODING))
                        .booleanValue();
            literalOnly =
                literalOnly
                    || Boolean
                        .valueOf(
                            _options.getProperty(
                                ProcessorOptions.USE_RPC_LITERAL_ENCODING))
                        .booleanValue();*/
            boolean validateWSDL =
                Boolean
                    .valueOf(
                        _options.getProperty(
                            ProcessorOptions.VALIDATE_WSDL_PROPERTY))
                    .booleanValue();
            if (validateWSDL) {
                document.validate(new SOAPEntityReferenceValidator());
            }

            // detecting naming conflicts before they occur is really hard,
            // because the name mapping rules are complex and the schema portion
            // of WSDL is processed lazily;
            // so we try to create a model without caring about conflicts and then
            // we check for conflicts; if we find any, we do another pass at modeling,
            // this time with the list of conflicts to help us choose better names
            // for things;
            // if there are still conflicts, we give up

            Model model = internalBuildModel(document);
            ClassNameCollector collector = new ClassNameCollector();
            collector.process(model);
            if (collector.getConflictingClassNames().isEmpty()) {
                return model;
            } else {
                // do another pass, this time with conflict resolution enabled
                _conflictingClassNames = collector.getConflictingClassNames();
                model = internalBuildModel(document);
                ClassNameCollector collector2 = new ClassNameCollector();
                collector2.process(model);
                if (collector2.getConflictingClassNames().isEmpty()) {
                    // we're done
                    return model;
                } else {
                    // give up
                    StringBuffer conflictList = new StringBuffer();
                    boolean first = true;
                    for (Iterator iter =
                        collector2.getConflictingClassNames().iterator();
                        iter.hasNext();
                        ) {
                        if (!first) {
                            conflictList.append(", ");
                        } else {
                            first = false;
                        }
                        conflictList.append((String)iter.next());
                    }
                    throw new ModelerException(
                        "wsdlmodeler.unsolvableNamingConflicts",
                        conflictList.toString());
                }
            }

        } catch (ModelException e) {
            throw new ModelerException(e);
        } catch (ParseException e) {
            throw new ModelerException(e);
        } catch (ValidationException e) {
            throw new ModelerException(e);
        } finally {
//            _analyzer = null;
            _conflictingClassNames = null;
        }
    }

    /**
     * @param document
     *      WSDL doucment being parsed
     *
     * @return Model
     *      Model contains abstraction which is used by the
     *      generators to generate the stub/tie/serializers etc. code.
     */
    private Model internalBuildModel(WSDLDocument document) {
        QName modelName =
            new QName(
                document.getDefinitions().getTargetNamespaceURI(),
                document.getDefinitions().getName() == null
                    ? "model"
                    : document.getDefinitions().getName());
        Model model = new Model(modelName);
        // This fails with the changed classname (WSDLModeler to WSDLModeler11 etc.)
        // with this source comaptibility change the WSDL Modeler class name is changed. Right now hardcoding the
        // modeler class name to the same one being checked in WSDLGenerator.

        model.setProperty(
            ModelProperties.PROPERTY_MODELER_NAME,
            ModelProperties.WSDL_MODELER_NAME);

        theModel = model;

        _javaTypes = new JavaSimpleTypeCreator();
//        _analyzer =
//            getSchemaAnalyzerInstance(
//                document,
//                _modelInfo,
//                _options,
//                _conflictingClassNames,
//                _javaTypes);

        _javaExceptions = new HashMap();
        _faultTypeToStructureMap = new HashMap();
        _bindingNameToPortMap = new HashMap<QName, Port>();

        // grab target namespace
        model.setTargetNamespaceURI(
            document.getDefinitions().getTargetNamespaceURI());

        setDocumentationIfPresent(
            model,
            document.getDefinitions().getDocumentation());

        /**
         * -f:searchschema option processing.
         *
         * With this option we look for all the schema types under
         * <wsdl:types> ..</wsdl:types>.
         *
         */

//        boolean searchSchema =
//            Boolean
//                .valueOf(
//                    _options.getProperty(
//                        ProcessorOptions.SEARCH_SCHEMA_FOR_SUBTYPES))
//                .booleanValue();
//        if (searchSchema) {
//            processSearchSchemaOption(document, model);
//        }

        boolean hasServices = document.getDefinitions().services().hasNext();
        if (hasServices) {
            for (Iterator iter = document.getDefinitions().services();
                iter.hasNext();
                ) {
                processService(
                    (com.sun.tools.ws.wsdl.document.Service)iter.next(),
                    model,
                    document);
                hasServices = true;
            }
        } else {
            // emit a warning if there are no service definitions
            warn("wsdlmodeler.warning.noServiceDefinitionsFound");
        }

        return model;
    }

    /**
     * @param document
     * @param _modelInfo
     * @param _options
     * @param _conflictingClassNames
     * @param _javaTypes
     * @return
     */
//    protected abstract SchemaAnalyzerBase getSchemaAnalyzerInstance(
//        WSDLDocument document,
//        WSDLModelInfo _modelInfo,
//        Properties _options,
//        Set _conflictingClassNames,
//        JavaSimpleTypeCreator _javaTypes);

//    protected SchemaAnalyzerBase getSchemaAnalyzer() {
////        return _analyzer;
//        return null;
//    }

    protected WSDLModelInfo getWSDLModelInfo(){
        return _modelInfo;
    }

    /**
     * Should be called for -f:searchschema option processing.
     *
     * With this option we look for all the schema types under
     * <wsdl:types> ..</wsdl:types>.
     *
     * @param document WSDL document
     * @param model Model which will be used by the generators to generate code.
     */
//    protected void processSearchSchemaOption(
//        WSDLDocument document,
//        Model model) {
//        // embark on a very aggressive search for types defined in this document
//        Map typeMap = document.getMap(SchemaKinds.XSD_TYPE);
//        int errorcount = 0;
//        for (Iterator iter = typeMap.keySet().iterator(); iter.hasNext();) {
//            QName typeName = (QName)iter.next();
//            try {
//                // just looking up the type is enough to trigger the subtyping behavior of the analyzer!
//                AbstractType extraType = null;
//                /* Here we retrieve from the wsdl the use defined for the operations in
//                   a binding in a WSDl */
//                //hSet = parser.getUse();
//
//                if (hSet.contains("encoded") && !hSet.contains("literal")) {
////                    extraType = _analyzer.schemaTypeToSOAPType(typeName);
//                } else if (
//                    !hSet.contains("encoded") && hSet.contains("literal")) {
////                    extraType = _analyzer.schemaTypeToLiteralType(typeName);
//                } else {
//                    throw new ModelerException("wsdlmodler.warning.operation.use");
//                    //throw new ModelerException("wsdlmodeler.invalid.bindingOperation.notFound");
//                }
//
//                if (extraType != null) {
//                    //if (soapType instanceof SOAPStructureType) {
//                    // the original idea was to add only those types that can participate in inheritance,
//                    // but that seems wrong
//                    model.addExtraType(extraType);
//                    //}
//                } else {
//                    ++errorcount;
//                }
//            } catch (ModelException e) {
//                ++errorcount;
//            }
//        }
//        if (errorcount > 0) {
//            warn(
//                "wsdlmodeler.warning.searchSchema.unrecognizedTypes",
//                Integer.toString(errorcount));
//        }
//
//    }

    protected Documentation getDocumentationFor(Element e) {
        String s = XmlUtil.getTextForNode(e);
        if (s == null) {
            return null;
        } else {
            return new Documentation(s);
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

    /**
     * @param serviceQName
     * @param wsdlService
     * @return
     */
    protected String getServiceInterfaceName(
        QName serviceQName,
        com.sun.tools.ws.wsdl.document.Service wsdlService) {
        String serviceInterface = "";
        String javaPackageName = null;
        if (_modelInfo.getJavaPackageName() != null
            && !_modelInfo.getJavaPackageName().equals("")) {
            javaPackageName = _modelInfo.getJavaPackageName();
        }
        if (javaPackageName != null) {
            serviceInterface = javaPackageName + ".";
        }
        serviceInterface
            += _env.getNames().validJavaClassName(wsdlService.getName());
        return serviceInterface;
    }

    /**
     * @param wsdlService
     * @param model
     * @param document
     */
    protected void processService(
        com.sun.tools.ws.wsdl.document.Service wsdlService,
        Model model,
        WSDLDocument document) {

        String serviceInterface = "";
        QName serviceQName = getQNameOf(wsdlService);
        serviceInterface = getServiceInterfaceName(serviceQName, wsdlService);
        if (isConflictingServiceClassName(serviceInterface)) {
            serviceInterface += "_Service";
        }
        Service service =
            new Service(
                serviceQName,
                new JavaInterface(serviceInterface, serviceInterface + "Impl"));

        setDocumentationIfPresent(service, wsdlService.getDocumentation());

        boolean hasPorts = false;
        for (Iterator iter = wsdlService.ports(); iter.hasNext();) {
            boolean processed =
                processPort(
                    (com.sun.tools.ws.wsdl.document.Port)iter.next(),
                    service,
                    document);
            hasPorts = hasPorts || processed;
        }
        model.addService(service);

        if (!hasPorts) {
            // emit a warning if there are no ports
            warn("wsdlmodeler.warning.noPortsInService", wsdlService.getName());
        }
    }

    protected String getJavaNameOfPort(QName portQName) {
        return null;
    }

    /**
     * @param wsdlPort
     * @param service
     * @param document
     * @return boolean
     */
    protected boolean processPort(
        com.sun.tools.ws.wsdl.document.Port wsdlPort,
        Service service,
        WSDLDocument document) {
        try {
            QName portQName = getQNameOf(wsdlPort);
            Port port = new Port(portQName);

            // bug fix: 4923650
            setCurrentPort(port);
            String metaPortName = getJavaNameOfPort(portQName);
            if (metaPortName != null)
                port.setProperty(
                    ModelProperties.PROPERTY_JAVA_PORT_NAME,
                    metaPortName);

            setDocumentationIfPresent(port, wsdlPort.getDocumentation());

            SOAPAddress soapAddress =
                (SOAPAddress)getExtensionOfType(wsdlPort, SOAPAddress.class);
            if (soapAddress == null) {
                // not a SOAP port, ignore it
                warn(
                    "wsdlmodeler.warning.ignoringNonSOAPPort.noAddress",
                    wsdlPort.getName());
                return false;
            }

            port.setAddress(soapAddress.getLocation());
            Binding binding = wsdlPort.resolveBinding(document);
            QName bindingName = getQNameOf(binding);
            PortType portType = binding.resolvePortType(document);

            port.setProperty(
                ModelProperties.PROPERTY_WSDL_PORT_NAME,
                getQNameOf(wsdlPort));
            port.setProperty(
                ModelProperties.PROPERTY_WSDL_PORT_TYPE_NAME,
                getQNameOf(portType));
            port.setProperty(
                ModelProperties.PROPERTY_WSDL_BINDING_NAME,
                bindingName);

            boolean isProvider = isProvider(wsdlPort);
            if (_bindingNameToPortMap.containsKey(bindingName) && !isProvider) {
                // this binding has been processed before
                Port existingPort =
                    (Port)_bindingNameToPortMap.get(bindingName);
                port.setOperationsList(existingPort.getOperationsList());
                port.setJavaInterface(existingPort.getJavaInterface());
            } else {
                // find out the SOAP binding extension, if any
                SOAPBinding soapBinding =
                    (SOAPBinding)getExtensionOfType(binding, SOAPBinding.class);

                if (soapBinding == null) {
                    // cannot deal with non-SOAP ports
                    warn(
                        "wsdlmodeler.warning.ignoringNonSOAPPort",
                        wsdlPort.getName());
                    return false;
                }

                if (soapBinding.getTransport() == null
                    || !soapBinding.getTransport().equals(
                        SOAPConstants.URI_SOAP_TRANSPORT_HTTP)) {
                    // cannot deal with non-HTTP ports
                    warn(
                        "wsdlmodeler.warning.ignoringSOAPBinding.nonHTTPTransport",
                        wsdlPort.getName());
                    return false;
                }

                port.setStyle(soapBinding.getStyle());
                boolean hasOverloadedOperations = false;
                Set operationNames = new HashSet();
                for (Iterator iter = portType.operations(); iter.hasNext();) {
                    com.sun.tools.ws.wsdl.document.Operation operation =
                        (com.sun.tools.ws.wsdl.document.Operation)iter.next();

                    if (operationNames.contains(operation.getName())) {
                        hasOverloadedOperations = true;
                        break;
                    }
                    operationNames.add(operation.getName());

                    for (Iterator itr = binding.operations();
                        iter.hasNext();
                        ) {
                        BindingOperation bindingOperation =
                            (BindingOperation)itr.next();
                        if (operation
                            .getName()
                            .equals(bindingOperation.getName())) {
                            /* once the operation is found bounded we check the next
                               operation */
                            break;
                        } else if (!itr.hasNext()) {
                            throw new ModelerException(
                                "wsdlmodeler.invalid.bindingOperation.notFound",
                                new Object[] {
                                    operation.getName(),
                                    binding.getName()});
                        }
                    }
                }

                Map headers = new HashMap();
                boolean hasOperations = false;
                for (Iterator iter = binding.operations(); iter.hasNext();) {
                    BindingOperation bindingOperation =
                        (BindingOperation)iter.next();

                    com.sun.tools.ws.wsdl.document.Operation portTypeOperation =
                        null;
                    Set operations =
                        portType.getOperationsNamed(bindingOperation.getName());
                    if (operations.size() == 0) {
                        // the WSDL document is invalid
                        throw new ModelerException(
                            "wsdlmodeler.invalid.bindingOperation.notInPortType",
                            new Object[] {
                                bindingOperation.getName(),
                                binding.getName()});
                    } else if (operations.size() == 1) {
                        portTypeOperation =
                            (com.sun.tools.ws.wsdl.document.Operation)operations
                                .iterator()
                                .next();
                    } else {
                        boolean found = false;
                        String expectedInputName =
                            bindingOperation.getInput().getName();
                        String expectedOutputName =
                            bindingOperation.getOutput().getName();

                        // if the style attribute on soap:binding and soap:operation
                        // are conflicting, then ignore the operation
                        // bugfix: 4939641
                        if (useWSIBasicProfile) {
                            //find out the SOAP operation extension, if any
                            SOAPOperation soapOperation =
                                (SOAPOperation) getExtensionOfType(bindingOperation,
                                    SOAPOperation.class);
                            if ((soapOperation.getStyle() != null) &&
                                (soapBinding.getStyle() != null) &&
                                (soapOperation.getStyle() != soapBinding.getStyle())) {
                                warn("wsdlmodeler.warning.ignoringOperation.conflictStyleInWSIMode", bindingOperation.getName());
                                continue;
                            }
                        }

                        for (Iterator iter2 = operations.iterator();
                            iter2.hasNext();
                            ) {
                            com.sun.tools.ws.wsdl.document
                                .Operation candidateOperation =
                                (com.sun.tools.ws.wsdl.document.Operation)iter2
                                    .next();

                            if (expectedInputName == null) {
                                // the WSDL document is invalid
                                throw new ModelerException(
                                    "wsdlmodeler.invalid.bindingOperation.missingInputName",
                                    new Object[] {
                                        bindingOperation.getName(),
                                        binding.getName()});
                            }
                            if (expectedOutputName == null) {
                                // the WSDL document is invalid
                                throw new ModelerException(
                                    "wsdlmodeler.invalid.bindingOperation.missingOutputName",
                                    new Object[] {
                                        bindingOperation.getName(),
                                        binding.getName()});
                            }
                            if (expectedInputName
                                .equals(candidateOperation.getInput().getName())
                                && expectedOutputName.equals(
                                    candidateOperation
                                        .getOutput()
                                        .getName())) {
                                if (found) {
                                    // the WSDL document is invalid
                                    throw new ModelerException(
                                        "wsdlmodeler.invalid.bindingOperation.multipleMatchingOperations",
                                        new Object[] {
                                            bindingOperation.getName(),
                                            binding.getName()});
                                } else {
                                    // got it!
                                    found = true;
                                    portTypeOperation = candidateOperation;
                                }
                            }
                        }

                        if (!found) {
                            // the WSDL document is invalid
                            throw new ModelerException(
                                "wsdlmodeler.invalid.bindingOperation.notFound",
                                new Object[] {
                                    bindingOperation.getName(),
                                    binding.getName()});
                        }
                    }
                    if(!isProvider){
                        this.info =
                            new ProcessSOAPOperationInfo(
                                port,
                                wsdlPort,
                                portTypeOperation,
                                bindingOperation,
                                soapBinding,
                                document,
                                hasOverloadedOperations,
                                headers);

                        Operation operation = processSOAPOperation();
                        postProcessSOAPOperation(operation);
                        if (operation != null) {
                            port.addOperation(operation);
                            hasOperations = true;
                        }
                    }
                }

                if (!isProvider && !hasOperations) {
                    // emit a warning if there are no operations
                    warn(
                        "wsdlmodeler.warning.noOperationsInPort",
                        wsdlPort.getName());
                }

                createJavaInterfaceForPort(port, isProvider);
                _bindingNameToPortMap.put(bindingName, port);
            }

            // now deal with the configured handlers
            port.setClientHandlerChainInfo(
                _modelInfo.getClientHandlerChainInfo());
            port.setServerHandlerChainInfo(
                _modelInfo.getServerHandlerChainInfo());

            setProperties(port, isProvider);
            applyPortMethodCustomization(port, wsdlPort);
            service.addPort(port);

            // bug fix: 4923650
            setCurrentPort(null);

            return true;

        } catch (NoSuchEntityException e) {
            warn(e);
            // should not happen
            return false;
        }
    }

    /**
     * @param portByName
     * @param wsdlPort
     */
    protected void applyPortMethodCustomization(Port port, com.sun.tools.ws.wsdl.document.Port wsdlPort) {
        if(isProvider(wsdlPort))
            return;
        JAXRPCBinding jaxrpcBinding = (JAXRPCBinding)getExtensionOfType(wsdlPort, JAXRPCBinding.class);

        String portMethodName = (jaxrpcBinding != null)?((jaxrpcBinding.getMethodName() != null)?jaxrpcBinding.getMethodName().getName():null):null;
        if(portMethodName != null){
            port.setPortGetter(portMethodName);
        }else{
            portMethodName = Names.getPortName(port);
            portMethodName = getEnvironment().getNames().validJavaClassName(portMethodName);
            port.setPortGetter("get"+portMethodName);
        }

    }

    /**
     * @param binding
     * @return
     */
    protected boolean validateWSDLBindingStyle(Binding binding) {
        // TODO Auto-generated method stub
        return true;
    }


    /**
     * @param port
     */
    protected void setProperties(Port port, boolean isProvider) {
//      generate stub and tie class names
        String stubClassName = _env.getNames().stubFor(port, null);
        if (isConflictingStubClassName(stubClassName)) {
            stubClassName =
                _env.getNames().stubFor(
                    port,
                    getNonQualifiedNameFor(port.getName()));
        }

        /* fix for bug 4778136, add infix to tie class, it will be:
           Port_Port1_Port2..._infix_Tie.
        */

        String tieClassName =
            _env.getNames().tieFor(
                port,
                _env.getNames().getSerializerNameInfix());
        tieClassName = getInfixedName(port, tieClassName);

        port.setProperty(
            ModelProperties.PROPERTY_STUB_CLASS_NAME,
            stubClassName);
        port.setProperty(
            ModelProperties.PROPERTY_TIE_CLASS_NAME,
            tieClassName);
    }

    protected boolean isProvider(com.sun.tools.ws.wsdl.document.Port wsdlPort){
        JAXRPCBinding portCustomization = (JAXRPCBinding)getExtensionOfType(wsdlPort, JAXRPCBinding.class);
        Boolean isProvider = (portCustomization != null)?portCustomization.isProvider():null;
        if(isProvider != null){
            return isProvider;
        }

        JAXRPCBinding jaxrpcGlobalCustomization = (JAXRPCBinding)getExtensionOfType(document.getDefinitions(), JAXRPCBinding.class);
        isProvider = (jaxrpcGlobalCustomization != null)?jaxrpcGlobalCustomization.isProvider():null;
        if(isProvider != null)
            return isProvider;
        return false;
    }


    //  bug fix: 4923650
    protected void setCurrentPort(Port port) {
    }

    /**
     * @return String Infixed tieClassName
     */
    private String getInfixedName(Port port, String tieClassName) {
        if (isConflictingTieClassName(tieClassName)) {
            String str = null;
            if (_env.getNames().getSerializerNameInfix() != null)
                str =
                    getNonQualifiedNameFor(port.getName())
                        + "_"
                        + _env.getNames().getSerializerNameInfix();
            else
                str = getNonQualifiedNameFor(port.getName());
            tieClassName = _env.getNames().tieFor(port, str);
        }
        return tieClassName;
    }

    protected Operation processSOAPOperation() {
        Operation operation =
            new Operation(new QName(null, info.bindingOperation.getName()));

        setDocumentationIfPresent(
            operation,
            info.portTypeOperation.getDocumentation());

        if (info.portTypeOperation.getStyle()
            != OperationStyle.REQUEST_RESPONSE
            && info.portTypeOperation.getStyle() != OperationStyle.ONE_WAY) {
            warn(
                "wsdlmodeler.warning.ignoringOperation.notSupportedStyle",
                info.portTypeOperation.getName());
            return null;
        }

        SOAPStyle soapStyle = info.soapBinding.getStyle();

        // find out the SOAP operation extension, if any
        SOAPOperation soapOperation =
            (SOAPOperation)getExtensionOfType(info.bindingOperation,
                SOAPOperation.class);

        if (soapOperation != null) {
            if (soapOperation.getStyle() != null) {
                soapStyle = soapOperation.getStyle();
            }
            if (soapOperation.getSOAPAction() != null) {
                operation.setSOAPAction(soapOperation.getSOAPAction());
            }
        }

        operation.setStyle(soapStyle);

        String uniqueOperationName =
            getUniqueName(info.portTypeOperation, info.hasOverloadedOperations);
        if (info.hasOverloadedOperations) {
            operation.setUniqueName(uniqueOperationName);
        }

        info.operation = operation;
        info.uniqueOperationName = uniqueOperationName;

        //attachment
        SOAPBody soapRequestBody = getSOAPRequestBody();
        if (soapRequestBody == null) {
            // the WSDL document is invalid
            throw new ModelerException(
                "wsdlmodeler.invalid.bindingOperation.inputMissingSoapBody",
                new Object[] { info.bindingOperation.getName()});
        }

        if (soapStyle == SOAPStyle.RPC) {
            if (soapRequestBody.isEncoded()) { // rpc/encoded style
//                return processSOAPOperationRPCEncodedStyle();
            } else { // rpc/literal
//                return processSOAPOperationRPCLiteralStyle();
            }
        } else {
            // document style
//            return processSOAPOperationDocumentLiteralStyle();
        }
        return null;
    }

    /*
     * This is a hook so that parameters ordering can be reset by the 109 mapping
     * information.  This is backwards.  Ideally, we should just do this right the
     * first time we process.  But since we are retro-fitting, this is the least
     * intrusive way I could think of to 1) be spec compliant 2) not introduing
     * regression.
     */
    protected void postProcessSOAPOperation(Operation operation) {
    }

    //  bug fix: 4923650
    protected void setJavaOperationNameProperty(
        com.sun.tools.ws.wsdl.document.Message inputMessage) {
    }

    protected boolean useExplicitServiceContextForRpcEncoded(
        com.sun.tools.ws.wsdl.document.Message inputMessage) {
        return useExplicitServiceContext();
    }

    protected boolean useExplicitServiceContextForRpcLit(
        com.sun.tools.ws.wsdl.document.Message inputMessage) {
        return useExplicitServiceContext();
    }

    protected boolean useExplicitServiceContextForDocLit(
        com.sun.tools.ws.wsdl.document.Message inputMessage) {
        return useExplicitServiceContext();
    }

    protected boolean useExplicitServiceContext() {
        return Boolean
            .valueOf(
                _options.getProperty(
                    ProcessorOptions.EXPLICIT_SERVICE_CONTEXT_PROPERTY))
            .booleanValue();
    }

//    protected Operation processSOAPOperationRPCEncodedStyle() {
//
//        // Bugfix: 5038631
//        if (useWSIBasicProfile)
//            warn(
//                "wsdlmodeler.warning.nonconforming.wsdl.use",
//                info.portTypeOperation.getName());
//
//        boolean isRequestResponse =
//            info.portTypeOperation.getStyle()
//                == OperationStyle.REQUEST_RESPONSE;
//        Request request = new Request();
//        Response response = new Response();
//
//        info.operation.setUse(SOAPUse.ENCODED);
//
//        SOAPBody soapRequestBody = getSOAPRequestBody();
//
//        SOAPBody soapResponseBody = null;
//        com.sun.xml.rpc.wsdl.document.Message outputMessage = null;
//        if (isRequestResponse) {
//            soapResponseBody = getSOAPResponseBody();
//            outputMessage = getOutputMessage();
//
//            // code added for 109
//            if (outputMessage != null)
//                response.setProperty(
//                    ModelProperties.PROPERTY_WSDL_MESSAGE_NAME,
//                    getQNameOf(outputMessage));
//        }
//
//        // TODO this SOAPEncodingNamespace check should be cleaned up
//        if (soapRequestBody.isLiteral()
//            || (!tokenListContains(soapRequestBody.getEncodingStyle(),
//                soap11WSDLConstants.getSOAPEncodingNamespace())
//                && !tokenListContains(soapRequestBody.getEncodingStyle(),
//                    soap12WSDLConstants.getSOAPEncodingNamespace()))
//            || (soapResponseBody != null
//                && (soapResponseBody.isLiteral()
//                    || (!tokenListContains(soapResponseBody.getEncodingStyle(),
//                        soap11WSDLConstants.getSOAPEncodingNamespace())
//                        && !tokenListContains(soapResponseBody.getEncodingStyle(),
//                            soap12WSDLConstants
//                                .getSOAPEncodingNamespace()))))) {
//            // with rpc style, we only support encoded use
//            // also, we only know about the SOAP encoding for now
//            // TODO - fix this to add support for other encodings
//            warn(
//                "wsdlmodeler.warning.ignoringOperation.notEncoded",
//                info.portTypeOperation.getName());
//            return null;
//        }
//
//        com.sun.xml.rpc.wsdl.document.Message inputMessage = getInputMessage();
//        // Code added for 109
//        if (inputMessage != null)
//            request.setProperty(
//                ModelProperties.PROPERTY_WSDL_MESSAGE_NAME,
//                getQNameOf(inputMessage));
//
//        // Process parameterOrder and get the parameterList
//        Set inputParameterNames = new HashSet();
//        Set outputParameterNames = new HashSet();
//        String resultParameterName = null;
//        StringBuffer result = new StringBuffer();
//
//        setJavaOperationNameProperty(inputMessage);
//
//        java.util.List parameterList =
//            processParameterOrder(
//                inputParameterNames,
//                outputParameterNames,
//                result);
//        if (result.length() > 0)
//            resultParameterName = result.toString();
//        // get request/response namespace uris. These uris are used to
//        // get the respective body attributes.
//        String requestNamespaceURI = getRequestNamespaceURI(soapRequestBody);
//        String responseNamespaceURI = null;
//        if (isRequestResponse) {
//            responseNamespaceURI = getResponseNamespaceURI(soapResponseBody);
//        }
//
//        // Get the structure prefix. This prefix is used to name the
//        // request/response class.
//        String structureNamePrefix = getStructureNamePrefix();
//
//        QName requestBodyName =
//            new QName(requestNamespaceURI, info.portTypeOperation.getName());
//        SOAPStructureType requestBodyType =
//            new RPCRequestUnorderedStructureType(requestBodyName);
//        JavaStructureType requestBodyJavaType =
//            new JavaStructureType(
//                getUniqueClassName(
//                    makePackageQualified(
//                        StringUtils.capitalize(
//                            structureNamePrefix
//                                + _env.getNames().validExternalJavaIdentifier(
//                                    info.uniqueOperationName))
//                            + "_RequestStruct",
//                        requestBodyName)),
//                false,
//                requestBodyType);
//        requestBodyType.setJavaType(requestBodyJavaType);
//
//        Block requestBodyBlock = new Block(requestBodyName, requestBodyType);
//        request.addBodyBlock(requestBodyBlock);
//
//        SOAPStructureType responseBodyType = null;
//        JavaStructureType responseBodyJavaType = null;
//        Block responseBodyBlock = null;
//        if (isRequestResponse) {
//            QName responseBodyName =
//                new QName(
//                    responseNamespaceURI,
//                    info.portTypeOperation.getName() + "Response");
//            responseBodyType = new RPCResponseStructureType(responseBodyName);
//            responseBodyJavaType =
//                new JavaStructureType(
//                    getUniqueClassName(
//                        makePackageQualified(
//                            StringUtils.capitalize(
//                                structureNamePrefix
//                                    + _env
//                                        .getNames()
//                                        .validExternalJavaIdentifier(
//                                        info.uniqueOperationName))
//                                + "_ResponseStruct",
//                            responseBodyName)),
//                    false,
//                    responseBodyType);
//            responseBodyType.setJavaType(responseBodyJavaType);
//
//            responseBodyBlock = new Block(responseBodyName, responseBodyType);
//            response.addBodyBlock(responseBodyBlock);
//        }
//
//        if (resultParameterName == null) {
//            // this is ugly, but we need to save information about the return type
//            // being void at this stage, so that when we later create a Java interface
//            // for the port this operation belongs to, we'll do the right thing
//            info.operation.setProperty(OPERATION_HAS_VOID_RETURN_TYPE, "true");
//        } else {
//            // handle result parameter a bit specially
//            MessagePart part = outputMessage.getPart(resultParameterName);
//            SOAPType soapType = getSchemaTypeToSOAPType(part.getDescriptor());
//
//            // bug fix: 4923650
//            soapType = (SOAPType)verifyResultType(soapType, info.operation);
//
//            SOAPStructureMember member =
//                new SOAPStructureMember(
//                    new QName(null, part.getName()),
//                    soapType);
//            JavaStructureMember javaMember =
//                new JavaStructureMember(
//                    _env.getNames().validJavaMemberName(part.getName()),
//                    soapType.getJavaType(),
//                    member,
//                    false);
//            javaMember.setReadMethod(
//                _env.getNames().getJavaMemberReadMethod(javaMember));
//            javaMember.setWriteMethod(
//                _env.getNames().getJavaMemberWriteMethod(javaMember));
//            member.setJavaStructureMember(javaMember);
//            responseBodyType.add(member);
//            responseBodyJavaType.add(javaMember);
//            Parameter parameter =
//                new Parameter(
//                    _env.getNames().validJavaMemberName(part.getName()));
//            // bug fix: 4931493
//            parameter.setProperty(
//                ModelProperties.PROPERTY_PARAM_MESSAGE_PART_NAME,
//                part.getName());
//            parameter.setEmbedded(true);
//            parameter.setType(soapType);
//            parameter.setBlock(responseBodyBlock);
//            response.addParameter(parameter);
//            info.operation.setProperty(
//                WSDL_RESULT_PARAMETER,
//                parameter.getName());
//        }
//
//        // create a definitive list of parameters to match what we'd like to get
//        // in the java interface (which is generated much later)
//        List definitiveParameterList = new ArrayList();
//
//        for (Iterator iter = parameterList.iterator(); iter.hasNext();) {
//            String name = (String)iter.next();
//            boolean isInput = inputParameterNames.contains(name);
//            boolean isOutput = outputParameterNames.contains(name);
//            SOAPType soapType = null;
//            Parameter inParameter = null;
//
//            if (isInput && isOutput) {
//                // make sure types match
//                if (!inputMessage
//                    .getPart(name)
//                    .getDescriptor()
//                    .equals(outputMessage.getPart(name).getDescriptor())) {
//                    throw new ModelerException(
//                        "wsdlmodeler.invalid.parameter.differentTypes",
//                        new Object[] {
//                            name,
//                            info.operation.getName().getLocalPart()});
//                }
//            }
//
//            if (isInput) {
//                MessagePart part = inputMessage.getPart(name);
//                soapType = getSchemaTypeToSOAPType(part.getDescriptor());
//
//                // bug fix: 4923650
//                soapType =
//                    (SOAPType)verifyParameterType(soapType,
//                        part.getName(),
//                        info.operation);
//
//                SOAPStructureMember member =
//                    new SOAPStructureMember(
//                        new QName(null, part.getName()),
//                        soapType);
//                JavaStructureMember javaMember =
//                    new JavaStructureMember(
//                        _env.getNames().validJavaMemberName(part.getName()),
//                        soapType.getJavaType(),
//                        member,
//                        false);
//                javaMember.setReadMethod(
//                    _env.getNames().getJavaMemberReadMethod(javaMember));
//                javaMember.setWriteMethod(
//                    _env.getNames().getJavaMemberWriteMethod(javaMember));
//                member.setJavaStructureMember(javaMember);
//                requestBodyType.add(member);
//                requestBodyJavaType.add(javaMember);
//                inParameter =
//                    new Parameter(
//                        _env.getNames().validJavaMemberName(part.getName()));
//                // bug fix: 4931493
//                inParameter.setProperty(
//                    ModelProperties.PROPERTY_PARAM_MESSAGE_PART_NAME,
//                    part.getName());
//                inParameter.setEmbedded(true);
//                inParameter.setType(soapType);
//                inParameter.setBlock(requestBodyBlock);
//                request.addParameter(inParameter);
//                definitiveParameterList.add(inParameter.getName());
//            }
//            if (isOutput) {
//                MessagePart part = outputMessage.getPart(name);
//                if (soapType == null) {
//                    soapType = getSchemaTypeToSOAPType(part.getDescriptor());
//                    // bug fix: 4923650
//                    soapType =
//                        (SOAPType)verifyParameterType(soapType,
//                            part.getName(),
//                            info.operation);
//                }
//
//                SOAPStructureMember member =
//                    new SOAPStructureMember(
//                        new QName(null, part.getName()),
//                        soapType);
//                responseBodyType.add(member);
//                JavaStructureMember javaMember =
//                    new JavaStructureMember(
//                        _env.getNames().validJavaMemberName(part.getName()),
//                        soapType.getJavaType(),
//                        member,
//                        false);
//                responseBodyJavaType.add(javaMember);
//                javaMember.setReadMethod(
//                    _env.getNames().getJavaMemberReadMethod(javaMember));
//                javaMember.setWriteMethod(
//                    _env.getNames().getJavaMemberWriteMethod(javaMember));
//                member.setJavaStructureMember(javaMember);
//                Parameter outParameter =
//                    new Parameter(
//                        _env.getNames().validJavaMemberName(part.getName()));
//                // bug fix: 4931493
//                outParameter.setProperty(
//                    ModelProperties.PROPERTY_PARAM_MESSAGE_PART_NAME,
//                    part.getName());
//                outParameter.setEmbedded(true);
//                outParameter.setType(soapType);
//                outParameter.setBlock(responseBodyBlock);
//                if (inParameter == null) {
//                    definitiveParameterList.add(outParameter.getName());
//                } else {
//                    inParameter.setLinkedParameter(outParameter);
//                    outParameter.setLinkedParameter(inParameter);
//                }
//                response.addParameter(outParameter);
//            }
//
//        }
//
//        info.operation.setRequest(request);
//
//        // DOUG one-way operation
//        if (isRequestResponse) {
//            info.operation.setResponse(response);
//        }
//
//        // faults with duplicate names
//        Set duplicateNames = getDuplicateFaultNames();
//
//        // handle soap:fault
//        handleEncodedSOAPFault(response, duplicateNames);
//
//        // handle headers
//        boolean explicitServiceContext =
//            useExplicitServiceContextForRpcEncoded(inputMessage);
//        if (explicitServiceContext) {
//            handleEncodedSOAPHeaders(
//                request,
//                response,
//                info.bindingOperation.getInput().extensions(),
//                duplicateNames,
//                definitiveParameterList,
//                true);
//            if (isRequestResponse) {
//                handleEncodedSOAPHeaders(
//                    request,
//                    response,
//                    info.bindingOperation.getOutput().extensions(),
//                    duplicateNames,
//                    definitiveParameterList,
//                    false);
//            }
//        }
//        info.operation.setProperty(
//            WSDL_PARAMETER_ORDER,
//            definitiveParameterList);
//        return info.operation;
//    }

    /*-------------------------------------------------------------------*/
    //     The following methods were added as part of bug fix: 4923650
    protected AbstractType verifyResultType(
        AbstractType type,
        Operation operation) {
        return type;
    }

    protected AbstractType verifyParameterType(
        AbstractType type,
        String partName,
        Operation operation) {
        return type;
    }
    // end of bug fix: 4923650
    /*-------------------------------------------------------------------*/

//    /**
//     * @param request
//     * @param response
//     * @param iterator
//     * @param duplicateNames
//     * @param definitiveParameterList
//     */
//    private void handleEncodedSOAPHeaders(
//        Request request,
//        Response response,
//        Iterator iter,
//        Set duplicateNames,
//        List definitiveParameterList,
//        boolean processRequest) {
//        while (iter.hasNext()) {
//            Extension extension = (Extension)iter.next();
//            if (extension instanceof SOAPHeader) {
//                SOAPHeader header = (SOAPHeader)extension;
//
//                //TODO: remove this check, it will never get called as this method is being called when explicitservicecontext is enabled
//                if (header.isLiteral()
//                    || !tokenListContains(header.getEncodingStyle(),
//                        soap11WSDLConstants.getSOAPEncodingNamespace())) {
//                    // TODO - fix this to add support literal use and other encodings
//                    warn(
//                        "wsdlmodeler.warning.ignoringHeader.notEncoded",
//                        new Object[] {
//                            header.getPart(),
//                            info.bindingOperation.getName()});
//                    continue;
//                }
//
//                // bug fix: 4857100
//                com.sun.xml.rpc.wsdl.document.Message headerMessage =
//                    findMessage(header.getMessage(), info);
//
//                if (headerMessage == null) {
//                    warn(
//                        "wsdlmodeler.warning.ignoringHeader.cant.resolve.message",
//                        new Object[] {
//                            header.getMessage(),
//                            info.bindingOperation.getName()});
//                    continue;
//                }
//
//                MessagePart part = headerMessage.getPart(header.getPart());
//                //bug fix:4857259
//                if (part == null) {
//                    warn(
//                        "wsdlmodeler.warning.ignoringHeader.notFound",
//                        new Object[] {
//                            header.getPart(),
//                            info.bindingOperation.getName()});
//                    continue;
//                }
//                //check if the part is already reffered by soap:body
//                //bug fix: 4912182
//                if (processRequest) {
//                    if (isHeaderPartPresentInBody(getSOAPRequestBody(),
//                        getInputMessage(),
//                        header.getPart(), true)) {
//                        warn(
//                            "wsdlmodeler.warning.ignoringHeader.partFromBody",
//                            new Object[] { header.getPart()});
//                        warn(
//                            "wsdlmodeler.warning.ignoringHeader",
//                            new Object[] {
//                                header.getPart(),
//                                info.bindingOperation.getName(),
//                                });
//                        continue;
//                    }
//                } else {
//                    if (isHeaderPartPresentInBody(getSOAPResponseBody(),
//                        getOutputMessage(),
//                        header.getPart(), false)) {
//                        warn(
//                            "wsdlmodeler.warning.ignoringHeader.partFromBody",
//                            new Object[] { header.getPart()});
//                        warn(
//                            "wsdlmodeler.warning.ignoringHeader",
//                            new Object[] {
//                                header.getPart(),
//                                info.bindingOperation.getName(),
//                                });
//
//                        continue;
//                    }
//                }
//
//                SOAPType headerType =
//                    getSchemaTypeToSOAPType(part.getDescriptor());
//
//                String namespaceURI = header.getNamespace();
//                if (namespaceURI == null) {
//                    // the WSDL document is invalid
//                    // at least, that's my interpretation of section 3.5 of the WSDL 1.1 spec!
//                    // throw new ModelerException("wsdlmodeler.invalid.bindingOperation.inputHeader.missingNamespace",
//                    //    new Object[] { info.bindingOperation.getName(), header.getPart() });
//                    //
//                    // after seeing what .NET does, I think that the WSDL rule in section 3.5
//                    // could be reinterpreted as saying that the following is legal for headers;
//                    // for the sake of interoperability, we'll match what .NET expects
//                    namespaceURI = headerType.getName().getNamespaceURI();
//                }
//
//                QName headerName = new QName(namespaceURI, header.getPart());
//                Block headerBlock = new Block(headerName, headerType);
//
//                //J2EE for 109
//                headerBlock.setProperty(
//                    ModelProperties.PROPERTY_WSDL_MESSAGE_NAME,
//                    getQNameOf(headerMessage));
//
//                AbstractType alreadySeenHeaderType =
//                    (AbstractType)info.headers.get(headerName);
//                if (alreadySeenHeaderType != null
//                    && alreadySeenHeaderType != headerType) {
//                    warn(
//                        "wsdlmodeler.warning.ignoringHeader.inconsistentDefinition",
//                        new Object[] {
//                            header.getPart(),
//                            info.bindingOperation.getName()});
//                } else {
//                    info.headers.put(headerName, headerType);
//                    if (processRequest)
//                        request.addHeaderBlock(headerBlock);
//                    else // processResponse
//                        response.addHeaderBlock(headerBlock);
//                    Parameter parameter =
//                        new Parameter(
//                            _env.getNames().validJavaMemberName(
//                                part.getName()));
//                    // bug fix: 4931493
//                    parameter.setProperty(
//                        ModelProperties.PROPERTY_PARAM_MESSAGE_PART_NAME,
//                        part.getName());
//                    parameter.setEmbedded(false);
//                    parameter.setType(headerType);
//                    parameter.setBlock(headerBlock);
//                    if (processRequest && definitiveParameterList != null) {
//                        request.addParameter(parameter);
//                        //bug fix: 4919808
//                        definitiveParameterList.add(parameter.getName());
//                    } else { //processResponse
//                        //bug fix: 4919808
//                        if (definitiveParameterList != null) {
//                            for (Iterator iterInParams =
//                                definitiveParameterList.iterator();
//                                iterInParams.hasNext();
//                                ) {
//                                String inParamName =
//                                    (String)iterInParams.next();
//                                if (inParamName.equals(parameter.getName())) {
//                                    Parameter inParam =
//                                        request.getParameterByName(inParamName);
//                                    parameter.setLinkedParameter(inParam);
//                                    inParam.setLinkedParameter(parameter);
//                                }
//                            }
//                            if (!definitiveParameterList
//                                .contains(parameter.getName())) {
//                                definitiveParameterList.add(
//                                    parameter.getName());
//                            }
//                        }
//                        response.addParameter(parameter);
//
//                    }
//                }
//                //generate headerfault listing in list of faults
//                processHeaderFaults(header, info, response, duplicateNames);
//            }
//        }
//    }

    /**
     * @param response
     * @param duplicateNames
     * @param faultNames
     */
//    private void handleEncodedSOAPFault(
//        Response response,
//        Set duplicateNames) {
//        for (Iterator iter = info.bindingOperation.faults(); iter.hasNext();) {
//            BindingFault bindingFault = (BindingFault)iter.next();
//
//            com.sun.xml.rpc.wsdl.document.Fault portTypeFault = null;
//            for (Iterator iter2 = info.portTypeOperation.faults();
//                iter2.hasNext();
//                ) {
//                com.sun.xml.rpc.wsdl.document.Fault aFault =
//                    (com.sun.xml.rpc.wsdl.document.Fault)iter2.next();
//
//                if (aFault.getName().equals(bindingFault.getName())) {
//                    if (portTypeFault != null) {
//                        // the WSDL document is invalid
//                        throw new ModelerException(
//                            "wsdlmodeler.invalid.bindingFault.notUnique",
//                            new Object[] {
//                                bindingFault.getName(),
//                                info.bindingOperation.getName()});
//                    } else {
//                        portTypeFault = aFault;
//                    }
//                }
//            }
//
//            if (portTypeFault == null) {
//                // the WSDL document is invalid
//                throw new ModelerException(
//                    "wsdlmodeler.invalid.bindingFault.notFound",
//                    new Object[] {
//                        bindingFault.getName(),
//                        info.bindingOperation.getName()});
//
//            }
//
//            // changed this so that the message name is used to create the java exception name later on
//            // Fault fault = new Fault(portTypeFault.getName());
//            Fault fault = new Fault(portTypeFault.getMessage().getLocalPart());
//
//            SOAPFault soapFault =
//                (SOAPFault)getExtensionOfType(bindingFault, SOAPFault.class);
//            if (soapFault == null) {
//                // the WSDL document is invalid
//                throw new ModelerException(
//                    "wsdlmodeler.invalid.bindingFault.outputMissingSoapFault",
//                    new Object[] {
//                        bindingFault.getName(),
//                        info.bindingOperation.getName()});
//            }
//
//            if (soapFault.getName() != null
//                && !soapFault.getName().equals(bindingFault.getName())) {
//                // the names should match
//                warn(
//                    "wsdlmodeler.invalid.bindingFault.wrongSoapFaultName",
//                    new Object[] {
//                        soapFault.getName(),
//                        bindingFault.getName(),
//                        info.bindingOperation.getName()});
//            }
//
//            if (soapFault.isLiteral()
//                || !tokenListContains(soapFault.getEncodingStyle(),
//                    soap11WSDLConstants.getSOAPEncodingNamespace())) {
//                // with rpc style, we only support encoded use
//                warn(
//                    "wsdlmodeler.warning.ignoringFault.notEncoded",
//                    new Object[] {
//                        bindingFault.getName(),
//                        info.bindingOperation.getName()});
//                continue;
//            }
//
//            String faultNamespaceURI = soapFault.getNamespace();
//            if (faultNamespaceURI == null) {
//                // the WSDL document is invalid
//                throw new ModelerException(
//                    "wsdlmodeler.invalid.bindingFault.missingNamespace",
//                    new Object[] {
//                        bindingFault.getName(),
//                        info.bindingOperation.getName()});
//            }
//
//            com.sun.xml.rpc.wsdl.document.Message faultMessage =
//                portTypeFault.resolveMessage(info.document);
//            Iterator iter2 = faultMessage.parts();
//            if (!iter2.hasNext()) {
//                // the WSDL document is invalid
//                throw new ModelerException(
//                    "wsdlmodeler.invalid.bindingFault.emptyMessage",
//                    new Object[] {
//                        bindingFault.getName(),
//                        faultMessage.getName()});
//            }
//            MessagePart faultPart = (MessagePart)iter2.next();
//            // bug fix: 4967940
////            QName faultQName =
////                new QName(faultNamespaceURI, faultPart.getName());
//            QName faultQName = new QName(faultNamespaceURI, faultMessage.getName());
//            // end bug fix: 4967940
//            // Don't include fault messages with non-unique part names
//            if (duplicateNames.contains(faultQName)) {
//                warn(
//                    "wsdlmodeler.duplicate.fault.part.name",
//                    new Object[] {
//                        bindingFault.getName(),
//                        info.portTypeOperation.getName(),
//                        faultPart.getName()});
//                continue;
//            }
//            if (iter2.hasNext()) {
//                // the WSDL document is invalid
//                throw new ModelerException(
//                    "wsdlmodeler.invalid.bindingFault.messageHasMoreThanOnePart",
//                    new Object[] {
//                        bindingFault.getName(),
//                        faultMessage.getName()});
//            }
//
//            if (faultPart.getDescriptorKind() != SchemaKinds.XSD_TYPE) {
//                throw new ModelerException(
//                    "wsdlmodeler.invalid.message.partMustHaveTypeDescriptor",
//                    new Object[] {
//                        faultMessage.getName(),
//                        faultPart.getName()});
//            }
//
//            // TODO - what should be the local name of an encoded fault? the name of the part?
//            // the WSDL spec is very unclear on this issue
//            SOAPType faultType =
//                getSchemaTypeToSOAPType(faultPart.getDescriptor());
//            // TODO should this be faultPart.getDescriptor()?
//            // bug fix: 4967940
//            QName elementName =
//                new QName(faultNamespaceURI, faultPart.getName());
//            fault.setElementName(elementName);
//            fault.setJavaMemberName(elementName.getLocalPart());
//            // end bug fix: 4967940
//
//            Block faultBlock = new Block(faultQName, faultType);
//            fault.setBlock(faultBlock);
//            createParentFault(fault);
//            createSubfaults(fault);
//            response.addFaultBlock(faultBlock);
//            info.operation.addFault(fault);
//        }
//    }

    protected void createParentFault(Fault fault) {
        AbstractType faultType = fault.getBlock().getType();
        AbstractType parentType = null;

        
        if (parentType == null) {
            return;
        }

        if (fault.getParentFault() != null) {
            return;
        }
        Fault parentFault =
            new Fault(((AbstractType)parentType).getName().getLocalPart());
        /* this is what it really should be but for interop with JAXRPC 1.0.1 we are not doing
         * this at this time.
         *
         * TODO - we should double-check this; the above statement might not be true anymore.
         */
        QName faultQName =
            new QName(
                fault.getBlock().getName().getNamespaceURI(),
                parentFault.getName());
        Block block = new Block(faultQName);
        block.setType((AbstractType)parentType);
        parentFault.setBlock(block);
        parentFault.addSubfault(fault);
        createParentFault(parentFault);
    }

    protected void createSubfaults(Fault fault) {
        AbstractType faultType = fault.getBlock().getType();
        Iterator subtypes = null;        
        if (subtypes != null) {
            AbstractType subtype;
            while (subtypes.hasNext()) {
                subtype = (AbstractType)subtypes.next();
                Fault subFault = new Fault(subtype.getName().getLocalPart());
                /* this is what it really is but for interop with JAXRPC 1.0.1 we are not doing
                 * this at this time
                 *
                 * TODO - we should double-check this; the above statement might not be true anymore.
                 */
                QName faultQName =
                    new QName(
                        fault.getBlock().getName().getNamespaceURI(),
                        subFault.getName());
                Block block = new Block(faultQName);
                block.setType(subtype);
                subFault.setBlock(block);
                fault.addSubfault(subFault);
                createSubfaults(subFault);
            }
        }
    }

    protected SOAPBody getSOAPRequestBody() {
        SOAPBody requestBody =
            (SOAPBody)getAnyExtensionOfType(info.bindingOperation.getInput(),
                SOAPBody.class);
        if (requestBody == null) {
            // the WSDL document is invalid
            throw new ModelerException(
                "wsdlmodeler.invalid.bindingOperation.inputMissingSoapBody",
                new Object[] { info.bindingOperation.getName()});
        }
        return requestBody;
    }

    protected boolean isRequestMimeMultipart() {
        for (Iterator iter = info.bindingOperation.getInput().extensions(); iter.hasNext();) {
            Extension extension = (Extension)iter.next();
            if (extension.getClass().equals(MIMEMultipartRelated.class)) {
                return true;
            }
        }
        return false;
    }

    protected boolean isResponseMimeMultipart() {
        for (Iterator iter = info.bindingOperation.getOutput().extensions(); iter.hasNext();) {
            Extension extension = (Extension)iter.next();
            if (extension.getClass().equals(MIMEMultipartRelated.class)) {
                return true;
            }
        }
        return false;
    }




    protected SOAPBody getSOAPResponseBody() {
        SOAPBody responseBody =
            (SOAPBody)getAnyExtensionOfType(info.bindingOperation.getOutput(),
                SOAPBody.class);
        if (responseBody == null) {
            // the WSDL document is invalid
            throw new ModelerException(
                "wsdlmodeler.invalid.bindingOperation.outputMissingSoapBody",
                new Object[] { info.bindingOperation.getName()});
        }
        return responseBody;
    }

    protected com.sun.tools.ws.wsdl.document.Message getOutputMessage() {
        if (info.portTypeOperation.getOutput() == null)
            return null;
        return info.portTypeOperation.getOutput().resolveMessage(info.document);
    }

    protected com.sun.tools.ws.wsdl.document.Message getInputMessage() {
        return info.portTypeOperation.getInput().resolveMessage(info.document);
    }

    protected void setSOAPUse() {
        SOAPBody requestBody = getSOAPRequestBody();
        SOAPBody responseBody = null;

        if (useWSIBasicProfile
            && requestBody != null
            && !(requestBody.isLiteral() || requestBody.isEncoded()))
            requestBody.setUse(SOAPUse.LITERAL);
        else if (requestBody != null && requestBody.isEncoded())
            requestBody.setUse(SOAPUse.ENCODED);

        if (info.portTypeOperation.getStyle()
            == OperationStyle.REQUEST_RESPONSE) {
            responseBody = getSOAPResponseBody();
            if (useWSIBasicProfile
                && responseBody != null
                && !(responseBody.isLiteral() || responseBody.isEncoded()))
                responseBody.setUse(SOAPUse.LITERAL);
            else if (responseBody != null && responseBody.isEncoded())
                responseBody.setUse(SOAPUse.ENCODED);
        }
    }

    /**
     * @param body request or response body, represents soap:body
     * @param message Input or output message, equivalent to wsdl:message
     * @return iterator over MessagePart
     */
    protected List getMessageParts(
        SOAPBody body,
        com.sun.tools.ws.wsdl.document.Message message, boolean isInput) {
        String bodyParts = body.getParts();
        ArrayList partsList = new ArrayList();
        List parts = new ArrayList();

        //get Mime parts
        List mimeParts = null;
        if(isInput)
            mimeParts = getMimeContentParts(message, info.bindingOperation.getInput());
        else
            mimeParts = getMimeContentParts(message, info.bindingOperation.getOutput());

        if (bodyParts != null) {
            StringTokenizer in = new StringTokenizer(bodyParts.trim(), " ");
            while (in.hasMoreTokens()) {
                String part = in.nextToken();
                MessagePart mPart = (MessagePart)message.getPart(part);
                if (null == mPart) {
                    throw new ModelerException(
                        "wsdlmodeler.error.partsNotFound",
                        new Object[] { part, message.getName()});
                }
                mPart.setBindingExtensibilityElementKind(MessagePart.SOAP_BODY_BINDING);
                partsList.add(mPart);
            }
        } else {
            for(Iterator iter = message.parts();iter.hasNext();) {
                MessagePart mPart = (MessagePart)iter.next();
                if(!mimeParts.contains(mPart))
                    mPart.setBindingExtensibilityElementKind(MessagePart.SOAP_BODY_BINDING);
                partsList.add(mPart);
            }
        }

        for(Iterator iter = message.parts();iter.hasNext();) {
            MessagePart mPart = (MessagePart)iter.next();
            if(mimeParts.contains(mPart)) {
                mPart.setBindingExtensibilityElementKind(MessagePart.WSDL_MIME_BINDING);
                parts.add(mPart);
            }else if(partsList.contains(mPart)) {
                mPart.setBindingExtensibilityElementKind(MessagePart.SOAP_BODY_BINDING);
                parts.add(mPart);
            }
        }

        return parts;
    }

    /**
     * @param message
     * @return MessageParts referenced by the mime:content
     */
    protected List getMimeContentParts(Message message, Extensible ext) {
        ArrayList mimeContentParts = new ArrayList();
        String mimeContentPartName = null;
        Iterator mimeParts = getMimeParts(ext);

        while(mimeParts.hasNext()) {
            MessagePart part = getMimeContentPart(message, (MIMEPart)mimeParts.next());
            if(part != null)
                mimeContentParts.add(part);
        }
        return mimeContentParts;
    }

    /**
     * @param iterator
     */
    protected boolean validateMimeParts(Iterator mimeParts) {
        boolean gotRootPart = false;
        List mimeContents = new ArrayList();
        while(mimeParts.hasNext()) {
            MIMEPart mPart = (MIMEPart)mimeParts.next();
            Iterator extns = mPart.extensions();
            while(extns.hasNext()){
                Object obj = extns.next();
                if(obj instanceof SOAPBody){
                    if(gotRootPart) {
                        //bug fix: 5024020
                        warn("mimemodeler.invalidMimePart.moreThanOneSOAPBody",
                                new Object[] {info.operation.getName().getLocalPart()});
                        return false;
                    }
                    gotRootPart = true;
                }else if (obj instanceof MIMEContent) {
                    mimeContents.add((MIMEContent)obj);
                }
            }
            if(!validateMimeContentPartNames(mimeContents.iterator()))
                return false;
            if(mPart.getName() != null) {
                //bug fix: 5024018
                warn("mimemodeler.invalidMimePart.nameNotAllowed",
                        info.portTypeOperation.getName());
            }
        }
        return true;

    }

    private MessagePart getMimeContentPart(Message message, MIMEPart part) {
        String mimeContentPartName = null;
        Iterator mimeContents = getMimeContents(part).iterator();
        if(mimeContents.hasNext()) {
            mimeContentPartName = ((MIMEContent)mimeContents.next()).getPart();
            MessagePart mPart = (MessagePart)message.getPart(mimeContentPartName);
            //RXXXX mime:content MUST have part attribute
            if(null == mPart) {
                throw new ModelerException("wsdlmodeler.error.partsNotFound",
                        new Object[] {mimeContentPartName, message.getName()});
            }
            mPart.setBindingExtensibilityElementKind(MessagePart.WSDL_MIME_BINDING);
            return mPart;
        }
        return null;
    }

    //List of mimeTypes
    private List getAlternateMimeTypes(List mimeContents) {
        List mimeTypes = new ArrayList();
        //validateMimeContentPartNames(mimeContents.iterator());
        String mimeType = null;
        for(Iterator iter = mimeContents.iterator();iter.hasNext();){
            MIMEContent mimeContent = (MIMEContent)iter.next();
            if(mimeType == null) {
                mimeType = getMimeContentType(mimeContent);
                mimeTypes.add(mimeType);

            }
            String newMimeType = getMimeContentType(mimeContent);
            if(!newMimeType.equals(mimeType))
                mimeTypes.add(newMimeType);
        }
        return mimeTypes;
    }

    /**
     * @param iterator
     */
    private boolean validateMimeContentPartNames(Iterator mimeContents) {
        //validate mime:content(s) in the mime:part as per R2909
        while(mimeContents.hasNext()){
            String mimeContnetPart = null;
            if(mimeContnetPart == null) {
                mimeContnetPart = getMimeContentPartName((MIMEContent)mimeContents.next());
                if(mimeContnetPart == null) {
                    warn("mimemodeler.invalidMimeContent.missingPartAttribute",
                            new Object[] {info.operation.getName().getLocalPart()});
                    return false;
                }
            }else {
                String newMimeContnetPart = getMimeContentPartName((MIMEContent)mimeContents.next());
                if(newMimeContnetPart == null) {
                    warn("mimemodeler.invalidMimeContent.missingPartAttribute",
                            new Object[] {info.operation.getName().getLocalPart()});
                    return false;
                }else if(!newMimeContnetPart.equals(mimeContnetPart)) {
                    //throw new ModelerException("mimemodeler.invalidMimeContent.differentPart");
                    warn("mimemodeler.invalidMimeContent.differentPart");
                    return false;
                }
            }
        }
        return true;
    }

    protected Iterator getMimeParts(Extensible ext) {
        MIMEMultipartRelated multiPartRelated =
            (MIMEMultipartRelated) getAnyExtensionOfType(ext,
                    MIMEMultipartRelated.class);
        if(multiPartRelated == null) {
            ArrayList parts = new ArrayList();
            return parts.iterator();
        }
        return multiPartRelated.getParts();
    }

    //returns MIMEContents
    protected List getMimeContents(MIMEPart part) {
        ArrayList mimeContents = new ArrayList();
        Iterator parts = part.extensions();
        while(parts.hasNext()) {
            Extension mimeContent = (Extension) parts.next();
            if (mimeContent instanceof MIMEContent) {
                mimeContents.add((MIMEContent)mimeContent);
            }
        }
        //validateMimeContentPartNames(mimeContents.iterator());
        return mimeContents;
    }

    private String getMimeContentPartName(MIMEContent mimeContent){
        /*String partName = mimeContent.getPart();
        if(partName == null){
            throw new ModelerException("mimemodeler.invalidMimeContent.missingPartAttribute",
                    new Object[] {info.operation.getName().getLocalPart()});
        }
        return partName;*/
        return mimeContent.getPart();
    }

    private String getMimeContentType(MIMEContent mimeContent){
        String mimeType = mimeContent.getType();
        if(mimeType == null){
            throw new ModelerException("mimemodeler.invalidMimeContent.missingTypeAttribute",
                    new Object[] {info.operation.getName().getLocalPart()});
        }
        return mimeType;
    }

    protected java.util.List processParameterOrder(
        Set inputParameterNames,
        Set outputParameterNames,
        StringBuffer resultParameterName) {
        if (resultParameterName == null)
            resultParameterName = new StringBuffer();
        SOAPBody soapRequestBody = getSOAPRequestBody();
        com.sun.tools.ws.wsdl.document.Message inputMessage = getInputMessage();
        boolean isRequestResponse =
            info.portTypeOperation.getStyle()
                == OperationStyle.REQUEST_RESPONSE;
        SOAPBody soapResponseBody = null;
        com.sun.tools.ws.wsdl.document.Message outputMessage = null;

        String parameterOrder = info.portTypeOperation.getParameterOrder();
        java.util.List parameterList = null;
        boolean buildParameterList = false;

        //bug 4741083, parameterOrder set to empty string is considered same as no parameterOrder
        if ((parameterOrder != null) && !(parameterOrder.trim().equals(""))) {
            parameterList = XmlUtil.parseTokenList(parameterOrder);
        } else {
            parameterList = new ArrayList();
            buildParameterList = true;
        }

        Set partNames = new HashSet();
        boolean gotOne = false;

        List inputMessageParts = getMessageParts(soapRequestBody, inputMessage, true);

        //find out the SOAP operation extension, if any
        SOAPOperation soapOperation =
            (SOAPOperation) getExtensionOfType(info.bindingOperation,
                SOAPOperation.class);

        for(Iterator iter = inputMessageParts.iterator(); iter.hasNext();) {
            MessagePart part = (MessagePart)iter.next();
            // bugfix: 4939641
            // check wsdl:part attribute for different style
            if ((part.getBindingExtensibilityElementKind() == MessagePart.SOAP_BODY_BINDING) &&
                    !isStyleAndPartMatch(soapOperation, part)) {
                continue;
            }
            partNames.add(part.getName());
            inputParameterNames.add(part.getName());
            if (buildParameterList) {
                parameterList.add(part.getName());
            }
            gotOne = true;
        }

        boolean inputIsEmpty = !gotOne;

        if (isRequestResponse) {
            outputMessage = getOutputMessage();
            soapResponseBody = getSOAPResponseBody();
            gotOne = false;
            List outputMessageParts = getMessageParts(soapResponseBody, outputMessage, false);
            Iterator partsIter = outputMessageParts.iterator();
            while(partsIter.hasNext()) {
                MessagePart part = (MessagePart)partsIter.next();

                // bugfix: 4939641
                // check wsdl:part attribute for different style
                if ((part.getBindingExtensibilityElementKind() == MessagePart.SOAP_BODY_BINDING) &&
                        !isStyleAndPartMatch(soapOperation, part)) {
                    continue;
                }

                partNames.add(part.getName());
                // bugid 4721551, parameterOrder bug.
                // return void if no parameterOrder, all the return params go as holder in method in param
                if (!partsIter.hasNext()
                    && (outputParameterNames.size() == 0)
                    && buildParameterList
                    //bug fix: 4922107
                    // if there is just one output param and its inout then it
                    // should go as holder and return should be void
                    && !isSingleInOutPart(inputParameterNames, part)
                    ) {
                    resultParameterName.append(part.getName());
                } else {
                    outputParameterNames.add(part.getName());
                    if (buildParameterList) {
                        if (!inputParameterNames.contains(part.getName())) {
                            parameterList.add(part.getName());
                        }
                    }
                }
                gotOne = true;
            }
        }

        if (!buildParameterList) {
            // do some validation of the given parameter order
            for (Iterator iter = parameterList.iterator(); iter.hasNext();) {
                String name = (String)iter.next();
                if (!partNames.contains(name)) {
                    throw new ModelerException(
                        "wsdlmodeler.invalid.parameterorder.parameter",
                        new Object[] {
                            name,
                            info.operation.getName().getLocalPart()});
                }
                partNames.remove(name);
            }

            // now we should be left with at most one part
            if (partNames.size() > 1) {

                for (Iterator partNameIterator = partNames.iterator();
                    partNameIterator.hasNext();
                    ) {
                    String name = (String)partNameIterator.next();
                    //bug fix 4922088, throw exception only when an input part is found unlisted in parameterOrder
                    if (inputParameterNames.contains(name)
                        && !outputParameterNames.contains(name)) {
                        throw new ModelerException(
                            "wsdlmodeler.invalid.parameterOrder.tooManyUnmentionedParts",
                            new Object[] {
                                 info.operation.getName().getLocalPart()});
                    } else if (outputParameterNames.contains(name)) {
                        //this is output part not listed in the parameterOrder
                        parameterList.add(name);
                    } //ignore the part listed in parameterOrder and dont appear in in/out parts.
                }
            }
            if (partNames.size() == 1) {
                // This is a fix for bug: 4734459
                String partName = (String)partNames.iterator().next();
                if (outputParameterNames.contains(partName)) {
                    resultParameterName.append(partName);
                }
            }
        }
        return parameterList;
    }



    /**
     * Returns true when there is single inout part and no parameterOrder defined.
     * This fixes bug: 4922107. This method should be overriden for 1.1 or previous
     * version to 1.1.1 as this fix results in the generated method signature change.
     *
     * @return true if there is single inout part
     */
    protected boolean isSingleInOutPart(Set inputParameterNames, MessagePart outputPart) {
        // As of now, we dont have support for in/out in doc-lit. So return false.
        SOAPOperation soapOperation =
            (SOAPOperation) getExtensionOfType(info.bindingOperation,
                    SOAPOperation.class);
        if((soapOperation != null) &&
                (soapOperation.isDocument() || info.soapBinding.isDocument()))
            return false;

        com.sun.tools.ws.wsdl.document.Message inputMessage = getInputMessage();
        if(inputParameterNames.contains(outputPart.getName())) {
            if (inputMessage.getPart(outputPart.getName()).getDescriptor().equals(outputPart.getDescriptor())) {
                return true;
            }
        }
        return false;
    }

    protected boolean isOperationDocumentLiteral(){
        // As of now, we dont have support for in/out in doc-lit. So return false.
        SOAPOperation soapOperation =
            (SOAPOperation) getExtensionOfType(info.bindingOperation, SOAPOperation.class);
        if((soapOperation != null &&
                soapOperation.isDocument()) || info.soapBinding.isDocument())
            return true;
        return false;
    }

    /**
     * For Document/Lit the wsdl:part should only have element attribute and
     * for RPC/Lit or RPC/Encoded the wsdl:part should only have type attribute
     * inside wsdl:message.
     */
    protected boolean isStyleAndPartMatch(
        SOAPOperation soapOperation,
        MessagePart part) {

        // style attribute on soap:operation takes precedence over the
        // style attribute on soap:binding

        if ((soapOperation != null) && (soapOperation.getStyle() != null)) {
            if ((soapOperation.isDocument()
                && (part.getDescriptorKind() != SchemaKinds.XSD_ELEMENT))
                || (soapOperation.isRPC()
                    && (part.getDescriptorKind() != SchemaKinds.XSD_TYPE))) {
                return false;
            }
        } else {
            if ((info.soapBinding.isDocument()
                && (part.getDescriptorKind() != SchemaKinds.XSD_ELEMENT))
                || (info.soapBinding.isRPC()
                    && (part.getDescriptorKind() != SchemaKinds.XSD_TYPE))) {
                return false;
            }
        }

        return true;
    }



    protected String getRequestNamespaceURI(SOAPBody body) {
        String namespaceURI = body.getNamespace();
        if (namespaceURI == null) {
            // the WSDL document is invalid
            // at least, that's my interpretation of section 3.5 of the WSDL 1.1 spec!
            throw new ModelerException(
                "wsdlmodeler.invalid.bindingOperation.inputSoapBody.missingNamespace",
                new Object[] { info.bindingOperation.getName()});
        }
        return namespaceURI;
    }

    protected String getResponseNamespaceURI(SOAPBody body) {
        String namespaceURI = body.getNamespace();
        if (namespaceURI == null) {
            // the WSDL document is invalid
            // at least, that's my interpretation of section 3.5 of the WSDL 1.1 spec!
            throw new ModelerException(
                "wsdlmodeler.invalid.bindingOperation.outputSoapBody.missingNamespace",
                new Object[] { info.bindingOperation.getName()});
        }
        return namespaceURI;
    }

    protected String getStructureNamePrefix() {
        String structureNamePrefix = null;
        QName portTypeName =
            (QName)info.modelPort.getProperty(
                ModelProperties.PROPERTY_WSDL_PORT_TYPE_NAME);
        if (portTypeName != null) {
            structureNamePrefix = getNonQualifiedNameFor(portTypeName);
        } else {
            structureNamePrefix =
                getNonQualifiedNameFor(info.modelPort.getName());
        }
        structureNamePrefix += "_";
        return structureNamePrefix;
    }

//    protected Operation processSOAPOperationRPCLiteralStyle() {
//        boolean isRequestResponse =
//            info.portTypeOperation.getStyle()
//                == OperationStyle.REQUEST_RESPONSE;
//        Request request = new Request();
//        Response response = new Response();
//
//        info.operation.setUse(SOAPUse.LITERAL);
//
//        SOAPBody soapRequestBody = getSOAPRequestBody();
//
//        if(soapRequestBody != null && isRequestMimeMultipart()) {
//            request.setProperty(
//                    MESSAGE_HAS_MIME_MULTIPART_RELATED_BINDING,
//            "true");
//        }
//
//        SOAPBody soapResponseBody = null;
//        com.sun.xml.rpc.wsdl.document.Message outputMessage = null;
//        if (isRequestResponse) {
//            soapResponseBody = getSOAPResponseBody();
//            outputMessage = getOutputMessage();
//            // code added for 109
//            if (outputMessage != null)
//                response.setProperty(
//                    ModelProperties.PROPERTY_WSDL_MESSAGE_NAME,
//                    getQNameOf(outputMessage));
//
//            if(soapResponseBody != null && isResponseMimeMultipart()) {
//                response.setProperty(
//                        MESSAGE_HAS_MIME_MULTIPART_RELATED_BINDING,
//                "true");
//            }
//        }
//        // set the use attribute, it can be Encoded or Literal
//        setSOAPUse();
//
//        if (!soapRequestBody.isLiteral()
//            || (soapResponseBody != null && !soapResponseBody.isLiteral())) {
//            warn(
//                "wsdlmodeler.warning.ignoringOperation.notLiteral",
//                info.portTypeOperation.getName());
//            return null;
//        }
//
//        //bug fix: 5024020, ignore operation if there are more than one root part
//        if(!validateMimeParts(getMimeParts(info.bindingOperation.getInput())) ||
//                !validateMimeParts(getMimeParts(info.bindingOperation.getOutput())))
//            return null;
//
//        com.sun.xml.rpc.wsdl.document.Message inputMessage = getInputMessage();
//
//        setJavaOperationNameProperty(inputMessage);
//
//        // code added for 109
//        if (inputMessage != null)
//            request.setProperty(
//                ModelProperties.PROPERTY_WSDL_MESSAGE_NAME,
//                getQNameOf(inputMessage));
//
//        // Process parameterOrder and get the parameterList
//        Set inputParameterNames = new HashSet();
//        Set outputParameterNames = new HashSet();
//        Set mimeContentParameterNames = new HashSet();
//        String resultParameterName = null;
//        StringBuffer result = new StringBuffer();
//        java.util.List parameterList =
//            processParameterOrder(
//                inputParameterNames,
//                outputParameterNames,
//                result);
//        if (result.length() > 0)
//            resultParameterName = result.toString();
//        // get request/response namespace uris. These uris are used to
//        // get the respective body attributes.
//        String requestNamespaceURI = getRequestNamespaceURI(soapRequestBody);
//        String responseNamespaceURI = null;
//        if (isRequestResponse) {
//            responseNamespaceURI = getResponseNamespaceURI(soapResponseBody);
//        }
//
//        // Get the structure prefix. This prefix is used to name the
//        // request/response class.
//        String structureNamePrefix = getStructureNamePrefix();
//
//        // setup the request Block
//        QName reqBodyName =
//            new QName(requestNamespaceURI, info.portTypeOperation.getName());
//        LiteralSequenceType reqType = new LiteralSequenceType(reqBodyName);
//        reqType.setRpcWrapper(true);
//        Block reqBlock = new Block(reqBodyName, reqType);
//        JavaStructureType requestBodyJavaType =
//            new JavaStructureType(
//                getUniqueClassName(
//                    makePackageQualified(
//                        StringUtils.capitalize(
//                            structureNamePrefix
//                                + _env.getNames().validExternalJavaIdentifier(
//                                    info.uniqueOperationName))
//                            + "_RequestStruct",
//                        reqBodyName)),
//                false,
//                reqType);
//        reqType.setJavaType(requestBodyJavaType);
//        request.addBodyBlock(reqBlock);
//
//        // setup the response block
//        LiteralSequenceType resType = null;
//        Block resBlock = null;
//        JavaStructureType responseBodyJavaType = null;
//        if (isRequestResponse) {
//            QName resBodyName =
//                new QName(
//                    responseNamespaceURI,
//                    info.portTypeOperation.getName() + "Response");
//            resType = new LiteralSequenceType(resBodyName);
//            resType.setRpcWrapper(true);
//            resBlock = new Block(resBodyName, resType);
//            //            responseBodyJavaType = new JavaStructureType(makePackageQualified(StringUtils.capitalize(structureNamePrefix + _env.getNames().validExternalJavaIdentifier(info.uniqueOperationName)) + "_ResponseStruct", resBodyName), false, resType);
//            responseBodyJavaType =
//                new JavaStructureType(
//                    getUniqueClassName(
//                        makePackageQualified(
//                            StringUtils.capitalize(
//                                structureNamePrefix
//                                    + _env
//                                        .getNames()
//                                        .validExternalJavaIdentifier(
//                                        info.uniqueOperationName))
//                                + "_ResponseStruct",
//                            resBodyName)),
//                    false,
//                    resType);
//            resType.setJavaType(responseBodyJavaType);
//            response.addBodyBlock(resBlock);
//        }
//
//        if (resultParameterName == null) {
//            // this is ugly, but we need to save information about the return type
//            // being void at this stage, so that when we later create a Java interface
//            // for the port this operation belongs to, we'll do the right thing
//            info.operation.setProperty(OPERATION_HAS_VOID_RETURN_TYPE, "true");
//        } else {
//            // handle result parameter a bit specially
//            MessagePart part = outputMessage.getPart(resultParameterName);
//            if(isBoundToMimeContent(part)){
//                //handle mime:part
//                List mimeContents = getMimeContents(info.bindingOperation.getOutput(),
//                        getOutputMessage(), resultParameterName);
//                LiteralAttachmentType mimeModelType = getAttachmentType(mimeContents, part);
//                //create Parameters in request or response
//                Block block = new Block(new QName(part.getName()),
//                        mimeModelType);
//                response.addAttachmentBlock(block);
//                Parameter outParameter =
//                    new Parameter(
//                            getEnvironment()
//                            .getNames()
//                            .validJavaMemberName(
//                                    part.getName()));
//                outParameter.setEmbedded(false);
//                outParameter.setType(mimeModelType);
//                outParameter.setBlock(block);
//                outParameter.setProperty(
//                        ModelProperties.PROPERTY_PARAM_MESSAGE_PART_NAME,
//                        part.getName());
//                response.addParameter(outParameter);
//                info.operation.setProperty(
//                        WSDL_RESULT_PARAMETER,
//                        outParameter.getName());
//            }else if(isBoundToSOAPBody(part)) {
//                LiteralType literalType=null;
//                if (part.getDescriptorKind() == SchemaKinds.XSD_TYPE) {
////                    literalType =
////                        _analyzer.schemaTypeToLiteralType(part.getDescriptor());
//                } else {
//                    literalType = getElementTypeToLiteralType(part.getDescriptor());
//                }
//
////                // bug fix: 4923650
////                literalType =
////                    (LiteralType)verifyResultType(literalType, info.operation);
//
//                LiteralElementMember member =
//                    new LiteralElementMember(
//                        new QName(null, part.getName()),
//                        literalType);
//                // bug: 4860484, set RPC/LIT param with implicit assumption
//                // of minOccurs=maxOccurs=1, nillable=false.
//                member.setRequired(RPCLIT_PARAM_REQUIRED);
//                JavaStructureMember javaMember =
//                    getJavaMember(part, literalType, member);
//                member.setJavaStructureMember(javaMember);
//                resType.add(member);
//                responseBodyJavaType.add(javaMember);
//                Parameter parameter = getParameter(part, literalType, resBlock);
//                response.addParameter(parameter);
//                info.operation.setProperty(
//                    WSDL_RESULT_PARAMETER,
//                    parameter.getName());
//            }
//        }
//
//        // create a definitive list of parameters to match what we'd like to get
//        // in the java interface (which is generated much later), parameterOrder
//        List definitiveParameterList = new ArrayList();
//
//        Parameter inParameter, outParameter;
//        for (Iterator iter = parameterList.iterator(); iter.hasNext();) {
//            String name = (String)iter.next();
//            boolean isInput = inputParameterNames.contains(name);
//            boolean isOutput = outputParameterNames.contains(name);
//            boolean isBoundToSoapBody = isBoundToSOAPBody(inputMessage.getPart(name));
//            boolean isBoundToMimeContent = isBoundToMimeContent(inputMessage.getPart(name));
//
//            inParameter = outParameter = null;
//
//            if (isInput && isOutput && isBoundToSOAPBody(inputMessage.getPart(name))) {
//                // make sure types match
//                if (!inputMessage
//                    .getPart(name)
//                    .getDescriptor()
//                    .equals(outputMessage.getPart(name).getDescriptor())) {
//                    throw new ModelerException(
//                        "wsdlmodeler.invalid.parameter.differentTypes",
//                        new Object[] {
//                            name,
//                            info.operation.getName().getLocalPart()});
//                }
//            }
//
//            if (isInput && isBoundToSOAPBody(inputMessage.getPart(name))) {
//                MessagePart part = inputMessage.getPart(name);
//                LiteralType literalType;
//                if (part.getDescriptorKind() == SchemaKinds.XSD_TYPE) {
////                    literalType =
////                        _analyzer.schemaTypeToLiteralType(part.getDescriptor());
//                } else {
//                    literalType =
//                        getElementTypeToLiteralType(part.getDescriptor());
//                }
//
//                // bug fix: 4923650
//                literalType =
//                    (LiteralType)verifyParameterType(literalType,
//                        part.getName(),
//                        info.operation);
//
//                inParameter =
//                    new Parameter(
//                        _env.getNames().validJavaMemberName(part.getName()));
//                // bug fix: 4931493
//                inParameter.setProperty(
//                    ModelProperties.PROPERTY_PARAM_MESSAGE_PART_NAME,
//                    part.getName());
//                inParameter.setEmbedded(true);
//                inParameter.setType(literalType);
//                inParameter.setBlock(reqBlock);
//                request.addParameter(inParameter);
//                definitiveParameterList.add(inParameter.getName());
//                addParameterToStructures(
//                    part,
//                    inParameter,
//                    reqType,
//                    requestBodyJavaType);
//            }else if(isInput && isBoundToMimeContent(inputMessage.getPart(name))) {
//                //handle mime:part
//                MessagePart part = inputMessage.getPart(name);
//                List mimeContents = getMimeContents(info.bindingOperation.getInput(),
//                                    getInputMessage(), name);
//
//                LiteralAttachmentType mimeModelType = getAttachmentType(mimeContents, part);
//                //create Parameters in request or response
//                Block block = new Block(new QName(part.getName()),
//                        mimeModelType);
//                    request.addAttachmentBlock(block);
//                    inParameter =
//                    new Parameter(
//                            getEnvironment()
//                            .getNames()
//                            .validJavaMemberName(
//                                    part.getName()));
//                inParameter.setEmbedded(false);
//                inParameter.setType(mimeModelType);
//                inParameter.setBlock(block);
//                inParameter.setProperty(
//                    ModelProperties.PROPERTY_PARAM_MESSAGE_PART_NAME,
//                    part.getName());
//                request.addParameter(inParameter);
//                definitiveParameterList.add(inParameter.getName());
//            }
//            if (isOutput && isBoundToSOAPBody(outputMessage.getPart(name))) {
//                MessagePart part = outputMessage.getPart(name);
//                LiteralType literalType;
//                if (part.getDescriptorKind() == SchemaKinds.XSD_TYPE) {
////                    literalType =
////                        _analyzer.schemaTypeToLiteralType(part.getDescriptor());
//                } else {
//                    literalType =
//                        getElementTypeToLiteralType(part.getDescriptor());
//                }
//                // bug fix: 4923650
////                literalType =
////                    (LiteralType)verifyParameterType(literalType,
////                        part.getName(),
////                        info.operation);
//
//                outParameter =
//                    new Parameter(
//                        _env.getNames().validJavaMemberName(part.getName()));
//                // bug fix: 4931493
//                outParameter.setProperty(
//                    ModelProperties.PROPERTY_PARAM_MESSAGE_PART_NAME,
//                    part.getName());
//                outParameter.setEmbedded(true);
//                outParameter.setType(literalType);
//                outParameter.setBlock(resBlock);
//                response.addParameter(outParameter);
//                addParameterToStructures(
//                    part,
//                    outParameter,
//                    resType,
//                    responseBodyJavaType);
//
//                if (inParameter == null) {
//                    definitiveParameterList.add(outParameter.getName());
//                } else {
//                    inParameter.setLinkedParameter(outParameter);
//                    outParameter.setLinkedParameter(inParameter);
//                }
//            }else if(isOutput && isBoundToMimeContent(outputMessage.getPart(name))) {
//                //handle mime:part
//                MessagePart part = outputMessage.getPart(name);
//                if(part != null) {
//                    List mimeContents = getMimeContents(info.bindingOperation.getOutput(),
//                            getOutputMessage(), name);
//                    LiteralAttachmentType mimeModelType = getAttachmentType(mimeContents, part);
//                    //create Parameters in request or response
//                    Block block = new Block(new QName(part.getName()),
//                            mimeModelType);
//                    response.addAttachmentBlock(block);
//                    outParameter =
//                        new Parameter(
//                                getEnvironment()
//                                .getNames()
//                                .validJavaMemberName(
//                                        part.getName()));
//                    outParameter.setEmbedded(false);
//                    outParameter.setType(mimeModelType);
//                    outParameter.setBlock(block);
//                    outParameter.setProperty(
//                            ModelProperties.PROPERTY_PARAM_MESSAGE_PART_NAME,
//                            part.getName());
//                    response.addParameter(outParameter);
//                    if (inParameter == null) {
//                        definitiveParameterList.add(outParameter.getName());
//                    } else {
//                        Parameter inParam =
//                            request.getParameterByName(
//                                    inParameter.getName());
//
//                        List inMimeTypes = ((LiteralAttachmentType)inParameter.getType()).getAlternateMIMETypes();
//                        List outMimeTypes = ((LiteralAttachmentType)outParameter.getType()).getAlternateMIMETypes();
//                        boolean sameMimeTypes = true;
//                        if(inMimeTypes.size() == outMimeTypes.size()) {
//                            //now that alternate mimeTypes are same size, lets compare them
//                            Iterator inTypesIter = inMimeTypes.iterator();
//                            Iterator outTypesIter = outMimeTypes.iterator();
//                            while(inTypesIter.hasNext()){
//                                String inTypeName = (String)inTypesIter.next();
//                                String outTypeName = (String)outTypesIter.next();
//                                if(!inTypeName.equals(outTypeName)) {
//                                    sameMimeTypes = false;
//                                    break;
//                                }
//                            }
//                        }
//
//                        String inMimeType = ((LiteralAttachmentType)inParam.getType()).getMIMEType();
//                        String outMimeType = ((LiteralAttachmentType)outParameter.getType()).getMIMEType();
//                        if(inParameter.getType().getName().equals(outParameter.getType().getName()) &&
//                                sameMimeTypes) {
//                            outParameter.setLinkedParameter(inParameter);
//                            inParameter.setLinkedParameter(outParameter);
//                        }
//
//                    }
//                }
//            }
//        }
//
//        info.operation.setRequest(request);
//
//        // DOUG for one-way operations
//        if (isRequestResponse) {
//            info.operation.setResponse(response);
//        }
//
//        // get fault names
//        Set duplicateNames = getDuplicateFaultNames();
//
//        // handle soap:fault
//        handleLiteralSOAPFault(response, duplicateNames);
//
//        // handle headers
//        boolean explicitServiceContext =
//            useExplicitServiceContextForRpcLit(inputMessage);
//        if (explicitServiceContext) {
//            handleLiteralSOAPHeaders(
//                request,
//                response,
//                //info.bindingOperation.getInput().extensions(),
//                getHeaderExtensions(info.bindingOperation.getInput()).iterator(),
//                duplicateNames,
//                definitiveParameterList,
//                true);
//            if (isRequestResponse) {
//                handleLiteralSOAPHeaders(
//                    request,
//                    response,
//                    //info.bindingOperation.getOutput().extensions(),
//                    getHeaderExtensions(info.bindingOperation.getOutput()).iterator(),
//                    duplicateNames,
//                    definitiveParameterList,
//                    false);
//            }
//        }
//        info.operation.setProperty(
//            WSDL_PARAMETER_ORDER,
//            definitiveParameterList);
//        return info.operation;
//    }



    /**
     * @param part
     * @return
     */
    protected boolean isBoundToMimeContent(MessagePart part) {
        if((part != null) && part.getBindingExtensibilityElementKind() == MessagePart.WSDL_MIME_BINDING)
            return true;
        return false;
    }

    /**
     * @param part
     * @return
     */
    protected boolean isBoundToSOAPBody(MessagePart part) {
        if((part != null) && part.getBindingExtensibilityElementKind() == MessagePart.SOAP_BODY_BINDING)
            return true;
        return false;
    }

    /**
     * @param part
     * @return
     */
    protected boolean isBoundToSOAPHeader(MessagePart part) {
        if((part != null) && part.getBindingExtensibilityElementKind() == MessagePart.SOAP_HEADER_BINDING)
            return true;
        return false;
    }

    protected boolean isUnbound(MessagePart part) {
        if((part != null) && part.getBindingExtensibilityElementKind() == MessagePart.PART_NOT_BOUNDED)
            return true;
        return false;
    }


    /**
     * @return
     */
    protected List<SOAPHeader> getHeaderExtensions(Extensible extensible) {
        List<SOAPHeader> headerList = new ArrayList<SOAPHeader>();
        Iterator bindingIter = extensible.extensions();
        while (bindingIter.hasNext()) {
            Extension extension = (Extension) bindingIter.next();
            if (extension.getClass().equals(MIMEMultipartRelated.class)) {
                for (Iterator parts = ((MIMEMultipartRelated) extension).getParts();
                parts.hasNext();) {
                    Extension part = (Extension) parts.next();
                    if (part.getClass().equals(MIMEPart.class)) {
                        boolean isRootPart = isRootPart((MIMEPart)part);
                        Iterator iter = ((MIMEPart)part).extensions();
                        while(iter.hasNext()) {
                            Object obj = iter.next();
                            if(obj instanceof SOAPHeader){
                                //bug fix: 5024015
                                if(!isRootPart) {
                                    warn(
                                            "mimemodeler.warning.IgnoringinvalidHeaderPart.notDeclaredInRootPart",
                                            new Object[] {
                                                    info.bindingOperation.getName()});
                                    return new ArrayList<SOAPHeader>();
                                }
                                headerList.add((SOAPHeader)obj);
                            }
                        }
                    }

                }
            }else if(extension instanceof SOAPHeader) {
                headerList.add((SOAPHeader)extension);
            }
         }
         return headerList;
    }

    /**
     * @param part
     * @return
     */
    private boolean isRootPart(MIMEPart part) {
        Iterator iter = part.extensions();
        while(iter.hasNext()){
            if(iter.next() instanceof SOAPBody)
                return true;
        }
        return false;
    }

    /**
     * @param response
     * @param duplicateNames
     * @param faultNames
     */
//    protected void handleLiteralSOAPFault(
//        Response response,
//        Set duplicateNames) {
//        for (Iterator iter = info.bindingOperation.faults(); iter.hasNext();) {
//            BindingFault bindingFault = (BindingFault)iter.next();
//            com.sun.xml.rpc.wsdl.document.Fault portTypeFault = null;
//            for (Iterator iter2 = info.portTypeOperation.faults();
//                iter2.hasNext();
//                ) {
//                com.sun.xml.rpc.wsdl.document.Fault aFault =
//                    (com.sun.xml.rpc.wsdl.document.Fault)iter2.next();
//                if (aFault.getName().equals(bindingFault.getName())) {
//                    if (portTypeFault != null) {
//                        // the WSDL document is invalid
//                        throw new ModelerException(
//                            "wsdlmodeler.invalid.bindingFault.notUnique",
//                            new Object[] {
//                                bindingFault.getName(),
//                                info.bindingOperation.getName()});
//                    } else {
//                        portTypeFault = aFault;
//                    }
//                }
//            }
//            if (portTypeFault == null) {
//                // the WSDL document is invalid
//                throw new ModelerException(
//                    "wsdlmodeler.invalid.bindingFault.notFound",
//                    new Object[] {
//                        bindingFault.getName(),
//                        info.bindingOperation.getName()});
//
//            }
//            // changed this so that the message name is used to create the java exception name later on
//            // Fault fault = new Fault(portTypeFault.getName());
//            Fault fault = new Fault(portTypeFault.getMessage().getLocalPart());
//            SOAPFault soapFault =
//                (SOAPFault)getExtensionOfType(bindingFault, SOAPFault.class);
//            if (soapFault == null) {
//                // the WSDL document is invalid
//                throw new ModelerException(
//                    "wsdlmodeler.invalid.bindingFault.outputMissingSoapFault",
//                    new Object[] {
//                        bindingFault.getName(),
//                        info.bindingOperation.getName()});
//            }
//            if (!soapFault.isLiteral()) {
//                warn(
//                    "wsdlmodeler.warning.ignoringFault.notLiteral",
//                    new Object[] {
//                        bindingFault.getName(),
//                        info.bindingOperation.getName()});
//                continue;
//            }
//            if (useWSIBasicProfile) {
//                if (soapFault.getName() == null) {
//                    // the soapFault name must be present
//                    warn(
//                        "wsdlmodeler.invalid.bindingFault.noSoapFaultName",
//                        new Object[] {
//                            bindingFault.getName(),
//                            info.bindingOperation.getName()});
//                } else if (
//                    !soapFault.getName().equals(bindingFault.getName())) {
//                    // the soapFault name must match bindingFault name
//                    warn(
//                        "wsdlmodeler.invalid.bindingFault.wrongSoapFaultName",
//                        new Object[] {
//                            soapFault.getName(),
//                            bindingFault.getName(),
//                            info.bindingOperation.getName()});
//                } // bug fix: 4852729
//                else if (soapFault.getNamespace() != null) {
//                    warn(
//                        "wsdlmodeler.warning.r2716r2726",
//                        new Object[] { "soapbind:fault", soapFault.getName()});
//                }
//            }
//            String faultNamespaceURI = soapFault.getNamespace();
//            if (faultNamespaceURI == null) {
//                // TODO is this  right 1/9/03
//                faultNamespaceURI =
//                    portTypeFault.getMessage().getNamespaceURI();
//            }
//
//            com.sun.xml.rpc.wsdl.document.Message faultMessage =
//                portTypeFault.resolveMessage(info.document);
//            Iterator iter2 = faultMessage.parts();
//            if (!iter2.hasNext()) {
//                // the WSDL document is invalid
//                throw new ModelerException(
//                    "wsdlmodeler.invalid.bindingFault.emptyMessage",
//                    new Object[] {
//                        bindingFault.getName(),
//                        faultMessage.getName()});
//            }
//            MessagePart faultPart = (MessagePart)iter2.next();
//            // bug fix: 4967940
//            // bug fix: 4884736, use soap:fault name attribute instead of fault part name
////            QName faultQName =
////                (soapFault.getName() == null)
////                    ? new QName(faultNamespaceURI, bindingFault.getName())
////                    : new QName(faultNamespaceURI, soapFault.getName());
//
//
//            QName faultQName = new QName(faultNamespaceURI, faultMessage.getName());
//            // end bug fix 4967940
//
//            // Don't include fault messages with non-unique soap:fault names
//            if (duplicateNames.contains(faultQName)) {
//                warn(
//                    "wsdlmodeler.duplicate.fault.soap.name",
//                    new Object[] {
//                        bindingFault.getName(),
//                        info.portTypeOperation.getName(),
//                        faultPart.getName()});
//                continue;
//            }
//
//            if (iter2.hasNext()) {
//                // the WSDL document is invalid
//                throw new ModelerException(
//                    "wsdlmodeler.invalid.bindingFault.messageHasMoreThanOnePart",
//                    new Object[] {
//                        bindingFault.getName(),
//                        faultMessage.getName()});
//            }
//
//            if (faultPart.getDescriptorKind() != SchemaKinds.XSD_ELEMENT) {
//                throw new ModelerException(
//                    "wsdlmodeler.invalid.message.partMustHaveElementDescriptor",
//                    new Object[] {
//                        faultMessage.getName(),
//                        faultPart.getName()});
//            }
//
//            AbstractType faultType;
//            faultType = getElementTypeToLiteralType(faultPart.getDescriptor());
//            LiteralSequenceType partType =
//                new LiteralSequenceType(faultPart.getDescriptor());
//            // bug fix: 4967940
////          fault.setElementName(faultPart.getDescriptor());
//            QName elemName;
//            if (info.soapBinding.getStyle() == SOAPStyle.RPC &&
//                !(faultType instanceof LiteralStructuredType)) {
//                elemName = new QName(faultNamespaceURI, faultPart.getName());
//            }
//            else {
//                elemName = faultPart.getDescriptor();
//            }
//            fault.setElementName(elemName);
//            fault.setJavaMemberName(faultPart.getName());
//            // end bug fix: 4967940
//
//            Block faultBlock = new Block(faultQName, faultType);
//            fault.setBlock(faultBlock);
//            createParentFault(fault);
//            createSubfaults(fault);
//            response.addFaultBlock(faultBlock);
//            info.operation.addFault(fault);
//        }
//    }


    /**
     *
     */
//    protected void handleLiteralSOAPHeaders(
//        Request request,
//        Response response,
//        Iterator iter,
//        Set duplicateNames,
//        List definitiveParameterList,
//        boolean processRequest) {
//        while (iter.hasNext()) {
//            Extension extension = (Extension)iter.next();
//            if (extension instanceof SOAPHeader) {
//                SOAPHeader header = (SOAPHeader)extension;
//                if (useWSIBasicProfile
//                    && !(header.isLiteral() || header.isEncoded()))
//                    header.setUse(SOAPUse.LITERAL);
//
//                if (!header.isLiteral()) {
//                    warn(
//                        "wsdlmodeler.warning.ignoringHeader.notLiteral",
//                        new Object[] {
//                            header.getPart(),
//                            info.bindingOperation.getName()});
//                    continue;
//                } else {
//                    //  bug fix: 4852729
//                    if (header.getNamespace() != null) {
//                        warn(
//                            "wsdlmodeler.warning.r2716r2726",
//                            new Object[] {
//                                "soapbind:header",
//                                info.bindingOperation.getName()});
//                    }
//                    // bug fix: 4857100
//                    com.sun.xml.rpc.wsdl.document.Message headerMessage =
//                        findMessage(header.getMessage(), info);
//
//                    if (headerMessage == null) {
//                        warn(
//                            "wsdlmodeler.warning.ignoringHeader.cant.resolve.message",
//                            new Object[] {
//                                header.getMessage(),
//                                info.bindingOperation.getName()});
//                        continue;
//                    }
//
//                    MessagePart part = headerMessage.getPart(header.getPart());
//
//                    // bug fix:4857259
//                    if (part == null) {
//                        warn(
//                            "wsdlmodeler.warning.ignoringHeader.notFound",
//                            new Object[] {
//                                header.getPart(),
//                                info.bindingOperation.getName()});
//                        continue;
//                    }
//                    if (part.getDescriptorKind() != SchemaKinds.XSD_ELEMENT) {
//                        // right now, we only support "element" message parts in headers
//                        //bug fix:4857435
//                        warn(
//                            "wsdlmodeler.invalid.message.partMustHaveElementDescriptor",
//                            new Object[] {
//                                headerMessage.getName(),
//                                part.getName()});
//                        warn(
//                            "wsdlmodeler.warning.ignoringHeader",
//                            new Object[] {
//                                header.getPart(),
//                                info.bindingOperation.getName()});
//                        continue;
//                    }
//                    //check if the part is already reffered by soap:body
//                    //bug fix: 4912182
//                    if (processRequest) {
//                        if (isHeaderPartPresentInBody(getSOAPRequestBody(),
//                            getInputMessage(),
//                            header.getPart(), true)) {
//                            warn(
//                                "wsdlmodeler.warning.ignoringHeader.partFromBody",
//                                new Object[] { header.getPart()});
//                            warn(
//                                "wsdlmodeler.warning.ignoringHeader",
//                                new Object[] {
//                                    header.getPart(),
//                                    info.bindingOperation.getName(),
//                                    });
//                            continue;
//                        }
//                    } else {
//                        if (isHeaderPartPresentInBody(getSOAPResponseBody(),
//                            getOutputMessage(),
//                            header.getPart(), false)) {
//                            warn(
//                                "wsdlmodeler.warning.ignoringHeader.partFromBody",
//                                new Object[] { header.getPart()});
//                            warn(
//                                "wsdlmodeler.warning.ignoringHeader",
//                                new Object[] {
//                                    header.getPart(),
//                                    info.bindingOperation.getName(),
//                                    });
//
//                            continue;
//                        }
//                    }
//                    LiteralType headerType;
//                    if (part.getDescriptorKind() == SchemaKinds.XSD_TYPE) {
//                        headerType =
//                            _analyzer.schemaTypeToLiteralType(
//                                part.getDescriptor());
//                    } else {
//                        headerType =
//                            getElementTypeToLiteralType(part.getDescriptor());
//                    }
//                    Block block = new Block(part.getDescriptor(), headerType);
//
//                    //J2EE for 109
//                    block.setProperty(
//                        ModelProperties.PROPERTY_WSDL_MESSAGE_NAME,
//                        getQNameOf(headerMessage));
//
//                    AbstractType alreadySeenHeaderType =
//                        (AbstractType)info.headers.get(block.getName());
//                    if (alreadySeenHeaderType != null
//                        && alreadySeenHeaderType != headerType) {
//                        warn(
//                            "wsdlmodeler.warning.ignoringHeader.inconsistentDefinition",
//                            new Object[] {
//                                header.getPart(),
//                                info.bindingOperation.getName()});
//                        continue;
//                    } else {
//                        info.headers.put(block.getName(), headerType);
//                        if (processRequest)
//                            request.addHeaderBlock(block);
//                        else
//                            response.addHeaderBlock(block);
//                        Parameter parameter =
//                            new Parameter(
//                                _env.getNames().validJavaMemberName(
//                                    part.getName()));
//                        // bug fix: 4931493
//                        parameter.setProperty(
//                            ModelProperties.PROPERTY_PARAM_MESSAGE_PART_NAME,
//                            part.getName());
//
//                        parameter.setEmbedded(false);
//                        parameter.setType(headerType);
//                        parameter.setBlock(block);
//                        if (processRequest
//                            && definitiveParameterList != null) {
//                            request.addParameter(parameter);
//                            //bug fix: 4919808
//                            definitiveParameterList.add(parameter.getName());
//                        } else {
//                            //bug fix: 4919808
//                            if (definitiveParameterList != null) {
//                                for (Iterator iterInParams =
//                                    definitiveParameterList.iterator();
//                                    iterInParams.hasNext();
//                                    ) {
//                                    String inParamName =
//                                        (String)iterInParams.next();
//                                    if (inParamName
//                                        .equals(parameter.getName())) {
//                                        Parameter inParam =
//                                            request.getParameterByName(
//                                                inParamName);
//                                        parameter.setLinkedParameter(inParam);
//                                        inParam.setLinkedParameter(parameter);
//                                    }
//                                }
//                                if (!definitiveParameterList
//                                    .contains(parameter.getName())) {
//                                    definitiveParameterList.add(
//                                        parameter.getName());
//                                }
//                            }
//                            response.addParameter(parameter);
//                        }
//                    }
//                    //generate headerfault listing in list of faults, rpc/lit
//                    processHeaderFaults(header, info, response, duplicateNames);
//                }
//            }
//        }
//    }

    /**
     * bug fix: 4912182
     * @param body
     * @param message
     * @param headerPart
     * @return
     */
    private boolean isHeaderPartPresentInBody(
        SOAPBody body,
        Message message,
        String headerPart, boolean isInput) {
        Iterator parts = getMessageParts(body, message, isInput).iterator();
        while (parts.hasNext()) {
            if (((MessagePart)parts.next()).getName().equals(headerPart)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param faultNames
     * @param duplicateNames
     */
    protected Set getDuplicateFaultNames() {
        // look for fault messages with the same soap:fault name
        Set faultNames = new HashSet();
        Set duplicateNames = new HashSet();
        for (Iterator iter = info.bindingOperation.faults(); iter.hasNext();) {
            BindingFault bindingFault = (BindingFault)iter.next();
            com.sun.tools.ws.wsdl.document.Fault portTypeFault = null;
            for (Iterator iter2 = info.portTypeOperation.faults();
                iter2.hasNext();
                ) {
                com.sun.tools.ws.wsdl.document.Fault aFault =
                    (com.sun.tools.ws.wsdl.document.Fault)iter2.next();

                if (aFault.getName().equals(bindingFault.getName())) {
                    if (portTypeFault != null) {
                        // the WSDL document is invalid
                        throw new ModelerException(
                            "wsdlmodeler.invalid.bindingFault.notUnique",
                            new Object[] {
                                bindingFault.getName(),
                                info.bindingOperation.getName()});
                    } else {
                        portTypeFault = aFault;
                    }
                }
            }
            if (portTypeFault == null) {
                // the WSDL document is invalid
                throw new ModelerException(
                    "wsdlmodeler.invalid.bindingFault.notFound",
                    new Object[] {
                        bindingFault.getName(),
                        info.bindingOperation.getName()});

            }
            SOAPFault soapFault =
                (SOAPFault)getExtensionOfType(bindingFault, SOAPFault.class);
            if (soapFault == null) {
                // the WSDL document is invalid
                throw new ModelerException(
                    "wsdlmodeler.invalid.bindingFault.outputMissingSoapFault",
                    new Object[] {
                        bindingFault.getName(),
                        info.bindingOperation.getName()});
            }

            com.sun.tools.ws.wsdl.document.Message faultMessage =
                portTypeFault.resolveMessage(info.document);
            Iterator iter2 = faultMessage.parts();
            if (!iter2.hasNext()) {
                // the WSDL document is invalid
                throw new ModelerException(
                    "wsdlmodeler.invalid.bindingFault.emptyMessage",
                    new Object[] {
                        bindingFault.getName(),
                        faultMessage.getName()});
            }
            //  bug fix: 4852729
            if (useWSIBasicProfile && (soapFault.getNamespace() != null)) {
                warn(
                    "wsdlmodeler.warning.r2716r2726",
                    new Object[] { "soapbind:fault", soapFault.getName()});
            }
            String faultNamespaceURI = soapFault.getNamespace();
            if (faultNamespaceURI == null) {
                faultNamespaceURI =
                    portTypeFault.getMessage().getNamespaceURI();
            }
            MessagePart faultPart = (MessagePart)iter2.next();
            //bug fix: 4884736, to take care of source compatibility
            String faultName =
                getFaultName(
                    faultPart.getName(),
                    soapFault.getName(),
                    bindingFault.getName(),
                    faultMessage.getName()); // bug fix 4967940 added this param
            QName faultQName = new QName(faultNamespaceURI, faultName);
            if (faultNames.contains(faultQName)) {
                duplicateNames.add(faultQName);
            } else {
                faultNames.add(faultQName);
            }
        }
        return duplicateNames;
    }

    /**
     * bug fix: 4884736, this method can be overriden from subclasses of WSDLModelerBase
     * Returns soapbinding:fault name. If null then gives warning for wsi R2721 and uses
     * wsdl:fault name.
     *
     * @param faultPartName - to be used by versions < 1.1
     * @param soapFaultName
     * @param bindFaultName
     * @return
     */
    protected String getFaultName(
        String faultPartName,
        String soapFaultName,
        String bindFaultName,
        String faultMessageName) { // bug fix 4967940 added this param

        // bug fix: 4967940
        return faultMessageName;
//        return (soapFaultName == null) ? bindFaultName : soapFaultName;
    }

    /**
     * @param part
     * @param literalType
     * @param resBlock
     * @return
     */
//    private Parameter getParameter(
//        MessagePart part,
//        LiteralType literalType,
//        Block resBlock) {
//        Parameter parameter =
//            new Parameter(_env.getNames().validJavaMemberName(part.getName()));
//        // bug fix: 4931493
//        parameter.setProperty(
//            ModelProperties.PROPERTY_PARAM_MESSAGE_PART_NAME,
//            part.getName());
//
//        parameter.setEmbedded(true);
//        parameter.setType(literalType);
//        parameter.setBlock(resBlock);
//        return parameter;
//    }

    /**
     * @param part
     * @param literalType
     * @param member
     * @param responseBodyJavaType
     * @return
     */
//    private JavaStructureMember getJavaMember(
//        MessagePart part,
//        LiteralType literalType,
//        LiteralElementMember member) {
//        JavaStructureMember javaMember =
//            new JavaStructureMember(
//                _env.getNames().validJavaMemberName(part.getName()),
//                literalType.getJavaType(),
//                member,
//                false);
//        javaMember.setReadMethod(
//            _env.getNames().getJavaMemberReadMethod(javaMember));
//        javaMember.setWriteMethod(
//            _env.getNames().getJavaMemberWriteMethod(javaMember));
//        return javaMember;
//    }

//    protected void addParameterToStructures(
//        MessagePart part,
//        Parameter parameter,
//        LiteralStructuredType literalType,
//        JavaStructureType javaType) {
//        LiteralElementMember member =
//            new LiteralElementMember(
//                new QName(part.getName()),
//                (LiteralType)parameter.getType());
//        // bug: 4860484, set RPC/LIT param with implicit assumption
//        // of minOccurs=maxOccurs=1, nillable=false.
//        member.setRequired(RPCLIT_PARAM_REQUIRED);
//        JavaStructureMember javaMember =
//            new JavaStructureMember(
//                _env.getNames().validJavaMemberName(parameter.getName()),
//                parameter.getType().getJavaType(),
//                member,
//                false);
//        javaMember.setReadMethod(
//            _env.getNames().getJavaMemberReadMethod(javaMember));
//        javaMember.setWriteMethod(
//            _env.getNames().getJavaMemberWriteMethod(javaMember));
//        member.setJavaStructureMember(javaMember);
//        literalType.add(member);
//        javaType.add(javaMember);
//    }

//    protected boolean isUnwrappable(
//        com.sun.xml.rpc.wsdl.document.Message inputMessage) {
//        return isUnwrappable();
//    }

//    /*
//     *  This routine checks for conditions of section 6.4.1 of
//     *  the JAXRPC 1.1 spec.  It also takes into consideration
//     *  some of the commandline flags.
//     */
//    protected boolean isUnwrappable() {
//        com.sun.xml.rpc.wsdl.document.Message inputMessage = getInputMessage();
//        com.sun.xml.rpc.wsdl.document.Message outputMessage =
//            getOutputMessage();
//
//        if (!strictCompliance) {
//            if (!useWSIBasicProfile) {
//                return true;
//            } else if (!unwrap) {
//                return false;
//            }
//        }
//
//        if ((inputMessage != null && inputMessage.numParts() != 1)
//            || (outputMessage != null && outputMessage.numParts() != 1)) {
//            return false;
//        }
//        MessagePart inputPart =
//            inputMessage != null
//                ? (MessagePart)inputMessage.parts().next()
//                : null;
//        MessagePart outputPart =
//            outputMessage != null
//                ? (MessagePart)outputMessage.parts().next()
//                : null;
//        String operationName = info.operation.getName().getLocalPart();
//        if ((inputPart != null
//            && !inputPart.getDescriptor().getLocalPart().equals(operationName))
//            || (outputPart != null
//                && !outputPart.getDescriptor().getLocalPart().startsWith(
//                    operationName))) {
//            return false;
//        }
//        LiteralType inputType = null;
//        LiteralType outputType = null;
//        if (inputPart != null)
//            inputType = getElementTypeToLiteralType(inputPart.getDescriptor());
//        if (outputPart != null)
//            outputType =
//                getElementTypeToLiteralType(outputPart.getDescriptor());
//
//        if (!isWrapperType(inputType) || !isWrapperType(outputType)) {
//            return false;
//        }
//
//        return true;
//    }

//    protected boolean isWrapperType(LiteralType type) {
//        if (type != null
//            && (!(type instanceof LiteralSequenceType)
//                || ((LiteralSequenceType)type).getAttributeMembersCount() > 0)) {
//            return false;
//        }
//        return true;
//    }

//    protected boolean typeHasNoWildcardElement(LiteralType type, boolean unwrappable){
//        //if not unwrappable then dont look further return it
//        if(!unwrappable)
//            return unwrappable;
//
//        if(type == null || (type != null && !(type instanceof LiteralSequenceType)))
//            return unwrappable;
//        LiteralSequenceType sequenceType = (LiteralSequenceType)type;
//        for (Iterator iter2 = sequenceType.getElementMembers(); iter2.hasNext();){
//            LiteralElementMember element =
//                (LiteralElementMember)iter2.next();
//            if(element.isWildcard())
//                return false;
//        }
//        return unwrappable;
//    }

//    protected void setUnwrapped(LiteralStructuredType type) {
//        if (type instanceof LiteralSequenceType)
//             ((LiteralSequenceType)type).setUnwrapped(true);
//    }

//    protected Operation processSOAPOperationDocumentLiteralStyle() {
//        boolean isRequestResponse =
//            info.portTypeOperation.getStyle()
//                == OperationStyle.REQUEST_RESPONSE;
//        Request request = new Request();
//        Response response = new Response();
//
//        info.operation.setUse(SOAPUse.LITERAL);
//        SOAPBody soapRequestBody = getSOAPRequestBody();
//
//        if(soapRequestBody != null && isRequestMimeMultipart()) {
//            request.setProperty(
//                    MESSAGE_HAS_MIME_MULTIPART_RELATED_BINDING,
//            "true");
//        }
//
//        //  bug fix: 4852729
//        if (useWSIBasicProfile && (soapRequestBody.getNamespace() != null)) {
//            warn(
//                "wsdlmodeler.warning.r2716",
//                new Object[] {
//                    "soapbind:body",
//                    info.bindingOperation.getName()});
//        }
//
//        SOAPBody soapResponseBody = null;
//        com.sun.xml.rpc.wsdl.document.Message outputMessage = null;
//        if (isRequestResponse) {
//            soapResponseBody = getSOAPResponseBody();
//
//            //bug fix: 4852729
//            if (useWSIBasicProfile
//                && (soapResponseBody.getNamespace() != null)) {
//                warn(
//                    "wsdlmodeler.warning.r2716",
//                    new Object[] {
//                        "soapbind:body",
//                        info.bindingOperation.getName()});
//            }
//            outputMessage = getOutputMessage();
//            // code added for 109
//            if (outputMessage != null)
//                response.setProperty(
//                    ModelProperties.PROPERTY_WSDL_MESSAGE_NAME,
//                    getQNameOf(outputMessage));
//
//            if(soapResponseBody != null && isResponseMimeMultipart()) {
//                response.setProperty(
//                        MESSAGE_HAS_MIME_MULTIPART_RELATED_BINDING,
//                "true");
//            }
//
//        }
//
//        // set the use attribute, it can be Encoded or Literal
//        setSOAPUse();
//
//        if (!soapRequestBody.isLiteral()
//            || (soapResponseBody != null && !soapResponseBody.isLiteral())) {
//            warn(
//                "wsdlmodeler.warning.ignoringOperation.notLiteral",
//                info.portTypeOperation.getName());
//            return null;
//        }
//
//        //bug fix: 5024020, ignore operation if there are more than one root part
//        if(!validateMimeParts(getMimeParts(info.bindingOperation.getInput())) ||
//                !validateMimeParts(getMimeParts(info.bindingOperation.getOutput())))
//            return null;
//
//        //bug fix: 5033862
//        if(!validateBodyParts(info.bindingOperation)) {
//            warn(
//                    "wsdlmodeler.warning.ignoringOperation.cannotHandleTypeMessagePart",
//                    info.portTypeOperation.getName());
//            return null;
//        }
//
//        com.sun.xml.rpc.wsdl.document.Message inputMessage = getInputMessage();
//
//        setJavaOperationNameProperty(inputMessage);
//
//        // code added for 109
//        if (inputMessage != null)
//            request.setProperty(
//                ModelProperties.PROPERTY_WSDL_MESSAGE_NAME,
//                getQNameOf(inputMessage));
//
//        // Process parameterOrder and get the parameterList
//        Set inputParameterNames = new HashSet();
//        Set outputParameterNames = new HashSet();
//        Set mimeContentParameterNames = new HashSet();
//        String resultParameterName = null;
//        StringBuffer result = new StringBuffer();
//        java.util.List parameterList =
//            processParameterOrder(
//                    inputParameterNames,
//                    outputParameterNames,
//                    result);
//        if (result.length() > 0)
//            resultParameterName = result.toString();
//
//        boolean unwrappable = isUnwrappable(inputMessage);
//        if (resultParameterName == null) {
//            // this is ugly, but we need to save information about the return type
//            // being void at this stage, so that when we later create a Java interface
//            // for the port this operation belongs to, we'll do the right thing
//            info.operation.setProperty(OPERATION_HAS_VOID_RETURN_TYPE, "true");
//        } else {
//            // handle result parameter a bit specially
//            MessagePart part = outputMessage.getPart(resultParameterName);
//            if(isBoundToSOAPBody(part)) {
//                //MessagePart part = outputMessage.getPart(name);
//                LiteralType literalType =
//                    getElementTypeToLiteralType(part.getDescriptor());
//
//                // bug fix: 4923650
//                literalType =
//                    (LiteralType)verifyParameterType(literalType,
//                            part.getName(),
//                            info.operation);
//
//                //if the type has wildcard children then mark unwrappable false
//                unwrappable = typeHasNoWildcardElement(literalType, unwrappable);
//
//                Block block = new Block(part.getDescriptor(), literalType);
//                response.addBodyBlock(block);
//                if ((literalType instanceof LiteralStructuredType)) {
//                    int memberCount =
//                        ((LiteralStructuredType)literalType)
//                    .getElementMembersCount()
//                    + ((LiteralStructuredType)literalType)
//                    .getAttributeMembersCount();
//                    // bugid 4839047, handle complexType/simpleContent
//                    if (((LiteralStructuredType)literalType).getContentMember()
//                            != null)
//                        memberCount++;
//                    //bug fix: 4897635, to keep backward compatibility dont unwrap output
//                    //for default case (when no wsi or strict flag)
//                    boolean j2eeUnwrap = false;
//                    if (info.operation.getProperty("J2EE_UNWRAP") != null) {
//                        j2eeUnwrap = true;
//                    }
//
//                    if (memberCount == 0
//                            && unwrappable
//                            && (useWSIBasicProfile || strictCompliance || j2eeUnwrap)) {
//                        setUnwrapped((LiteralStructuredType)literalType);
//                        info.operation.setProperty(OPERATION_HAS_VOID_RETURN_TYPE, "true");
//                        // no members -- don't introduce any parameters
//                    } else if ((memberCount == 1) && unwrappable) {
//                        // only one value -- use it as the result value directly
//                        LiteralStructuredType structuredType =
//                            (LiteralStructuredType)literalType;
//                        setUnwrapped(structuredType);
//                        JavaStructureType javaStructureType =
//                            (JavaStructureType)structuredType.getJavaType();
//                        Iterator iter2 = structuredType.getAttributeMembers();
//                        if (iter2.hasNext()) {
//                            LiteralAttributeMember attribute =
//                                (LiteralAttributeMember)iter2.next();
//                            Parameter parameter =
//                                new Parameter(
//                                        attribute
//                                        .getJavaStructureMember()
//                                        .getName());
//                            // bug fix: 4931493
//                            parameter.setProperty(
//                                    ModelProperties
//                                    .PROPERTY_PARAM_MESSAGE_PART_NAME,
//                                    attribute.getName().getLocalPart());
//
//                            parameter.setEmbedded(true);
//                            parameter.setType(attribute.getType());
//                            parameter.setBlock(block);
//                            response.addParameter(parameter);
//                            info.operation.setProperty(
//                                    WSDL_RESULT_PARAMETER,
//                                    parameter.getName());
//
//                        } else {
//                            iter2 = structuredType.getElementMembers();
//                            // this case is more complicated because we may run into a
//                            // repeated element and we have to turn it into an array
//                            LiteralElementMember element =
//                                (LiteralElementMember)iter2.next();
//                            Parameter parameter =
//                                new Parameter(
//                                        element.getJavaStructureMember().getName());
//                            // bug fix: 4931493
//                            parameter.setProperty(
//                                    ModelProperties
//                                    .PROPERTY_PARAM_MESSAGE_PART_NAME,
//                                    element.getName().getLocalPart());
//
//                            parameter.setEmbedded(true);
//                            if (element.isRepeated()) {
//                                LiteralArrayType arrayType =
//                                    new LiteralArrayType();
//                                arrayType.setName(
//                                        new QName("synthetic-array-type"));
//                                arrayType.setElementType(element.getType());
//                                JavaArrayType javaArrayType =
//                                    new JavaArrayType(
//                                            element
//                                            .getType()
//                                            .getJavaType()
//                                            .getName()
//                                            + "[]");
//                                javaArrayType.setElementType(
//                                        element.getType().getJavaType());
//                                arrayType.setJavaType(javaArrayType);
//                                parameter.setType(arrayType);
//                            } else {
//                                parameter.setType(element.getType());
//                            }
//                            parameter.setBlock(block);
//                            response.addParameter(parameter);
//                            info.operation.setProperty(
//                                    WSDL_RESULT_PARAMETER,
//                                    parameter.getName());
//
//                        }
//                    } else {
//                        // more than one member -- use it as is
//                        Parameter parameter =
//                            new Parameter(
//                                    _env.getNames().validJavaMemberName(
//                                            part.getName()));
//                        // bug fix: 4931493
//                        parameter.setProperty(
//                                ModelProperties.PROPERTY_PARAM_MESSAGE_PART_NAME,
//                                part.getName());
//                        parameter.setEmbedded(false);
//                        parameter.setType(literalType);
//                        parameter.setBlock(block);
//                        response.addParameter(parameter);
//                        info.operation.setProperty(
//                                WSDL_RESULT_PARAMETER,
//                                parameter.getName());
//
//                    }
//                } else {
//                    // any other type -- use it as is
//                    Parameter parameter =
//                        new Parameter(
//                                _env.getNames().validJavaMemberName(
//                                        part.getName()));
//                    // bug fix: 4931493
//                    parameter.setProperty(
//                            ModelProperties.PROPERTY_PARAM_MESSAGE_PART_NAME,
//                            part.getName());
//                    parameter.setEmbedded(false);
//                    parameter.setType(literalType);
//                    parameter.setBlock(block);
//                    response.addParameter(parameter);
//                    info.operation.setProperty(
//                            WSDL_RESULT_PARAMETER,
//                            parameter.getName());
//
//                }
//            }else if(isBoundToMimeContent(part)) {
//                //handle mime:part
//                //MessagePart part = outputMessage.getPart(name);
//                if(part != null) {
//                    List mimeContents = getMimeContents(info.bindingOperation.getOutput(),
//                            getOutputMessage(), resultParameterName);
//                    LiteralAttachmentType mimeModelType = getAttachmentType(mimeContents, part);
//                    //create Parameters in request or response
//                    Block block = new Block(new QName(part.getName()),
//                            mimeModelType);
//                    response.addAttachmentBlock(block);
//                    Parameter outMimeParameter =
//                        new Parameter(
//                                getEnvironment()
//                                .getNames()
//                                .validJavaMemberName(
//                                        part.getName()));
//                    outMimeParameter.setEmbedded(false);
//                    outMimeParameter.setType(mimeModelType);
//                    outMimeParameter.setBlock(block);
//                    outMimeParameter.setProperty(
//                            ModelProperties.PROPERTY_PARAM_MESSAGE_PART_NAME,
//                            part.getName());
//                    response.addParameter(outMimeParameter);
//                    info.operation.setProperty(
//                            WSDL_RESULT_PARAMETER,
//                            outMimeParameter.getName());
//
//                }
//            }
//        }
//
//        // create a definitive list of parameters to match what we'd like to get
//        // in the java interface (which is generated much later), parameterOrder
//        List definitiveParameterList = new ArrayList();
//
//        //boolean unwrappable = isUnwrappable();
//        //boolean unwrappable = isUnwrappable(inputMessage);
//        for (Iterator iter = parameterList.iterator(); iter.hasNext();) {
//            String name = (String)iter.next();
//            boolean isInput = inputParameterNames.contains(name);
//            boolean isOutput = outputParameterNames.contains(name);
//            Parameter inMimeParameter = null;
//            Parameter outMimeParameter = null;
//
//            // NOTE - there is no linkage between input and output parameters
//            // with the same name in document mode, because their types don't
//            // have to match (and they likely won't)
//            // this means that the java interface/stub/tie generator may have
//            // to rename some of the parameters in order to avoid naming conflicts
//            // -- actually this is not necessary right now because we don't map
//            // multiple output parameters to holders in literal mode; rather, we
//            // return the response structure as the result of the method
//
//            if (isInput && isBoundToSOAPBody(inputMessage.getPart(name))) {
//                MessagePart part = inputMessage.getPart(name);
//                // remember that we are assuming that we only encounter "element" parts!
//                LiteralType literalType =
//                    getElementTypeToLiteralType(part.getDescriptor());
//
//                // bug fix: 4923650
//                literalType =
//                    (LiteralType)verifyParameterType(literalType,
//                        part.getName(),
//                        info.operation);
//
//                //if the type has wildcard children then mark unwrappable false
//                unwrappable = typeHasNoWildcardElement(literalType, unwrappable);
//
//                Block block = new Block(part.getDescriptor(), literalType);
//                request.addBodyBlock(block);
//
//                if ((literalType instanceof LiteralSequenceType)
//                    && unwrappable) {
//                    LiteralSequenceType sequenceType =
//                        (LiteralSequenceType)literalType;
//                    setUnwrapped(sequenceType);
//                    for (Iterator iter2 = sequenceType.getAttributeMembers();
//                        iter2.hasNext();
//                        ) {
//                        LiteralAttributeMember attribute =
//                            (LiteralAttributeMember)iter2.next();
//                        Parameter parameter =
//                            new Parameter(
//                                attribute.getJavaStructureMember().getName());
//                        // bug fix: 4931493
//                        //                        parameter.setProperty(ModelProperties.PROPERTY_PARAM_MESSAGE_PART_NAME, attribute.getName().getLocalPart());
//                        parameter.setProperty(
//                            ModelProperties.PROPERTY_PARAM_MESSAGE_PART_NAME,
//                            attribute.getName().getLocalPart());
//
//                        parameter.setEmbedded(true);
//                        parameter.setType(attribute.getType());
//                        parameter.setBlock(block);
//                        request.addParameter(parameter);
//                        definitiveParameterList.add(parameter.getName());
//                    }
//                    // bugid 4839047, handle complexType/simpleContent
//                    if (sequenceType.getContentMember() != null) {
//                        LiteralContentMember content =
//                            sequenceType.getContentMember();
//                        Parameter parameter =
//                            new Parameter(
//                                content.getJavaStructureMember().getName());
//                        // bug fix: 4931493
//                        parameter.setProperty(
//                            ModelProperties.PROPERTY_PARAM_MESSAGE_PART_NAME,
//                            parameter.getName());
//                        parameter.setEmbedded(true);
//                        parameter.setType(content.getType());
//                        parameter.setBlock(block);
//                        request.addParameter(parameter);
//                        definitiveParameterList.add(parameter.getName());
//                    }
//                    for (Iterator iter2 = sequenceType.getElementMembers();
//                        iter2.hasNext();
//                        ) {
//                        LiteralElementMember element =
//                            (LiteralElementMember)iter2.next();
//                        Parameter parameter =
//                            new Parameter(
//                                element.getJavaStructureMember().getName());
//                        // bug fix: 4931493
//                        parameter.setProperty(
//                            ModelProperties.PROPERTY_PARAM_MESSAGE_PART_NAME,
//                            element.getName().getLocalPart());
//                        parameter.setEmbedded(true);
//                        if (element.isRepeated()) {
//                            LiteralArrayType arrayType = new LiteralArrayType();
//                            // TODO - we need a type name here, but this type is totally artificial
//                            // so we assign it a bogus name; perhaps with a bit more work we can
//                            // generate a unique name for it
//                            arrayType.setName(
//                                new QName("synthetic-array-type"));
//                            arrayType.setElementType(element.getType());
//                            JavaArrayType javaArrayType =
//                                new JavaArrayType(
//                                    element.getType().getJavaType().getName()
//                                        + "[]");
//                            javaArrayType.setElementType(
//                                element.getType().getJavaType());
//                            arrayType.setJavaType(javaArrayType);
//                            parameter.setType(arrayType);
//                        } else {
//                            parameter.setType(element.getType());
//                        }
//                        parameter.setBlock(block);
//                        request.addParameter(parameter);
//                        definitiveParameterList.add(parameter.getName());
//                    }
//                } else {
//                    Parameter parameter =
//                        new Parameter(
//                            _env.getNames().validJavaMemberName(
//                                part.getName()));
//                    // bug fix: 4931493
//                    parameter.setProperty(
//                        ModelProperties.PROPERTY_PARAM_MESSAGE_PART_NAME,
//                        part.getName());
//                    parameter.setEmbedded(false);
//                    parameter.setType(literalType);
//                    parameter.setBlock(block);
//                    request.addParameter(parameter);
//                    definitiveParameterList.add(parameter.getName());
//                }
//            }else if(isInput && isBoundToMimeContent(inputMessage.getPart(name))) {
//                //handle mime:part
//                MessagePart part = inputMessage.getPart(name);
//                List mimeContents = getMimeContents(info.bindingOperation.getInput(),
//                        getInputMessage(), name);
//
//                LiteralAttachmentType mimeModelType = getAttachmentType(mimeContents, part);
//                //create Parameters in request or response
//                Block block = new Block(new QName(part.getName()),
//                        mimeModelType);
//                request.addAttachmentBlock(block);
//                inMimeParameter =
//                    new Parameter(
//                            getEnvironment()
//                            .getNames()
//                            .validJavaMemberName(
//                                    part.getName()));
//                inMimeParameter.setEmbedded(false);
//                inMimeParameter.setType(mimeModelType);
//                inMimeParameter.setBlock(block);
//                inMimeParameter.setProperty(
//                        ModelProperties.PROPERTY_PARAM_MESSAGE_PART_NAME,
//                        part.getName());
//                request.addParameter(inMimeParameter);
//                definitiveParameterList.add(inMimeParameter.getName());
//            }
//
//            if (isOutput && isBoundToSOAPBody(outputMessage.getPart(name))) {
//                MessagePart part = outputMessage.getPart(name);
//                LiteralType literalType =
//                    getElementTypeToLiteralType(part.getDescriptor());
//
//                // bug fix: 4923650
//                literalType =
//                    (LiteralType)verifyParameterType(literalType,
//                        part.getName(),
//                        info.operation);
//
//                //if the type has wildcard children then mark unwrappable false
//                unwrappable = typeHasNoWildcardElement(literalType, unwrappable);
//
//                Block block = new Block(part.getDescriptor(), literalType);
//                response.addBodyBlock(block);
//                if ((literalType instanceof LiteralStructuredType)) {
//                    int memberCount =
//                        ((LiteralStructuredType)literalType)
//                            .getElementMembersCount()
//                            + ((LiteralStructuredType)literalType)
//                                .getAttributeMembersCount();
//                    // bugid 4839047, handle complexType/simpleContent
//                    if (((LiteralStructuredType)literalType).getContentMember()
//                        != null)
//                        memberCount++;
//                    //bug fix: 4897635, to keep backward compatibility dont unwrap output
//                    //for default case (when no wsi or strict flag)
//                    if (memberCount == 0
//                        && unwrappable
//                        && (useWSIBasicProfile || strictCompliance)) {
//                        setUnwrapped((LiteralStructuredType)literalType);
//                        // no members -- don't introduce any parameters
//                    } else if ((memberCount == 1) && unwrappable) {
//                        // only one value -- use it as the result value directly
//                        LiteralStructuredType structuredType =
//                            (LiteralStructuredType)literalType;
//                        setUnwrapped(structuredType);
//                        JavaStructureType javaStructureType =
//                            (JavaStructureType)structuredType.getJavaType();
//                        Iterator iter2 = structuredType.getAttributeMembers();
//                        if (iter2.hasNext()) {
//                            LiteralAttributeMember attribute =
//                                (LiteralAttributeMember)iter2.next();
//                            Parameter parameter =
//                                new Parameter(
//                                    attribute
//                                        .getJavaStructureMember()
//                                        .getName());
//                            // bug fix: 4931493
//                            parameter.setProperty(
//                                ModelProperties
//                                    .PROPERTY_PARAM_MESSAGE_PART_NAME,
//                                attribute.getName().getLocalPart());
//
//                            parameter.setEmbedded(true);
//                            parameter.setType(attribute.getType());
//                            parameter.setBlock(block);
//                            response.addParameter(parameter);
//                            definitiveParameterList.add(parameter.getName());
//                        } else {
//                            iter2 = structuredType.getElementMembers();
//                            // this case is more complicated because we may run into a
//                            // repeated element and we have to turn it into an array
//                            LiteralElementMember element =
//                                (LiteralElementMember)iter2.next();
//                            Parameter parameter =
//                                new Parameter(
//                                    element.getJavaStructureMember().getName());
//                            // bug fix: 4931493
//                            parameter.setProperty(
//                                ModelProperties
//                                    .PROPERTY_PARAM_MESSAGE_PART_NAME,
//                                element.getName().getLocalPart());
//
//                            parameter.setEmbedded(true);
//                            if (element.isRepeated()) {
//                                LiteralArrayType arrayType =
//                                    new LiteralArrayType();
//                                arrayType.setName(
//                                    new QName("synthetic-array-type"));
//                                arrayType.setElementType(element.getType());
//                                JavaArrayType javaArrayType =
//                                    new JavaArrayType(
//                                        element
//                                            .getType()
//                                            .getJavaType()
//                                            .getName()
//                                            + "[]");
//                                javaArrayType.setElementType(
//                                    element.getType().getJavaType());
//                                arrayType.setJavaType(javaArrayType);
//                                parameter.setType(arrayType);
//                            } else {
//                                parameter.setType(element.getType());
//                            }
//                            parameter.setBlock(block);
//                            response.addParameter(parameter);
//                            definitiveParameterList.add(parameter.getName());
//                        }
//                    } else {
//                        // more than one member -- use it as is
//                        Parameter parameter =
//                            new Parameter(
//                                _env.getNames().validJavaMemberName(
//                                    part.getName()));
//                        // bug fix: 4931493
//                        parameter.setProperty(
//                            ModelProperties.PROPERTY_PARAM_MESSAGE_PART_NAME,
//                            part.getName());
//                        parameter.setEmbedded(false);
//                        parameter.setType(literalType);
//                        parameter.setBlock(block);
//                        response.addParameter(parameter);
//                        definitiveParameterList.add(parameter.getName());
//                    }
//                } else {
//                    // any other type -- use it as is
//                    Parameter parameter =
//                        new Parameter(
//                            _env.getNames().validJavaMemberName(
//                                part.getName()));
//                    // bug fix: 4931493
//                    parameter.setProperty(
//                        ModelProperties.PROPERTY_PARAM_MESSAGE_PART_NAME,
//                        part.getName());
//                    parameter.setEmbedded(false);
//                    parameter.setType(literalType);
//                    parameter.setBlock(block);
//                    response.addParameter(parameter);
//                    definitiveParameterList.add(parameter.getName());
//                }
//            }else if(isOutput && isBoundToMimeContent(outputMessage.getPart(name))) {
//                //handle mime:part
//                MessagePart part = outputMessage.getPart(name);
//                if(part != null) {
//                    List mimeContents = getMimeContents(info.bindingOperation.getOutput(),
//                            getOutputMessage(), name);
//                    LiteralAttachmentType mimeModelType = getAttachmentType(mimeContents, part);
//                    //create Parameters in request or response
//                    Block block = new Block(new QName(part.getName()),
//                            mimeModelType);
//                    response.addAttachmentBlock(block);
//                    outMimeParameter =
//                        new Parameter(
//                                getEnvironment()
//                                .getNames()
//                                .validJavaMemberName(
//                                        part.getName()));
//                    outMimeParameter.setEmbedded(false);
//                    outMimeParameter.setType(mimeModelType);
//                    outMimeParameter.setBlock(block);
//                    outMimeParameter.setProperty(
//                            ModelProperties.PROPERTY_PARAM_MESSAGE_PART_NAME,
//                            part.getName());
//                    response.addParameter(outMimeParameter);
//                    if (inMimeParameter == null) {
//                        definitiveParameterList.add(outMimeParameter.getName());
//                    } else {
//                        List inMimeTypes = ((LiteralAttachmentType)inMimeParameter.getType()).getAlternateMIMETypes();
//                        List outMimeTypes = ((LiteralAttachmentType)outMimeParameter.getType()).getAlternateMIMETypes();
//                        boolean sameMimeTypes = true;
//                        if(inMimeTypes.size() == outMimeTypes.size()) {
//                            //now that alternate mimeTypes are same size, lets compare them
//                            Iterator inTypesIter = inMimeTypes.iterator();
//                            Iterator outTypesIter = outMimeTypes.iterator();
//                            while(inTypesIter.hasNext()){
//                                String inTypeName = (String)inTypesIter.next();
//                                String outTypeName = (String)outTypesIter.next();
//                                if(!inTypeName.equals(outTypeName)) {
//                                    sameMimeTypes = false;
//                                    break;
//                                }
//                            }
//                        }
//                        if(inMimeParameter.getType().getName().equals(outMimeParameter.getType().getName()) &&
//                                sameMimeTypes) {
//                            outMimeParameter.setLinkedParameter(inMimeParameter);
//                            inMimeParameter.setLinkedParameter(outMimeParameter);
//                        }
//
//                    }
//                }
//            }
//        }
//
//        info.operation.setRequest(request);
//
//        // DOUG one-way operation
//        if (isRequestResponse) {
//            info.operation.setResponse(response);
//        }
//
//        // faults with duplicate names
//        Set duplicateNames = getDuplicateFaultNames();
//
//        // handle soap:fault
//        handleLiteralSOAPFault(response, duplicateNames);
//
//        // handle headers
//        //List inputHdrParams = new ArrayList();
//        boolean explicitServiceContext =
//            useExplicitServiceContextForDocLit(inputMessage);
//        if (explicitServiceContext) {
//            handleLiteralSOAPHeaders(
//                request,
//                response,
//                //info.bindingOperation.getInput().extensions(),
//                getHeaderExtensions(info.bindingOperation.getInput()).iterator(),
//                duplicateNames,
//                definitiveParameterList,
//                true);
//            if (isRequestResponse) {
//                handleLiteralSOAPHeaders(
//                    request,
//                    response,
//                    //info.bindingOperation.getOutput().extensions(),
//                    getHeaderExtensions(info.bindingOperation.getOutput()).iterator(),
//                    duplicateNames,
//                    definitiveParameterList,
//                    false);
//            }
//        }
//        info.operation.setProperty(
//                WSDL_PARAMETER_ORDER,
//                definitiveParameterList);
//
//        return info.operation;
//    }

    /**
     * @param operation
     * @return
     */
    protected boolean validateBodyParts(BindingOperation operation) {
        boolean isRequestResponse =
            info.portTypeOperation.getStyle()
            == OperationStyle.REQUEST_RESPONSE;
        List inputParts = getMessageParts(getSOAPRequestBody(), getInputMessage(), true);
        if(!validateStyleAndPart(operation, inputParts))
            return false;

        if(isRequestResponse){
            List outputParts = getMessageParts(getSOAPResponseBody(), getOutputMessage(), false);
            if(!validateStyleAndPart(operation, outputParts))
                return false;
        }
        return true;
    }

    /**
     * @param operation
     * @param inputParts
     * @return
     */
    private boolean validateStyleAndPart(BindingOperation operation, List parts) {
        SOAPOperation soapOperation =
            (SOAPOperation) getExtensionOfType(operation, SOAPOperation.class);
        for(Iterator iter = parts.iterator(); iter.hasNext();){
            MessagePart part = (MessagePart)iter.next();
            if(part.getBindingExtensibilityElementKind() == MessagePart.SOAP_BODY_BINDING){
                if(!isStyleAndPartMatch(soapOperation, part))
                    return false;
            }
        }
        return true;
    }

//    protected void processHeaderFaults(
//        SOAPHeader header,
//        ProcessSOAPOperationInfo info,
//        Response response,
//        Set duplicateNames) {
//        Iterator faults = header.faults();
//        while (faults.hasNext()) {
//            Extension extn = (Extension)faults.next();
//            if (!(extn instanceof SOAPHeaderFault)) {
//                return; //do we throw warning here?
//            }
//
//            SOAPHeaderFault headerFault = (SOAPHeaderFault)extn;
//            if (null == headerFault.getMessage()) {
//                //throw warning here
//                return;
//            }
//
//            // bug fix: 4852729
//            if (useWSIBasicProfile && (headerFault.getNamespace() != null)) {
//                warn(
//                    "wsdlmodeler.warning.r2716r2726",
//                    new Object[] {
//                        "soapbind:headerfault",
//                        info.bindingOperation.getName()});
//            }
//
//            String faultNamespaceURI = headerFault.getNamespace();
//            // bug fix: 4857100
//            com.sun.xml.rpc.wsdl.document.Message faultMessage =
//                findMessage(headerFault.getMessage(), info);
//
//            if (faultMessage == null) {
//                warn(
//                    "wsdlmodeler.warning.ignoringFault.cant.resolve.message",
//                    new Object[] {
//                        header.getMessage(),
//                        info.bindingOperation.getName()});
//                continue;
//            }
//
//            MessagePart faultPart = faultMessage.getPart(headerFault.getPart());
//
//            if (faultPart == null) {
//                warn(
//                    "wsdlmodeler.warning.ignoringHeaderFault.notFound",
//                    new Object[] {
//                        header.getMessage(),
//                        headerFault.getPart(),
//                        info.bindingOperation.getName()});
//                continue;
//            }
//
//            QName faultQName =
//                new QName(faultNamespaceURI, faultPart.getName());
//
//            //Don't include fault messages with non-unique part names
//            if (duplicateNames.contains(faultQName)) {
//                warn(
//                    "wsdlmodeler.duplicate.fault.part.name",
//                    new Object[] {
//                        headerFault.getMessage(),
//                        info.portTypeOperation.getName(),
//                        faultPart.getName()});
//                continue;
//            }
//
//            //bug fix:4857435
//            if (faultPart.getDescriptorKind() != SchemaKinds.XSD_TYPE
//                && (headerFault.getUse() == SOAPUse.ENCODED)) {
//                warn(
//                    "wsdlmodeler.invalid.message.partMustHaveTypeDescriptor",
//                    new Object[] {
//                        faultMessage.getName(),
//                        faultPart.getName()});
//                warn(
//                    "wsdlmodeler.warning.ignoringHeaderFault",
//                    new Object[] {
//                        headerFault.getPart(),
//                        info.bindingOperation.getName()});
//                continue;
//            }
//
//            if (faultPart.getDescriptorKind() != SchemaKinds.XSD_ELEMENT
//                && (headerFault.getUse() == SOAPUse.LITERAL)) {
//                warn(
//                    "wsdlmodeler.invalid.message.partMustHaveElementDescriptor",
//                    new Object[] {
//                        faultMessage.getName(),
//                        faultPart.getName()});
//                warn(
//                    "wsdlmodeler.warning.ignoringHeaderFault",
//                    new Object[] {
//                        headerFault.getPart(),
//                        info.bindingOperation.getName()});
//                continue;
//            }
//
//            AbstractType faultType;
//            if (headerFault.getUse() == SOAPUse.LITERAL) {
//                if (faultPart.getDescriptorKind() == SchemaKinds.XSD_TYPE) {
////                    faultType =
////                        _analyzer.schemaTypeToLiteralType(
////                            faultPart.getDescriptor());
//                } else {
//                    faultType =
//                        getElementTypeToLiteralType(faultPart.getDescriptor());
//                }
//            } else {
//                //headerfault can only be literal?
//                warn(
//                    "wsdlmodeler.warning.ignoringHeader.notLiteral",
//                    new Object[] {
//                        header.getPart(),
//                        info.bindingOperation.getName()});
//                continue;
//            }
//
//
//            //bug fix: 4912182
//            HeaderFault fault =
//                new HeaderFault(faultPart.getDescriptor().toString());
//
//            fault.setElementName(faultPart.getDescriptor());
//            fault.setMessage(headerFault.getMessage());
//            fault.setPart(headerFault.getPart());
//
//            //bug fix: 4980873, set headerfault to LiteralSequenceType so that a
//            //serializer is generated. This  bug also fixes 4967940
//            faultType = getHeaderFaultSequenceType(faultType, faultPart, faultPart.getDescriptor());
//
//            Block faultBlock = new Block(faultPart.getDescriptor(), faultType);
//            fault.setBlock(faultBlock);
//            createParentFault(fault);
//            createSubfaults(fault);
//            response.addFaultBlock(faultBlock);
//            info.operation.addFault(fault);
//        }
//    }

//    /**
//     * @param faultType
//     */
//    protected AbstractType getHeaderFaultSequenceType(AbstractType faultType, MessagePart faultPart, QName elemName) {
//        if(faultType instanceof LiteralSimpleType) {
//            LiteralSimpleType faultSimpleType = new LiteralSimpleType(faultType.getName(),
//                    (JavaSimpleType)faultType.getJavaType());
//            LiteralSequenceType structureType =
//                new LiteralSequenceType(faultPart.getDescriptor());
//            JavaStructureType javaStructureType =
//                new JavaStructureType(
//                        makePackageQualified(
//                                _env.getNames().validJavaClassName(
//                                        structureType.getName().getLocalPart()),
//                                        structureType.getName()),
//                                        false,
//                                        structureType);
//            // resolveNamingConflictsFor(javaStructureType);
//            structureType.setJavaType(javaStructureType);
//            LiteralElementMember member =
//                new LiteralElementMember(
//                        elemName,
//                        faultSimpleType);
//            JavaStructureMember javaMember =
//                new JavaStructureMember(
//                        faultPart.getName(),
//                        faultSimpleType.getJavaType(),
//                        member,
//                        false);
//            javaMember.setReadMethod(
//                    _env.getNames().getJavaMemberReadMethod(javaMember));
//            javaMember.setWriteMethod(
//                    _env.getNames().getJavaMemberWriteMethod(javaMember));
//            member.setJavaStructureMember(javaMember);
//            javaStructureType.add(javaMember);
//            structureType.add(member);
//            return structureType;
//        }
//        return faultType;
//    }

    protected String getJavaNameOfSEI(Port port) {
        QName portTypeName =
            (QName)port.getProperty(
                ModelProperties.PROPERTY_WSDL_PORT_TYPE_NAME);
        QName bindingName =
            (QName)port.getProperty(ModelProperties.PROPERTY_WSDL_BINDING_NAME);

        String interfaceName = null;
        if (portTypeName != null) {
            // got portType information from WSDL, use it to name the interface
            interfaceName =
                makePackageQualified(
                    _env.getNames().validJavaClassName(
                        getNonQualifiedNameFor(portTypeName)),
                    portTypeName,
                    false);
        } else {
            // somehow we only got the port name, so we use that
            interfaceName =
                makePackageQualified(
                    _env.getNames().validJavaClassName(
                        getNonQualifiedNameFor(port.getName())),
                    port.getName(),
                    false);
        }
        return interfaceName;
    }

    protected void createJavaInterfaceForPort(Port port, boolean isProvider) {
        String interfaceName = getJavaNameOfSEI(port);

        if (isConflictingPortClassName(interfaceName)) {
            interfaceName += "_PortType";
        }

        JavaInterface intf = new JavaInterface(interfaceName);

        Set methodNames = new HashSet();
        Set methodSignatures = new HashSet();

        for (Iterator iter = port.getOperations(); iter.hasNext();) {
            Operation operation = (Operation)iter.next();
            createJavaMethodForOperation(
                port,
                operation,
                intf,
                methodNames,
                methodSignatures);

            for(JavaParameter jParam : operation.getJavaMethod().getParametersList()){
                Parameter param = jParam.getParameter();
                if(param.getCustomName() != null)
                    jParam.setName(param.getCustomName());
            }
        }

        port.setJavaInterface(intf);
    }

    protected String getJavaNameForOperation(Operation operation) {
        return _env.getNames().validJavaMemberName(
            operation.getName().getLocalPart());
    }

    protected void createJavaMethodForOperation(
        Port port,
        Operation operation,
        JavaInterface intf,
        Set methodNames,
        Set methodSignatures) {
        String candidateName = getJavaNameForOperation(operation);
        JavaMethod method = new JavaMethod(candidateName);

        Request request = operation.getRequest();
        Iterator requestBodyBlocks = request.getBodyBlocks();
        Block requestBlock =
            (requestBodyBlocks.hasNext()
                ? (Block)request.getBodyBlocks().next()
                : null);

        Response response = operation.getResponse();
        Iterator responseBodyBlocks = null;
        Block responseBlock = null;
        if (response != null) {
            responseBodyBlocks = response.getBodyBlocks();
            responseBlock =
                responseBodyBlocks.hasNext()
                    ? (Block)response.getBodyBlocks().next()
                    : null;
        }

        // build a signature of the form "opName%arg1type%arg2type%...%argntype so that we
        // detect overloading conflicts in the generated java interface/classes
        String signature = candidateName;

        List parameterOrder = (List)operation.getProperty(WSDL_PARAMETER_ORDER);
        if (parameterOrder == null) {
            for (Iterator iter = request.getParameters(); iter.hasNext();) {
                Parameter parameter = (Parameter)iter.next();

                if (parameter.getJavaParameter() != null) {
                    throw new ModelerException(
                        "wsdlmodeler.invalidOperation",
                        operation.getName().getLocalPart());
                }

                JavaType parameterType = parameter.getType().getJavaType();
                JavaParameter javaParameter =
                    new JavaParameter(
                        _env.getNames().validJavaMemberName(
                            parameter.getName()),
                        parameterType,
                        parameter,
                        parameter.getLinkedParameter() != null);
                method.addParameter(javaParameter);
                parameter.setJavaParameter(javaParameter);

                signature += "%" + parameterType.getName();
            }

            boolean operationHasVoidReturnType =
                operation.getProperty(OPERATION_HAS_VOID_RETURN_TYPE) != null;
            if (response != null) {
                Parameter resultParameter = null;
                for (Iterator iter = response.getParameters();
                    iter.hasNext();
                    ) {
                    if (!operationHasVoidReturnType
                        && resultParameter == null) {
                        resultParameter = (Parameter)iter.next();

                        if (resultParameter.getJavaParameter() != null) {
                            throw new ModelerException(
                                "wsdlmodeler.invalidOperation",
                                operation.getName().getLocalPart());
                        }

                        if (resultParameter.getLinkedParameter() != null) {
                            // result is an [inout] parameter
                            throw new ModelerException(
                                "wsdlmodeler.resultIsInOutParameter",
                                operation.getName().getLocalPart());
                        }

                        if (resultParameter.getBlock() != responseBlock) {
                            // result outside of the response body
                            throw new ModelerException(
                                "wsdlmodeler.invalidOperation",
                                operation.getName().getLocalPart());
                        }

                        JavaType returnType =
                            resultParameter.getType().getJavaType();
                        method.setReturnType(returnType);

                    } else {

                        // ordinary out parameter, may be in/out
                        Parameter parameter = (Parameter)iter.next();

                        if (parameter.getJavaParameter() != null) {
                            throw new ModelerException(
                                "wsdlmodeler.invalidOperation",
                                operation.getName().getLocalPart());
                        }

                        JavaParameter javaParameter = null;
                        if (parameter.getLinkedParameter() != null) {
                            javaParameter =
                                parameter
                                    .getLinkedParameter()
                                    .getJavaParameter();
                        }
                        JavaType parameterType =
                            parameter.getType().getJavaType();
                        parameterType.setHolder(true);
                        parameterType.setHolderPresent(false);
                        if (javaParameter == null) {
                            javaParameter =
                                new JavaParameter(
                                    _env.getNames().validJavaMemberName(
                                        parameter.getName()),
                                    parameterType,
                                    parameter,
                                    true);
                        }
                        parameter.setJavaParameter(javaParameter);
                        if (parameter.getLinkedParameter() == null) {
                            method.addParameter(javaParameter);
                        }
                    }
                }
            }
            if (response == null || operationHasVoidReturnType) {
                method.setReturnType(_javaTypes.VOID_JAVATYPE);
            }

        } else {
            // parameter order is not null
            boolean operationHasVoidReturnType =
                operation.getProperty(OPERATION_HAS_VOID_RETURN_TYPE) != null;

            for (Iterator iter = parameterOrder.iterator(); iter.hasNext();) {
                String parameterName = (String)iter.next();
                Parameter requestParameter =
                    request.getParameterByName(parameterName);
                Parameter responseParameter =
                    response != null
                        ? response.getParameterByName(parameterName)
                        : null;

                if (requestParameter == null && responseParameter == null) {
                    // should not happen
                    throw new ModelerException(
                        "wsdlmodeler.invalidState.modelingOperation",
                        operation.getName().getLocalPart());
                }

                if (requestParameter != null) {
                    Parameter linkedParameter =
                        requestParameter.getLinkedParameter();
                    if (responseParameter == null || linkedParameter == null) {
                        // in parameter
                        JavaType parameterType =
                            requestParameter.getType().getJavaType();
                        JavaParameter javaParameter =
                            new JavaParameter(
                                _env.getNames().validJavaMemberName(
                                    requestParameter.getName()),
                                parameterType,
                                requestParameter,
                                false);
                        method.addParameter(javaParameter);
                        requestParameter.setJavaParameter(javaParameter);
                        signature += "%" + parameterType.getName();
                    } else {
                        // inout parameter
                        if (responseParameter != linkedParameter) {
                            // should not happen
                            throw new ModelerException(
                                "wsdlmodeler.invalidState.modelingOperation",
                                operation.getName().getLocalPart());
                        }

                        JavaType parameterType =
                            responseParameter.getType().getJavaType();
                        JavaParameter javaParameter =
                            new JavaParameter(
                                _env.getNames().validJavaMemberName(
                                    responseParameter.getName()),
                                parameterType,
                                responseParameter,
                                true);
                        parameterType.setHolder(true);
                        parameterType.setHolderPresent(false);
                        requestParameter.setJavaParameter(javaParameter);
                        responseParameter.setJavaParameter(javaParameter);
                        method.addParameter(javaParameter);
                        requestParameter.setJavaParameter(javaParameter);
                        responseParameter.setJavaParameter(javaParameter);
                        signature += "%" + parameterType.getName();
                    }
                } else if (responseParameter != null) {
                    // out parameter
                    Parameter linkedParameter =
                        responseParameter.getLinkedParameter();
                    if (linkedParameter != null) {
                        // should not happen
                        throw new ModelerException(
                            "wsdlmodeler.invalidState.modelingOperation",
                            operation.getName().getLocalPart());
                    }

                    JavaType parameterType =
                        responseParameter.getType().getJavaType();
                    parameterType.setHolder(true);
                    parameterType.setHolderPresent(false);
                    JavaParameter javaParameter =
                        new JavaParameter(
                            _env.getNames().validJavaMemberName(
                                responseParameter.getName()),
                            parameterType,
                            responseParameter,
                            true);
                    responseParameter.setJavaParameter(javaParameter);
                    method.addParameter(javaParameter);
                    signature += "%" + parameterType.getName();
                }
            }

            // handle result parameter separately
            String resultParameterName =
                (String)operation.getProperty(WSDL_RESULT_PARAMETER);
            if (resultParameterName == null) {
                if (!operationHasVoidReturnType) {
                    // should not happen
                    throw new ModelerException(
                        "wsdlmodeler.invalidState.modelingOperation",
                        operation.getName().getLocalPart());
                }

                method.setReturnType(_javaTypes.VOID_JAVATYPE);
            } else {
                if (operationHasVoidReturnType) {
                    // should not happen
                    throw new ModelerException(
                        "wsdlmodeler.invalidState.modelingOperation",
                        operation.getName().getLocalPart());
                }

                Parameter resultParameter =
                    response.getParameterByName(resultParameterName);
                JavaType returnType = resultParameter.getType().getJavaType();
                method.setReturnType(returnType);
            }

        }

        String operationName = candidateName;
        if (methodSignatures.contains(signature)) {
            operationName = makeNameUniqueInSet(candidateName, methodNames);
            method.setName(operationName);
        }
        methodSignatures.add(signature);
        methodNames.add(method.getName());

        operation.setJavaMethod(method);
        intf.addMethod(method);

        for (Iterator iter = operation.getFaults();
            iter != null && iter.hasNext();
            ) {
            Fault fault = (Fault)iter.next();
            createJavaException(fault, port, operationName);
        }
        JavaException javaException;
        Fault fault;
        for (Iterator iter = operation.getFaults(); iter.hasNext();) {
            fault = (Fault)iter.next();
            javaException = fault.getJavaException();
            method.addException(javaException.getName());
        }

    }

    protected boolean createJavaException(
        Fault fault,
        Port port,
        String operationName) {
            return createJavaExceptionFromLiteralType(
                fault,
                port,
                operationName);
    }

    protected WSDLExceptionInfo getExceptionInfo(Fault fault) {
        return null;
    }

//    protected boolean createJavaExceptionFromSOAPType(
//        Fault fault,
//        Port port,
//        String operationName) {
//        String exceptionName = null;
//        String propertyName =
//            _env.getNames().validJavaMemberName(fault.getName());
//        SOAPType faultType = (SOAPType)fault.getBlock().getType();
//        SOAPStructureType soapStruct;
//        WSDLExceptionInfo exInfo = getExceptionInfo(fault);
//        if (faultType instanceof SOAPStructureType) {
//            if (exInfo != null) {
//                exceptionName = exInfo.exceptionType;
//            } else
//                exceptionName =
//                    makePackageQualified(
//                        _env.getNames().validJavaClassName(
//                            faultType.getName().getLocalPart()),
//                        faultType.getName());
//
//            soapStruct =
//                (SOAPStructureType)_faultTypeToStructureMap.get(
//                    faultType.getName());
//            if (soapStruct == null) {
//                soapStruct = new SOAPOrderedStructureType(faultType.getName());
//                SOAPStructureType temp = (SOAPStructureType)faultType;
//                Iterator members = temp.getMembers();
//                while (members.hasNext()) {
//                    soapStruct.add((SOAPStructureMember)members.next());
//                }
//                _faultTypeToStructureMap.put(faultType.getName(), soapStruct);
//            }
//        } else {
//            if (exInfo != null) {
//                exceptionName = exInfo.exceptionType;
//            } else {
//                exceptionName =
//                    makePackageQualified(
//                        _env.getNames().validJavaClassName(fault.getName()),
//                        port.getName());
//            }
//
//            // TODO is this namespaceURI OK?
//            soapStruct =
//                new SOAPOrderedStructureType(
//                    new QName(
//                        fault.getBlock().getName().getNamespaceURI(),
//                        fault.getName()));
//            // fix for bug 4847438
//            QName memberName = fault.getElementName();
//            SOAPStructureMember soapMember =
//                new SOAPStructureMember(memberName, faultType);
//            // bug fix: 4967940
////            JavaStructureMember javaMember =
////                new JavaStructureMember(
////                    memberName.getLocalPart(),
////                    faultType.getJavaType(),
////                    soapMember);
//
//            String javaMemberName = fault.getJavaMemberName();
//            if (javaMemberName == null)
//                javaMemberName = memberName.getLocalPart();
//            JavaStructureMember javaMember =
//                new JavaStructureMember(
//                    javaMemberName,
//                    faultType.getJavaType(),
//                    soapMember);
//            // end bug fix  4967940
//            soapMember.setJavaStructureMember(javaMember);
//            javaMember.setConstructorPos(0);
//            // fix for bug 4847438
//            javaMember.setReadMethod(
//                _env.getNames().getJavaMemberReadMethod(javaMember));
//            javaMember.setInherited(soapMember.isInherited());
//            soapMember.setJavaStructureMember(javaMember);
//            soapStruct.add(soapMember);
//        }
//        if (isConflictingExceptionClassName(exceptionName)) {
//            exceptionName += "_Exception";
//        }
//
//        JavaException existingJavaException =
//            (JavaException)_javaExceptions.get(exceptionName);
//        if (existingJavaException != null) {
//            if (existingJavaException.getName().equals(exceptionName)) {
//                if (((SOAPType)existingJavaException.getOwner())
//                    .getName()
//                    .equals(soapStruct.getName())
//                    || isEquivalentSOAPStructures(
//                        soapStruct,
//                        (SOAPStructureType)existingJavaException.getOwner())) {
//                    // we have mapped this fault already
//                    if (faultType instanceof SOAPStructureType) {
//                        fault.getBlock().setType(
//                            (SOAPType)existingJavaException.getOwner());
//                    }
//                    fault.setJavaException(existingJavaException);
//                    createRelativeJavaExceptions(fault, port, operationName);
//                    return false;
//                }
//            }
//        }
//
//        JavaException javaException =
//            new JavaException(exceptionName, false, soapStruct);
//        soapStruct.setJavaType(javaException);
//
//        _javaExceptions.put(javaException.getName(), javaException);
//
//        Iterator members = soapStruct.getMembers();
//        SOAPStructureMember member = null;
//        JavaStructureMember javaMember;
//        for (int i = 0; members.hasNext(); i++) {
//            member = (SOAPStructureMember)members.next();
//            javaMember = member.getJavaStructureMember();
//            if ((faultType instanceof SOAPStructureType) && exInfo != null) {
//                /* Not a message whose part is a simple type, and
//                mapping meta-data is defined */
//                String memberName = member.getName().getLocalPart();
//                Integer pos = (Integer)exInfo.constructorOrder.get(memberName);
//                if (pos == null)
//                    throw new ModelerException(
//                        "unable to find member "
//                            + memberName
//                            + " in jaxrpcmapping meta for exception whose wsdl message is: "
//                            + fault.getBlock().getName());
//                javaMember.setConstructorPos(pos.intValue());
//            } else {
//                javaMember.setConstructorPos(i);
//            }
//            javaException.add(javaMember);
//        }
//        if (faultType instanceof SOAPStructureType) {
//            fault.getBlock().setType(soapStruct);
//        }
//        fault.setJavaException(javaException);
//
//        createRelativeJavaExceptions(fault, port, operationName);
//        return true;
//    }

    protected String getLiteralJavaMemberName(Fault fault) {
        String javaMemberName;

        QName memberName = fault.getElementName();
        javaMemberName = fault.getJavaMemberName();
        if (javaMemberName == null)
            javaMemberName = memberName.getLocalPart();
        return javaMemberName;
    }

//    public boolean isEquivalentSOAPStructures(
//        SOAPStructureType struct1,
//        SOAPStructureType struct2) {
//        if (struct1.getMembersCount() != struct2.getMembersCount())
//            return false;
//        Iterator members = struct1.getMembers();
//        SOAPStructureMember member1;
//        JavaStructureMember javaMember1, javaMember2;
//        for (int i = 0; members.hasNext(); i++) {
//            member1 = (SOAPStructureMember)members.next();
//            javaMember1 = member1.getJavaStructureMember();
//            javaMember2 =
//                ((JavaStructureType)struct2.getJavaType()).getMemberByName(
//                    member1.getJavaStructureMember().getName());
//            if (javaMember2.getConstructorPos() != i
//                || !javaMember1.getType().equals(javaMember2.getType())) {
//                return false;
//            }
//        }
//        return true;
//    }

//    public boolean isEquivalentLiteralStructures(
//        LiteralStructuredType struct1,
//        LiteralStructuredType struct2) {
//        if (struct1.getElementMembersCount()
//            != struct2.getElementMembersCount()
//            || struct1.getAttributeMembersCount()
//                != struct2.getAttributeMembersCount())
//            return false;
//        Iterator members = struct1.getElementMembers();
//        LiteralElementMember member1;
//        JavaStructureMember javaMember1, javaMember2;
//        for (int i = 0; members.hasNext(); i++) {
//            member1 = (LiteralElementMember)members.next();
//            javaMember1 = member1.getJavaStructureMember();
//            javaMember2 =
//                ((JavaStructureType)struct2.getJavaType()).getMemberByName(
//                    member1.getJavaStructureMember().getName());
//            if (javaMember2.getConstructorPos() != i
//                || !javaMember1.getType().equals(javaMember2.getType())) {
//                return false;
//            }
//        }
//        members = struct1.getAttributeMembers();
//        LiteralAttributeMember member;
//        for (int i = 0; members.hasNext(); i++) {
//            member = (LiteralAttributeMember)members.next();
//            javaMember1 = member.getJavaStructureMember();
//            javaMember2 =
//                ((JavaStructureType)struct2.getJavaType()).getMemberByName(
//                    member.getJavaStructureMember().getName());
//            if (javaMember2.getConstructorPos() != i
//                || !javaMember1.getType().equals(javaMember2.getType())) {
//                return false;
//            }
//        }
//        return true;
//    }

    protected boolean createJavaExceptionFromLiteralType(
        Fault fault,
        Port port,
        String operationName) {
//        WSDLExceptionInfo exInfo = getExceptionInfo(fault);
//        String exceptionName = null;
//        String propertyName =
//            _env.getNames().validJavaMemberName(fault.getName());
//        LiteralType faultType = (LiteralType)fault.getBlock().getType();
//        LiteralStructuredType literalStruct;
//        if (faultType instanceof LiteralStructuredType) {
//            if (exInfo != null) {
//                exceptionName = exInfo.exceptionType;
//            } else {
//                exceptionName =
//                    makePackageQualified(
//                        _env.getNames().validJavaClassName(
//                            faultType.getName().getLocalPart()),
//                        faultType.getName());
//            }
//
//            literalStruct =
//                (LiteralStructuredType)_faultTypeToStructureMap.get(
//                    faultType.getName());
//            if (literalStruct == null) {
//                literalStruct = new LiteralSequenceType(faultType.getName());
//                LiteralStructuredType temp = (LiteralStructuredType)faultType;
//                Iterator members = temp.getElementMembers();
//                while (members.hasNext()) {
//                    literalStruct.add((LiteralElementMember)members.next());
//                }
//                for (Iterator iter2 = temp.getAttributeMembers();
//                    iter2.hasNext();
//                    ) {
//                    LiteralAttributeMember attribute =
//                        (LiteralAttributeMember)iter2.next();
//                    literalStruct.add(attribute);
//                }
//                _faultTypeToStructureMap.put(
//                    faultType.getName(),
//                    literalStruct);
//            }
//        } else {
//            if (exInfo != null) {
//                exceptionName = exInfo.exceptionType;
//            } else {
//                exceptionName =
//                    makePackageQualified(
//                        _env.getNames().validJavaClassName(fault.getName()),
//                        port.getName());
//            }
//
//            // use fault namespace attribute
//            literalStruct =
//                new LiteralSequenceType(
//                    new QName(
//                        fault.getBlock().getName().getNamespaceURI(),
//                        fault.getName()));
//            // fix for bug 4847438
//            QName memberName = fault.getElementName();
//            LiteralElementMember literalMember =
//                new LiteralElementMember(memberName, faultType);
//            literalMember.setNillable(faultType.isNillable());
//            // bug fix: 4967940
////            JavaStructureMember javaMember =
////                new JavaStructureMember(
////                    fault.getBlock().getName().getLocalPart(),
////                    faultType.getJavaType(),
////                    literalMember);
///*          String javaMemberName = fault.getJavaMemberName();
//            if (javaMemberName == null)
//               javaMemberName = memberName.getLocalPart();*/
//            String javaMemberName = getLiteralJavaMemberName(fault);
//            JavaStructureMember javaMember =
//                new JavaStructureMember(
//                    javaMemberName,
//                    faultType.getJavaType(),
//                    literalMember);
//            // end bug fix 4967940
//            literalMember.setJavaStructureMember(javaMember);
//            javaMember.setConstructorPos(0);
//            // fix for bug 4847438
//            javaMember.setReadMethod(
//                _env.getNames().getJavaMemberReadMethod(javaMember));
//            javaMember.setInherited(false);
//            literalMember.setJavaStructureMember(javaMember);
//            literalStruct.add(literalMember);
//        }
//        if (isConflictingExceptionClassName(exceptionName)) {
//            exceptionName += "_Exception";
//        }
//
//        JavaException existingJavaException =
//            (JavaException)_javaExceptions.get(exceptionName);
//        if (existingJavaException != null) {
//            if (existingJavaException.getName().equals(exceptionName)) {
//                if (((LiteralType)existingJavaException.getOwner())
//                    .getName()
//                    .equals(literalStruct.getName())
//                    || isEquivalentLiteralStructures(
//                        literalStruct,
//                        (LiteralStructuredType)existingJavaException
//                            .getOwner())) {
//                    // we have mapped this fault already
//                    if (faultType instanceof LiteralStructuredType) {
//                        fault.getBlock().setType(
//                            (LiteralType)existingJavaException.getOwner());
//                    }
//                    fault.setJavaException(existingJavaException);
//                    createRelativeJavaExceptions(fault, port, operationName);
//                    return false;
//                }
//            }
//        }
//
//        JavaException javaException =
//            new JavaException(exceptionName, false, literalStruct);
//        literalStruct.setJavaType(javaException);
//
//        _javaExceptions.put(javaException.getName(), javaException);
//        int constPos = 0;
//        JavaStructureMember javaMember;
//        for (Iterator iter2 = literalStruct.getAttributeMembers();
//            iter2.hasNext();
//            ) {
//            LiteralAttributeMember attribute =
//                (LiteralAttributeMember)iter2.next();
//            javaMember = attribute.getJavaStructureMember();
//            javaMember.setConstructorPos(constPos++);
//            javaException.add(javaMember);
//        }
//
//        Iterator members = literalStruct.getElementMembers();
//        LiteralElementMember member = null;
//        for (int i = constPos; members.hasNext(); i++) {
//            member = (LiteralElementMember)members.next();
//            javaMember = member.getJavaStructureMember();
//
//            if ((faultType instanceof LiteralStructuredType)
//                && exInfo != null) {
//                /* Not a message whose part is a simple type, and
//                mapping meta-data is defined */
//                String memberName = null;
//                if (member instanceof LiteralWildcardMember) {
//                    memberName = javaMember.getName();
//                } else {
//                    memberName = member.getName().getLocalPart();
//                }
//                Integer pos = (Integer)exInfo.constructorOrder.get(memberName);
//                if (pos == null)
//                    throw new ModelerException(
//                        "unable to find member "
//                            + memberName
//                            + " in jaxrpcmapping meta for exception whose wsdl message is: "
//                            + fault.getBlock().getName());
//                javaMember.setConstructorPos(pos.intValue());
//            } else {
//                javaMember.setConstructorPos(i);
//            }
//            javaException.add(javaMember);
//        }
//        if (faultType instanceof LiteralStructuredType) {
//            fault.getBlock().setType(literalStruct);
//        }
//        fault.setJavaException(javaException);
//
//        createRelativeJavaExceptions(fault, port, operationName);
//        return true;
        return false;
    }

    protected void createRelativeJavaExceptions(
        Fault fault,
        Port port,
        String operationName) {
//        if (fault.getParentFault() != null
//            && fault.getParentFault().getJavaException() == null) {
//            createJavaException(fault.getParentFault(), port, operationName);
//            fault.getParentFault().getJavaException().addSubclass(
//                fault.getJavaException());
//            if (fault.getParentFault().getJavaException().getOwner()
//                instanceof SOAPStructureType)
//                (
//                    (SOAPStructureType)fault
//                        .getParentFault()
//                        .getJavaException()
//                        .getOwner())
//                        .addSubtype(
//                    (SOAPStructureType)fault.getJavaException().getOwner());
//            else if (
//                fault.getParentFault().getJavaException().getOwner()
//                    instanceof LiteralStructuredType)
//                (
//                    (LiteralStructuredType)fault
//                        .getParentFault()
//                        .getJavaException()
//                        .getOwner())
//                        .addSubtype(
//                    (LiteralStructuredType)fault.getJavaException().getOwner());
//        }
//        Iterator subfaults = fault.getSubfaults();
//        if (subfaults != null) {
//            Fault subfault;
//            while (subfaults.hasNext()) {
//                subfault = (Fault)subfaults.next();
//                if (subfault.getJavaException() == null) {
//                    boolean didCreateNewException =
//                        createJavaException(subfault, port, operationName);
//                    fault.getJavaException().addSubclass(
//                        subfault.getJavaException());
//                    if (fault.getJavaException().getOwner()
//                        instanceof SOAPStructureType)
//                        (
//                            (SOAPStructureType)fault
//                                .getJavaException()
//                                .getOwner())
//                                .addSubtype(
//                            (SOAPStructureType)subfault
//                                .getJavaException()
//                                .getOwner());
//                    else if (
//                        fault.getJavaException().getOwner()
//                            instanceof LiteralStructuredType)
//                        (
//                            (LiteralStructuredType)fault
//                                .getJavaException()
//                                .getOwner())
//                                .addSubtype(
//                            (LiteralStructuredType)subfault
//                                .getJavaException()
//                                .getOwner());
//                }
//            }
//        }
    }

    /**
     * @param ext
     * @param message
     * @param name
     * @return
     */
    protected List getMimeContents(Extensible ext, Message message, String name) {
        Iterator mimeParts = getMimeParts(ext);
        String mimeContentPartName = null;
        while(mimeParts.hasNext()){
            MIMEPart mimePart = (MIMEPart)mimeParts.next();
            List mimeContents = getMimeContents(mimePart);
            Iterator mimeIter = mimeContents.iterator();
            if(mimeIter.hasNext()) {
                MIMEContent mimeContent = (MIMEContent)mimeIter.next();
                mimeContentPartName = mimeContent.getPart();
                if(mimeContentPartName.equals(name))
                    return mimeContents;
            }
        }
        return null;
    }

//    /**
//     * @param mimeContents
//     * @return
//     */
//    protected LiteralAttachmentType getAttachmentType(List mimeContents, MessagePart part) {
//        JavaSimpleType type = null;
//        MimeHelper mimeHelper = new MimeHelper();
//        boolean useDataHandlerOnly = Boolean.valueOf(_options.getProperty(ProcessorOptions.USE_DATA_HANDLER_ONLY)).booleanValue();
//
//        List mimeTypes = getAlternateMimeTypes(mimeContents);
//        if(mimeTypes.size() > 1) {
//            type = MimeHelper.javaType.DATA_HANDLER_JAVATYPE;
//        }else if(mimeTypes.size() == 1){
//            String mimeType = (String)mimeTypes.iterator().next();
//            type = (JavaSimpleType) MimeHelper.mimeTypeToJavaType.get(mimeType);
//            if(type == null && mimeType.startsWith("multipart/")) {
//                type = (JavaSimpleType) MimeHelper.mimeTypeToJavaType.get(
//                "multipart/*");
//            }else if (type == null || useDataHandlerOnly) {
//                type = MimeHelper.javaType.DATA_HANDLER_JAVATYPE;
//            }
//        }
//
//        LiteralType contentType = null;
//        if (part.getDescriptorKind()
//                == SchemaKinds.XSD_TYPE) {
//            contentType = getSchemaAnalyzer().schemaTypeToLiteralType(part.getDescriptor());
//        } else if (part.getDescriptorKind()== SchemaKinds.XSD_ELEMENT) {
//            for(Iterator mimeTypeIter = mimeTypes.iterator(); mimeTypeIter.hasNext();) {
//                String mimeType = (String)mimeTypeIter.next();
//                if((!mimeType.equals("text/xml") &&
//                        !mimeType.equals("applicatioon/xml"))){
//                    //According to AP 1.0,
//                    //RZZZZ: In a DESCRIPTION, if a wsdl:part element refers to a
//                    //global element declaration (via the element attribute of the wsdl:part
//                    //element) then the value of the type attribute of a mime:content element
//                    //that binds that part MUST be a content type suitable for carrying an
//                    //XML serialization.
//                    //should we throw warning?
//                    //type = MimeHelper.javaType.DATA_HANDLER_JAVATYPE;
//                    warn("mimemodeler.elementPart.invalidElementMimeType",
//                            new Object[] {
//                            part.getName(), mimeType});
//                }
//                contentType = getElementTypeToLiteralType(part.getDescriptor());
//            }
//        }
//
//        if (contentType == null) {
//            throw new ModelerException(
//                    "mimemodeler.invalidMimeContent.unknownSchemaType",
//                    new Object[] {
//                            part.getName(),
//                            part.getDescriptor()});
//        }
//
//        //create attachment model
//        LiteralAttachmentType mimeModelType =
//            new LiteralAttachmentType(contentType.getName(), type);
//        if (mimeModelType == null) {
//            //R2909
//            throw new ModelerException(
//                    "mimemodeler.invalidMimeContent.invalidSchemaType",
//                    new Object[] {
//                            part.getName(),
//                            part.getDescriptor()});
//        }
//
//        if(mimeTypes.size() >= 1)
//            mimeModelType.setMIMEType((String)mimeTypes.iterator().next());
//        mimeModelType.setContentID(
//                MimeHelper.getAttachmentUniqueID(part.getName()));
//        mimeModelType.addAlternateMIMEType(getAlternateMimeTypes(mimeContents).iterator());
//        return mimeModelType;
//    }

    protected ProcessorEnvironment getEnvironment() {
        return _env;
    }

    protected void warn(Localizable msg) {
        getEnvironment().warn(msg);
    }

    protected void warn(String key) {
        getEnvironment().warn(_messageFactory.getMessage(key));
    }

    protected void warn(String key, String arg) {
        getEnvironment().warn(_messageFactory.getMessage(key, arg));
    }

    protected void error(String key, String arg) {
        getEnvironment().error(_messageFactory.getMessage(key, arg));
    }

    protected void warn(String key, Object[] args) {
        getEnvironment().warn(_messageFactory.getMessage(key, args));
    }

    protected void info(String key) {
        getEnvironment().info(_messageFactory.getMessage(key));
    }

    protected void info(String key, String arg) {
        getEnvironment().info(_messageFactory.getMessage(key, arg));
    }

    protected String makePackageQualified(String s, QName name) {
        return makePackageQualified(s, name, true);
    }

    protected String makePackageQualified(
        String s,
        QName name,
        boolean useNamespaceMapping) {
        String javaPackageName = null;
        if (useNamespaceMapping) {
            javaPackageName = getJavaPackageName(name);
        }
        if (javaPackageName != null) {
            return javaPackageName + "." + s;
        } else if (
            _modelInfo.getJavaPackageName() != null
                && !_modelInfo.getJavaPackageName().equals("")) {
            return _modelInfo.getJavaPackageName() + "." + s;
        } else {
            return s;
        }
    }

    protected QName makePackageQualified(QName name) {
        return makePackageQualified(name, true);
    }

    protected QName makePackageQualified(
        QName name,
        boolean useNamespaceMapping) {
        return new QName(
            name.getNamespaceURI(),
            makePackageQualified(name.getLocalPart(), name));
    }

    protected String makeNameUniqueInSet(String candidateName, Set names) {
        String baseName = candidateName;
        String name = baseName;
        for (int i = 2; names.contains(name); ++i) {
            name = baseName + Integer.toString(i);
        }
        return name;
    }

    protected String getUniqueName(
        com.sun.tools.ws.wsdl.document.Operation operation,
        boolean hasOverloadedOperations) {
        if (hasOverloadedOperations) {
            return operation.getUniqueKey().replace(' ', '_');
        } else {
            return operation.getName();
        }
    }

    protected String getUniqueParameterName(
        Operation operation,
        String baseName) {
        Set names = new HashSet();
        for (Iterator iter = operation.getRequest().getParameters();
            iter.hasNext();
            ) {
            Parameter p = (Parameter)iter.next();
            names.add(p.getName());
        }
        for (Iterator iter = operation.getResponse().getParameters();
            iter.hasNext();
            ) {
            Parameter p = (Parameter)iter.next();
            names.add(p.getName());
        }
        String candidateName = baseName;
        while (names.contains(candidateName)) {
            candidateName += "_prime";
        }
        return candidateName;
    }

    protected String getNonQualifiedNameFor(QName name) {
        return _env.getNames().validJavaClassName(name.getLocalPart());
    }

    protected static void setDocumentationIfPresent(
        ModelObject obj,
        Documentation documentation) {
        if (documentation != null && documentation.getContent() != null) {
            obj.setProperty(WSDL_DOCUMENTATION, documentation.getContent());
        }
    }

    protected static QName getQNameOf(GloballyKnown entity) {
        return new QName(
            entity.getDefining().getTargetNamespaceURI(),
            entity.getName());
    }

    protected static Extension getExtensionOfType(
            Extensible extensible,
            Class type) {
        for (Iterator iter = extensible.extensions(); iter.hasNext();) {
            Extension extension = (Extension)iter.next();
            if (extension.getClass().equals(type)) {
                return extension;
            }
        }

        return null;
    }

    protected Extension getAnyExtensionOfType(
        Extensible extensible,
        Class type) {
        if(extensible == null)
            return null;
        for (Iterator iter = extensible.extensions(); iter.hasNext();) {
            Extension extension = (Extension)iter.next();
            if(extension.getClass().equals(type)) {
                return extension;
            }else if (extension.getClass().equals(MIMEMultipartRelated.class) &&
                    (type.equals(SOAPBody.class) || type.equals(MIMEContent.class)
                            || type.equals(MIMEPart.class))) {
                for (Iterator parts =
                    ((MIMEMultipartRelated) extension).getParts();
                parts.hasNext();
                ) {
                    Extension part = (Extension) parts.next();
                    if (part.getClass().equals(MIMEPart.class)) {
                        MIMEPart mPart = (MIMEPart)part;
                        //bug fix: 5024001
                        Extension extn =  getExtensionOfType((Extensible) part, type);
                        if(extn != null)
                            return extn;
                    }
                }
            }
        }

        return null;
    }

    // bug fix: 4857100
    protected static com.sun.tools.ws.wsdl.document.Message findMessage(
        QName messageName,
        ProcessSOAPOperationInfo info) {
        com.sun.tools.ws.wsdl.document.Message message = null;
        try {
            message =
                (com.sun.tools.ws.wsdl.document.Message)info.document.find(
                    Kinds.MESSAGE,
                    messageName);
        } catch (NoSuchEntityException e) {
        }
        return message;
    }

    protected static boolean tokenListContains(
        String tokenList,
        String target) {
        if (tokenList == null) {
            return false;
        }

        StringTokenizer tokenizer = new StringTokenizer(tokenList, " ");
        while (tokenizer.hasMoreTokens()) {
            String s = tokenizer.nextToken();
            if (target.equals(s)) {
                return true;
            }
        }
        return false;
    }

    protected String getUniqueClassName(String className) {
        int cnt = 2;
        String uniqueName = className;
        while (reqResNames.contains(uniqueName.toLowerCase())) {
            uniqueName = className + cnt;
            cnt++;
        }
        reqResNames.add(uniqueName.toLowerCase());
        return uniqueName;
    }

    private String getJavaPackageName(QName name) {
        String packageName = null;
/*        if (_modelInfo.getNamespaceMappingRegistry() != null) {
            NamespaceMappingInfo i =
                _modelInfo
                    .getNamespaceMappingRegistry()
                    .getNamespaceMappingInfo(
                    name);
            if (i != null)
                return i.getJavaPackageName();
        }*/
        return packageName;
    }

    protected boolean isConflictingClassName(String name) {
        if (_conflictingClassNames == null) {
            return false;
        }

        return _conflictingClassNames.contains(name);
    }

    protected boolean isConflictingServiceClassName(String name) {
        return isConflictingClassName(name);
    }

    protected boolean isConflictingStubClassName(String name) {
        return isConflictingClassName(name);
    }

    protected boolean isConflictingTieClassName(String name) {
        return isConflictingClassName(name);
    }

    protected boolean isConflictingPortClassName(String name) {
        return isConflictingClassName(name);
    }

    protected boolean isConflictingExceptionClassName(String name) {
        return isConflictingClassName(name);
    }

//    protected LiteralType getElementTypeToLiteralType(QName elementType) {
//        return _analyzer.schemaElementTypeToLiteralType(elementType);
//    }
//
//    private SOAPType getSchemaTypeToSOAPType(QName elementType) {
//        return _analyzer.schemaTypeToSOAPType(elementType);
//    }

    protected Model getModel(){
        return theModel;
    }

    protected static final String OPERATION_HAS_VOID_RETURN_TYPE =
        "com.sun.xml.rpc.processor.modeler.wsdl.operationHasVoidReturnType";
    private static final String WSDL_DOCUMENTATION =
        "com.sun.xml.rpc.processor.modeler.wsdl.documentation";
    protected static final String WSDL_PARAMETER_ORDER =
        "com.sun.xml.rpc.processor.modeler.wsdl.parameterOrder";
    public static final String WSDL_RESULT_PARAMETER =
        "com.sun.xml.rpc.processor.modeler.wsdl.resultParameter";
    public static final String MESSAGE_HAS_MIME_MULTIPART_RELATED_BINDING =
        "com.sun.xml.rpc.processor.modeler.wsdl.mimeMultipartRelatedBinding";


    public ProcessorEnvironment getProcessorEnvironment(){
        return _env;
    }
    protected ProcessSOAPOperationInfo info;

    protected WSDLModelInfo _modelInfo;
    protected Properties _options;
    //private SchemaAnalyzerBase _analyzer;
    protected LocalizableMessageFactory _messageFactory;
    private Set _conflictingClassNames;
    protected Map _javaExceptions;
    protected Map _faultTypeToStructureMap;
    private ProcessorEnvironment _env;
    protected JavaSimpleTypeCreator _javaTypes;
    protected Map<QName, Port> _bindingNameToPortMap;
    protected static SOAPConstants soap11WSDLConstants = null;
    //protected static SOAPWSDLConstants soap12WSDLConstants = null;
    protected boolean useWSIBasicProfile = false;
//    private boolean literalOnly = false;
    private boolean unwrap = true;
    protected boolean strictCompliance = false;
    //    private boolean doNotUnwrap = false;

    private Model theModel;
    private Set reqResNames;
    private static final boolean RPCLIT_PARAM_REQUIRED = true;
    public class ProcessSOAPOperationInfo {

        public ProcessSOAPOperationInfo(
            Port modelPort,
            com.sun.tools.ws.wsdl.document.Port port,
            com.sun.tools.ws.wsdl.document.Operation portTypeOperation,
            BindingOperation bindingOperation,
            SOAPBinding soapBinding,
            WSDLDocument document,
            boolean hasOverloadedOperations,
            Map headers) {
            this.modelPort = modelPort;
            this.port = port;
            this.portTypeOperation = portTypeOperation;
            this.bindingOperation = bindingOperation;
            this.soapBinding = soapBinding;
            this.document = document;
            this.hasOverloadedOperations = hasOverloadedOperations;
            this.headers = headers;
        }

        public Port modelPort;
        public com.sun.tools.ws.wsdl.document.Port port;
        public com.sun.tools.ws.wsdl.document.Operation portTypeOperation;
        public BindingOperation bindingOperation;
        public SOAPBinding soapBinding;
        public WSDLDocument document;
        public boolean hasOverloadedOperations;
        public Map headers;

        // additional data
        public Operation operation;
        public String uniqueOperationName;
    }

    public static class WSDLExceptionInfo {
        public String exceptionType;
        public QName wsdlMessage;
        public String wsdlMessagePartName;
        public HashMap constructorOrder; // mapping of element name to
                                             // constructor order (of type Integer)
    };


    protected WSDLParser parser;
    protected WSDLDocument document;
    protected HashSet hSet;
}
