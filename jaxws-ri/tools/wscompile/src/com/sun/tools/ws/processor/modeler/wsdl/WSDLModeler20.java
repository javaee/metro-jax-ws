/*
 * $Id: WSDLModeler20.java,v 1.32 2005-09-29 00:36:20 vivekp Exp $
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

import org.xml.sax.InputSource;

import com.sun.tools.ws.processor.ProcessorOptions;
import com.sun.tools.ws.processor.config.WSDLModelInfo;
import com.sun.tools.ws.processor.generator.GeneratorConstants;
import com.sun.tools.ws.processor.generator.SimpleToBoxedUtil;
import com.sun.tools.ws.processor.model.AsyncOperation;
import com.sun.tools.ws.processor.model.AsyncOperationType;
import com.sun.tools.ws.processor.model.Block;
import com.sun.tools.ws.processor.model.Fault;
import com.sun.tools.ws.processor.model.HeaderFault;
import com.sun.tools.ws.processor.model.Model;
import com.sun.tools.ws.processor.model.ModelException;
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
import com.sun.tools.ws.processor.model.java.JavaSimpleType;
import com.sun.tools.ws.processor.model.java.JavaStructureMember;
import com.sun.tools.ws.processor.model.java.JavaType;
import com.sun.tools.ws.processor.model.jaxb.*;
import com.sun.tools.ws.processor.modeler.JavaSimpleTypeCreator;
import com.sun.tools.ws.processor.modeler.ModelerException;
import com.sun.tools.ws.processor.modeler.ModelerUtils;
import com.sun.tools.ws.processor.util.ClassNameCollector;
import com.sun.tools.ws.processor.util.ProcessorEnvironment;
import com.sun.tools.ws.util.ClassNameInfo;
import com.sun.tools.ws.util.JAXWSUtils;
import com.sun.tools.ws.wsdl.document.Binding;
import com.sun.tools.ws.wsdl.document.BindingFault;
import com.sun.tools.ws.wsdl.document.BindingOperation;
import com.sun.tools.ws.wsdl.document.Kinds;
import com.sun.tools.ws.wsdl.document.Message;
import com.sun.tools.ws.wsdl.document.MessagePart;
import com.sun.tools.ws.wsdl.document.OperationStyle;
import com.sun.tools.ws.wsdl.document.PortType;
import com.sun.tools.ws.wsdl.document.WSDLConstants;
import com.sun.tools.ws.wsdl.document.WSDLDocument;
import com.sun.tools.ws.wsdl.document.mime.MIMEContent;
import com.sun.tools.ws.wsdl.document.jaxws.CustomName;
import com.sun.tools.ws.wsdl.document.jaxws.JAXWSBinding;
import com.sun.tools.ws.wsdl.document.schema.SchemaKinds;
import com.sun.tools.ws.wsdl.document.soap.*;
import com.sun.tools.ws.wsdl.framework.Entity;
import com.sun.tools.ws.wsdl.framework.Extensible;
import com.sun.tools.ws.wsdl.framework.Extension;
import com.sun.tools.ws.wsdl.framework.NoSuchEntityException;
import com.sun.tools.ws.wsdl.framework.ParseException;
import com.sun.tools.ws.wsdl.framework.ParserListener;
import com.sun.tools.ws.wsdl.framework.ValidationException;
import com.sun.tools.ws.wsdl.parser.SOAPEntityReferenceValidator;
import com.sun.tools.ws.wsdl.parser.WSDLParser;
import com.sun.tools.ws.wsdl.parser.WSDLParser20;
import com.sun.tools.xjc.api.S2JJAXBModel;
import com.sun.tools.xjc.api.TypeAndAnnotation;
import com.sun.tools.xjc.api.XJC;
import com.sun.xml.ws.util.xml.XmlUtil;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JType;


/**
 * The WSDLModeler processes a WSDL to create a Model.
 *
 * @author WS Development Team
 */
public class WSDLModeler20 extends WSDLModelerBase {

    /**
     * @param modelInfo
     * @param options
     */
    public WSDLModeler20(WSDLModelInfo modelInfo, Properties options) {
        super(modelInfo, options);
        _modelerUtils = new ModelerUtils();
        classNameCollector = new ClassNameCollector();
    }

