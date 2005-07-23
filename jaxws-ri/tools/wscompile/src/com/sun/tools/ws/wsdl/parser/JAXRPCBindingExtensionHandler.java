/*
 * $Id: JAXRPCBindingExtensionHandler.java,v 1.2 2005-07-23 04:11:06 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.ws.wsdl.parser;

import java.util.Iterator;
import java.io.IOException;

import javax.xml.namespace.QName;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.org.apache.xpath.internal.XPathAPI;
import com.sun.tools.ws.wsdl.document.BindingOperation;
import com.sun.tools.ws.wsdl.document.Documentation;
import com.sun.tools.ws.wsdl.document.MessagePart;
import com.sun.tools.ws.wsdl.document.Operation;
import com.sun.tools.ws.wsdl.document.Service;
import com.sun.tools.ws.wsdl.document.jaxrpc.CustomName;
import com.sun.tools.ws.wsdl.document.jaxrpc.JAXRPCBinding;
import com.sun.tools.ws.wsdl.document.jaxrpc.JAXRPCBindingsConstants;
import com.sun.tools.ws.wsdl.document.jaxrpc.Parameter;
import com.sun.tools.ws.wsdl.document.schema.SchemaKinds;
import com.sun.tools.ws.wsdl.framework.Extensible;
import com.sun.tools.ws.wsdl.framework.Extension;
import com.sun.tools.ws.wsdl.framework.ParserContext;
import com.sun.tools.ws.wsdl.framework.WriterContext;
import com.sun.tools.ws.util.xml.XmlUtil;


/**
 * @author Vivek Pandey
 *
 * jaxrpc:bindings exension handler.
 *
 */
public class JAXRPCBindingExtensionHandler extends ExtensionHandlerBase {

    /**
     *
     */
    public JAXRPCBindingExtensionHandler() {
    }

    /* (non-Javadoc)
     * @see ExtensionHandler#getNamespaceURI()
     */
    public String getNamespaceURI() {
        return JAXRPCBindingsConstants.NS_JAXRPC_BINDINGS;
    }


    /**
     * @param context
     * @param parent
     * @param e
     */
    private boolean parseGlobalJAXRPCBindings(ParserContext context, Extensible parent, Element e) {
        context.push();
        context.registerNamespaces(e);
        JAXRPCBinding jaxrpcBinding = new JAXRPCBinding();

        String attr = XmlUtil.getAttributeOrNull(e, JAXRPCBindingsConstants.WSDL_LOCATION_ATTR);
        if (attr != null) {
            jaxrpcBinding.setWsdlLocation(attr);
        }

        attr = XmlUtil.getAttributeOrNull(e, JAXRPCBindingsConstants.NODE_ATTR);
        if (attr != null) {
            jaxrpcBinding.setNode(attr);
        }

        attr = XmlUtil.getAttributeOrNull(e, JAXRPCBindingsConstants.VERSION_ATTR);
        if (attr != null) {
            jaxrpcBinding.setVersion(attr);
        }

        for(Iterator iter = XmlUtil.getAllChildren(e); iter.hasNext();){
            Element e2 = Util.nextElement(iter);
            if (e2 == null)
                break;

            if(XmlUtil.matchesTagNS(e2, JAXRPCBindingsConstants.PACKAGE)){
                parsePackage(context, jaxrpcBinding, e2);
            }else if(XmlUtil.matchesTagNS(e2, JAXRPCBindingsConstants.ENABLE_WRAPPER_STYLE)){
                parseWrapperStyle(context, jaxrpcBinding, e2);
            }else if(XmlUtil.matchesTagNS(e2, JAXRPCBindingsConstants.ENABLE_ASYNC_MAPPING)){
                parseAsynMapping(context, jaxrpcBinding, e2);
            }else if(XmlUtil.matchesTagNS(e2, JAXRPCBindingsConstants.ENABLE_ADDITIONAL_SOAPHEADER_MAPPING)){
                parseAdditionalSOAPHeaderMapping(context, jaxrpcBinding, e2);
            }else if(XmlUtil.matchesTagNS(e2, JAXRPCBindingsConstants.ENABLE_MIME_CONTENT)){
                parseMimeContent(context, jaxrpcBinding, e2);
            }else if(XmlUtil.matchesTagNS(e2, JAXRPCBindingsConstants.PROVIDER)){
                parseProvider(context, jaxrpcBinding, e2);
            }else if(XmlUtil.matchesTagNS(e2, JAXRPCBindingsConstants.JAXB_BINDINGS)){
                parseJAXBBindings(context, jaxrpcBinding, e2);
            }else{
                Util.fail(
                    "parsing.invalidExtensionElement",
                    e2.getTagName(),
                    e2.getNamespaceURI());
                return false;
            }
        }
        parent.addExtension(jaxrpcBinding);
        context.pop();
        context.fireDoneParsingEntity(
                JAXRPCBindingsConstants.JAXRPC_BINDINGS,
                jaxrpcBinding);
        return true;
    }

