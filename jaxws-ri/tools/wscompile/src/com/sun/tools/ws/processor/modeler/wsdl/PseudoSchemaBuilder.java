/*
 * $Id: PseudoSchemaBuilder.java,v 1.8 2005-11-03 22:32:36 kohlert Exp $
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

import static com.sun.tools.ws.processor.modeler.wsdl.WSDLModelerBase.*;
import com.sun.tools.ws.processor.config.ModelInfo;
import com.sun.tools.ws.processor.modeler.wsdl.WSDLModelerBase.ProcessSOAPOperationInfo; 
import com.sun.tools.ws.wsdl.document.schema.SchemaKinds;
import com.sun.tools.ws.wsdl.framework.Extensible;
import com.sun.tools.ws.wsdl.framework.NoSuchEntityException;

import org.xml.sax.InputSource;

import javax.xml.namespace.QName;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.*;
import com.sun.tools.ws.wsdl.document.MessagePart;
import com.sun.tools.ws.wsdl.document.Operation;
import com.sun.tools.ws.wsdl.document.Kinds;
import com.sun.tools.ws.wsdl.document.Message;
import com.sun.tools.ws.wsdl.document.PortType;
import com.sun.tools.ws.wsdl.document.Port;
import com.sun.tools.ws.wsdl.document.Service;
import com.sun.tools.ws.wsdl.document.WSDLDocument;
import com.sun.tools.ws.wsdl.document.Binding;
import com.sun.tools.ws.wsdl.document.BindingOperation;
import com.sun.tools.ws.wsdl.document.jaxws.JAXWSBinding;
import com.sun.tools.ws.wsdl.document.soap.*;



/**
 * @author Vivek Pandey
 *
 * Builds all possible pseudo schemas for async operation ResponseBean to feed to XJC.
 */
public class PseudoSchemaBuilder {

    private final StringWriter buf = new StringWriter();
    private final WSDLDocument wsdlDocument;
    private final ModelInfo modelInfo;
    private WSDLModeler wsdlModeler;
    private final List<InputSource> schemas = new ArrayList<InputSource>();
    private final HashMap<QName, Port> bindingNameToPortMap = new HashMap<QName, Port>();

    public static List<InputSource> build(WSDLModeler wsdlModeler, ModelInfo modelInfo) {
        PseudoSchemaBuilder b = new PseudoSchemaBuilder(wsdlModeler.document, modelInfo);
        b.wsdlModeler = wsdlModeler;
        JAXBModelBuilder analyzer = wsdlModeler.getJAXBModelBuilder();
        b.build();
        for(int i = 0; i < b.schemas.size(); i++){
            InputSource is = b.schemas.get(i);
            is.setSystemId("http://dummy.pseudo-schema#schema"+(i+1));
        }
        return b.schemas;
    }

    private PseudoSchemaBuilder(WSDLDocument _wsdl, ModelInfo _modelInfo) {
        this.wsdlDocument = _wsdl;
        this.modelInfo = _modelInfo;
    }

    private void build() {
        for(Iterator<Service> itr=wsdlDocument.getDefinitions().services(); itr.hasNext(); )
            build(itr.next());
    }

    private void build(Service service) {
        for( Iterator<Port> itr=service.ports(); itr.hasNext(); )
            build(itr.next() );
    }

    private void build(Port port) {
        if(wsdlModeler.isProvider(port))
            return;
        Binding binding = port.resolveBinding(wsdlDocument);

        SOAPBinding soapBinding =
                    (SOAPBinding)getExtensionOfType(binding, SOAPBinding.class);
        //lets try and see if its SOAP 1.2. dont worry about extension flag, its
        // handled much earlier
        if (soapBinding == null) {
                    soapBinding =
                            (SOAPBinding)getExtensionOfType(binding, SOAP12Binding.class);
        }
        if(soapBinding == null)
            return;
        PortType portType = binding.resolvePortType(wsdlDocument);

        QName bindingName = WSDLModelerBase.getQNameOf(binding);

        //we dont want to process the port bound to the binding processed earlier
        if(bindingNameToPortMap.containsKey(bindingName))
            return;

        bindingNameToPortMap.put(bindingName, port);


        for(Iterator itr=binding.operations(); itr.hasNext();){
            BindingOperation bindingOperation = (BindingOperation)itr.next();
            SOAPOperation soapOperation =
            (SOAPOperation)getExtensionOfType(bindingOperation,
                SOAPOperation.class);

            // get only the bounded operations
            Set boundedOps = portType.getOperationsNamed(bindingOperation.getName());
            if(boundedOps.size() != 1)
                continue;
            Operation operation = (Operation)boundedOps.iterator().next();

            // No pseudo schema required for doc/lit
            SOAPStyle bindingStyle = soapBinding.getStyle();
            SOAPStyle operationStyle = (soapOperation != null) ? soapOperation.getStyle() : bindingStyle;
            if(wsdlModeler.isAsync(portType, operation)){
                buildAsync(portType, operation, bindingOperation);
            }
        }
    }