    private ClassNameCollector classNameCollector;
    private boolean extensions = false;
    public Model buildModel() {
        try {

            parser = createWSDLParser();
            String wsdlLoc = JAXWSUtils.absolutize(JAXWSUtils.getFileOrURLName(_modelInfo.getLocation()));
            InputSource inputSource = new InputSource(wsdlLoc);
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

            extensions = Boolean.valueOf(_options.getProperty(ProcessorOptions.EXTENSION));
            
            useWSIBasicProfile = !extensions;
//                Boolean
//                    .valueOf(
//                        _options.getProperty(
//                            ProcessorOptions.USE_WSI_BASIC_PROFILE))
//                    .booleanValue();

            document =
                parser.parse(inputSource, useWSIBasicProfile);
            document.validateLocally();

            boolean validateWSDL =
                Boolean
                    .valueOf(
                        _options.getProperty(
                            ProcessorOptions.VALIDATE_WSDL_PROPERTY))
                    .booleanValue();
            if (validateWSDL) {
                document.validate(new SOAPEntityReferenceValidator());
            }


            Model model = internalBuildModel(document);
            //ClassNameCollector classNameCollector = new ClassNameCollector();
            classNameCollector.process(model);
            if (classNameCollector.getConflictingClassNames().isEmpty()) {
                return model;
            }
            // do another pass, this time with conflict resolution enabled
            model = internalBuildModel(document);
            classNameCollector.process(model);
            if (classNameCollector.getConflictingClassNames().isEmpty()) {
                // we're done
                return model;
            }
            // give up
            StringBuffer conflictList = new StringBuffer();
            boolean first = true;
            for (Iterator iter =
                classNameCollector.getConflictingClassNames().iterator();
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

        } catch (ModelException e) {
            throw new ModelerException((Exception)e);
        } catch (ParseException e) {
            throw new ModelerException((Exception)e);
        } catch (ValidationException e) {
            throw new ModelerException((Exception)e);
        }
    }

    private Model internalBuildModel(WSDLDocument document) {

        //build the jaxbModel to be used latter
        buildJAXBModel(document, _modelInfo, classNameCollector);

        QName modelName =
            new QName(
                document.getDefinitions().getTargetNamespaceURI(),
                document.getDefinitions().getName() == null
                    ? "model"
                    : document.getDefinitions().getName());
        Model model = new Model(modelName);
        model.setJAXBModel(getJAXBModelBuilder().getJAXBModel());
        
        // This fails with the changed classname (WSDLModeler to WSDLModeler11 etc.)
        // with this source comaptibility change the WSDL Modeler class name is changed. Right now hardcoding the
        // modeler class name to the same one being checked in WSDLGenerator.

        model.setProperty(
            ModelProperties.PROPERTY_MODELER_NAME,
            ModelProperties.WSDL_MODELER_NAME);

        _javaTypes = new JavaSimpleTypeCreator();
        _javaExceptions = new HashMap();
        _bindingNameToPortMap = new HashMap();

        // grab target namespace
        model.setTargetNamespaceURI(document.getDefinitions().getTargetNamespaceURI());

        setDocumentationIfPresent(model,
            document.getDefinitions().getDocumentation());

        boolean hasServices = document.getDefinitions().services().hasNext();
        if (hasServices) {
            for (Iterator iter = document.getDefinitions().services();
                iter.hasNext();
                ) {
                processService((com.sun.tools.ws.wsdl.document.Service)iter.next(),
                                model, document);
                hasServices = true;
            }
        } else {
            // emit a warning if there are no service definitions
            warn("wsdlmodeler.warning.noServiceDefinitionsFound");
        }

        return model;
    }


    /* (non-Javadoc)
     * @see WSDLModelerBase#processService(Service, Model, WSDLDocument)
     */
    protected void processService(com.sun.tools.ws.wsdl.document.Service wsdlService, Model model, WSDLDocument document) {
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
        if (!hasPorts) {
            // emit a warning if there are no ports
            warn("wsdlmodeler.warning.noPortsInService", wsdlService.getName());
        }else{
            model.addService(service);
        }
    }

    /* (non-Javadoc)
     * @see WSDLModelerBase#processPort(Port, Service, WSDLDocument)
     */
    protected boolean processPort(com.sun.tools.ws.wsdl.document.Port wsdlPort,
            Service service, WSDLDocument document) {
        try {
            QName portQName = getQNameOf(wsdlPort);
            Port port = new Port(portQName);

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
                warn("wsdlmodeler.warning.ignoringNonSOAPPort.noAddress", wsdlPort.getName());
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
                    _bindingNameToPortMap.get(bindingName);
                port.setOperations(existingPort.getOperations());
                port.setJavaInterface(existingPort.getJavaInterface());
                port.setStyle(existingPort.getStyle());
                port.setWrapped(existingPort.isWrapped());
            } else {
                // find out the SOAP binding extension, if any
                SOAPBinding soapBinding =
                    (SOAPBinding)getExtensionOfType(binding, SOAPBinding.class);
                
                if (soapBinding == null) {
                    soapBinding = 
                            (SOAPBinding)getExtensionOfType(binding, SOAP12Binding.class);
                    if (soapBinding == null) {
                        // cannot deal with non-SOAP ports
                        warn(
                            "wsdlmodeler.warning.ignoringNonSOAPPort",
                            wsdlPort.getName());
                        return false;
                    }
                    // we can only do soap1.2 if extensions are on
                    if (extensions) {
                        warn("wsdlmodeler.warning.port.SOAPBinding12", wsdlPort.getName());
                    } else {
                        fail("wsdlmodeler.warning.ignoringSOAPBinding12",
                                wsdlPort.getName());
                        return false;
                    }                                        
                }

                if (soapBinding.getTransport() == null
                    || (!soapBinding.getTransport().equals(
                        SOAPConstants.URI_SOAP_TRANSPORT_HTTP) && !soapBinding.getTransport().equals(
                        SOAP12Constants.URI_SOAP_TRANSPORT_HTTP))) {
                    // cannot deal with non-HTTP ports
                    warn(
                        "wsdlmodeler.warning.ignoringSOAPBinding.nonHTTPTransport",
                        wsdlPort.getName());
                    return false;
                }

                /**
                 * validate wsdl:binding uniqueness in style, e.g. rpclit or doclit
                 * ref: WSI BP 1.1 R 2705
                 */
                if(!validateWSDLBindingStyle(binding)){
                    if(extensions){
                        warn("wsdlmodeler.warning.port.SOAPBinding.mixedStyle", wsdlPort.getName());
                    }else{
                        fail("wsdlmodeler.warning.ignoringSOAPBinding.mixedStyle",
                                wsdlPort.getName());
                        return false;
                    }
                }

                port.setStyle(soapBinding.getStyle());
                boolean hasOverloadedOperations = false;
                Set<String> operationNames = new HashSet<String>();
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

                        for (Iterator iter2 = operations.iterator();iter2.hasNext();) {
                            com.sun.tools.ws.wsdl.document.Operation candidateOperation =
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
                                }
                                // got it!
                                found = true;
                                portTypeOperation = candidateOperation;
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
                    // emit a warning if there are no operations, except when its a provider port
                    warn("wsdlmodeler.warning.noOperationsInPort",
                        wsdlPort.getName());
                    return false;
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

            service.addPort(port);
            applyPortMethodCustomization(port, wsdlPort);
            applyWrapperStyleCustomization(port, binding.resolvePortType(document));

            // bug fix: 4923650
            setCurrentPort(null);

            return true;

        } catch (NoSuchEntityException e) {
            warn(e);
            // should not happen
            return false;
        }
    }


    /* (non-Javadoc)
     * @see WSDLModelerBase#processSOAPOperation()
     */
    protected Operation processSOAPOperation() {
        Operation operation =
            new Operation(new QName(null, info.bindingOperation.getName()));

        setDocumentationIfPresent(
            operation,
            info.portTypeOperation.getDocumentation());

        if (info.portTypeOperation.getStyle()
            != OperationStyle.REQUEST_RESPONSE
            && info.portTypeOperation.getStyle() != OperationStyle.ONE_WAY) {
            if(extensions){
                warn(
                    "wsdlmodeler.warning.ignoringOperation.notSupportedStyle",
                    info.portTypeOperation.getName());
                return null;
            }
            fail("wsdlmodeler.invalid.operation.notSupportedStyle",
                    new Object[]{info.portTypeOperation.getName(),
                    info.port.resolveBinding(document).resolvePortType(document)});
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
            if (soapRequestBody.isEncoded()) {
                throw new ModelerException("wsdlmodeler20.rpcenc.not.supported");
            }
            return processLiteralSOAPOperation(StyleAndUse.RPC_LITERAL);
        }
        // document style
        return processLiteralSOAPOperation(StyleAndUse.DOC_LITERAL);
    }

    /* (non-Javadoc)
     * @see WSDLModelerBase#createWSDLParser()
     */
    protected WSDLParser createWSDLParser() {
        return new WSDLParser20(_modelInfo);
    }

    protected Operation processLiteralSOAPOperation(StyleAndUse styleAndUse){
        //returns false if the operation name is not acceptable
        if(!applyOperationNameCustomization())
            return null;

        boolean isRequestResponse = info.portTypeOperation.getStyle() == OperationStyle.REQUEST_RESPONSE;
        Request request = new Request();
        Response response = new Response();
        info.operation.setUse(SOAPUse.LITERAL);
        SOAPBody soapRequestBody = getSOAPRequestBody();
        if(soapRequestBody != null && isRequestMimeMultipart()) {
            request.setProperty(
                    MESSAGE_HAS_MIME_MULTIPART_RELATED_BINDING,
            "true");
        }

        if((StyleAndUse.DOC_LITERAL == styleAndUse) && (soapRequestBody.getNamespace() != null)){
            warn("wsdlmodeler.warning.r2716", new Object[]{"soapbind:body", info.bindingOperation.getName()});
        }

        Message inputMessage = getInputMessage();
        setJavaOperationNameProperty(inputMessage);

        // code added for 109
        if (inputMessage != null)
            request.setProperty(ModelProperties.PROPERTY_WSDL_MESSAGE_NAME, getQNameOf(inputMessage));


        SOAPBody soapResponseBody = null;
        Message outputMessage = null;
        if (isRequestResponse) {
            soapResponseBody = getSOAPResponseBody();
            if (isOperationDocumentLiteral(styleAndUse) && (soapResponseBody.getNamespace() != null)) {
                warn("wsdlmodeler.warning.r2716", new Object[]{"soapbind:body", info.bindingOperation.getName()});
            }
            outputMessage = getOutputMessage();
            if (outputMessage != null)
                response.setProperty(ModelProperties.PROPERTY_WSDL_MESSAGE_NAME, getQNameOf(outputMessage));

            if(soapResponseBody != null && isResponseMimeMultipart())
                response.setProperty(MESSAGE_HAS_MIME_MULTIPART_RELATED_BINDING, "true");

        }

        //ignore operation if there are more than one root part
        if(!validateMimeParts(getMimeParts(info.bindingOperation.getInput())) ||
                !validateMimeParts(getMimeParts(info.bindingOperation.getOutput())))
            return null;


        if(!validateBodyParts(info.bindingOperation)){
            // BP 1.1
            // R2204   A document-literal binding in a DESCRIPTION MUST refer, in each of its soapbind:body element(s),
            // only to wsdl:part element(s) that have been defined using the element attribute.

            // R2203   An rpc-literal binding in a DESCRIPTION MUST refer, in its soapbind:body element(s),
            // only to wsdNl:part element(s) that have been defined using the type attribute.
            if(isOperationDocumentLiteral(styleAndUse))
                if(extensions)
                    warn("wsdlmodeler.warning.ignoringOperation.cannotHandleTypeMessagePart", info.portTypeOperation.getName());
                else
                    fail("wsdlmodeler.invalid.doclitoperation", info.portTypeOperation.getName());
            else if(isOperationRpcLiteral(styleAndUse)) {
                if(extensions)
                    warn("wsdlmodeler.warning.ignoringOperation.cannotHandleElementMessagePart", info.portTypeOperation.getName());
                else
                    fail("wsdlmodeler.invalid.rpclitoperation", info.portTypeOperation.getName());
            }
            return null;
        }

        //check incase of rpclit if operation is not NCName
        if(StyleAndUse.RPC_LITERAL == styleAndUse){
            String opName = info.operation.getName().getLocalPart();
            if(opName.contains(":")){
                //warn("wsdlmodeler.warning.ignoringOperation.notNCName", new Object[]{info.operation.getName().getLocalPart(), ":"});
                //return null;
            }
        }

        // Process parameterOrder and get the parameterList
        Map<String, QName> inputParameterMap = new HashMap<String, QName>();
        Map<String, QName> outputParameterMap = new HashMap<String,QName>();
        String resultParameterName = null;
        StringBuffer result = new StringBuffer();
        java.util.List<String> parameterList = getParameterOrder(result, inputParameterMap, outputParameterMap);
        if (result.length() > 0)
            resultParameterName = result.toString();

        //binding is invalid in the wsdl, ignore the operation.
        if(!setMessagePartsBinding(styleAndUse))
            return null;

        List<Parameter> inParameters = null;
        List<Parameter> outParameters = null;

        boolean unwrappable = isUnwrappable();
        info.operation.setWrapped(unwrappable);
        if(isOperationDocumentLiteral(styleAndUse)){
            inParameters = getRequestParameters(request, parameterList);
            outParameters = getResponseParameters(response);
        }else if(isOperationRpcLiteral(styleAndUse)){
            String operationName = info.bindingOperation.getName();
            Block reqBlock = null;
            if (inputMessage != null) {
                QName name = new QName(getRequestNamespaceURI(soapRequestBody), operationName);
                RpcLitStructure rpcStruct = new RpcLitStructure(name, getJAXBModelBuilder().getJAXBModel());
                rpcStruct.setJavaType(new JavaSimpleType("com.sun.xml.ws.encoding.jaxb.RpcLitPayload", null));
                reqBlock = new Block(name, rpcStruct);
                request.addBodyBlock(reqBlock);
            }

            Block resBlock = null;
            if (isRequestResponse && outputMessage != null) {
                QName name = new QName(getResponseNamespaceURI(soapResponseBody), operationName + "Response");
                RpcLitStructure rpcStruct = new RpcLitStructure(name, getJAXBModelBuilder().getJAXBModel());
                rpcStruct.setJavaType(new JavaSimpleType("com.sun.xml.ws.encoding.jaxb.RpcLitPayload", null));
                resBlock = new Block(name, rpcStruct);
                response.addBodyBlock(resBlock);
            }
            inParameters = createRpcLitRequestParameters(request, parameterList, reqBlock);
            outParameters = createRpcLitResponseParameters(response, resBlock);
        }

        //refresh parameterList for unwrap case
        Map<String, String> unwrappedInputParameterMap = new HashMap<String, String>();
        Map<String, String> unwrappedOutputParameterMap = new HashMap<String, String>();
        if(unwrappable){
            StringBuffer unwrappedResult = new StringBuffer();
            java.util.List<String> unwrappedParameterList = processUnwrappedParameterOrder(unwrappedResult,
                    unwrappedInputParameterMap, unwrappedOutputParameterMap);
            if(unwrappedResult.length() > 0)
                resultParameterName = unwrappedResult.toString();
            else
                resultParameterName = null;

            parameterList.clear();
            parameterList.addAll(unwrappedParameterList);
        }

        if(!validateParameterName(inParameters, outParameters, resultParameterName)) {
            return null;
        }

        if (resultParameterName == null) {
            // this is ugly, but we need to save information about the return type
            // being void at this stage, so that when we later create a Java interface
            // for the port this operation belongs to, we'll do the right thing
            info.operation.setProperty(OPERATION_HAS_VOID_RETURN_TYPE, "true");
        }else{
            Parameter resultParameter = ModelerUtils.getParameter(resultParameterName, outParameters);
            if(resultParameter == null){
                if(extensions)
                    warn("wsdlmodeler.warning.ignoringOperation.partNotFound", new Object[]{info.operation.getName().getLocalPart(), resultParameterName});
                else
                    fail("wsdlmodeler.error.partNotFound", new Object[]{info.operation.getName().getLocalPart(), resultParameterName});
                return null;
            }
            response.addParameter(resultParameter);
            info.operation.setProperty(WSDL_RESULT_PARAMETER, resultParameter.getName());
            resultParameter.setParameterOrderPosition(-1);
        }

        // create a definitive list of parameters to match what we'd like to get
        // in the java interface (which is generated much later), parameterOrder
        List<String> definitiveParameterList = new ArrayList<String>();
        int parameterOrderPosition = 0;
        for (String name: parameterList) {
            boolean isInput = false;
            boolean isOutput = false;
            if(unwrappable){
                isInput = unwrappedInputParameterMap.containsKey(name);
                isOutput = unwrappedOutputParameterMap.containsKey(name);
                if(isInput && isOutput && !unwrappedInputParameterMap.get(name).equals(unwrappedOutputParameterMap.get(name)))
                    throw new ModelerException("wsdlmodeler.invalid.parameter.differentTypes",
                        new Object[] {name, info.operation.getName().getLocalPart()});
            }else{
                isInput = inputParameterMap.containsKey(name);
                isOutput = outputParameterMap.containsKey(name);
                if(isInput && isOutput && !inputParameterMap.get(name).equals(outputParameterMap.get(name)))
                    throw new ModelerException("wsdlmodeler.invalid.parameter.differentTypes",
                        new Object[] {name, info.operation.getName().getLocalPart()});
            }

            Parameter inParameter = null;
            Parameter outParameter = null;
            if(isInput){
                inParameter = ModelerUtils.getParameter(name, inParameters);
                if(inParameter == null){
                    if(extensions)
                        warn("wsdlmodeler.warning.ignoringOperation.partNotFound", new Object[]{info.operation.getName().getLocalPart(), name});
                    else
                        fail("wsdlmodeler.error.partNotFound", new Object[]{info.operation.getName().getLocalPart(), name});
                    return null;
                }
                request.addParameter(inParameter);
                inParameter.setParameterOrderPosition(parameterOrderPosition);
                definitiveParameterList.add(name);
            }

            if(isOutput){
                outParameter = ModelerUtils.getParameter(name, outParameters);
                if(outParameter == null){
                    if(extensions)
                        warn("wsdlmodeler.warning.ignoringOperation.partNotFound", new Object[]{info.operation.getName().getLocalPart(), name});
                    else
                        fail("wsdlmodeler.error.partNotFound", new Object[]{info.operation.getName().getLocalPart(), name});
                    return null;
                }

                response.addParameter(outParameter);
                outParameter.setParameterOrderPosition(parameterOrderPosition);

                //for different mime types that otherwise qualifies as INOUT, map the java type as DataHandler
                if((inParameter!= null) && enableMimeContent()){
                    TypeAndAnnotation inTa = inParameter.getType().getJavaType().getType().getTypeAnn();
                    TypeAndAnnotation outTa = outParameter.getType().getJavaType().getType().getTypeAnn();
                    if(inTa.equals(outTa)){
                        String javaType = "javax.activation.DataHandler";
                        inParameter.setTypeName(javaType);
                        outParameter.setTypeName(javaType);                        
                        S2JJAXBModel jaxbModel = getJAXBModelBuilder().getJAXBModel().getS2JJAXBModel();
                        JCodeModel cm = jaxbModel.generateCode(null,
                                    new ConsoleErrorReporter(getEnvironment(), false));
                        JType jt= cm.ref(javaType);

                        JAXBTypeAndAnnotation jaxbTa = inParameter.getType().getJavaType().getType();
                        jaxbTa.setType(jt);

                        jaxbTa = outParameter.getType().getJavaType().getType();
                        jaxbTa.setType(jt);
                    }
                }

                if(inParameter == null){
                    definitiveParameterList.add(name);
                }else if(isOperationDocumentLiteral(styleAndUse)){
                    //detect inout part, for doclit, the part name and same element name
                    QName inElementName = ((JAXBType)inParameter.getType()).getName();
                    QName outElementName = ((JAXBType)outParameter.getType()).getName();
                    String inJavaType = inParameter.getTypeName();
                    String outJavaType = outParameter.getTypeName();
                    if(inElementName.getLocalPart().equals(outElementName.getLocalPart()) && inJavaType.equals(outJavaType)) {
                        inParameter.setLinkedParameter(outParameter);
                        outParameter.setLinkedParameter(inParameter);
                        //its in/out parameter, input and output parameter have the same order position.
                        outParameter.setParameterOrderPosition(inParameter.getParameterOrderPosition());
                        outParameter.setCustomName(inParameter.getCustomName());

                    }
                }else if(isOperationRpcLiteral(styleAndUse)){
                    inParameter.setLinkedParameter(outParameter);
                    outParameter.setLinkedParameter(inParameter);
                    outParameter.setCustomName(inParameter.getCustomName());
                }
            }
            parameterOrderPosition++;
        }

        info.operation.setRequest(request);

        if (isRequestResponse) {
            info.operation.setResponse(response);
        }

        // faults with duplicate names
        Set duplicateNames = getDuplicateFaultNames();

        // handle soap:fault
        handleLiteralSOAPFault(response, duplicateNames);

        // handle headers
//        boolean enableAdditionalHeader = enableAdditionalHeaderMapping();
//
//        if (enableAdditionalHeader) {
//            handleLiteralSOAPHeaders(
//                    request,
//                    response,
//                    getHeaderPartsNotFromMessage(getInputMessage(), true).iterator(),
//                    duplicateNames,
//                    definitiveParameterList,
//                    true);
//            if (isRequestResponse) {
//                handleLiteralSOAPHeaders(
//                        request,
//                        response,
//                        getHeaderPartsNotFromMessage(getOutputMessage(), false).iterator(),
//                        duplicateNames,
//                        definitiveParameterList,
//                        false);
//            }
//        }
        //process all the headerfaults
//        handleHeaderFaults(info, response);

        info.operation.setProperty(
                WSDL_PARAMETER_ORDER,
                definitiveParameterList);

        //set Async property
        Binding binding = info.port.resolveBinding(document);
        PortType portType = binding.resolvePortType(document);
        if(isAsync(portType, info.portTypeOperation)){
            addAsyncOperations(info.operation, styleAndUse);
        }

        return info.operation;
    }

    /**
     * @param inParameters
     * @param outParameters
     * @param resultParameterName
     * @return
     */
    private boolean validateParameterName(List<Parameter> inParameters, List<Parameter> outParameters, String resultParameterName) {
        Message msg = getInputMessage();
        for(Parameter param : inParameters){
            if(param.getCustomName() != null){
                if(getEnvironment().getNames().isJavaReservedWord(param.getCustomName())){
                    if(extensions)
                        warn("wsdlmodeler.warning.ignoringOperation.javaReservedWordNotAllowed.customName",
                                new Object[]{info.operation.getName(), param.getCustomName()});
                    else
                        fail("wsdlmodeler.invalid.operation.javaReservedWordNotAllowed.customName",
                                new Object[]{info.operation.getName(), param.getCustomName()});
                    return false;
                }
                return true;
            }
            //process doclit wrapper style
            if(param.isEmbedded() && !(param.getBlock().getType() instanceof RpcLitStructure)){
                if(getEnvironment().getNames().isJavaReservedWord(param.getName())){
                    if(extensions)
                        warn("wsdlmodeler.warning.ignoringOperation.javaReservedWordNotAllowed.wrapperStyle", new Object[]{info.operation.getName(), param.getName(), param.getBlock().getName()});
                    else
                        fail("wsdlmodeler.invalid.operation.javaReservedWordNotAllowed.wrapperStyle", new Object[]{info.operation.getName(), param.getName(), param.getBlock().getName()});
                    return false;
                }
            }else{
                //non-wrapper style and rpclit
                if(getEnvironment().getNames().isJavaReservedWord(param.getName())){
                    if(extensions)
                        warn("wsdlmodeler.warning.ignoringOperation.javaReservedWordNotAllowed.nonWrapperStyle", new Object[]{info.operation.getName(), msg.getName(), param.getName()});
                    else
                        fail("wsdlmodeler.invalid.operation.javaReservedWordNotAllowed.nonWrapperStyle", new Object[]{info.operation.getName(), msg.getName(), param.getName()});
                    return false;
                }
            }
        }

        boolean isRequestResponse = info.portTypeOperation.getStyle() == OperationStyle.REQUEST_RESPONSE;
        if(isRequestResponse){
            msg = getOutputMessage();
            for(Parameter param : outParameters){
                if(param.getCustomName() != null){
                    if(getEnvironment().getNames().isJavaReservedWord(param.getCustomName())){
                        if(extensions)
                            warn("wsdlmodeler.warning.ignoringOperation.javaReservedWordNotAllowed.customName",
                                    new Object[]{info.operation.getName(), param.getCustomName()});
                        else
                            fail("wsdlmodeler.invalid.operation.javaReservedWordNotAllowed.customName",
                                    new Object[]{info.operation.getName(), param.getCustomName()});
                        return false;
                    }
                    return true;
                }
                //process doclit wrapper style
                if(param.isEmbedded() && !(param.getBlock().getType() instanceof RpcLitStructure)){
                    if(resultParameterName != null && param.getName().equals(resultParameterName))
                        continue;
                    if(!param.getName().equals("return") && getEnvironment().getNames().isJavaReservedWord(param.getName())){
                        if(extensions)
                            warn("wsdlmodeler.warning.ignoringOperation.javaReservedWordNotAllowed.wrapperStyle",
                                    new Object[]{info.operation.getName(), param.getName(), param.getBlock().getName()});
                        else
                            fail("wsdlmodeler.invalid.operation.javaReservedWordNotAllowed.wrapperStyle",
                                    new Object[]{info.operation.getName(), param.getName(), param.getBlock().getName()});
                        return false;
                    }
                }else{
                    if(resultParameterName != null && param.getName().equals(resultParameterName))
                        continue;

                    //non-wrapper style and rpclit
                    if(getEnvironment().getNames().isJavaReservedWord(param.getName())){
                        if(extensions)
                            warn("wsdlmodeler.warning.ignoringOperation.javaReservedWordNotAllowed.nonWrapperStyle", new Object[]{info.operation.getName(), msg.getName(), param.getName()});
                        else
                            fail("wsdlmodeler.invalid.operation.javaReservedWordNotAllowed.nonWrapperStyle",
                                    new Object[]{info.operation.getName(), msg.getName(), param.getName()});
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * @return
     */
//    private boolean enableAdditionalHeaderMapping() {
//        //first we look at binding operation
//        JAXWSBinding jaxrpcCustomization = (JAXWSBinding)getExtensionOfType(info.bindingOperation, JAXWSBinding.class);
//        Boolean additionalHeader = (jaxrpcCustomization != null)?jaxrpcCustomization.isEnableAdditionalHeaderMapping():null;
//        if(additionalHeader != null)
//            return additionalHeader;
//
//        //then in wsdl:binding
//        Binding binding = info.port.resolveBinding(info.document);
//        jaxrpcCustomization = (JAXWSBinding)getExtensionOfType(binding, JAXWSBinding.class);
//        additionalHeader = (jaxrpcCustomization != null)?jaxrpcCustomization.isEnableAdditionalHeaderMapping():null;
//        if(additionalHeader != null)
//            return additionalHeader;
//
//        //at last look in wsdl:definitions
//        jaxrpcCustomization = (JAXWSBinding)getExtensionOfType(info.document.getDefinitions(), JAXWSBinding.class);
//        additionalHeader = (jaxrpcCustomization != null)?jaxrpcCustomization.isEnableAdditionalHeaderMapping():null;
//        if(additionalHeader != null)
//            return additionalHeader;
//        return false;
//
//    }

    /**
     * @return
     */
    private boolean enableMimeContent() {
        //first we look at binding operation
        JAXWSBinding jaxwsCustomization = (JAXWSBinding)getExtensionOfType(info.bindingOperation, JAXWSBinding.class);
        Boolean mimeContentMapping = (jaxwsCustomization != null)?jaxwsCustomization.isEnableMimeContentMapping():null;
        if(mimeContentMapping != null)
            return mimeContentMapping;

        //then in wsdl:binding
        Binding binding = info.port.resolveBinding(info.document);
        jaxwsCustomization = (JAXWSBinding)getExtensionOfType(binding, JAXWSBinding.class);
        mimeContentMapping = (jaxwsCustomization != null)?jaxwsCustomization.isEnableMimeContentMapping():null;
        if(mimeContentMapping != null)
            return mimeContentMapping;

        //at last look in wsdl:definitions
        jaxwsCustomization = (JAXWSBinding)getExtensionOfType(info.document.getDefinitions(), JAXWSBinding.class);
        mimeContentMapping = (jaxwsCustomization != null)?jaxwsCustomization.isEnableMimeContentMapping():null;
        if(mimeContentMapping != null)
            return mimeContentMapping;
        return false;
    }

    /**
     *
     */
    private boolean applyOperationNameCustomization() {
        JAXWSBinding jaxwsCustomization = (JAXWSBinding)getExtensionOfType(info.portTypeOperation, JAXWSBinding.class);
        String operationName = (jaxwsCustomization != null)?((jaxwsCustomization.getMethodName() != null)?jaxwsCustomization.getMethodName().getName():null):null;
        if(operationName != null){
            if(getEnvironment().getNames().isJavaReservedWord(operationName)){
                if(extensions)
                    warn("wsdlmodeler.warning.ignoringOperation.javaReservedWordNotAllowed.customizedOperationName", new Object[]{info.operation.getName(), operationName});
                else
                    fail("wsdlmodeler.invalid.operation.javaReservedWordNotAllowed.customizedOperationName", new Object[]{info.operation.getName(), operationName});
                return false;
            }

            //Name(new QName(info.operation.getName().getNamespaceURI(), operationName));
//            info.operation.setUniqueName(operationName);
            info.operation.setCustomizedName(operationName);
        }

        if(getEnvironment().getNames().isJavaReservedWord(info.operation.getJavaMethodName())){
            if(extensions)
                warn("wsdlmodeler.warning.ignoringOperation.javaReservedWordNotAllowed.operationName", new Object[]{info.operation.getName()});
            else
                fail("wsdlmodeler.invalid.operation.javaReservedWordNotAllowed.operationName", new Object[]{info.operation.getName()});
            return false;
        }
        return true;
    }

    protected String getAsyncOperationName(Operation operation){
        String name = operation.getCustomizedName();
        if(name == null)
            name = operation.getUniqueName();
        return name;
    }

    /**
     * @param styleAndUse
     */
    private void addAsyncOperations(Operation syncOperation, StyleAndUse styleAndUse) {
        Operation operation = createAsyncOperation(syncOperation, styleAndUse, AsyncOperationType.POLLING);
        if(operation != null)
            info.modelPort.addOperation(operation);

        operation = createAsyncOperation(syncOperation, styleAndUse, AsyncOperationType.CALLBACK);
        if(operation != null)
            info.modelPort.addOperation(operation);
    }

    /**
     *
     * @param syncOperation
     * @param styleAndUse
     * @param asyncType
     * @return
     */
    private Operation createAsyncOperation(Operation syncOperation, StyleAndUse styleAndUse, AsyncOperationType asyncType) {
        boolean isRequestResponse = info.portTypeOperation.getStyle() == OperationStyle.REQUEST_RESPONSE;
        if(!isRequestResponse)
            return null;
        Request request = new Request();
        Response response = new Response();

        //create async operations
        AsyncOperation operation = new AsyncOperation(info.operation);

        //creation the async operation name: operationName+Async or customized name
        //operation.setName(new QName(operation.getName().getNamespaceURI(), getAsyncOperationName(info.portTypeOperation, operation)));
        if(asyncType.equals(AsyncOperationType.CALLBACK))
            operation.setUniqueName(info.operation.getUniqueName()+"_async_callback");
        else if(asyncType.equals(AsyncOperationType.POLLING))
            operation.setUniqueName(info.operation.getUniqueName()+"_async_polling");

        operation.setAsyncType(asyncType);
        operation.setSOAPAction(info.operation.getSOAPAction());
        boolean unwrappable = info.operation.isWrapped();
        operation.setWrapped(unwrappable);
        SOAPBody soapRequestBody = getSOAPRequestBody();

        Message inputMessage = getInputMessage();
        setJavaOperationNameProperty(inputMessage);

        // code added for 109
        if (inputMessage != null)
            request.setProperty(ModelProperties.PROPERTY_WSDL_MESSAGE_NAME, getQNameOf(inputMessage));


        SOAPBody soapResponseBody = null;
        Message outputMessage = null;
        if (isRequestResponse) {
            soapResponseBody = getSOAPResponseBody();
            outputMessage = getOutputMessage();
        }

        // Process parameterOrder and get the parameterList
        String resultParameterName = null;
        java.util.List<String> parameterList = getAsynParameterOrder();

        List<Parameter> inParameters = null;
        List<Parameter> outParameters = null;
        //boolean unwrappable = isUnwrappable();


        if(isOperationDocumentLiteral(styleAndUse)){
            inParameters = getRequestParameters(request, parameterList);
            // outParameters = getResponseParameters(response);
            // re-create parameterList with unwrapped parameters
            if(unwrappable){
                List<String> unwrappedParameterList = new ArrayList<String>();
                if(inputMessage != null){
                    Iterator<MessagePart> parts = inputMessage.parts();
                    if(parts.hasNext()){
                        MessagePart part = parts.next();
                        JAXBType jaxbType = getJAXBType(part.getDescriptor());
                        List<JAXBProperty> memberList = jaxbType.getWrapperChildren();
                        Iterator<JAXBProperty> props = memberList.iterator();
                        while(props.hasNext()){
                            JAXBProperty prop = props.next();
                            unwrappedParameterList.add(prop.getElementName().getLocalPart());
                        }
                    }
                }

                parameterList.clear();
                parameterList.addAll(unwrappedParameterList);
            }
        }else if(isOperationRpcLiteral(styleAndUse)){
            String operationName = info.bindingOperation.getName();
            Block reqBlock = null;
            if (inputMessage != null) {
                QName name = new QName(getRequestNamespaceURI(soapRequestBody), operationName);
                RpcLitStructure rpcStruct = new RpcLitStructure(name, getJAXBModelBuilder().getJAXBModel());
                rpcStruct.setJavaType(new JavaSimpleType("com.sun.xml.ws.encoding.jaxb.RpcLitPayload", null));
                reqBlock = new Block(name, rpcStruct);
                request.addBodyBlock(reqBlock);
            }
            inParameters = createRpcLitRequestParameters(request, parameterList, reqBlock);
        }

        // add response blocks, we dont need to create respnse parameters, just blocks will be fine, lets
        // copy them from sync optraions
        //copy the response blocks from the sync operation
        Iterator<Block> blocks = info.operation.getResponse().getBodyBlocks();

        while(blocks.hasNext()){
            response.addBodyBlock(blocks.next());
        }

        blocks = info.operation.getResponse().getHeaderBlocks();
        while(blocks.hasNext()){
            response.addHeaderBlock(blocks.next());
        }

        blocks = info.operation.getResponse().getAttachmentBlocks();
        while(blocks.hasNext()){
            response.addAttachmentBlock(blocks.next());
        }

        List<MessagePart> outputParts = outputMessage.getParts();

        // handle headers
//        boolean enableAdditionalHeader = enableAdditionalHeaderMapping();

//        int numOfOutMsgParts = outputParts.size() + ((enableAdditionalHeader)?getHeaderParts(false).size():0);
        int numOfOutMsgParts = outputParts.size();

        if(isRequestResponse){
            if(numOfOutMsgParts == 1){
                MessagePart part = outputParts.get(0);
                if(isOperationDocumentLiteral(styleAndUse)){
                    JAXBType type = getJAXBType(part.getDescriptor());
                    operation.setResponseBean(type);
                }else if(isOperationRpcLiteral(styleAndUse)){
                    String operationName = info.bindingOperation.getName();
                    Block resBlock = null;
                    if (isRequestResponse && outputMessage != null) {
                        resBlock = info.operation.getResponse().getBodyBlocksMap().get(new QName(getResponseNamespaceURI(soapResponseBody),
                                operationName + "Response"));
                    }
                    RpcLitStructure resBean = (resBlock == null) ? null : (RpcLitStructure)resBlock.getType();
                    List<RpcLitMember> members = resBean.getRpcLitMembers();

                    operation.setResponseBean(members.get(0));
                }
            }else{
                //create response bean
                //String nspace = document.getDefinitions().getTargetNamespaceURI()+"?"+info.port.resolveBinding(document).resolvePortType(document).getName()+"?" + info.portTypeOperation.getName();
                String nspace = "";
                QName responseBeanName = new QName(nspace,getAsyncOperationName(info.operation) +"Response");
                JAXBType responseBeanType = getJAXBType(responseBeanName);
                operation.setResponseBean(responseBeanType);
            }
        }
        QName respBeanName = new QName(soapResponseBody.getNamespace(),getAsyncOperationName(info.operation)+"Response");
        Block block = new Block(respBeanName, operation.getResponseBeanType());
        JavaType respJavaType = operation.getResponseBeanJavaType();
        JAXBType respType = new JAXBType(respBeanName, respJavaType);
        Parameter respParam = ModelerUtils.createParameter(info.operation.getName()+"Response", respType, block);
        respParam.setParameterOrderPosition(-1);
        response.addParameter(respParam);
        operation.setProperty(WSDL_RESULT_PARAMETER, respParam.getName());


        List<String> definitiveParameterList = new ArrayList<String>();
        int parameterOrderPosition = 0;
        for (String name: parameterList) {
            Parameter inParameter = null;

            inParameter = ModelerUtils.getParameter(name, inParameters);
            if(inParameter == null){
                if(extensions)
                    warn("wsdlmodeler.warning.ignoringOperation.partNotFound", new Object[]{info.operation.getName().getLocalPart(), name});
                else
                    fail("wsdlmodeler.error.partNotFound", new Object[]{info.operation.getName().getLocalPart(), name});
                return null;
            }
            request.addParameter(inParameter);
            inParameter.setParameterOrderPosition(parameterOrderPosition);
            definitiveParameterList.add(name);
            parameterOrderPosition++;
        }

        if (isRequestResponse) {
            operation.setResponse(response);
        }

        // faults with duplicate names
        Set duplicateNames = getDuplicateFaultNames();

//        if (enableAdditionalHeader) {
//            handleLiteralSOAPHeaders(
//                    request,
//                    response,
//                    getHeaderPartsNotFromMessage(getInputMessage(), true).iterator(),
//                    duplicateNames,
//                    definitiveParameterList,
//                    true);
//        }

        //  add callback handlerb Parameter to request
        if(operation.getAsyncType().equals(AsyncOperationType.CALLBACK)){
            JavaType cbJavaType = operation.getCallBackType();
            JAXBType callbackType = new JAXBType(respBeanName, cbJavaType);
            Parameter cbParam = ModelerUtils.createParameter("asyncHandler", callbackType, block);
            request.addParameter(cbParam);
        }

        operation.setRequest(request);

        return operation;
    }

    protected boolean isAsync(com.sun.tools.ws.wsdl.document.PortType portType, com.sun.tools.ws.wsdl.document.Operation wsdlOperation){
        //First look into wsdl:operation
        JAXWSBinding jaxwsCustomization = (JAXWSBinding)getExtensionOfType(wsdlOperation, JAXWSBinding.class);
        Boolean isAsync = (jaxwsCustomization != null)?jaxwsCustomization.isEnableAsyncMapping():null;

        if(isAsync != null)
            return isAsync;

        // then into wsdl:portType
        QName portTypeName = new QName(portType.getDefining().getTargetNamespaceURI(), portType.getName());
        if(portTypeName != null){
            jaxwsCustomization = (JAXWSBinding)getExtensionOfType(portType, JAXWSBinding.class);
            isAsync = (jaxwsCustomization != null)?jaxwsCustomization.isEnableAsyncMapping():null;
            if(isAsync != null)
                return isAsync;
        }

        //then wsdl:definitions
        jaxwsCustomization = (JAXWSBinding)getExtensionOfType(document.getDefinitions(), JAXWSBinding.class);
        isAsync = (jaxwsCustomization != null)?jaxwsCustomization.isEnableAsyncMapping():null;
        if(isAsync != null)
            return isAsync;
        return false;
    }

    protected void handleLiteralSOAPHeaders(Request request, Response response, Iterator headerParts, Set duplicateNames, List definitiveParameterList, boolean processRequest) {
        QName headerName = null;
        Block headerBlock = null;
        JAXBType jaxbType = null;
        int parameterOrderPosition = definitiveParameterList.size();
        while(headerParts.hasNext()){
            MessagePart part = (MessagePart)headerParts.next();
            headerName = part.getDescriptor();
            jaxbType = getJAXBType(headerName);
            headerBlock = new Block(headerName, jaxbType);
            Extensible ext;
            if(processRequest){
                ext = info.bindingOperation.getInput();
            }else{
                ext = info.bindingOperation.getOutput();
            }
            Message headerMessage = getHeaderMessage(part, ext);

            //J2EE for 109
            headerBlock.setProperty(ModelProperties.PROPERTY_WSDL_MESSAGE_NAME, getQNameOf(headerMessage));
            if(processRequest){
                request.addHeaderBlock(headerBlock);
            }else{
                response.addHeaderBlock(headerBlock);
            }

            Parameter parameter = ModelerUtils.createParameter(part.getName(), jaxbType, headerBlock);
            parameter.setParameterOrderPosition(parameterOrderPosition);
            setCustomizedParameterName(info.bindingOperation, part, parameter, false);
            if (processRequest && definitiveParameterList != null) {
                request.addParameter(parameter);
                definitiveParameterList.add(parameter.getName());
            } else {
                if (definitiveParameterList != null) {
                    for (Iterator iterInParams = definitiveParameterList.iterator(); iterInParams.hasNext();) {
                        String inParamName =
                            (String)iterInParams.next();
                        if (inParamName.equals(parameter.getName())) {
                            Parameter inParam = request.getParameterByName(inParamName);
                            parameter.setLinkedParameter(inParam);
                            inParam.setLinkedParameter(parameter);
                            //its in/out parameter, input and output parameter have the same order position.
                            parameter.setParameterOrderPosition(inParam.getParameterOrderPosition());
                        }
                    }
                    if (!definitiveParameterList.contains(parameter.getName())) {
                        definitiveParameterList.add(parameter.getName());
                    }
                }
                response.addParameter(parameter);
            }
            parameterOrderPosition++;
        }

    }

    protected void handleLiteralSOAPFault(Response response, Set duplicateNames){
        for (Iterator iter = info.bindingOperation.faults(); iter.hasNext();){
            BindingFault bindingFault = (BindingFault)iter.next();
            com.sun.tools.ws.wsdl.document.Fault portTypeFault = null;
            for(Iterator iter2 = info.portTypeOperation.faults(); iter2.hasNext();){
                com.sun.tools.ws.wsdl.document.Fault aFault =
                    (com.sun.tools.ws.wsdl.document.Fault)iter2.next();
                if(aFault.getName().equals(bindingFault.getName())){
                    if(portTypeFault != null){
                        // the WSDL document is invalid, a wsld:fault in a wsdl:operation of a portType can be bound only once
                        throw new ModelerException("wsdlmodeler.invalid.bindingFault.notUnique",
                                new Object[]{bindingFault.getName(), info.bindingOperation.getName()});
                    }
                    portTypeFault = aFault;
                }
            }

            // The WSDL document is invalid, the wsdl:fault in abstract operation is does not have any binding
            if(portTypeFault == null){
                throw new ModelerException("wsdlmodeler.invalid.bindingFault.notFound",
                        new Object[] {bindingFault.getName(), info.bindingOperation.getName()});

            }

            // wsdl:fault message name is used to create the java exception name later on
            String faultName = getFaultClassName(portTypeFault);
            Fault fault = new Fault(faultName);

            //get the soapbind:fault from wsdl:fault in the binding
            SOAPFault soapFault = (SOAPFault)getExtensionOfType(bindingFault, SOAPFault.class);

            // The WSDL document is invalid, can't have wsdl:fault without soapbind:fault
            if(soapFault == null){
                throw new ModelerException("wsdlmodeler.invalid.bindingFault.outputMissingSoapFault",
                    new Object[]{bindingFault.getName(), info.bindingOperation.getName()});
            }

            //the soapbind:fault must have use="literal" or no use attribute, in that case its assumed "literal"
            if(!soapFault.isLiteral()){
                if(extensions)
                warn("wsdlmodeler.warning.ignoringFault.notLiteral",
                    new Object[]{bindingFault.getName(), info.bindingOperation.getName()});
                else
                    fail("wsdlmodeler.invalid.operation.fault.notLiteral",
                            new Object[]{bindingFault.getName(), info.bindingOperation.getName()});
                continue;
            }

            // the soapFault name must be present
            if(soapFault.getName() == null){
                warn("wsdlmodeler.invalid.bindingFault.noSoapFaultName",
                    new Object[]{bindingFault.getName(), info.bindingOperation.getName()});
            }else if (!soapFault.getName().equals(bindingFault.getName())) {
                // the soapFault name must match bindingFault name
                warn("wsdlmodeler.invalid.bindingFault.wrongSoapFaultName",
                    new Object[]{soapFault.getName(), bindingFault.getName(), info.bindingOperation.getName()});
            }else if(soapFault.getNamespace() != null){
                // bug fix: 4852729
                warn("wsdlmodeler.warning.r2716r2726",
                    new Object[] { "soapbind:fault", soapFault.getName()});
            }

            String faultNamespaceURI = soapFault.getNamespace();
            if(faultNamespaceURI == null){
                faultNamespaceURI = portTypeFault.getMessage().getNamespaceURI();
            }

            com.sun.tools.ws.wsdl.document.Message faultMessage = portTypeFault.resolveMessage(info.document);
            Iterator iter2 = faultMessage.parts();
            if(!iter2.hasNext()){
                // the WSDL document is invalid
                throw new ModelerException("wsdlmodeler.invalid.bindingFault.emptyMessage",
                    new Object[]{bindingFault.getName(), faultMessage.getName()});
            }
            MessagePart faultPart = (MessagePart)iter2.next();
            QName faultQName = faultPart.getDescriptor();

            // Don't include fault messages with non-unique soap:fault names
            if (duplicateNames.contains(faultQName)) {
                warn("wsdlmodeler.duplicate.fault.soap.name",
                    new Object[] {bindingFault.getName(), info.portTypeOperation.getName(), faultPart.getName()});
                continue;
            }

            if (iter2.hasNext()) {
                // the WSDL document is invalid
                throw new ModelerException("wsdlmodeler.invalid.bindingFault.messageHasMoreThanOnePart",
                    new Object[]{bindingFault.getName(), faultMessage.getName()});
            }

            if (faultPart.getDescriptorKind() != SchemaKinds.XSD_ELEMENT) {
                throw new ModelerException("wsdlmodeler.invalid.message.partMustHaveElementDescriptor",
                    new Object[]{faultMessage.getName(), faultPart.getName()});
            }

            JAXBType jaxbType = getJAXBType(faultPart.getDescriptor());

            fault.setElementName(faultPart.getDescriptor());
            fault.setJavaMemberName(getEnvironment().getNames().getExceptionClassMemberName());

            Block faultBlock = new Block(faultQName, jaxbType);
            fault.setBlock(faultBlock);
            createParentFault(fault);
            createSubfaults(fault);
            if(!response.getFaultBlocksMap().containsKey(faultBlock.getName()))
                response.addFaultBlock(faultBlock);
            info.operation.addFault(fault);
        }
    }

    /**
     * @param portTypeFault
     * @return
     */
    private String getFaultClassName(com.sun.tools.ws.wsdl.document.Fault portTypeFault) {
        JAXWSBinding jaxwsBinding = (JAXWSBinding)getExtensionOfType(portTypeFault, JAXWSBinding.class);
        if(jaxwsBinding != null){
            CustomName className = jaxwsBinding.getClassName();
            if(className != null){
                return className.getName();
            }
        }
        return portTypeFault.getMessage().getLocalPart();
    }

    protected  boolean setMessagePartsBinding(StyleAndUse styleAndUse){
        SOAPBody inBody = getSOAPRequestBody();
        Message inMessage = getInputMessage();
        if(!setMessagePartsBinding(inBody, inMessage, styleAndUse, true))
            return false;

        if(isRequestResponse()){
            SOAPBody outBody = getSOAPResponseBody();
            Message outMessage = getOutputMessage();
            if(!setMessagePartsBinding(outBody, outMessage, styleAndUse, false))
                return false;
        }
        return true;
    }

    //returns false if the wsdl is invalid and operation should be ignored
    protected boolean setMessagePartsBinding(SOAPBody body, Message message, StyleAndUse styleAndUse, boolean isInput) {
        List<MessagePart> parts = new ArrayList<MessagePart>();

        //get Mime parts
        List<MessagePart> mimeParts = null;
        List<MessagePart> headerParts = null;
        List<MessagePart> bodyParts = getBodyParts(body, message);

        if(isInput){
            headerParts = getHeaderPartsFromMessage(message, isInput);
            mimeParts = getMimeContentParts(message, info.bindingOperation.getInput());
        }else{
            headerParts = getHeaderPartsFromMessage(message, isInput);
            mimeParts = getMimeContentParts(message, info.bindingOperation.getOutput());
        }

        //As of now WSDL MIME binding is not supported, so throw the exception when such binding is encounterd
//        if(mimeParts.size() > 0){
//            fail("wsdlmodeler.unsupportedBinding.mime", new Object[]{});
//        }

        //if soap:body parts attribute not there, then all unbounded message parts will
        // belong to the soap body
        if(bodyParts == null){
            bodyParts = new ArrayList<MessagePart>();
            for(Iterator<MessagePart> iter = message.parts();iter.hasNext();) {
                MessagePart mPart = iter.next();
                //Its a safe assumption that the parts in the message not belonging to header or mime will
                // belong to the body?
                if(mimeParts.contains(mPart) || headerParts.contains(mPart) || boundToFault(mPart.getName())){
                    //throw error that a part cant be bound multiple times, not ignoring operation, if there
                    //is conflict it will fail latter
                    if(extensions)
                        warn("wsdlmodeler.warning.bindingOperation.multiplePartBinding",
                                new Object[]{info.bindingOperation.getName(), mPart.getName()});
                    else
                        fail("wsdlmodeler.invalid.bindingOperation.multiplePartBinding",
                                new Object[]{info.bindingOperation.getName(), mPart.getName()});
                }
                bodyParts.add(mPart);
            }
        }

        //now build the final parts list with header, mime parts and body parts
        for(Iterator iter = message.parts();iter.hasNext();) {
            MessagePart mPart = (MessagePart)iter.next();
            if(mimeParts.contains(mPart)) {
                mPart.setBindingExtensibilityElementKind(MessagePart.WSDL_MIME_BINDING);
                parts.add(mPart);
            }else if(headerParts.contains(mPart)) {
                mPart.setBindingExtensibilityElementKind(MessagePart.SOAP_HEADER_BINDING);
                parts.add(mPart);
            }else if(bodyParts.contains(mPart)) {
                mPart.setBindingExtensibilityElementKind(MessagePart.SOAP_BODY_BINDING);
                parts.add(mPart);
            }else{
                mPart.setBindingExtensibilityElementKind(MessagePart.PART_NOT_BOUNDED);
            }
        }

        if(isOperationDocumentLiteral(styleAndUse) && bodyParts.size() > 1){
            if(extensions)
                warn("wsdlmodeler.warning.operation.MoreThanOnePartInMessage",
                            info.portTypeOperation.getName());
            else
                fail("wsdlmodeler.invalid.operation.MoreThanOnePartInMessage", info.portTypeOperation.getName());
            return false;
        }
        return true;
    }

    private boolean boundToFault(String partName){
        for (Iterator iter = info.bindingOperation.faults(); iter.hasNext();){
            BindingFault bindingFault = (BindingFault)iter.next();
            if(partName.equals(bindingFault.getName()))
                return true;
        }
        return false;
    }

    //get MessagePart(s) referenced by parts attribute of soap:body element
    private List<MessagePart> getBodyParts(SOAPBody body, Message message){
        String bodyParts = body.getParts();
        if (bodyParts != null) {
            List<MessagePart> partsList = new ArrayList<MessagePart>();
            StringTokenizer in = new StringTokenizer(bodyParts.trim(), " ");
            while (in.hasMoreTokens()) {
                String part = in.nextToken();
                MessagePart mPart = message.getPart(part);
                if (null == mPart) {
                    throw new ModelerException(
                        "wsdlmodeler.error.partsNotFound",
                        new Object[] { part, message.getName()});
                }
                mPart.setBindingExtensibilityElementKind(MessagePart.SOAP_BODY_BINDING);
                partsList.add(mPart);
            }
            return partsList;
        }
        return null;
    }

    private List<MessagePart> getHeaderPartsFromMessage(Message message, boolean isInput){
        List<MessagePart> headerParts = new ArrayList<MessagePart>();
        Iterator<MessagePart> parts = message.parts();
        List<MessagePart> headers = getHeaderParts(isInput);
        while(parts.hasNext()){
            MessagePart part = parts.next();
            if(headers.contains(part)){
                headerParts.add(part);
            }
        }
        return headerParts;
    }

    private Message getHeaderMessage(MessagePart part, Extensible ext) {
        Iterator<SOAPHeader> headers =  getHeaderExtensions(ext).iterator();
        while(headers.hasNext()){
            SOAPHeader header = headers.next();
            if (!header.isLiteral())
                continue;
            com.sun.tools.ws.wsdl.document.Message headerMessage = findMessage(header.getMessage(), info);
            if (headerMessage == null)
                continue;

            MessagePart headerPart = headerMessage.getPart(header.getPart());
            if(headerPart == part)
                return headerMessage;
        }
        return null;
    }

    private List<MessagePart> getHeaderPartsNotFromMessage(Message message, boolean isInput){
        List<MessagePart> headerParts = new ArrayList<MessagePart>();
        List<MessagePart> parts = message.getParts();
        Iterator<MessagePart> headers = getHeaderParts(isInput).iterator();
        while(headers.hasNext()){
            MessagePart part = headers.next();
            if(!parts.contains(part)){
                headerParts.add(part);
            }
        }
        return headerParts;
    }

    private List<MessagePart> getHeaderParts(boolean isInput) {
        Extensible ext;
        if(isInput){
            ext = info.bindingOperation.getInput();
        }else{
            ext = info.bindingOperation.getOutput();
        }

        List<MessagePart> parts = new ArrayList<MessagePart>();
        Iterator<SOAPHeader> headers =  getHeaderExtensions(ext).iterator();
        while(headers.hasNext()){
            SOAPHeader header = headers.next();
            if (!header.isLiteral()){
                fail("wsdlmodeler.invalid.header.notLiteral",
                        new Object[] {header.getPart(), info.bindingOperation.getName()});
            }

            if (header.getNamespace() != null){
                warn("wsdlmodeler.warning.r2716r2726",
                        new Object[]{"soapbind:header", info.bindingOperation.getName()});
            }
            com.sun.tools.ws.wsdl.document.Message headerMessage = findMessage(header.getMessage(), info);
            if (headerMessage == null){
                fail("wsdlmodeler.invalid.header.cant.resolve.message",
                        new Object[]{header.getMessage(), info.bindingOperation.getName()});
            }

            MessagePart part = headerMessage.getPart(header.getPart());
            if (part == null){
                fail("wsdlmodeler.invalid.header.notFound",
                        new Object[]{header.getPart(), info.bindingOperation.getName()});
            }
            if (part.getDescriptorKind() != SchemaKinds.XSD_ELEMENT) {
                fail("wsdlmodeler.invalid.header.message.partMustHaveElementDescriptor",
                        new Object[]{part.getName(), info.bindingOperation.getName()});
            }
            part.setBindingExtensibilityElementKind(MessagePart.SOAP_HEADER_BINDING);
            parts.add(part);
        }
        return parts;
    }

    private boolean isOperationDocumentLiteral(StyleAndUse styleAndUse){
        return StyleAndUse.DOC_LITERAL == styleAndUse;
    }

    private boolean isOperationRpcLiteral(StyleAndUse styleAndUse){
        return StyleAndUse.RPC_LITERAL == styleAndUse;
    }

    private Block getRpcLiteralBlock(QName elementName){
        JAXBType jaxbType = getJAXBType(elementName);
        JAXBStructuredType jaxbRequestType = ModelerUtils.createJAXBStructureType(jaxbType);
        return new Block(elementName, jaxbRequestType);
    }

    private List<Parameter> getResponseParameters(Response response) {
        Message outputMessage = getOutputMessage();
        boolean isRequestResponse = info.portTypeOperation.getStyle() == OperationStyle.REQUEST_RESPONSE;
        if(!isRequestResponse || (outputMessage != null && !outputMessage.parts().hasNext()))
            return new ArrayList<Parameter>();

        List<Parameter> outParameters = new ArrayList<Parameter>();
        //response
        QName resBodyName = null;
        Block resBlock = null;
        JAXBType jaxbResType = null;
        boolean doneSOAPBody = false;
        boolean unwrappable = isUnwrappable();
        if (isRequestResponse && outputMessage != null) {
            Iterator<MessagePart> parts = outputMessage.parts();
            while(parts.hasNext()){
                MessagePart part = parts.next();
                resBodyName = part.getDescriptor();
                jaxbResType = getJAXBType(part);
                if(unwrappable && isRequestResponse && jaxbResType.isUnwrappable()){
                    JAXBStructuredType jaxbResponseType = ModelerUtils.createJAXBStructureType(jaxbResType);
                    resBlock = new Block(resBodyName, jaxbResponseType);
                    if(ModelerUtils.isBoundToSOAPBody(part))
                        response.addBodyBlock(resBlock);
                    else if(ModelerUtils.isUnbound(part))
                        response.addUnboundBlock(resBlock);
                    outParameters = ModelerUtils.createUnwrappedParameters(jaxbResponseType, resBlock);
                    for(Parameter param: outParameters){
                        setCustomizedParameterName(info.portTypeOperation, part, param, unwrappable);
                    }
                }else if(isRequestResponse && outputMessage != null){
                    resBlock = new Block(resBodyName, jaxbResType);
                    if(ModelerUtils.isBoundToSOAPBody(part) && !doneSOAPBody){
                        response.addBodyBlock(resBlock);
                        doneSOAPBody = true;
                    }else if(ModelerUtils.isBoundToSOAPHeader(part)){
                        response.addHeaderBlock(resBlock);
                    }else if(ModelerUtils.isBoundToMimeContent(part)){
                        List<MIMEContent> mimeContents = getMimeContents(info.bindingOperation.getOutput(),
                                getOutputMessage(), part.getName());
                        jaxbResType = getAttachmentType(mimeContents, part);
                        //resBlock = new Block(new QName(part.getName()), jaxbResType);
                        resBlock = new Block(jaxbResType.getName(), jaxbResType);
                        response.addAttachmentBlock(resBlock);
                    }else if(ModelerUtils.isUnbound(part)){
                        response.addUnboundBlock(resBlock);
                    }
                    Parameter param = ModelerUtils.createParameter(part.getName(), jaxbResType, resBlock);
                    setCustomizedParameterName(info.portTypeOperation, part, param, false);
                    outParameters.add(param);
                }
            }
        }
        return outParameters;
    }

    /**
     * @param part
     * @return Returns a JAXBType object
     */
    private JAXBType getJAXBType(MessagePart part){
        JAXBType type=null;
        QName name = part.getDescriptor();
        if(part.getDescriptorKind().equals(SchemaKinds.XSD_ELEMENT)){
            type = getJAXBType(name);
        }else {
            S2JJAXBModel jaxbModel = getJAXBModelBuilder().getJAXBModel().getS2JJAXBModel();
            TypeAndAnnotation typeAnno = jaxbModel.getJavaType(name);
            if(typeAnno == null){
                fail("wsdlmodeler.jaxb.javatype.notfound", new Object[]{name, part.getName()});
            }
            JavaType javaType = new  JavaSimpleType(new JAXBTypeAndAnnotation(typeAnno));
            type = new JAXBType(new QName("", part.getName()), javaType);
        }
        return type;
    }

    private List<Parameter> getRequestParameters(Request request, List<String> parameterList) {
        Message inputMessage = getInputMessage();
        //there is no input message, return zero parameters
        if(inputMessage != null && !inputMessage.parts().hasNext())
            return new ArrayList<Parameter>();

        List<Parameter> inParameters = null;
        QName reqBodyName = null;
        Block reqBlock = null;
        JAXBType jaxbReqType = null;
        boolean unwrappable = isUnwrappable();
        boolean doneSOAPBody = false;
        //setup request parameters
        for(String inParamName: parameterList){
            MessagePart part = inputMessage.getPart(inParamName);
            if(part == null)
                continue;
            reqBodyName = part.getDescriptor();
            jaxbReqType = getJAXBType(part);
            if(unwrappable){
                //So build body and header blocks and set to request and response
                JAXBStructuredType jaxbRequestType = ModelerUtils.createJAXBStructureType(jaxbReqType);
                reqBlock = new Block(reqBodyName, jaxbRequestType);
                if(ModelerUtils.isBoundToSOAPBody(part)){
                    request.addBodyBlock(reqBlock);
                }else if(ModelerUtils.isUnbound(part)){
                    request.addUnboundBlock(reqBlock);
                }
                inParameters = ModelerUtils.createUnwrappedParameters(jaxbRequestType, reqBlock);
                for(Parameter param: inParameters){
                    setCustomizedParameterName(info.portTypeOperation, part, param, unwrappable);
                }
            }else{
                reqBlock = new Block(reqBodyName, jaxbReqType);
                if(ModelerUtils.isBoundToSOAPBody(part) && !doneSOAPBody){
                    doneSOAPBody = true;
                    request.addBodyBlock(reqBlock);
                }else if(ModelerUtils.isBoundToSOAPHeader(part)){
                    request.addHeaderBlock(reqBlock);
                }else if(ModelerUtils.isBoundToMimeContent(part)){
                    List<MIMEContent> mimeContents = getMimeContents(info.bindingOperation.getInput(),
                        getInputMessage(), part.getName());
                    jaxbReqType = getAttachmentType(mimeContents, part);
                    //reqBlock = new Block(new QName(part.getName()), jaxbReqType);
                    reqBlock = new Block(jaxbReqType.getName(), jaxbReqType);
                    request.addAttachmentBlock(reqBlock);
                }else if(ModelerUtils.isUnbound(part)){
                    request.addUnboundBlock(reqBlock);
                }
                if(inParameters == null)
                    inParameters = new ArrayList<Parameter>();
                Parameter param = ModelerUtils.createParameter(part.getName(), jaxbReqType, reqBlock);
                setCustomizedParameterName(info.portTypeOperation, part, param, false);
                inParameters.add(param);
            }
        }
        return inParameters;
    }

    /**
     * @param part
     * @param param
     * @param wrapperStyle TODO
     */
    private void setCustomizedParameterName(Extensible extension, MessagePart part, Parameter param, boolean wrapperStyle) {
        JAXWSBinding jaxwsBinding = (JAXWSBinding)getExtensionOfType(extension, JAXWSBinding.class);
        if(jaxwsBinding == null)
            return;
        String paramName = part.getName();
        QName elementName = part.getDescriptor();
        if(wrapperStyle)
            elementName = param.getType().getName();
        String customName = jaxwsBinding.getParameterName(paramName, elementName, wrapperStyle);
        if(customName != null && !customName.equals("")){
            param.setCustomName(customName);
        }
    }

    private List<String> processUnwrappedParameterOrder(StringBuffer unwrappedResult, Map<String, String> inputParamMap, Map<String, String> outputParamMap) {
        List <String> paramList = new ArrayList<String>();
        Message inputMessage = getInputMessage();
        Message outputMessage = getOutputMessage();

        if(inputMessage != null){
            Iterator<MessagePart> parts = inputMessage.parts();
            if(parts.hasNext()){
                MessagePart part = parts.next();
                JAXBType jaxbType = getJAXBType(part.getDescriptor());
                List<JAXBProperty> memberList = jaxbType.getWrapperChildren();
                Iterator<JAXBProperty> props = memberList.iterator();
                while(props.hasNext()){
                    JAXBProperty prop = props.next();
                    paramList.add(prop.getElementName().getLocalPart());
                    if(inputParamMap != null)
                        inputParamMap.put(prop.getElementName().getLocalPart(), prop.getType().getName());
                }
            }
        }

        if(isRequestResponse() && outputMessage != null){
            Iterator<MessagePart> parts = outputMessage.parts();
            if(parts.hasNext()){
                MessagePart part = parts.next();
                JAXBType jaxbType = getJAXBType(part.getDescriptor());
                List <JAXBProperty> outWrapperChildren = new ArrayList<JAXBProperty>();
                List<JAXBProperty> memberList = jaxbType.getWrapperChildren();
                //extract the out wrapper children
                for(JAXBProperty prop : memberList){
                    String type = null;
                    if(inputParamMap != null)
                        type = inputParamMap.get(prop.getElementName().getLocalPart());
                    if(outputParamMap != null)
                        outputParamMap.put(prop.getElementName().getLocalPart(), prop.getType().getName());
                    if(type != null && type.equals(prop.getType().getName())){
                        continue;
                    }
                    outWrapperChildren.add(prop);
                }

                // make a second pass to memberList, if one is left or the name is "return" then
                // its out(return) else its out holder
                for(JAXBProperty prop : outWrapperChildren){
                    String wrapperChildName = prop.getElementName().getLocalPart();
                    if((wrapperChildName.equals("return") || outWrapperChildren.size() == 1)){
                        unwrappedResult.append(wrapperChildName);
                        if(outputParamMap != null)
                            outputParamMap.remove(prop.getElementName().getLocalPart());
                        continue;
                    }
                    paramList.add(prop.getElementName().getLocalPart());
                }
            }
        }

        return paramList;
    }

    /**
     * @param name
     * @return
     */
    private JAXBType getJAXBType(QName name) {
        return _jaxbModelBuilder.getJAXBType(name);
    }

    protected boolean isConflictingPortClassName(String name) {
        return false;
    }

    /* (non-Javadoc)
     * @see WSDLModelerBase#getJAXBSchemaAnalyzerInstnace(WSDLModelInfo, Properties, org.w3c.dom.Element)
     */
    protected JAXBModelBuilder getJAXBSchemaAnalyzerInstnace(WSDLModelInfo info,
                                                             Properties options,
                                                             ClassNameCollector classNameCollector, List elements) {
        return new JAXBModelBuilder(info, options, classNameCollector, elements);
    }

    /* (non-Javadoc)
     * @see WSDLModelerBase#isUnwrappable()
     */
    protected boolean isUnwrappable() {
        if(!getWrapperStyleCustomization())
            return false;

        com.sun.tools.ws.wsdl.document.Message inputMessage = getInputMessage();
        com.sun.tools.ws.wsdl.document.Message outputMessage = getOutputMessage();

        // Wrapper style if the operation's input and output messages each contain
        // only a single part
        if ((inputMessage != null && inputMessage.numParts() != 1)
            || (outputMessage != null && outputMessage.numParts() != 1)) {
            return false;
        }

        MessagePart inputPart = inputMessage != null
                ? (MessagePart)inputMessage.parts().next() : null;
        MessagePart outputPart = outputMessage != null
                ? (MessagePart)outputMessage.parts().next() : null;
        String operationName = info.portTypeOperation.getName();

        // Wrapper style if the input message part refers to a global element declaration whose localname
        // is equal to the operation name
        // Wrapper style if the output message part refers to a global element declaration
        if ((inputPart != null && !inputPart.getDescriptor().getLocalPart().equals(operationName)) ||
            (outputPart != null && outputPart.getDescriptorKind() != SchemaKinds.XSD_ELEMENT))
            return false;

        //check to see if either input or output message part not bound to soapbing:body
        //in that case the operation is not wrapper style
        if(((inputPart != null) && (inputPart.getBindingExtensibilityElementKind() != MessagePart.SOAP_BODY_BINDING)) ||
                ((outputPart != null) &&(outputPart.getBindingExtensibilityElementKind() != MessagePart.SOAP_BODY_BINDING)))
            return false;

        // Wrapper style if the elements referred to by the input and output message parts
        // (henceforth referred to as wrapper elements) are both complex types defined
        // using the xsd:sequence compositor
        // Wrapper style if the wrapper elements only contain child elements, they must not
        // contain other structures such as xsd:choice, substitution groups1 or attributes
        //These checkins are done by jaxb, we just check if jaxb has wrapper children. If there
        // are then its wrapper style
        //if(inputPart != null && outputPart != null){
        if(inputPart != null){
            boolean inputWrappable = false;
            JAXBType inputType = getJAXBType(inputPart.getDescriptor());
            if(inputType != null){
                inputWrappable = inputType.isUnwrappable();
            }
            //if there are no output part (oneway), the operation can still be wrapper style
            if(outputPart == null){
               return inputWrappable;
            }
            JAXBType outputType = getJAXBType(outputPart.getDescriptor());
            if((inputType != null) && (outputType != null))
                return inputType.isUnwrappable() && outputType.isUnwrappable();
        }

        return false;
    }

    /**
     * @return
     */
    private boolean getWrapperStyleCustomization() {
        //first we look into wsdl:portType/wsdl:operation
        com.sun.tools.ws.wsdl.document.Operation portTypeOperation = info.portTypeOperation;
        JAXWSBinding jaxwsBinding = (JAXWSBinding)getExtensionOfType(portTypeOperation, JAXWSBinding.class);
        if(jaxwsBinding != null){
             Boolean isWrappable = jaxwsBinding.isEnableWrapperStyle();
             if(isWrappable != null)
                 return isWrappable;
        }

        //then into wsdl:portType        
        PortType portType = info.port.resolveBinding(document).resolvePortType(document);
        jaxwsBinding = (JAXWSBinding)getExtensionOfType(portType, JAXWSBinding.class);
        if(jaxwsBinding != null){
             Boolean isWrappable = jaxwsBinding.isEnableWrapperStyle();
             if(isWrappable != null)
                 return isWrappable;
        }

        //then wsdl:definitions
        jaxwsBinding = (JAXWSBinding)getExtensionOfType(document.getDefinitions(), JAXWSBinding.class);
        if(jaxwsBinding != null){
             Boolean isWrappable = jaxwsBinding.isEnableWrapperStyle();
             if(isWrappable != null)
                 return isWrappable;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see WSDLModelerBase#isSingleInOutPart(Set, MessagePart)
     */
    protected boolean isSingleInOutPart(Set inputParameterNames,
            MessagePart outputPart) {
        // As of now, we dont have support for in/out in doc-lit. So return false.
        SOAPOperation soapOperation =
            (SOAPOperation) getExtensionOfType(info.bindingOperation,
                    SOAPOperation.class);
        if((soapOperation != null) && (soapOperation.isDocument() || info.soapBinding.isDocument())) {
            Iterator iter = getInputMessage().parts();
            while(iter.hasNext()){
                MessagePart part = (MessagePart)iter.next();
                if(outputPart.getName().equals(part.getName()) && outputPart.getDescriptor().equals(part.getDescriptor()))
                    return true;
            }
        }else if(soapOperation != null && soapOperation.isRPC()|| info.soapBinding.isRPC()){
            com.sun.tools.ws.wsdl.document.Message inputMessage = getInputMessage();
            if(inputParameterNames.contains(outputPart.getName())) {
                if (inputMessage.getPart(outputPart.getName()).getDescriptor().equals(outputPart.getDescriptor())) {
                    return true;
                }
            }
        }
        return false;
    }

    private List<Parameter> createRpcLitRequestParameters(Request request, List<String> parameterList, Block block) {
        Message message = getInputMessage();
        S2JJAXBModel jaxbModel = ((RpcLitStructure)block.getType()).getJaxbModel().getS2JJAXBModel();
        List<Parameter> parameters = ModelerUtils.createRpcLitParameters(message, block, jaxbModel);

        //create parameters for header and mime parts
        for(String paramName: parameterList){
            MessagePart part = message.getPart(paramName);
            if(part == null)
                continue;
            if(ModelerUtils.isBoundToSOAPHeader(part)){
                if(parameters == null)
                    parameters = new ArrayList<Parameter>();
                QName headerName = part.getDescriptor();
                JAXBType jaxbType = getJAXBType(headerName);
                Block headerBlock = new Block(headerName, jaxbType);
                request.addHeaderBlock(headerBlock);
                Parameter param = ModelerUtils.createParameter(part.getName(), jaxbType, headerBlock);
                if(param != null){
                    parameters.add(param);
                }
            }else if(ModelerUtils.isBoundToMimeContent(part)){
                if(parameters == null)
                    parameters = new ArrayList<Parameter>();
                List<MIMEContent> mimeContents = getMimeContents(info.bindingOperation.getInput(),
                        getInputMessage(), paramName);

                JAXBType type = getAttachmentType(mimeContents, part);
                //create Parameters in request or response
                //Block mimeBlock = new Block(new QName(part.getName()), type);
                Block mimeBlock = new Block(type.getName(), type);
                request.addAttachmentBlock(mimeBlock);
                Parameter param = ModelerUtils.createParameter(part.getName(), type, mimeBlock);
                if(param != null){
                    parameters.add(param);
                }
            }else if(ModelerUtils.isUnbound(part)){
                if(parameters == null)
                    parameters = new ArrayList<Parameter>();
                QName name = part.getDescriptor();
                JAXBType type = getJAXBType(part);
                Block unboundBlock = new Block(name, type);
                request.addUnboundBlock(unboundBlock);
                Parameter param = ModelerUtils.createParameter(part.getName(), type, unboundBlock);
                if(param != null){
                    parameters.add(param);
                }
            }
        }
        for(Parameter param : parameters){
            setCustomizedParameterName(info.portTypeOperation, message.getPart(param.getName()), param, false);
        }
        return parameters;
    }

    private List<Parameter> createRpcLitResponseParameters(Response response, Block block) {

        Message message = getOutputMessage();
        boolean isRequestResponse =
            info.portTypeOperation.getStyle()
            == OperationStyle.REQUEST_RESPONSE;

        if(!isRequestResponse || message == null)
            return new ArrayList<Parameter>();

        S2JJAXBModel jaxbModel = ((RpcLitStructure)block.getType()).getJaxbModel().getS2JJAXBModel();
        List<Parameter> parameters = ModelerUtils.createRpcLitParameters(message, block, jaxbModel);
        //create parameters for header and mime parts
        List<MessagePart> msgParts = getOutputMessage().getParts();
        for(MessagePart part: msgParts){
            if(part == null)
                continue;
            if(ModelerUtils.isBoundToSOAPHeader(part)){
                if(parameters == null)
                    parameters = new ArrayList<Parameter>();
                QName headerName = part.getDescriptor();
                JAXBType jaxbType = getJAXBType(headerName);
                Block headerBlock = new Block(headerName, jaxbType);
                response.addHeaderBlock(headerBlock);
                Parameter param = ModelerUtils.createParameter(part.getName(), jaxbType, headerBlock);
                if(param != null){
                    parameters.add(param);
                }
            }else if(ModelerUtils.isBoundToMimeContent(part)){
                if(parameters == null)
                    parameters = new ArrayList<Parameter>();
                List<MIMEContent> mimeContents = getMimeContents(info.bindingOperation.getOutput(),
                        getOutputMessage(), part.getName());

                JAXBType type = getAttachmentType(mimeContents, part);
                //create Parameters in request or response
                //Block mimeBlock = new Block(new QName(part.getName()), type);
                Block mimeBlock = new Block(type.getName(), type);
                response.addAttachmentBlock(mimeBlock);
                Parameter param = ModelerUtils.createParameter(part.getName(), type, mimeBlock);
                if(param != null){
                    parameters.add(param);
                }
            }else if(ModelerUtils.isUnbound(part)){
                if(parameters == null)
                    parameters = new ArrayList<Parameter>();
                QName name = part.getDescriptor();
                JAXBType type = getJAXBType(part);
                Block unboundBlock = new Block(name, type);
                response.addUnboundBlock(unboundBlock);
                Parameter param = ModelerUtils.createParameter(part.getName(), type, unboundBlock);
                if(param != null){
                    parameters.add(param);
                }
            }
        }
        for(Parameter param : parameters){
            setCustomizedParameterName(info.portTypeOperation, message.getPart(param.getName()), param, false);
        }
        return parameters;
    }

    private String getJavaTypeForMimeType(String mimeType){
        if(mimeType.equals("image/jpeg") || mimeType.equals("image/gif")){
            return "java.awt.Image";
        }else if(mimeType.equals("text/xml") || mimeType.equals("application/xml")){
            return "javax.xml.transform.Source";
        }
        return "javax.activation.DataHandler";
    }

    /**
     * @param mimeContents
     * @return
     */
    private JAXBType getAttachmentType(List<MIMEContent> mimeContents, MessagePart part) {
        if(!enableMimeContent()){
            return getJAXBType(part);
        }
        String javaType = null;
        List<String> mimeTypes = getAlternateMimeTypes(mimeContents);
        if(mimeTypes.size() > 1) {
            javaType = "javax.activation.DataHandler";
        }else{
           javaType = getJavaTypeForMimeType(mimeTypes.get(0));
        }

        S2JJAXBModel jaxbModel = getJAXBModelBuilder().getJAXBModel().getS2JJAXBModel();
        JCodeModel cm = jaxbModel.generateCode(null,
                    new ConsoleErrorReporter(getEnvironment(), false));
        JType jt= cm.ref(javaType);;
        QName desc = part.getDescriptor();
        TypeAndAnnotation typeAnno = null;

        if (part.getDescriptorKind() == SchemaKinds.XSD_TYPE) {
            typeAnno = jaxbModel.getJavaType(desc);
            desc = new QName("", part.getName());            
        } else if (part.getDescriptorKind()== SchemaKinds.XSD_ELEMENT) {
            typeAnno = getJAXBModelBuilder().getElementTypeAndAnn(desc);
            for(Iterator mimeTypeIter = mimeTypes.iterator(); mimeTypeIter.hasNext();) {
                String mimeType = (String)mimeTypeIter.next();
                if((!mimeType.equals("text/xml") &&
                        !mimeType.equals("application/xml"))){
                    //According to AP 1.0,
                    //RZZZZ: In a DESCRIPTION, if a wsdl:part element refers to a
                    //global element declaration (via the element attribute of the wsdl:part
                    //element) then the value of the type attribute of a mime:content element
                    //that binds that part MUST be a content type suitable for carrying an
                    //XML serialization.
                    //should we throw warning?
                    //type = MimeHelper.javaType.DATA_HANDLER_JAVATYPE;
                    warn("mimemodeler.elementPart.invalidElementMimeType",
                            new Object[] {
                            part.getName(), mimeType});
                }
            }
        }
        if(typeAnno == null){
            fail("wsdlmodeler.jaxb.javatype.notfound", new Object[]{desc, part.getName()});
        }
        return new JAXBType(desc, new JavaSimpleType(new JAXBTypeAndAnnotation(typeAnno, jt)),
                null, getJAXBModelBuilder().getJAXBModel());
    }

    protected void buildJAXBModel(WSDLDocument wsdlDocument, WSDLModelInfo modelInfo, ClassNameCollector classNameCollector) {
        //set the java package where wsdl artifacts will be generated
        String jaxwsPackage = getJavaPackage();
        getWSDLModelInfo().setJavaPackageName(jaxwsPackage);
        JAXBModelBuilder jaxbModelBuilder = new JAXBModelBuilder(getWSDLModelInfo(), _options, classNameCollector, parser.getSchemaElements());

        //create pseudo schema for async operations(if any) response bean
        List<InputSource> schemas = PseudoSchemaBuilder.build(this, _modelInfo);
        for(InputSource schema : schemas){
            jaxbModelBuilder.getJAXBSchemaCompiler().parseSchema(schema);
        }
        jaxbModelBuilder.bind();
        _jaxbModelBuilder = jaxbModelBuilder;
    }

    protected String getJavaPackage(){
        String jaxwsPackage = null;
        JAXWSBinding jaxwsCustomization = (JAXWSBinding)getExtensionOfType(document.getDefinitions(), JAXWSBinding.class);
        if(jaxwsCustomization != null && jaxwsCustomization.getJaxwsPackage() != null){
            jaxwsPackage = jaxwsCustomization.getJaxwsPackage().getName();
        }
        if(jaxwsPackage != null){
            return jaxwsPackage;
        }
        String wsdlUri = document.getDefinitions().getTargetNamespaceURI();
        return XJC.getDefaultPackageName(wsdlUri);

    }

    protected void createJavaInterfaceForPort(Port port, boolean isProvider) {
        if(!isProvider){
            super.createJavaInterfaceForPort(port, isProvider);
            return;
        }
        String interfaceName = "javax.xml.ws.Provider";
        JavaInterface intf = new JavaInterface(interfaceName);
        port.setJavaInterface(intf);
    }



    /* (non-Javadoc)
     * @see WSDLModelerBase#getServiceInterfaceName(QName, Service)
     */
    @Override
    protected String getServiceInterfaceName(QName serviceQName, com.sun.tools.ws.wsdl.document.Service wsdlService) {
        String serviceName = wsdlService.getName();
        JAXWSBinding jaxwsCust = (JAXWSBinding)getExtensionOfType(wsdlService, JAXWSBinding.class);
        if(jaxwsCust != null && jaxwsCust.getClassName() != null){
            CustomName name = jaxwsCust.getClassName();
            if(name != null && !name.equals(""))
                serviceName = name.getName();
        }
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
            += getEnvironment().getNames().validJavaClassName(serviceName);
        return serviceInterface;
    }

    /* (non-Javadoc)
     * @see WSDLModelerBase#getJavaNameOfSEI(Port)
     */
    protected String getJavaNameOfSEI(Port port) {
        QName portTypeName =
            (QName)port.getProperty(
                ModelProperties.PROPERTY_WSDL_PORT_TYPE_NAME);
        PortType pt = (PortType)document.find(Kinds.PORT_TYPE, portTypeName);
        JAXWSBinding jaxwsCust = (JAXWSBinding)getExtensionOfType(pt, JAXWSBinding.class);
        if(jaxwsCust != null && jaxwsCust.getClassName() != null){
            CustomName name = jaxwsCust.getClassName();
            if(name != null && !name.equals("")){
                return makePackageQualified(
                        name.getName(),
                        portTypeName,
                        false);
            }
        }

        QName bindingName =
            (QName)port.getProperty(ModelProperties.PROPERTY_WSDL_BINDING_NAME);

        String interfaceName = null;
        if (portTypeName != null) {
            // got portType information from WSDL, use it to name the interface
            interfaceName =
                makePackageQualified(XJC.mangleNameToClassName(portTypeName.getLocalPart()),
                                        portTypeName,
                                        false);
        } else {
            // somehow we only got the port name, so we use that
            interfaceName =
                makePackageQualified(
                    XJC.mangleNameToClassName(port.getName().getLocalPart()),
                    port.getName(),
                    false);
        }
        return interfaceName;
    }

    /* (non-Javadoc)
     * @see WSDLModelerBase#createJavaMethodForOperation(Port, Operation, JavaInterface, Set, Set)
     */
    protected void createJavaMethodForOperation(Port port, Operation operation,
            JavaInterface intf, Set methodNames, Set methodSignatures) {
        if(!(operation instanceof AsyncOperation)){
            super.createJavaMethodForOperation(port, operation, intf, methodNames,
                    methodSignatures);
            return;
        }
        String candidateName = getJavaNameForOperation(operation);
        JavaMethod method = new JavaMethod(candidateName);
        method.setThrowsRemoteException(false);
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
                    getEnvironment().getNames().validJavaMemberName(parameter.getName()),
                    parameterType,
                    parameter,
                    parameter.getLinkedParameter() != null);
            if (javaParameter.isHolder()) {
                javaParameter.setHolderName(javax.xml.ws.Holder.class.getName());
            }
            method.addParameter(javaParameter);
            parameter.setJavaParameter(javaParameter);

            signature += "%" + parameterType.getName();
        }

        if (response != null) {
            String resultParameterName =
                (String)operation.getProperty(WSDL_RESULT_PARAMETER);
            Parameter resultParameter =
                response.getParameterByName(resultParameterName);
            JavaType returnType = resultParameter.getType().getJavaType();
            method.setReturnType(returnType);

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
    }

    protected boolean createJavaExceptionFromLiteralType(Fault fault, com.sun.tools.ws.processor.model.Port port, String operationName) {
        ProcessorEnvironment _env = getProcessorEnvironment();
        WSDLExceptionInfo exInfo = getExceptionInfo(fault);
        String exceptionName = null;

        JAXBType faultType = (JAXBType)fault.getBlock().getType();

        if (exInfo != null) {
            exceptionName = exInfo.exceptionType;
        } else {
            exceptionName =
                makePackageQualified(
                    _env.getNames().validJavaClassName(fault.getName()),
                    port.getName());
        }

        // use fault namespace attribute
        JAXBStructuredType jaxbStruct = new JAXBStructuredType(new QName(
                                            fault.getBlock().getName().getNamespaceURI(),
                                            fault.getName()));

        QName memberName = fault.getElementName();
        JAXBElementMember jaxbMember =
            new JAXBElementMember(memberName, faultType);
        //jaxbMember.setNillable(faultType.isNillable());

        String javaMemberName = getLiteralJavaMemberName(fault);
        JavaStructureMember javaMember = new JavaStructureMember(
                                            javaMemberName,
                                            faultType.getJavaType(),
                                            jaxbMember);
        jaxbMember.setJavaStructureMember(javaMember);
        javaMember.setReadMethod(_env.getNames().getJavaMemberReadMethod(javaMember));
        javaMember.setInherited(false);
        jaxbMember.setJavaStructureMember(javaMember);
        jaxbStruct.add(jaxbMember);

        if (isConflictingExceptionClassName(exceptionName)) {
            exceptionName += "_Exception";
        }

        JavaException existingJavaException = (JavaException)_javaExceptions.get(exceptionName);
        if (existingJavaException != null) {
            if (existingJavaException.getName().equals(exceptionName)) {
                if (((JAXBType)existingJavaException.getOwner()).getName().equals(jaxbStruct.getName())
                    || ModelerUtils.isEquivalentLiteralStructures(jaxbStruct, (JAXBStructuredType) existingJavaException.getOwner())) {
                    // we have mapped this fault already
                    if (faultType instanceof JAXBStructuredType) {
                        fault.getBlock().setType((JAXBType) existingJavaException.getOwner());
                    }
                    fault.setJavaException(existingJavaException);
                    return false;
                }
            }
        }

        JavaException javaException = new JavaException(exceptionName, false, jaxbStruct);
        javaException.add(javaMember);
        jaxbStruct.setJavaType(javaException);

        _javaExceptions.put(javaException.getName(), javaException);

        fault.setJavaException(javaException);
        return true;
    }

    protected boolean isRequestResponse(){
        return info.portTypeOperation.getStyle() == OperationStyle.REQUEST_RESPONSE;
    }

    protected java.util.List<String> getAsynParameterOrder(){
        //for async operation ignore the parameterOrder
        java.util.List<String> parameterList = new ArrayList<String>();
        Message inputMessage = getInputMessage();
        List<MessagePart> inputParts = inputMessage.getParts();
        for(MessagePart part: inputParts){
            parameterList.add(part.getName());
        }
        return parameterList;
    }


    protected java.util.List<String> getParameterOrder(StringBuffer resultParameter, Map<String, QName> inputParamMap, Map<String, QName> outputParamMap){
        String parameterOrder = info.portTypeOperation.getParameterOrder();
        java.util.List<String> parameterList = new ArrayList<String>();
        boolean parameterOrderPresent = false;
        if ((parameterOrder != null) && !(parameterOrder.trim().equals(""))) {
            parameterList = XmlUtil.parseTokenList(parameterOrder);
            parameterOrderPresent = true;
        } else {
            parameterList = new ArrayList<String>();
        }
        Message inputMessage = getInputMessage();
        Message outputMessage = getOutputMessage();
        List<MessagePart> outputParts = null;
        List<MessagePart> inputParts = inputMessage.getParts();

        for(MessagePart part:inputParts){
            inputParamMap.put(part.getName(), part.getDescriptor());
        }

        if(isRequestResponse()){
            outputParts = outputMessage.getParts();
            for(MessagePart part:outputParts){
                outputParamMap.put(part.getName(), part.getDescriptor());
            }
        }

        if(parameterOrderPresent){
            boolean validParameterOrder = true;
            Iterator<String> paramOrders = parameterList.iterator();
            // If any part in the parameterOrder is not present in the request or
            // response message, we completely ignore the parameterOrder hint
            while(paramOrders.hasNext()){
                String param = paramOrders.next();
                boolean partFound = false;
                for(MessagePart part : inputParts){
                    if(param.equals(part.getName())){

                        partFound = true;
                        break;
                    }
                }
                // if not found, check in output parts
                if(!partFound){
                    for(MessagePart part : outputParts){
                        if(param.equals(part.getName())){
                            partFound = true;
                            break;
                        }
                    }
                }
                if(!partFound){
                    warn("wsdlmodeler.invalid.parameterorder.parameter",
                            new Object[] {param, info.operation.getName().getLocalPart()});
                    validParameterOrder = false;
                }
            }

            List<MessagePart> inputUnlistedParts = new ArrayList<MessagePart>();
            List<MessagePart> outputUnlistedParts = new ArrayList<MessagePart>();

            //gather input Parts
            if(validParameterOrder){

                for(MessagePart part: inputParts){
                    if(!parameterList.contains(part.getName())) {
                        inputUnlistedParts.add(part);
                    }
                }
            }

            if(validParameterOrder && isRequestResponse()){
                // at most one output part should be unlisted
                for(MessagePart part: outputParts){
                    if(!parameterList.contains(part.getName())) {
                        MessagePart inPart = inputMessage.getPart(part.getName());
                        //dont add inout as unlisted part
                        if(inPart == null ||
                                (inPart != null &&!inPart.getDescriptor().equals(part.getDescriptor())))
                        outputUnlistedParts.add(part);
                    }
                }
                if(outputUnlistedParts.size() == 1){
                    MessagePart outPart = outputUnlistedParts.get(0);
                    resultParameter.append(outPart.getName());
                    outputParamMap.remove(outPart.getName());
                    outputUnlistedParts.clear();
                }
            }

            if(validParameterOrder){
                //append the unlisted parts, first input then output
                for(MessagePart param:inputUnlistedParts){
                    parameterList.add(param.getName());
                }
                for(MessagePart param:outputUnlistedParts){
                    parameterList.add(param.getName());
                }

                return parameterList;
            }

            //parameterOrder attribute is not valid, we ignore it
            warn("wsdlmodeler.invalid.parameterOrder.invalidParameterOrder",
                    new Object[] {info.operation.getName().getLocalPart()});
            parameterOrderPresent = false;
            parameterList.clear();
        }

        List<String> outParts = new ArrayList<String>();

        //construct input parameter list with the same order as in input message
        for(MessagePart part: inputParts){
            parameterList.add(part.getName());
        }

        if(isRequestResponse()){
            for(MessagePart part:outputParts){
                MessagePart outPart = inputMessage.getPart(part.getName());
                if(outPart != null && part.getDescriptorKind() == outPart.getDescriptorKind() &&
                        part.getDescriptor().equals(outPart.getDescriptor())){
                    continue;
                }
                outParts.add(part.getName());
            }

            //append the out parts to the parameterList
            for(String name : outParts){
                if(outParts.size() == 1){
                    resultParameter.append(name);
                    outputParamMap.remove(name);
                }else{
                    parameterList.add(name);
                }
            }
        }
        return parameterList;
    }

    /* (non-Javadoc)
     * @see WSDLModelerBase#setProperties(Port, boolean)
     */
    protected void setProperties(Port port, boolean isProvider) {
        if(!isProvider){
            super.setProperties(port, isProvider);
            return;
        }
        String tieClassName = getClassName(port, GeneratorConstants.TIE_SUFFIX);
        port.setProperty(
            ModelProperties.PROPERTY_TIE_CLASS_NAME,
            tieClassName);

        String ptieClassName = getClassName(port, GeneratorConstants.PEPT_TIE_SUFFIX);
        port.setProperty(
            ModelProperties.PROPERTY_PTIE_CLASS_NAME,
            ptieClassName);

        String sedClassName = getClassName(port, GeneratorConstants.SERVER_ENCODER_DECODER_SUFFIX);
        port.setProperty(
            ModelProperties.PROPERTY_SED_CLASS_NAME,
            sedClassName);

        String eptffClassName = getClassName(port, GeneratorConstants.EPTFF_SUFFIX);
        port.setProperty(
            ModelProperties.PROPERTY_EPTFF_CLASS_NAME,
            eptffClassName);
    }

    /**
     *
     * @param port
     * @param suffix
     * @return the Java ClassName for a port
     */
    protected String getClassName(Port port, String suffix) {
        String name = "";
        String javaPackageName = "";
        if (_modelInfo.getJavaPackageName() != null
            && !_modelInfo.getJavaPackageName().equals("")) {
            javaPackageName = _modelInfo.getJavaPackageName();
        }
        String prefix = getEnvironment().getNames().validJavaClassName(port.getName().getLocalPart());
        name = javaPackageName+"."+prefix+suffix;
        return name;
    }

    protected boolean isConflictingServiceClassName(String name) {
       if(conflictsWithSEIClass(name) || conflictsWithJAXBClass(name) ||conflictsWithExceptionClass(name)){
            return true;
        }
        return false;
    }

    private boolean conflictsWithSEIClass(String name){
        Set<String> seiNames = classNameCollector.getSeiClassNames();
        if(seiNames != null && seiNames.contains(name))
            return true;
        return false;
    }

    private boolean conflictsWithJAXBClass(String name){
        Set<String> jaxbNames = classNameCollector.getJaxbGeneratedClassNames();
        if(jaxbNames != null && jaxbNames.contains(name))
            return true;
        return false;
    }

    private boolean conflictsWithExceptionClass(String name){
        Set<String> exceptionNames = classNameCollector.getExceptionClassNames();
        if(exceptionNames != null && exceptionNames.contains(name))
            return true;
        return false;
    }

    protected boolean isConflictingExceptionClassName(String name) {
        if(conflictsWithSEIClass(name) || conflictsWithJAXBClass(name)){
            return true;
        }
        return false;
    }

    protected JAXBModelBuilder getJAXBModelBuilder() {
        return _jaxbModelBuilder;
    }

    protected enum StyleAndUse  {RPC_LITERAL, DOC_LITERAL};
    private ModelerUtils _modelerUtils;
    private JAXBModelBuilder _jaxbModelBuilder;

    /* (non-Javadoc)
     * @see WSDLModelerBase#validWSDLBindingStyle(Binding)
     */
    protected boolean validateWSDLBindingStyle(Binding binding) {
        boolean mixedStyle = false;
        SOAPBinding soapBinding =
            (SOAPBinding)getExtensionOfType(binding, SOAPBinding.class);

        //dont process the binding
        if(soapBinding == null)
            soapBinding =
                (SOAPBinding)getExtensionOfType(binding, SOAP12Binding.class);
        if(soapBinding == null)
            return false;

        //if soapbind:binding has no style attribute, the default is DOCUMENT
        if(soapBinding.getStyle() == null)
            soapBinding.setStyle(SOAPStyle.DOCUMENT);

        SOAPStyle opStyle = soapBinding.getStyle();
        for (Iterator iter = binding.operations(); iter.hasNext();) {
            BindingOperation bindingOperation =
                (BindingOperation)iter.next();
            SOAPOperation soapOperation =
                (SOAPOperation) getExtensionOfType(bindingOperation,
                    SOAPOperation.class);
            if(soapOperation != null){
                SOAPStyle currOpStyle = (soapOperation.getStyle() != null)?soapOperation.getStyle():soapBinding.getStyle();
                //dont check for the first operation
                if(!currOpStyle.equals(opStyle))
                    return false;
            }
        }
        return true;
    }

    /**
     * @param port
     */
    private void applyWrapperStyleCustomization(Port port, PortType portType) {
        JAXWSBinding jaxwsBinding = (JAXWSBinding)getExtensionOfType(portType, JAXWSBinding.class);
        Boolean wrapperStyle = (jaxwsBinding != null)?jaxwsBinding.isEnableWrapperStyle():null;
        if(wrapperStyle != null){
            port.setWrapped(wrapperStyle);
        }
    }

    /* (non-Javadoc)
     * @see WSDLModelerBase#getJavaNameForOperation(Operation)
     */
    @Override
    protected String getJavaNameForOperation(Operation operation) {
        String name = operation.getJavaMethodName();
        if(getEnvironment().getNames().isJavaReservedWord(name)){
            name = "_"+name;
        }
        return name;
    }

    protected void fail(String key, String arg){
        throw new ModelerException(key, arg);
    }
    protected void fail(String key, Object[] args){
        throw new ModelerException(key, args);
    }
}