    /**
     * @param context
     * @param jaxrpcBinding
     * @param e2
     */
    private void parseProvider(ParserContext context, Extensible parent, Element e) {
        String val = e.getTextContent();
        if(val == null)
            return;
        if(val.equals("false") || val.equals("0")){
            ((JAXRPCBinding)parent).setProvider(Boolean.FALSE);
        }else if(val.equals("true") || val.equals("1")){
            ((JAXRPCBinding)parent).setProvider(Boolean.TRUE);
        }

    }

    /**
     * @param context
     * @param jaxrpcBinding
     * @param e2
     */
    private void parseJAXBBindings(ParserContext context, Extensible parent, Element e) {
        //System.out.println("In handleJAXBBindingsExtension: " + e.getNodeName());
        JAXRPCBinding binding = (JAXRPCBinding)parent;
        binding.addJaxbBindings(e);
    }

    /**
     * @param context
     * @param parent
     * @param e
     */
    private void parsePackage(ParserContext context, Extensible parent, Element e) {
        //System.out.println("In handlePackageExtension: " + e.getNodeName());
        String packageName = XmlUtil.getAttributeOrNull(e, JAXRPCBindingsConstants.NAME_ATTR);
        JAXRPCBinding binding = (JAXRPCBinding)parent;
        binding.setJaxrpcPackage(new CustomName(packageName, getJavaDoc(e)));
    }

    /**
     * @param context
     * @param parent
     * @param e
     */
    private void parseWrapperStyle(ParserContext context, Extensible parent, Element e) {
        //System.out.println("In handleWrapperStyleExtension: " + e.getNodeName());
        String val = e.getTextContent();
        if(val == null)
            return;
        if(val.equals("false") || val.equals("0")){
            ((JAXRPCBinding)parent).setEnableWrapperStyle(Boolean.FALSE);
        }else if(val.equals("true") || val.equals("1")){
            ((JAXRPCBinding)parent).setEnableWrapperStyle(Boolean.TRUE);
        }
    }

    /**
     * @param context
     * @param parent
     * @param e
     */
    private void parseAdditionalSOAPHeaderMapping(ParserContext context, Extensible parent, Element e) {
        //System.out.println("In handleAdditionalSOAPHeaderExtension: " + e.getNodeName());
        String val = e.getTextContent();
        if(val == null)
            return;
        if(val.equals("false") || val.equals("0")){
            ((JAXRPCBinding)parent).setEnableAdditionalHeaderMapping(Boolean.FALSE);
        }else if(val.equals("true") || val.equals("1")){
            ((JAXRPCBinding)parent).setEnableAdditionalHeaderMapping(Boolean.TRUE);
        }
    }

    /**
     * @param context
     * @param parent
     * @param e
     */
    private void parseAsynMapping(ParserContext context, Extensible parent, Element e) {
        //System.out.println("In handleAsynMappingExtension: " + e.getNodeName());
        String val = e.getTextContent();
        if(val == null)
            return;
        if(val.equals("false") || val.equals("0")){
            ((JAXRPCBinding)parent).setEnableAsyncMapping(Boolean.FALSE);
        }else if(val.equals("true") || val.equals("1")){
            ((JAXRPCBinding)parent).setEnableAsyncMapping(Boolean.TRUE);
        }
    }

