/*
 * $Id: Internalizer.java,v 1.1 2005-05-24 14:07:28 bbissett Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.tools.ws.wsdl.parser;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.tools.xjc.util.DOMUtils;
import com.sun.tools.ws.processor.util.ProcessorEnvironment;
import com.sun.tools.ws.util.JAXRPCUtils;
import com.sun.xml.ws.util.localization.Localizable;
import com.sun.xml.ws.util.localization.LocalizableMessageFactory;
import com.sun.tools.ws.wsdl.document.jaxrpc.JAXRPCBindingsConstants;
import com.sun.tools.ws.util.xml.XmlUtil;


/**
 * Internalizes external binding declarations.
 * @author Vivek Pandey
 */
public class Internalizer {
    private Map<String, Document> wsdlDocuments;
    private static final XPathFactory xpf = XPathFactory.newInstance();
    private final XPath xpath = xpf.newXPath();
    private final  LocalizableMessageFactory messageFactory = new LocalizableMessageFactory("com.sun.tools.ws.resources.wsdl");;
    private ProcessorEnvironment env;
    public  void transform(Set<Element> jaxrpcBindings, Map<String, Document> wsdlDocuments, ProcessorEnvironment env) {
        if(jaxrpcBindings == null)
            return;
        this.env = env;
        this.wsdlDocuments = wsdlDocuments;
        Map targetNodes = new HashMap<Element, Node>();

        // identify target nodes for all <jaxrpc:bindings>
        for(Element jaxrpcBinding : jaxrpcBindings) {
            // initially, the inherited context is itself
            buildTargetNodeMap( jaxrpcBinding, jaxrpcBinding, targetNodes );
        }

        // then move them to their respective positions.
        for( Element jaxrpcBinding : jaxrpcBindings) {
            move( jaxrpcBinding, targetNodes );
        }

    }

    /**
     * Validates attributes of a &lt;JAXRPC:bindings> element.
     */
    private void validate( Element bindings ) {
        NamedNodeMap atts = bindings.getAttributes();
        for( int i=0; i<atts.getLength(); i++ ) {
            Attr a = (Attr)atts.item(i);
            if( a.getNamespaceURI()!=null )
                continue;   // all foreign namespace OK.
            if( a.getLocalName().equals("node") )
                continue;
            if( a.getLocalName().equals("wsdlLocation"))
                continue;

            // TODO: flag error for this undefined attribute
        }
    }

    /**
     * Gets the DOM tree associated with the specified system ID,
     * or null if none is found.
     */
    public Document get( String systemId ) {
        Document doc = wsdlDocuments.get(systemId);

        if( doc==null && systemId.startsWith("file:/") && !systemId.startsWith("file://") ) {
            // As of JDK1.4, java.net.URL.toExternal method returns URLs like
            // "file:/abc/def/ghi" which is an incorrect file protocol URL according to RFC1738.
            // Some other correctly functioning parts return the correct URLs ("file:///abc/def/ghi"),
            // and this descripancy breaks DOM look up by system ID.

            // this extra check solves this problem.
            doc = wsdlDocuments.get( "file://"+systemId.substring(5) );
        }

        return doc;
    }

    /**
     * Determines the target node of the "bindings" element
     * by using the inherited target node, then put
     * the result into the "result" map.
     */
    private void buildTargetNodeMap( Element bindings, Node inheritedTarget, Map<Element, Node> result ) {
        // start by the inherited target
        Node target = inheritedTarget;

        validate(bindings); // validate this node

        // look for @wsdlLocation
        if( bindings.getAttributeNode("wsdlLocation")!=null ) {
            String wsdlLocation = bindings.getAttribute("wsdlLocation");

            try {
                // absolutize this URI.
                // TODO: use the URI class
                // TODO: honor xml:base
                wsdlLocation = new URL(new URL(bindings.getOwnerDocument().getBaseURI()),
                        wsdlLocation ).toExternalForm();
            } catch( MalformedURLException e ) {
                wsdlLocation = JAXRPCUtils.absolutize(JAXRPCUtils.getFileOrURLName(wsdlLocation));
            }

            //target = wsdlDocuments.get(wsdlLocation);
            target = get(wsdlLocation);
            if(target==null) {
                error("internalizer.targetNotFound", new Object[]{wsdlLocation});
                return; // abort processing this <jaxrpc:bindings>
            }
        }

        // look for @node
        if( bindings.getAttributeNode("node")!=null ) {
            String nodeXPath = bindings.getAttribute("node");

            // evaluate this XPath
            NodeList nlst;
            try {
                xpath.setNamespaceContext(new NamespaceContextImpl(bindings));
                nlst = (NodeList)xpath.evaluate(nodeXPath,target,XPathConstants.NODESET);
            } catch (XPathExpressionException e) {
                error("internalizer.XPathEvaluationError", new Object[]{e.getMessage()});
                if(env.verbose())
                    e.printStackTrace();
                return; // abort processing this <jaxb:bindings>
            }

            if( nlst.getLength()==0 ) {
                error("internalizer.XPathEvaluatesToNoTarget", new Object[]{nodeXPath});
                return; // abort
            }

            if( nlst.getLength()!=1 ) {
                error("internalizer.XPathEvaulatesToTooManyTargets", new Object[]{nodeXPath, nlst.getLength()});
                return; // abort
            }

            Node rnode = nlst.item(0);
            if(!(rnode instanceof Element )) {
                error("internalizer.XPathEvaluatesToNonElement", new Object[]{nodeXPath});
                return; // abort
            }
            target = (Element)rnode;
        }

        // update the result map
        result.put( bindings, target );

        // look for child <jaxrpc:bindings> and process them recursively
        Element[] children = DOMUtils.getChildElements( bindings, JAXRPCBindingsConstants.NS_JAXRPC_BINDINGS, "bindings" );
        for( int i=0; i<children.length; i++ )
            buildTargetNodeMap( children[i], target, result );
    }

