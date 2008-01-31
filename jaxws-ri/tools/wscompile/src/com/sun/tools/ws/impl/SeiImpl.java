/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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
package com.sun.tools.ws.impl;

import com.sun.codemodel.ClassType;
import com.sun.codemodel.JAnnotationArrayMember;
import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JClass;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JDefinedClass;
import com.sun.tools.ws.api.JavaVisitor;
import com.sun.tools.ws.api.Operation;
import com.sun.tools.ws.api.Sei;
import com.sun.tools.ws.processor.util.DirectoryUtil;
import com.sun.tools.ws.processor.util.IndentingWriter;
import com.sun.tools.ws.wscompile.Options;
import com.sun.tools.ws.wscompile.WsimportOptions;
import com.sun.xml.ws.util.xml.XmlUtil;
import org.jvnet.wom.api.WSDLBoundPortType;
import org.jvnet.wom.api.WSDLOperation;
import org.jvnet.wom.api.WSDLPortType;
import org.jvnet.wom.api.binding.wsdl11.soap.SOAPBinding;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.namespace.QName;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Vivek Pandey
 */
public class SeiImpl implements Sei {

    private final WSDLPortType portType;
    private final JDefinedClass seiClass;
    private final List<Operation> operations = new ArrayList<Operation>();
    private final ModelerContext context;
    private final WsimportOptions options;
    private boolean wrapperStyle;

    public SeiImpl(WSDLPortType portType, ModelerContext context) throws JClassAlreadyExistsException {
        this.portType = portType;
        this.context = context;
        this.options = context.getOptions();
        QName name = portType.getName();
        String seiName = name.getLocalPart();
        BClass classCustomization = portType.getFirstExtension(BClass.class);
        if (classCustomization != null) {
            if (classCustomization.getClassName() != null)
                seiName = classCustomization.getClassName();
        }
        seiName = context.getPackageName(name.getNamespaceURI()) + "." + seiName;


        seiClass = context.getClass(seiName, ClassType.CLASS);

        // If the class has methods it has already been defined, skip it
        if (!seiClass.methods().isEmpty())
            return;

        StringBuffer javadoc = new StringBuffer();
        javadoc.append(portType.getDocumentation());
        if (classCustomization.getJavaDoc() != null)
            javadoc.append(classCustomization.getJavaDoc());

        JAnnotationUse wsa = seiClass.annotate(options.getCodeModel().ref(WebService.class));
        wsa.param("name", name.getLocalPart());
        wsa.param("targetNamespace", name.getNamespaceURI());

        //write @XmlSeeAlso only for version 2.1 or latter
        if (options.target.isLaterThan(Options.Target.V2_1) && (context.getJaxbModel() != null)) {
            List<JClass> objectFactories = context.getJaxbModel().getAllObjectFactories();

            //if there are no object facotires, dont generate @XmlSeeAlso
            if (objectFactories.size() == 0)
                return;

            JAnnotationUse xmlSeeAlso = seiClass.annotate(options.getCodeModel().ref(XmlSeeAlso.class));
            JAnnotationArrayMember paramArray = xmlSeeAlso.paramArray("value");
            for (JClass of : objectFactories) {
                paramArray = paramArray.param(of);
            }
        }


        createOperations();
    }

    private void createOperations() {
        for (WSDLOperation wsdlOperation : portType.getOperations()) {

            Operation op = new OperationImpl(wsdlOperation, seiClass, context);
            QName name = wsdlOperation.getName();
            String opName = name.getLocalPart();

            //see if there is any customization
            BMethod binding = wsdlOperation.getFirstExtension(BMethod.class);
            StringBuffer javadoc = new StringBuffer();
            javadoc.append(wsdlOperation.getDocumentation());
            if (binding != null) {
                if (binding.getJavaDoc() != null) {
                    javadoc.append(binding.getJavaDoc());
                }
                if (binding.getMethodName() != null)
                    opName = binding.getMethodName();
            }
        }
    }

    public WSDLPortType getWSDLPortType() {
        return portType;
    }

    public JClass getType() {
        return seiClass;
    }

    public Collection<Operation> getOperations() {
        return operations;
    }

    public void addOperation(Operation op) {
        this.operations.add(op);
    }

    public <V, P> V accept(JavaVisitor<V, P> visitor, P param) {
        return visitor.sei(this, param);
    }

    private static final String HANDLER_FILE_SUFFIX = "_handler.xml";

    protected void writeHandlerConfig() {
        Element e = options.getHandlerChainConfiguration();
        if (e == null)
            return;
        JAnnotationUse handlerChainAnn = seiClass.annotate(options.getCodeModel().ref(HandlerChain.class));
        NodeList nl = e.getElementsByTagNameNS(
                "http://java.sun.com/xml/ns/javaee", "handler-chain");
        if (nl.getLength() > 0) {
            String fName = seiClass.name() + HANDLER_FILE_SUFFIX;
            //String fName = getHandlerConfigFileName(className);
            handlerChainAnn.param("file", fName);
            generateHandlerChainFile(e, fName);
        }
    }

    private void generateHandlerChainFile(Element hChains, String hcName) {
        File packageDir = DirectoryUtil.getOutputDirectoryFor(seiClass.fullName(), options.destDir);
        File hcFile = new File(packageDir, hcName);

        options.addGeneratedFile(hcFile);

        try {
            IndentingWriter p =
                    new IndentingWriter(
                            new OutputStreamWriter(new FileOutputStream(hcFile)));
            Transformer it = XmlUtil.newTransformer();

            it.setOutputProperty(OutputKeys.METHOD, "xml");
            it.setOutputProperty(OutputKeys.INDENT, "yes");
            it.setOutputProperty(
                    "{http://xml.apache.org/xslt}indent-amount",
                    "2");
            it.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            it.transform(new DOMSource(hChains), new StreamResult(p));
            p.close();
        } catch (Exception e) {
            context.getErrReceiver().error(e);
        }
    }

    private boolean isDocStyle = true;
    private boolean sameParamStyle = true;

    private void writeSOAPBinding() {
        WSDLBoundPortType binding = portType.getOwnerWSDLModel().getBinding(portType.getName());
        if (binding == null)
            return;

        SOAPBinding sb = binding.getFirstExtension(SOAPBinding.class);

        JAnnotationUse soapBindingAnn = null;

        //isDocStyle = port.getStyle() == null || port.getStyle().equals(SOAPStyle.DOCUMENT);
        if (!isDocStyle) {
            soapBindingAnn = seiClass.annotate(javax.jws.soap.SOAPBinding.class);
            soapBindingAnn.param("style", javax.jws.soap.SOAPBinding.Style.RPC);
            wrapperStyle = false;
        }
        if (isDocStyle) {
//            boolean first = true;
//            boolean isWrapper = true;
//            for(Operation operation:port.getOperations()){
//                if(first){
//                    isWrapper = operation.isWrapped();
//                    first = false;
//                    continue;
//                }
//                sameParamStyle = (isWrapper == operation.isWrapped());
//                if(!sameParamStyle)
//                    break;
//            }
//            if(sameParamStyle)
//                port.setWrapped(isWrapper);
//        }
//        if(sameParamStyle && !port.isWrapped()){
//            if(soapBindingAnn == null)
//                soapBindingAnn = cls.annotate(SOAPBinding.class);
//            soapBindingAnn.param("parameterStyle", SOAPBinding.ParameterStyle.BARE);
//        }
        }
    }

}
