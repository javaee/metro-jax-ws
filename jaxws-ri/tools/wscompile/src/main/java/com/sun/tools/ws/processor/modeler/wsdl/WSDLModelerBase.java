/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.tools.ws.processor.modeler.wsdl;

import com.sun.tools.ws.api.wsdl.TWSDLExtensible;
import com.sun.tools.ws.api.wsdl.TWSDLExtension;
import com.sun.tools.ws.processor.generator.Names;
import com.sun.tools.ws.processor.model.Fault;
import com.sun.tools.ws.processor.model.Operation;
import com.sun.tools.ws.processor.model.Port;
import com.sun.tools.ws.processor.model.java.JavaException;
import com.sun.tools.ws.processor.modeler.Modeler;
import com.sun.tools.ws.resources.ModelerMessages;
import com.sun.tools.ws.wscompile.AbortException;
import com.sun.tools.ws.wscompile.ErrorReceiver;
import com.sun.tools.ws.wscompile.ErrorReceiverFilter;
import com.sun.tools.ws.wscompile.WsimportOptions;
import com.sun.tools.ws.wsdl.document.*;
import com.sun.tools.ws.wsdl.document.jaxws.JAXWSBinding;
import com.sun.tools.ws.wsdl.document.mime.MIMEContent;
import com.sun.tools.ws.wsdl.document.mime.MIMEMultipartRelated;
import com.sun.tools.ws.wsdl.document.mime.MIMEPart;
import com.sun.tools.ws.wsdl.document.schema.SchemaKinds;
import com.sun.tools.ws.wsdl.document.soap.*;
import com.sun.tools.ws.wsdl.framework.Entity;
import com.sun.tools.ws.wsdl.framework.GloballyKnown;
import com.sun.tools.ws.wsdl.framework.NoSuchEntityException;
import com.sun.tools.ws.wsdl.parser.WSDLParser;
import com.sun.tools.ws.wsdl.parser.MetadataFinder;
import com.sun.xml.ws.spi.db.BindingHelper;

import org.xml.sax.helpers.LocatorImpl;

import javax.xml.namespace.QName;
import java.util.*;

/**
 *
 * @author WS Development Team
 *
 * Base class for WSDL->Model classes.
 */
public abstract class WSDLModelerBase implements Modeler {
    protected final ErrorReceiverFilter errReceiver;
    protected final WsimportOptions options;
    protected MetadataFinder forest;


    public WSDLModelerBase(WsimportOptions options, ErrorReceiver receiver, MetadataFinder forest) {
        this.options = options;
        this.errReceiver = new ErrorReceiverFilter(receiver);
        this.forest = forest;
    }

    /**
     *
     * @param port
     * @param wsdlPort
     */
    protected void applyPortMethodCustomization(Port port, com.sun.tools.ws.wsdl.document.Port wsdlPort) {
        if (isProvider(wsdlPort)) {
            return;
        }
        JAXWSBinding jaxwsBinding = (JAXWSBinding)getExtensionOfType(wsdlPort, JAXWSBinding.class);

        String portMethodName = (jaxwsBinding != null)?((jaxwsBinding.getMethodName() != null)?jaxwsBinding.getMethodName().getName():null):null;
        if(portMethodName != null){
            port.setPortGetter(portMethodName);
        }else{
            portMethodName = Names.getPortName(port);
            portMethodName = BindingHelper.mangleNameToClassName(portMethodName);
            port.setPortGetter("get"+portMethodName);
        }

    }

    protected boolean isProvider(com.sun.tools.ws.wsdl.document.Port wsdlPort){
        JAXWSBinding portCustomization = (JAXWSBinding)getExtensionOfType(wsdlPort, JAXWSBinding.class);
        Boolean isProvider = (portCustomization != null)?portCustomization.isProvider():null;
        if(isProvider != null){
            return isProvider;
        }

        JAXWSBinding jaxwsGlobalCustomization = (JAXWSBinding)getExtensionOfType(document.getDefinitions(), JAXWSBinding.class);
        isProvider = (jaxwsGlobalCustomization != null)?jaxwsGlobalCustomization.isProvider():null;
        if (isProvider != null) {
            return isProvider;
        }
        return false;
    }

    protected SOAPBody getSOAPRequestBody() {
        SOAPBody requestBody =
            (SOAPBody)getAnyExtensionOfType(info.bindingOperation.getInput(),
                SOAPBody.class);
        if (requestBody == null) {
            // the WSDL document is invalid
            error(info.bindingOperation.getInput(), ModelerMessages.WSDLMODELER_INVALID_BINDING_OPERATION_INPUT_MISSING_SOAP_BODY(info.bindingOperation.getName()));
        }
        return requestBody;
    }