    /**
     * Moves JAXRPC customizations under their respective target nodes.
     */
    private void move( Element bindings, Map<Element, Node> targetNodes ) {
        Node target = targetNodes.get(bindings);
        if(target==null)
            // this must be the result of an error on the external binding.
            // recover from the error by ignoring this node
            return;

        Element[] children = DOMUtils.getChildElements(bindings);
        for( int i=0; i<children.length; i++ ) {
            Element item = children[i];

            if("bindings".equals(item.getLocalName())){
                target = targetNodes.get(item);
                if(!(target instanceof Element )) {
                    //warn("internalizer.targetNotAnElement", new Object[]{});
                    return; // abort
                }


                Element[] bindingChild = getBindingChildren(item);
                for(int j = 0; j< bindingChild.length; j++){
                    // move this node under the target
                    moveUnder(bindingChild[j], (Element)target );
                }
            }
        }
    }

    private Element getChildElement(Element e, QName item){
        Element[] children = DOMUtils.getChildElements(e);
        for(int i = 0; i < children.length; i++){
            if(XmlUtil.matchesTagNS(children[i], item))
                return children[i];
        }
        return null;
    }

    private boolean hasJAXBBindingElement(Element e){
        Element[] children = DOMUtils.getChildElements(e);
        for(int i = 0; i < children.length; i++){
            if(children[i].getNamespaceURI().equals(JAXRPCBindingsConstants.NS_JAXB_BINDINGS))
                return true;
        }
        return false;
    }

    private boolean isJAXBBindingElement(Element e){
        if((e.getNamespaceURI() != null ) && e.getNamespaceURI().equals(JAXRPCBindingsConstants.NS_JAXB_BINDINGS))
            return true;
        return false;
    }

    private boolean isJAXRPCBindingElement(Element e){
        if((e.getNamespaceURI() != null ) && e.getNamespaceURI().equals(JAXRPCBindingsConstants.NS_JAXRPC_BINDINGS))
            return true;
        return false;
    }


    private Element getJAXBBindingElement(Element e){
        if(e.getAttributeNode("node") != null){
            Element[] children = DOMUtils.getChildElements(e);
            for(int i = 0; i < children.length; i++){
                if(children[i].getNamespaceURI().equals(JAXRPCBindingsConstants.NS_JAXB_BINDINGS)){
                    return children[i];
                }
            }
        }
        return null;
    }

    private Element[] getBindingChildren(Element e){
        if(e.getAttributeNode("node") != null){
            return DOMUtils.getChildElements(e);
        }
        return null;
    }

    /**
     * Moves the "decl" node under the "target" node.
     *
     * @param decl
     *      A JAXRPC customization element (e.g., &lt;jaxrpc:class>)
     *
     * @param target
     *      XML wsdl element under which the declaration should move.
     *      For example, &lt;xs:element>
     */
    private void moveUnder( Element decl, Element target ) {

        //if there is @node on decl and has a child element jaxb:bindings, move it under the target
        //Element jaxb = getJAXBBindingElement(decl);
        if(isJAXBBindingElement(decl)){
            //add jaxb namespace declaration
            if(!target.hasAttributeNS(Constants.NS_XMLNS, "jaxb")){
                target.setAttributeNS(Constants.NS_XMLNS, "xmlns:jaxb", JAXRPCBindingsConstants.NS_JAXB_BINDINGS);
            }

            //add jaxb:bindings version info. Lets put it to 1.0, may need to change latter
            if(!target.hasAttributeNS(JAXRPCBindingsConstants.NS_JAXB_BINDINGS, "version")){
                target.setAttributeNS(JAXRPCBindingsConstants.NS_JAXB_BINDINGS, "jaxb:version", JAXRPCBindingsConstants.JAXB_BINDING_VERSION);
            }

            //insert xs:annotation/xs:appinfo where in jaxb:binding will be put
            target = refineSchemaTarget(target);
            copyInscopeNSAttributes(decl);
        }else if(isJAXRPCBindingElement(decl)){
            //add jaxb namespace declaration
            if(!target.hasAttributeNS(Constants.NS_XMLNS, "jaxrpc")){
                target.setAttributeNS(Constants.NS_XMLNS, "xmlns:jaxrpc", JAXRPCBindingsConstants.NS_JAXRPC_BINDINGS);
            }

            //insert xs:annotation/xs:appinfo where in jaxb:binding will be put
            target = refineWSDLTarget(target);
        }else{
            return;
        }

        // finally move the declaration to the target node.
        if( target.getOwnerDocument()!=decl.getOwnerDocument() ) {
            // if they belong to different DOM documents, we need to clone them
            Element original = decl;
            decl = (Element)target.getOwnerDocument().importNode(decl,true);

        }

        target.appendChild( decl );
    }