    /**
     * @param context
     * @param parent
     * @param e
     * @return TODO
     */
    private void parseMimeContent(ParserContext context, Extensible parent, Element e) {
        //System.out.println("In handleMimeContentExtension: " + e.getNodeName());
        String val = e.getTextContent();
        if(val == null)
            return;
        if(val.equals("false") || val.equals("0")){
            ((JAXRPCBinding)parent).setEnableMimeContentMapping(Boolean.FALSE);
        }else if(val.equals("true") || val.equals("1")){
            ((JAXRPCBinding)parent).setEnableMimeContentMapping(Boolean.TRUE);
        }
    }

    /**
     * @param context
     * @param jaxrpcBinding
     * @param e2
     */
    private void parseMethod(ParserContext context, JAXRPCBinding jaxrpcBinding, Element e) {
        String methodName = XmlUtil.getAttributeOrNull(e, JAXRPCBindingsConstants.NAME_ATTR);
        String javaDoc = getJavaDoc(e);
        CustomName name = new CustomName(methodName, javaDoc);
        jaxrpcBinding.setMethodName(name);
    }

    /**
     * @param context
     * @param jaxrpcBinding
     * @param e2
     */
    private void parseParameter(ParserContext context, JAXRPCBinding jaxrpcBinding, Element e) {
        String part = XmlUtil.getAttributeOrNull(e, JAXRPCBindingsConstants.PART_ATTR);

        //evaluate this XPath
        NodeList nlst;
        try {
            nlst = XPathAPI.selectNodeList(e.getOwnerDocument(), part, e.getOwnerDocument().getFirstChild());
        } catch( TransformerException ex ) {
            ex.printStackTrace();
            return; // abort processing this <jaxrpc:bindings>
        }

        if( nlst.getLength()==0 ) {
            //System.out.println("ERROR: XPATH evaluated node lenght is 0!");
            return; // abort
        }

        if( nlst.getLength()!=1 ) {
            //System.out.println("ERROR: XPATH has more than one evaluated target!");
            return; // abort
        }

        Node rnode = nlst.item(0);
        if(!(rnode instanceof Element )) {
            //System.out.println("ERROR:XPATH evaluated node is not instanceof Element!");
            return; // abort
        }

        Element msgPartElm = (Element)rnode;

        MessagePart msgPart = new MessagePart();

        String partName = XmlUtil.getAttributeOrNull(msgPartElm, "name");
        if(partName == null)
            return;
        msgPart.setName(partName);

        String val = XmlUtil.getAttributeOrNull(msgPartElm, "element");
        if(val != null){
            msgPart.setDescriptor(context.translateQualifiedName(val));
            msgPart.setDescriptorKind(SchemaKinds.XSD_ELEMENT);
        }else{
            val = XmlUtil.getAttributeOrNull(msgPartElm, "type");
            if(val == null)
                    return;
            msgPart.setDescriptor(context.translateQualifiedName(val));
            msgPart.setDescriptorKind(SchemaKinds.XSD_TYPE);
        }

        String element = XmlUtil.getAttributeOrNull(e, JAXRPCBindingsConstants.ELEMENT_ATTR);
        String name = XmlUtil.getAttributeOrNull(e, JAXRPCBindingsConstants.NAME_ATTR);
        QName elementName = context.translateQualifiedName(element);
        jaxrpcBinding.addParameter(new Parameter(msgPart.getName(), elementName, name));
    }

    /**
     * @param context
     * @param jaxrpcBinding
     * @param e2
     */
    private void parseClass(ParserContext context, JAXRPCBinding jaxrpcBinding, Element e) {
        String className = XmlUtil.getAttributeOrNull(e, JAXRPCBindingsConstants.NAME_ATTR);
        String javaDoc = getJavaDoc(e);
        jaxrpcBinding.setClassName(new CustomName(className, javaDoc));
    }