    /**
     * @param portType
     * @param operation
     * @param bindingOperation
     */
    private void buildAsync(PortType portType, Operation operation, BindingOperation bindingOperation) {
        String operationName = getCustomizedOperationName(operation);//operation.getName();
        if(operationName == null)
            return;
        Message outputMessage = null;
        if(operation.getOutput() != null)
            outputMessage = operation.getOutput().resolveMessage(wsdlDocument);
        if(outputMessage != null){
            List<MessagePart> allParts = new ArrayList<MessagePart>(outputMessage.getParts());
            if(allParts.size() > 1)
                build(getOperationName(portType, operationName, bindingOperation.getOutput()), allParts);
        }

    }

    private String getCustomizedOperationName(Operation operation) {
        JAXWSBinding jaxwsCustomization = (JAXWSBinding)getExtensionOfType(operation, JAXWSBinding.class);
        String operationName = (jaxwsCustomization != null)?((jaxwsCustomization.getMethodName() != null)?jaxwsCustomization.getMethodName().getName():null):null;
        if(operationName != null){
            if(wsdlModeler.getEnvironment().getNames().isJavaReservedWord(operationName)){
                return null;
            }

            return operationName;
        }
        return operation.getName();
    }

    private void writeImports(QName elementName, List<MessagePart> parts){
        Set<String> uris = new HashSet<String>();
        for(MessagePart p:parts){
            String ns = p.getDescriptor().getNamespaceURI();
            if(!uris.contains(ns) && !ns.equals("http://www.w3.org/2001/XMLSchema") && !ns.equals(elementName.getNamespaceURI())){
                print("<xs:import namespace=''{0}''/>", ns);
                uris.add(ns);
            }
        }
    }

    boolean asyncRespBeanBinding = false;
    private void build(QName elementName, List<MessagePart> allParts){

        print(
                "<xs:schema xmlns:xs=''http://www.w3.org/2001/XMLSchema''" +
                "           xmlns:jaxb=''http://java.sun.com/xml/ns/jaxb''" +
                "           xmlns:xjc=''http://java.sun.com/xml/ns/jaxb/xjc''" +
                "           jaxb:extensionBindingPrefixes=''xjc''" +
                "           jaxb:version=''1.0''" +
                "           targetNamespace=''{0}''>",
                elementName.getNamespaceURI());

        writeImports(elementName, allParts);

        if(!asyncRespBeanBinding){
            print(
                    "<xs:annotation><xs:appinfo>" +
                    "  <jaxb:schemaBindings>" +
                    "    <jaxb:package name=''{0}'' />" +
                    "  </jaxb:schemaBindings>" +
                    "</xs:appinfo></xs:annotation>",
                    wsdlModeler.getJavaPackage() );
            asyncRespBeanBinding = true;
        }

        print("<xs:element name=''{0}''>", elementName.getLocalPart());
        print("<xs:complexType>");
        print("<xs:sequence>");


        for(MessagePart p:allParts) {
            //rpclit wsdl:part must reference schema type not element, also it must exclude headers and mime parts
            if(p.getDescriptorKind() == SchemaKinds.XSD_ELEMENT){
                print("<xs:element ref=''types:{0}'' xmlns:types=''{1}''/>",p.getDescriptor().getLocalPart(), p.getDescriptor().getNamespaceURI());
            }else{
                print("<xs:element name=''{0}'' type=''{1}'' xmlns=''{2}'' />",
                    p.getName(),
                    p.getDescriptor().getLocalPart(),
                    p.getDescriptor().getNamespaceURI() );
            }
        }

        print("</xs:sequence>");
        print("</xs:complexType>");
        print("</xs:element>");
        print("</xs:schema>");

        // reset the StringWriter, so that next operation element could be written
        if(buf.toString().length() > 0){
            //System.out.println("Response bean Schema for operation========> "+ elementName+"\n\n"+buf);
            InputSource is = new InputSource(new StringReader(buf.toString()));
            schemas.add(is);
            buf.getBuffer().setLength(0);
        }
    }

    private QName getOperationName(PortType portType, String operationName, Extensible binding){
        if(operationName == null)
            return null;
//        String namespaceURI = wsdlDocument.getDefinitions().getTargetNamespaceURI()+"?"+portType.getName()+"?" + operationName;
        String namespaceURI = "";
        return new QName(namespaceURI, operationName+"Response");
    }
   
    private void print( String msg ) {
        print( msg, new Object[0] );
    }
    private void print( String msg, Object arg1 ) {
        print( msg, new Object[]{arg1} );
    }
    private void print( String msg, Object arg1, Object arg2 ) {
        print( msg, new Object[]{arg1, arg2} );
    }
    private void print( String msg, Object arg1, Object arg2, Object arg3 ) {
        print( msg, new Object[]{arg1,arg2,arg3} );
    }
    private void print( String msg, Object[] args ) {
        buf.write(MessageFormat.format(msg,args));
        buf.write('\n');
    }

    private static final Set<QName> nspaceToPackageSchema = new HashSet<QName>();
    private static int  schemaCounter;
}