    /**
     *  Copy in-scope namespace declarations of the decl node
     *  to the decl node itself so that this move won't change
     *  the in-scope namespace bindings.
     */
    private void copyInscopeNSAttributes(Element e){
        Element p = e;
        Set inscopes = new HashSet();
        while(true) {
            NamedNodeMap atts = p.getAttributes();
            for( int i=0; i<atts.getLength(); i++ ) {
                Attr a = (Attr)atts.item(i);
                if( Constants.NS_XMLNS.equals(a.getNamespaceURI()) ) {
                    String prefix;
                    if( a.getName().indexOf(':')==-1 )  prefix = "";
                    else                                prefix = a.getLocalName();

                    if( inscopes.add(prefix) && p!=e ) {
                        // if this is the first time we see this namespace bindings,
                        // copy the declaration.
                        // if p==decl, there's no need to. Note that
                        // we want to add prefix to inscopes even if p==Decl

                        e.setAttributeNodeNS( (Attr)a.cloneNode(true) );
                    }
                }
            }

            if( p.getParentNode() instanceof Document )
                break;

            p = (Element)p.getParentNode();
        }

        if( !inscopes.contains("") ) {
            // if the default namespace was undeclared in the context of decl,
            // it must be explicitly set to "" since the new environment might
            // have a different default namespace URI.
            e.setAttributeNS(Constants.NS_XMLNS,"xmlns","");
        }
    }

    public Element refineSchemaTarget(Element target) {
        // look for existing xs:annotation
        Element annotation = DOMUtils.getFirstChildElement(target, Constants.NS_XSD, "annotation");
        if(annotation==null)
            // none exists. need to make one
            annotation = insertXMLSchemaElement( target, "annotation" );

        // then look for appinfo
        Element appinfo = DOMUtils.getFirstChildElement(annotation, Constants.NS_XSD, "appinfo" );
        if(appinfo==null)
            // none exists. need to make one
            appinfo = insertXMLSchemaElement( annotation, "appinfo" );

        return appinfo;
    }

    public Element refineWSDLTarget(Element target) {
        // look for existing xs:annotation
        Element jaxrpcBindings = DOMUtils.getFirstChildElement(target, JAXRPCBindingsConstants.NS_JAXRPC_BINDINGS, "bindings");
        if(jaxrpcBindings==null)
            // none exists. need to make one
            jaxrpcBindings = insertJAXRPCBindingsElement(target, "bindings" );
        return jaxrpcBindings;
    }

    /**
     * Creates a new XML Schema element of the given local name
     * and insert it as the first child of the given parent node.
     *
     * @return
     *      Newly create element.
     */
    private Element insertXMLSchemaElement( Element parent, String localName ) {
        // use the same prefix as the parent node to avoid modifying
        // the namespace binding.
        String qname = parent.getTagName();
        int idx = qname.indexOf(':');
        if(idx==-1)     qname = localName;
        else            qname = qname.substring(0,idx+1)+localName;

        Element child = parent.getOwnerDocument().createElementNS( Constants.NS_XSD, qname );

        NodeList children = parent.getChildNodes();

        if( children.getLength()==0 )
            parent.appendChild(child);
        else
            parent.insertBefore( child, children.item(0) );

        return child;
    }

    private Element insertJAXRPCBindingsElement( Element parent, String localName ) {
        String qname = "jaxrpc:"+localName;

        Element child = parent.getOwnerDocument().createElementNS(JAXRPCBindingsConstants.NS_JAXRPC_BINDINGS, qname );

        NodeList children = parent.getChildNodes();

        if( children.getLength()==0 )
            parent.appendChild(child);
        else
            parent.insertBefore( child, children.item(0) );

        return child;
    }
    protected void warn(Localizable msg) {
        env.warn(msg);
    }


    protected void error(String key, Object[] args) {
        env.error(messageFactory.getMessage(key, args));
    }

    protected void warn(String key) {
        env.warn(messageFactory.getMessage(key));
    }

    protected void warn(String key, Object[] args) {
        env.warn(messageFactory.getMessage(key, args));
    }

    protected void info(String key) {
        env.info(messageFactory.getMessage(key));
    }

    protected void info(String key, String arg) {
        env.info(messageFactory.getMessage(key, arg));
    }

}