    /**
     * @param context
     * @param jaxrpcBinding
     * @param e2
     */
    private void parseException(ParserContext context, JAXRPCBinding jaxrpcBinding, Element e) {
        for(Iterator iter = XmlUtil.getAllChildren(e); iter.hasNext();){
            Element e2 = Util.nextElement(iter);
            if (e2 == null)
                break;
            if(XmlUtil.matchesTagNS(e2, JAXRPCBindingsConstants.CLASS)){
                String className = XmlUtil.getAttributeOrNull(e2, JAXRPCBindingsConstants.NAME_ATTR);
                String javaDoc = getJavaDoc(e2);
                jaxrpcBinding.setException(new com.sun.tools.ws.wsdl.document.jaxrpc.Exception(new CustomName(className, javaDoc)));
            }
        }
    }

    /* (non-Javadoc)
     * @see ExtensionHandlerBase#handleDefinitionsExtension(ParserContext, Extensible, org.w3c.dom.Element)
     */
    protected boolean handleDefinitionsExtension(ParserContext context, Extensible parent, Element e) {
        if(XmlUtil.matchesTagNS(e, JAXRPCBindingsConstants.JAXRPC_BINDINGS)){
            parseGlobalJAXRPCBindings(context, parent, e);
        }else {
            Util.fail(
                "parsing.invalidExtensionElement",
                e.getTagName(),
                e.getNamespaceURI());
            return false;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see ExtensionHandlerBase#handleTypesExtension(ParserContext, Extensible, org.w3c.dom.Element)
     */
    protected boolean handleTypesExtension(ParserContext context, Extensible parent, Element e) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see ExtensionHandlerBase#handlePortTypeExtension(ParserContext, Extensible, org.w3c.dom.Element)
     */
    protected boolean handlePortTypeExtension(ParserContext context, Extensible parent, Element e) {
        if(XmlUtil.matchesTagNS(e, JAXRPCBindingsConstants.JAXRPC_BINDINGS)){
            context.push();
            context.registerNamespaces(e);
            JAXRPCBinding jaxrpcBinding = new JAXRPCBinding();

            for(Iterator iter = XmlUtil.getAllChildren(e); iter.hasNext();){
                Element e2 = Util.nextElement(iter);
                if (e2 == null)
                    break;

                if(XmlUtil.matchesTagNS(e2, JAXRPCBindingsConstants.ENABLE_WRAPPER_STYLE)){
                    parseWrapperStyle(context, jaxrpcBinding, e2);
                }else if(XmlUtil.matchesTagNS(e2, JAXRPCBindingsConstants.ENABLE_ASYNC_MAPPING)){
                    parseAsynMapping(context, jaxrpcBinding, e2);
                }else if(XmlUtil.matchesTagNS(e2, JAXRPCBindingsConstants.CLASS)){
                    parseClass(context, jaxrpcBinding, e2);
                }else{
                    Util.fail(
                        "parsing.invalidExtensionElement",
                        e2.getTagName(),
                        e2.getNamespaceURI());
                    return false;
                }
            }
            parent.addExtension(jaxrpcBinding);
            context.pop();
            context.fireDoneParsingEntity(
                    JAXRPCBindingsConstants.JAXRPC_BINDINGS,
                    jaxrpcBinding);
            return true;
        }else {
            Util.fail(
                "parsing.invalidExtensionElement",
                e.getTagName(),
                e.getNamespaceURI());
            return false;
        }
    }



    /* (non-Javadoc)
     * @see ExtensionHandlerBase#handleOperationExtension(ParserContext, Extensible, org.w3c.dom.Element)
     */
    protected boolean handleOperationExtension(ParserContext context, Extensible parent, Element e) {
        if(XmlUtil.matchesTagNS(e, JAXRPCBindingsConstants.JAXRPC_BINDINGS)){
            if(parent instanceof Operation){
                return handlePortTypeOperation(context, (Operation)parent, e);
            }else if(parent instanceof BindingOperation){
                return handleBindingOperation(context, (BindingOperation)parent, e);
            }
        }else {
            Util.fail(
                "parsing.invalidExtensionElement",
                e.getTagName(),
                e.getNamespaceURI());
            return false;
        }
        return false;
    }

    /**
     * @param context
     * @param operation
     * @param e
     * @return
     */
    private boolean handleBindingOperation(ParserContext context, BindingOperation operation, Element e) {
        if(XmlUtil.matchesTagNS(e, JAXRPCBindingsConstants.JAXRPC_BINDINGS)){
            context.push();
            context.registerNamespaces(e);
            JAXRPCBinding jaxrpcBinding = new JAXRPCBinding();

            for(Iterator iter = XmlUtil.getAllChildren(e); iter.hasNext();){
                Element e2 = Util.nextElement(iter);
                if (e2 == null)
                    break;

                if(XmlUtil.matchesTagNS(e2, JAXRPCBindingsConstants.ENABLE_ADDITIONAL_SOAPHEADER_MAPPING)){
                    parseAdditionalSOAPHeaderMapping(context, jaxrpcBinding, e2);
                }else if(XmlUtil.matchesTagNS(e2, JAXRPCBindingsConstants.ENABLE_MIME_CONTENT)){
                    parseMimeContent(context, jaxrpcBinding, e2);
                }else if(XmlUtil.matchesTagNS(e2, JAXRPCBindingsConstants.PARAMETER)){
                    parseParameter(context, jaxrpcBinding, e2);
                }else if(XmlUtil.matchesTagNS(e2, JAXRPCBindingsConstants.EXCEPTION)){
                    parseException(context, jaxrpcBinding, e2);
                }else{
                    Util.fail(
                        "parsing.invalidExtensionElement",
                        e2.getTagName(),
                        e2.getNamespaceURI());
                    return false;
                }
            }
            operation.addExtension(jaxrpcBinding);
            context.pop();
            context.fireDoneParsingEntity(
                    JAXRPCBindingsConstants.JAXRPC_BINDINGS,
                    jaxrpcBinding);
            return true;
        }else {
            Util.fail(
                "parsing.invalidExtensionElement",
                e.getTagName(),
                e.getNamespaceURI());
            return false;
        }
    }

    /**
     * @param context
     * @param operation
     * @param e
     * @return
     */
    private boolean handlePortTypeOperation(ParserContext context, Operation parent, Element e) {
        context.push();
        context.registerNamespaces(e);
        JAXRPCBinding jaxrpcBinding = new JAXRPCBinding();

        for(Iterator iter = XmlUtil.getAllChildren(e); iter.hasNext();){
            Element e2 = Util.nextElement(iter);
            if (e2 == null)
                break;

            if(XmlUtil.matchesTagNS(e2, JAXRPCBindingsConstants.ENABLE_WRAPPER_STYLE)){
                parseWrapperStyle(context, jaxrpcBinding, e2);
            }else if(XmlUtil.matchesTagNS(e2, JAXRPCBindingsConstants.ENABLE_ASYNC_MAPPING)){
                parseAsynMapping(context, jaxrpcBinding, e2);
            }else if(XmlUtil.matchesTagNS(e2, JAXRPCBindingsConstants.METHOD)){
                parseMethod(context, jaxrpcBinding, e2);
            }else if(XmlUtil.matchesTagNS(e2, JAXRPCBindingsConstants.PARAMETER)){
                parseParameter(context, jaxrpcBinding, e2);
            }else{
                Util.fail(
                    "parsing.invalidExtensionElement",
                    e2.getTagName(),
                    e2.getNamespaceURI());
                return false;
            }
        }
        parent.addExtension(jaxrpcBinding);
        context.pop();
        context.fireDoneParsingEntity(
                JAXRPCBindingsConstants.JAXRPC_BINDINGS,
                jaxrpcBinding);
        return true;
    }

    /* (non-Javadoc)
     * @see ExtensionHandlerBase#handleBindingExtension(ParserContext, Extensible, org.w3c.dom.Element)
     */
    protected boolean handleBindingExtension(ParserContext context, Extensible parent, Element e) {
        if(XmlUtil.matchesTagNS(e, JAXRPCBindingsConstants.JAXRPC_BINDINGS)){
            context.push();
            context.registerNamespaces(e);
            JAXRPCBinding jaxrpcBinding = new JAXRPCBinding();

            for(Iterator iter = XmlUtil.getAllChildren(e); iter.hasNext();){
                Element e2 = Util.nextElement(iter);
                if (e2 == null)
                    break;

                if(XmlUtil.matchesTagNS(e2, JAXRPCBindingsConstants.ENABLE_ADDITIONAL_SOAPHEADER_MAPPING)){
                    parseAdditionalSOAPHeaderMapping(context, jaxrpcBinding, e2);
                }else if(XmlUtil.matchesTagNS(e2, JAXRPCBindingsConstants.ENABLE_MIME_CONTENT)){
                    parseMimeContent(context, jaxrpcBinding, e2);
                }else{
                    Util.fail(
                        "parsing.invalidExtensionElement",
                        e2.getTagName(),
                        e2.getNamespaceURI());
                    return false;
                }
            }
            parent.addExtension(jaxrpcBinding);
            context.pop();
            context.fireDoneParsingEntity(
                    JAXRPCBindingsConstants.JAXRPC_BINDINGS,
                    jaxrpcBinding);
            return true;
        }else {
            Util.fail(
                "parsing.invalidExtensionElement",
                e.getTagName(),
                e.getNamespaceURI());
            return false;
        }
    }

    /* (non-Javadoc)
     * @see ExtensionHandlerBase#handleInputExtension(ParserContext, Extensible, org.w3c.dom.Element)
     */
    protected boolean handleInputExtension(ParserContext context, Extensible parent, Element e) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see ExtensionHandlerBase#handleOutputExtension(ParserContext, Extensible, org.w3c.dom.Element)
     */
    protected boolean handleOutputExtension(ParserContext context, Extensible parent, Element e) {
        // TODO Auto-generated method stub
        return false;
    }

    /* (non-Javadoc)
     * @see ExtensionHandlerBase#handleFaultExtension(ParserContext, Extensible, org.w3c.dom.Element)
     */
    protected boolean handleFaultExtension(ParserContext context, Extensible parent, Element e) {
        if(XmlUtil.matchesTagNS(e, JAXRPCBindingsConstants.JAXRPC_BINDINGS)){
            context.push();
            context.registerNamespaces(e);
            JAXRPCBinding jaxrpcBinding = new JAXRPCBinding();

            for(Iterator iter = XmlUtil.getAllChildren(e); iter.hasNext();){
                Element e2 = Util.nextElement(iter);
                if (e2 == null)
                    break;
                if(XmlUtil.matchesTagNS(e2, JAXRPCBindingsConstants.CLASS)){
                    parseClass(context, jaxrpcBinding, e2);
                }else{
                    Util.fail(
                        "parsing.invalidExtensionElement",
                        e2.getTagName(),
                        e2.getNamespaceURI());
                    return false;
                }
            }
            parent.addExtension(jaxrpcBinding);
            context.pop();
            context.fireDoneParsingEntity(
                    JAXRPCBindingsConstants.JAXRPC_BINDINGS,
                    jaxrpcBinding);
            return true;
        }else {
            Util.fail(
                "parsing.invalidExtensionElement",
                e.getTagName(),
                e.getNamespaceURI());
            return false;
        }
    }

    /* (non-Javadoc)
     * @see ExtensionHandlerBase#handleServiceExtension(ParserContext, Extensible, org.w3c.dom.Element)
     */
    protected boolean handleServiceExtension(ParserContext context, Extensible parent, Element e) {
        if(XmlUtil.matchesTagNS(e, JAXRPCBindingsConstants.JAXRPC_BINDINGS)){
            context.push();
            context.registerNamespaces(e);
            JAXRPCBinding jaxrpcBinding = new JAXRPCBinding();

            for(Iterator iter = XmlUtil.getAllChildren(e); iter.hasNext();){
                Element e2 = Util.nextElement(iter);
                if (e2 == null)
                    break;
                if(XmlUtil.matchesTagNS(e2, JAXRPCBindingsConstants.CLASS)){
                    parseClass(context, jaxrpcBinding, e2);
                }else{
                    Util.fail(
                        "parsing.invalidExtensionElement",
                        e2.getTagName(),
                        e2.getNamespaceURI());
                    return false;
                }
            }
            parent.addExtension(jaxrpcBinding);
            context.pop();
            context.fireDoneParsingEntity(
                    JAXRPCBindingsConstants.JAXRPC_BINDINGS,
                    jaxrpcBinding);
            return true;
        }else {
            Util.fail(
                "parsing.invalidExtensionElement",
                e.getTagName(),
                e.getNamespaceURI());
            return false;
        }
    }

    /* (non-Javadoc)
     * @see ExtensionHandlerBase#handlePortExtension(ParserContext, Extensible, org.w3c.dom.Element)
     */
    protected boolean handlePortExtension(ParserContext context, Extensible parent, Element e) {
        if(XmlUtil.matchesTagNS(e, JAXRPCBindingsConstants.JAXRPC_BINDINGS)){
            context.push();
            context.registerNamespaces(e);
            JAXRPCBinding jaxrpcBinding = new JAXRPCBinding();

            for(Iterator iter = XmlUtil.getAllChildren(e); iter.hasNext();){
                Element e2 = Util.nextElement(iter);
                if (e2 == null)
                    break;

                if(XmlUtil.matchesTagNS(e2, JAXRPCBindingsConstants.PROVIDER)){
                    parseProvider(context, jaxrpcBinding, e2);
                }else if(XmlUtil.matchesTagNS(e2, JAXRPCBindingsConstants.METHOD)){
                    parseMethod(context, jaxrpcBinding, e2);
                }else{
                    Util.fail(
                        "parsing.invalidExtensionElement",
                        e2.getTagName(),
                        e2.getNamespaceURI());
                    return false;
                }
            }
            parent.addExtension(jaxrpcBinding);
            context.pop();
            context.fireDoneParsingEntity(
                    JAXRPCBindingsConstants.JAXRPC_BINDINGS,
                    jaxrpcBinding);
            return true;
        }else {
            Util.fail(
                "parsing.invalidExtensionElement",
                e.getTagName(),
                e.getNamespaceURI());
            return false;
        }
    }

    /* (non-Javadoc)
     * @see ExtensionHandlerBase#handleMIMEPartExtension(ParserContext, Extensible, org.w3c.dom.Element)
     */
    protected boolean handleMIMEPartExtension(ParserContext context, Extensible parent, Element e) {
        // TODO Auto-generated method stub
        return false;
    }

    private String getJavaDoc(Element e){
        for(Iterator iter = XmlUtil.getAllChildren(e); iter.hasNext();){
            Element e2 = Util.nextElement(iter);
            if (e2 == null)
                break;
            if(XmlUtil.matchesTagNS(e2, JAXRPCBindingsConstants.JAVADOC)){
                return e2.getNodeValue();
            }
        }
        return null;
    }

    public void doHandleExtension(WriterContext context, Extension extension)
        throws IOException {
System.out.println("JAXRPCBindingExtensionHandler doHandleExtension: "+extension);
        // NOTE - this ugliness can be avoided by moving all the XML parsing/writing code
        // into the document classes themselves
        if (extension instanceof JAXRPCBinding) {
            JAXRPCBinding binding = (JAXRPCBinding) extension;
            System.out.println("binding.getElementName: "+binding.getElementName());
            context.writeStartTag(binding.getElementName());
            context.writeStartTag(JAXRPCBindingsConstants.ENABLE_WRAPPER_STYLE);
            context.writeChars(binding.isEnableWrapperStyle().toString());
            context.writeEndTag(JAXRPCBindingsConstants.ENABLE_WRAPPER_STYLE);
            context.writeEndTag(binding.getElementName());
        } else {
            throw new IllegalArgumentException();
        }
    }

}