    protected boolean isRequestMimeMultipart() {
        for (TWSDLExtension extension: info.bindingOperation.getInput().extensions()) {
            if (extension.getClass().equals(MIMEMultipartRelated.class)) {
                return true;
            }
        }
        return false;
    }

    protected boolean isResponseMimeMultipart() {
        for (TWSDLExtension extension: info.bindingOperation.getOutput().extensions()) {
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
            error(info.bindingOperation.getOutput(),  ModelerMessages.WSDLMODELER_INVALID_BINDING_OPERATION_OUTPUT_MISSING_SOAP_BODY(info.bindingOperation.getName()));
        }
        return responseBody;
    }

    protected com.sun.tools.ws.wsdl.document.Message getOutputMessage() {
        if (info.portTypeOperation.getOutput() == null) {
            return null;
        }
        return info.portTypeOperation.getOutput().resolveMessage(info.document);
    }

    protected com.sun.tools.ws.wsdl.document.Message getInputMessage() {
        return info.portTypeOperation.getInput().resolveMessage(info.document);
    }

    /**
     * @param body request or response body, represents soap:body
     * @param message Input or output message, equivalent to wsdl:message
     * @return iterator over MessagePart
     */
    protected List<MessagePart> getMessageParts(
        SOAPBody body,
        com.sun.tools.ws.wsdl.document.Message message, boolean isInput) {
        String bodyParts = body.getParts();
        ArrayList<MessagePart> partsList = new ArrayList<MessagePart>();
        List<MessagePart> parts = new ArrayList<MessagePart>();

        //get Mime parts
        List mimeParts;
        if (isInput) {
            mimeParts = getMimeContentParts(message, info.bindingOperation.getInput());
        } else {
            mimeParts = getMimeContentParts(message, info.bindingOperation.getOutput());
        }

        if (bodyParts != null) {
            StringTokenizer in = new StringTokenizer(bodyParts.trim(), " ");
            while (in.hasMoreTokens()) {
                String part = in.nextToken();
                MessagePart mPart = message.getPart(part);
                if (null == mPart) {
                    error(message,  ModelerMessages.WSDLMODELER_ERROR_PARTS_NOT_FOUND(part, message.getName()));
                }
                mPart.setBindingExtensibilityElementKind(MessagePart.SOAP_BODY_BINDING);
                partsList.add(mPart);
            }
        } else {
            for (MessagePart mPart : message.getParts()) {
                if (!mimeParts.contains(mPart)) {
                    mPart.setBindingExtensibilityElementKind(MessagePart.SOAP_BODY_BINDING);
                }
                partsList.add(mPart);
            }
        }

        for (MessagePart mPart : message.getParts()) {
            if (mimeParts.contains(mPart)) {
                mPart.setBindingExtensibilityElementKind(MessagePart.WSDL_MIME_BINDING);
                parts.add(mPart);
            } else if(partsList.contains(mPart)) {
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
    protected List<MessagePart> getMimeContentParts(Message message, TWSDLExtensible ext) {
        ArrayList<MessagePart> mimeContentParts = new ArrayList<MessagePart>();

        for (MIMEPart mimePart : getMimeParts(ext)) {
            MessagePart part = getMimeContentPart(message, mimePart);
            if (part != null) {
                mimeContentParts.add(part);
            }
        }
        return mimeContentParts;
    }

    /**
     * @param mimeParts
     */
    protected boolean validateMimeParts(Iterable<MIMEPart> mimeParts) {
        boolean gotRootPart = false;
        List<MIMEContent> mimeContents = new ArrayList<MIMEContent>();
        for (MIMEPart mPart : mimeParts) {
            for (TWSDLExtension obj : mPart.extensions()) {
                if (obj instanceof SOAPBody) {
                    if (gotRootPart) {
                        warning(mPart, ModelerMessages.MIMEMODELER_INVALID_MIME_PART_MORE_THAN_ONE_SOAP_BODY(info.operation.getName().getLocalPart()));
                        return false;
                    }
                    gotRootPart = true;
                } else if (obj instanceof MIMEContent) {
                    mimeContents.add((MIMEContent) obj);
                }
            }
            if (!validateMimeContentPartNames(mimeContents)) {
                return false;
            }
            if(mPart.getName() != null) {
                warning(mPart, ModelerMessages.MIMEMODELER_INVALID_MIME_PART_NAME_NOT_ALLOWED(info.portTypeOperation.getName()));
            }
        }
        return true;

    }

    private MessagePart getMimeContentPart(Message message, MIMEPart part) {
        for( MIMEContent mimeContent : getMimeContents(part) ) {
            String mimeContentPartName = mimeContent.getPart();
            MessagePart mPart = message.getPart(mimeContentPartName);
            //RXXXX mime:content MUST have part attribute
            if(null == mPart) {
                error(mimeContent,  ModelerMessages.WSDLMODELER_ERROR_PARTS_NOT_FOUND(mimeContentPartName, message.getName()));
            }
            mPart.setBindingExtensibilityElementKind(MessagePart.WSDL_MIME_BINDING);
            return mPart;
        }
        return null;
    }

    //List of mimeTypes
    protected List<String> getAlternateMimeTypes(List<MIMEContent> mimeContents) {
        List<String> mimeTypes = new ArrayList<String>();
        //validateMimeContentPartNames(mimeContents.iterator());
//        String mimeType = null;
        for(MIMEContent mimeContent:mimeContents){
            String mimeType = getMimeContentType(mimeContent);
            if (!mimeTypes.contains(mimeType)) {
                mimeTypes.add(mimeType);
            }
        }
        return mimeTypes;
    }

    private boolean validateMimeContentPartNames(List<MIMEContent> mimeContents) {
        //validate mime:content(s) in the mime:part as per R2909
        for (MIMEContent mimeContent : mimeContents) {
            String mimeContnetPart;
            mimeContnetPart = getMimeContentPartName(mimeContent);
            if(mimeContnetPart == null) {
                warning(mimeContent, ModelerMessages.MIMEMODELER_INVALID_MIME_CONTENT_MISSING_PART_ATTRIBUTE(info.operation.getName().getLocalPart()));
                return false;
            }
        }
        return true;
    }

    protected Iterable<MIMEPart> getMimeParts(TWSDLExtensible ext) {
        MIMEMultipartRelated multiPartRelated =
            (MIMEMultipartRelated) getAnyExtensionOfType(ext,
                    MIMEMultipartRelated.class);
        if(multiPartRelated == null) {
            return Collections.emptyList();
        }
        return multiPartRelated.getParts();
    }

    //returns MIMEContents
    protected List<MIMEContent> getMimeContents(MIMEPart part) {
        List<MIMEContent> mimeContents = new ArrayList<MIMEContent>();
        for (TWSDLExtension mimeContent : part.extensions()) {
            if (mimeContent instanceof MIMEContent) {
                mimeContents.add((MIMEContent) mimeContent);
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
            error(mimeContent, ModelerMessages.MIMEMODELER_INVALID_MIME_CONTENT_MISSING_TYPE_ATTRIBUTE(info.operation.getName().getLocalPart()));
        }
        return mimeType;
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
            if(options.isExtensionMode()){
                return info.modelPort.getName().getNamespaceURI();
            }
            // the WSDL document is invalid
            // at least, that's my interpretation of section 3.5 of the WSDL 1.1 spec!
            error(body, ModelerMessages.WSDLMODELER_INVALID_BINDING_OPERATION_INPUT_SOAP_BODY_MISSING_NAMESPACE(info.bindingOperation.getName()));
        }
        return namespaceURI;
    }

    protected String getResponseNamespaceURI(SOAPBody body) {
        String namespaceURI = body.getNamespace();
        if (namespaceURI == null) {
            if(options.isExtensionMode()){
                return info.modelPort.getName().getNamespaceURI();
            }
            // the WSDL document is invalid
            // at least, that's my interpretation of section 3.5 of the WSDL 1.1 spec!
            error(body, ModelerMessages.WSDLMODELER_INVALID_BINDING_OPERATION_OUTPUT_SOAP_BODY_MISSING_NAMESPACE(info.bindingOperation.getName()));
        }
        return namespaceURI;
    }

    /**
     * @return List of SOAPHeader extensions
     */
    protected List<SOAPHeader> getHeaderExtensions(TWSDLExtensible extensible) {
        List<SOAPHeader> headerList = new ArrayList<SOAPHeader>();
        for (TWSDLExtension extension : extensible.extensions()) {
            if (extension.getClass()==MIMEMultipartRelated.class) {
                for( MIMEPart part : ((MIMEMultipartRelated) extension).getParts() ) {
                    boolean isRootPart = isRootPart(part);
                    for (TWSDLExtension obj : part.extensions()) {
                        if (obj instanceof SOAPHeader) {
                            //bug fix: 5024015
                            if (!isRootPart) {
                                warning((Entity) obj, ModelerMessages.MIMEMODELER_WARNING_IGNORINGINVALID_HEADER_PART_NOT_DECLARED_IN_ROOT_PART(info.bindingOperation.getName()));
                                return new ArrayList<SOAPHeader>();
                            }
                            headerList.add((SOAPHeader) obj);
                        }
                    }
                }
            } else if (extension instanceof SOAPHeader) {
                headerList.add((SOAPHeader) extension);
            }
        }
         return headerList;
    }

    /**
     * @param part
     * @return true if part is the Root part
     */
    private boolean isRootPart(MIMEPart part) {
        for (TWSDLExtension twsdlExtension : part.extensions()) {
            if (twsdlExtension instanceof SOAPBody) {
                return true;
            }
        }
        return false;
    }

    protected Set getDuplicateFaultNames() {
        // look for fault messages with the same soap:fault name
        Set<QName> faultNames = new HashSet<QName>();
        Set<QName> duplicateNames = new HashSet<QName>();
        for( BindingFault bindingFault : info.bindingOperation.faults() ) {
            com.sun.tools.ws.wsdl.document.Fault portTypeFault = null;
            for (com.sun.tools.ws.wsdl.document.Fault aFault : info.portTypeOperation.faults()) {
                if (aFault.getName().equals(bindingFault.getName())) {
                    if (portTypeFault != null) {
                        // the WSDL document is invalid
                        error(bindingFault, ModelerMessages.WSDLMODELER_INVALID_BINDING_FAULT_NOT_UNIQUE(bindingFault.getName(),
                            info.bindingOperation.getName()));
                    } else {
                        portTypeFault = aFault;
                    }
                }
            }
            if (portTypeFault == null) {
                // the WSDL document is invalid
                error(bindingFault, ModelerMessages.WSDLMODELER_INVALID_BINDING_FAULT_NOT_FOUND(bindingFault.getName(),
                    info.bindingOperation.getName()));
            }
            SOAPFault soapFault =
                (SOAPFault)getExtensionOfType(bindingFault, SOAPFault.class);
            if (soapFault == null) {
                // the WSDL document is invalid
                if(options.isExtensionMode()){
                    warning(bindingFault, ModelerMessages.WSDLMODELER_INVALID_BINDING_FAULT_OUTPUT_MISSING_SOAP_FAULT(bindingFault.getName(),
                    info.bindingOperation.getName()));
                }else {
                    error(bindingFault, ModelerMessages.WSDLMODELER_INVALID_BINDING_FAULT_OUTPUT_MISSING_SOAP_FAULT(bindingFault.getName(),
                        info.bindingOperation.getName()));
                }
            }

            com.sun.tools.ws.wsdl.document.Message faultMessage =
                portTypeFault.resolveMessage(info.document);
            if(faultMessage.getParts().isEmpty()) {
                // the WSDL document is invalid
                error(faultMessage, ModelerMessages.WSDLMODELER_INVALID_BINDING_FAULT_EMPTY_MESSAGE(bindingFault.getName(),
                    faultMessage.getName()));
            }
            //  bug fix: 4852729
            if (!options.isExtensionMode() && (soapFault != null && soapFault.getNamespace() != null)) {
                warning(soapFault, ModelerMessages.WSDLMODELER_WARNING_R_2716_R_2726("soapbind:fault", soapFault.getName()));
            }
            String faultNamespaceURI = (soapFault != null && soapFault.getNamespace() != null)?soapFault.getNamespace():portTypeFault.getMessage().getNamespaceURI();
            String faultName = faultMessage.getName();
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
     * @param operation
     * @return true if operation has valid body parts
     */
    protected boolean validateBodyParts(BindingOperation operation) {
        boolean isRequestResponse =
            info.portTypeOperation.getStyle()
            == OperationStyle.REQUEST_RESPONSE;
        List<MessagePart> inputParts = getMessageParts(getSOAPRequestBody(), getInputMessage(), true);
        if (!validateStyleAndPart(operation, inputParts)) {
            return false;
        }

        if(isRequestResponse){
            List<MessagePart> outputParts = getMessageParts(getSOAPResponseBody(), getOutputMessage(), false);
            if (!validateStyleAndPart(operation, outputParts)) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param operation
     * @return true if operation has valid style and part
     */
    private boolean validateStyleAndPart(BindingOperation operation, List<MessagePart> parts) {
        SOAPOperation soapOperation =
            (SOAPOperation) getExtensionOfType(operation, SOAPOperation.class);
        for (MessagePart part : parts) {
            if (part.getBindingExtensibilityElementKind() == MessagePart.SOAP_BODY_BINDING) {
                if (!isStyleAndPartMatch(soapOperation, part)) {
                    return false;
                }
            }
        }
        return true;
    }

    protected String getLiteralJavaMemberName(Fault fault) {
        String javaMemberName;

        QName memberName = fault.getElementName();
        javaMemberName = fault.getJavaMemberName();
        if (javaMemberName == null) {
            javaMemberName = memberName.getLocalPart();
        }
        return javaMemberName;
    }

    /**
     * @param ext
     * @param message
     * @param name
     * @return List of MimeContents from ext
     */
    protected List<MIMEContent> getMimeContents(TWSDLExtensible ext, Message message, String name) {
        for (MIMEPart mimePart : getMimeParts(ext)) {
            List<MIMEContent> mimeContents = getMimeContents(mimePart);
            for (MIMEContent mimeContent : mimeContents) {
                if (mimeContent.getPart().equals(name)) {
                    return mimeContents;
                }
            }
        }
        return null;
    }

    protected String makePackageQualified(String s) {
        if (s.indexOf(".") != -1) {
            // s is already package qualified
            return s;
        } else if (options.defaultPackage != null
                && !options.defaultPackage.equals("")) {
            return options.defaultPackage + "." + s;
        } else {//options.defaultPackage seems to be never null, and this is never executed
            return s;
        }

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

    protected static QName getQNameOf(GloballyKnown entity) {
        return new QName(
            entity.getDefining().getTargetNamespaceURI(),
            entity.getName());
    }

    protected static TWSDLExtension getExtensionOfType(
            TWSDLExtensible extensible,
            Class type) {
        for (TWSDLExtension extension:extensible.extensions()) {
            if (extension.getClass().equals(type)) {
                return extension;
            }
        }

        return null;
    }

    protected TWSDLExtension getAnyExtensionOfType(
        TWSDLExtensible extensible,
        Class type) {
        if (extensible == null) {
            return null;
        }
        for (TWSDLExtension extension:extensible.extensions()) {
            if(extension.getClass().equals(type)) {
                return extension;
            }else if (extension.getClass().equals(MIMEMultipartRelated.class) &&
                    (type.equals(SOAPBody.class) || type.equals(MIMEContent.class)
                            || type.equals(MIMEPart.class))) {
                for (MIMEPart part : ((MIMEMultipartRelated)extension).getParts()) {
                    //bug fix: 5024001
                    TWSDLExtension extn = getExtensionOfType(part, type);
                    if (extn != null) {
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
        WSDLDocument document) {
        com.sun.tools.ws.wsdl.document.Message message = null;
        try {
            message =
                (com.sun.tools.ws.wsdl.document.Message)document.find(
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
        while (reqResNames.contains(uniqueName.toLowerCase(Locale.ENGLISH))) {
            uniqueName = className + cnt;
            cnt++;
        }
        reqResNames.add(uniqueName.toLowerCase(Locale.ENGLISH));
        return uniqueName;
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

    int numPasses = 0;

    protected void warning(Entity entity, String message){
        //avoid duplicate warning for the second pass
        if (numPasses > 1) {
            return;
        }
        if (entity == null) {
            errReceiver.warning(null, message);
        } else {
            errReceiver.warning(entity.getLocator(), message);
        }
    }

    protected void error(Entity entity, String message){
        if (entity == null) {
            errReceiver.error(null, message);
        } else {
            errReceiver.error(entity.getLocator(), message);
        }
        throw new AbortException();
    }

    protected static final String OPERATION_HAS_VOID_RETURN_TYPE =
        "com.sun.xml.ws.processor.modeler.wsdl.operationHasVoidReturnType";
    protected static final String WSDL_PARAMETER_ORDER =
        "com.sun.xml.ws.processor.modeler.wsdl.parameterOrder";
    public static final String WSDL_RESULT_PARAMETER =
        "com.sun.xml.ws.processor.modeler.wsdl.resultParameter";
    public static final String MESSAGE_HAS_MIME_MULTIPART_RELATED_BINDING =
        "com.sun.xml.ws.processor.modeler.wsdl.mimeMultipartRelatedBinding";


    protected ProcessSOAPOperationInfo info;

    private Set _conflictingClassNames;
    protected Map<String,JavaException> _javaExceptions;
    protected Map _faultTypeToStructureMap;
    protected Map<QName, Port> _bindingNameToPortMap;

    private final Set<String> reqResNames = new HashSet<String>();

    public static class ProcessSOAPOperationInfo {

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
    }

    protected WSDLParser parser;
    protected WSDLDocument document;
    protected static final LocatorImpl NULL_LOCATOR = new LocatorImpl();
}
