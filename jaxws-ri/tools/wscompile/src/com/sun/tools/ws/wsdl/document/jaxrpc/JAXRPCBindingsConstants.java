/*
 * $Id: JAXRPCBindingsConstants.java,v 1.1 2005-05-24 13:53:27 bbissett Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.wsdl.document.jaxrpc;

import javax.xml.namespace.QName;

import com.sun.tools.ws.wsdl.parser.Constants;

/**
 * @author Vivek Pandey
 *
 */
public interface JAXRPCBindingsConstants {

    public static String NS_JAXRPC_BINDINGS = "http://java.sun.com/xml/ns/jaxrpc";
    public static String NS_JAXB_BINDINGS = "http://java.sun.com/xml/ns/jaxb";

    /**
     * jaxrpc:bindings schema component
     *
     * <jaxrpc:bindings wsdlLocation="xs:anyURI"? node="xs:string"?
     *      version="string"?> binding declarations...
     * </jaxrpc:bindings>
     *
     * wsdlLocation="xs:anyURI"? node="xs:string"? version="string"?> binding
     * declarations... </jaxrpc:bindings>
     *
     * @wsdlLocation A URI pointing to a WSDL file establishing the scope of the
     *               contents of this binding declaration. It MUST NOT be
     *               present if the binding declaration is used as an extension
     *               inside a WSDL document or if there is an ancestor binding
     *               declaration that contains this attribute.
     *
     * @node An XPath expression pointing to the element in the WSDL file in
     *       scope that this binding declaration is attached to.
     *
     * @version A version identifier. It MAY only appear on jaxrpc:bindings
     *          elements that don't have any jaxrpc:bindings ancestors (i.e. on
     *          outermost binding declarations).
     */
    public static QName JAXRPC_BINDINGS = new QName(NS_JAXRPC_BINDINGS, "bindings");
    public static String WSDL_LOCATION_ATTR = "wsdlLocation";
    public static String NODE_ATTR = "node";
    public static String VERSION_ATTR = "version";

    /*
     * <jaxrpc:package name="xs:string">? <jaxrpc:javadoc>xs:string
     * </jaxrpc:javadoc> </jaxrpc:package>
     */
    public static QName PACKAGE = new QName(NS_JAXRPC_BINDINGS, "package");
    public static String NAME_ATTR = "name";
    public static QName JAVADOC = new QName(NS_JAXRPC_BINDINGS, "javadoc");

    /*
     * <jaxrpc:enableWrapperStyle>xs:boolean </jaxrpc:enableWrapperStyle>?
     */
    public static QName ENABLE_WRAPPER_STYLE = new QName(NS_JAXRPC_BINDINGS, "enableWrapperStyle");

    /*
     * <jaxrpc:enableAsynchronousMapping>xs:boolean
     *      </jaxrpc:enableAsynchronousMapping>?
     */
    public static QName ENABLE_ASYNC_MAPPING = new QName(NS_JAXRPC_BINDINGS, "enableAsyncMapping");

    /*
     * <jaxrpc:enableAdditionalSOAPHeaderMapping>xs:boolean</jaxrpc:enableAdditionalSOAPHeaderMapping>?
     */
    public static QName ENABLE_ADDITIONAL_SOAPHEADER_MAPPING = new QName(NS_JAXRPC_BINDINGS, "enableAdditionalSOAPHeaderMapping");

    /*
     * <jaxrpc:enableMIMEContent>xs:boolean</jaxrpc:enableMIMEContent>?
     */
    public static QName ENABLE_MIME_CONTENT = new QName(NS_JAXRPC_BINDINGS, "enableMIMEContent");

    /*
     * <jaxrpc:provider>xs:boolean</jaxrpc:provider>?
     */
    public static QName PROVIDER = new QName(NS_JAXRPC_BINDINGS, "provider");

    /*
     * PortType
     *
     * <jaxrpc:class name="xs:string">?
     *  <jaxrpc:javadoc>xs:string</jaxrpc:javadoc>?
     * </jaxrpc:class>
     *
     * <jaxrpc:enableWrapperStyle>
     *  xs:boolean
     * </jaxrpc:enableWrapperStyle>?
     *
     * <jaxrpc:enableAsynchronousMapping>
     *  xs:boolean
     * </jaxrpc:enableAsynchronousMapping>?
     *
     */

    public static QName CLASS = new QName(NS_JAXRPC_BINDINGS, "class");

    /*
     * PortType Operation
     *
     * <jaxrpc:method name="xs:string">?
     *   <jaxrpc:javadoc>xs:string</jaxrpc:javadoc>?
     * </jaxrpc:method>
     *
     * <jaxrpc:enableWrapperStyle>
     *  xs:boolean
     * </jaxrpc:enableWrapperStyle>?
     *
     * <jaxrpc:enableAsyncMapping>
     *  xs:boolean
     * </jaxrpc:enableAsyncMapping>?
     *
     * <jaxrpc:parameter part="xs:string"
     *      element="xs:QName"?
     *      name="xs:string"/>*
     */



    public static QName METHOD = new QName(NS_JAXRPC_BINDINGS, "method");
    public static QName PARAMETER = new QName(NS_JAXRPC_BINDINGS, "parameter");
    public static String PART_ATTR = "part";
    public static String ELEMENT_ATTR = "element";

    /*
     * Binding
     *
     * <jaxrpc:enableAdditionalSOAPHeaderMapping>
     *  xs:boolean
     * </jaxrpc:enableAdditionalSOAPHeaderMapping>?
     *
     * <jaxrpc:enableMIMEContent>
     *  xs:boolean
     * </jaxrpc:enableMIMEContent>?
     */

    /*
     * BindingOperation
     *
     * <jaxrpc:enableAdditionalSOAPHeaderMapping>
     *  xs:boolean
     * </jaxrpc:enableAdditionalSOAPHeaderMapping>?
     *
     * <jaxrpc:enableMIMEContent>
     *  xs:boolean
     * </jaxrpc:enableMIMEContent>?
     *
     * <jaxrpc:parameter part="xs:string"
     *                  element="xs:QName"?
     *                  name="xs:string"/>*
     *
     * <jaxrpc:exception part="xs:string">*
     *  <jaxrpc:class name="xs:string">?
     *      <jaxrpc:javadoc>xs:string</jaxrpc:javadoc>?
     *  </jaxrpc:class>
     * </jaxrpc:exception>
     */

    public static QName EXCEPTION = new QName(NS_JAXRPC_BINDINGS, "exception");


    /*
     * jaxb:bindgs QName
     */
    public static QName JAXB_BINDINGS = new QName(NS_JAXB_BINDINGS, "bindings");
    public static String JAXB_BINDING_VERSION = "1.0";
    public static QName XSD_APPINFO = new QName(Constants.NS_XSD, "appinfo");
    public static QName XSD_ANNOTATION = new QName(Constants.NS_XSD, "annotation");
}