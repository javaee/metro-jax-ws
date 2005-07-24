/*
 * $Id: JAXWSBindingsConstants.java,v 1.1 2005-07-24 01:48:49 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.tools.ws.wsdl.document.jaxws;

import javax.xml.namespace.QName;

import com.sun.tools.ws.wsdl.parser.Constants;

/**
 * @author Vivek Pandey
 *
 */
public interface JAXWSBindingsConstants {

    public static String NS_JAXWS_BINDINGS = "http://java.sun.com/xml/ns/jaxws";
    public static String NS_JAXB_BINDINGS = "http://java.sun.com/xml/ns/jaxb";

    /**
     * jaxws:bindings schema component
     *
     * <jaxws:bindings wsdlLocation="xs:anyURI"? node="xs:string"?
     *      version="string"?> binding declarations...
     * </jaxws:bindings>
     *
     * wsdlLocation="xs:anyURI"? node="xs:string"? version="string"?> binding
     * declarations... </jaxws:bindings>
     *
     * <code>@wsdlLocation</code> A URI pointing to a WSDL file establishing the scope of the
     *               contents of this binding declaration. It MUST NOT be
     *               present if the binding declaration is used as an extension
     *               inside a WSDL document or if there is an ancestor binding
     *               declaration that contains this attribute.
     *
     * <code>@node</code> An XPath expression pointing to the element in the WSDL file in
     *       scope that this binding declaration is attached to.
     *
     * <code>@version</code> A version identifier. It MAY only appear on jaxws:bindings
     *          elements that don't have any jaxws:bindings ancestors (i.e. on
     *          outermost binding declarations).
     */
    public static QName JAXWS_BINDINGS = new QName(NS_JAXWS_BINDINGS, "bindings");
    public static String WSDL_LOCATION_ATTR = "wsdlLocation";
    public static String NODE_ATTR = "node";
    public static String VERSION_ATTR = "version";

    /*
     * <jaxws:package name="xs:string">? <jaxws:javadoc>xs:string
     * </jaxws:javadoc> </jaxws:package>
     */
    public static QName PACKAGE = new QName(NS_JAXWS_BINDINGS, "package");
    public static String NAME_ATTR = "name";
    public static QName JAVADOC = new QName(NS_JAXWS_BINDINGS, "javadoc");

    /*
     * <jaxws:enableWrapperStyle>xs:boolean </jaxws:enableWrapperStyle>?
     */
    public static QName ENABLE_WRAPPER_STYLE = new QName(NS_JAXWS_BINDINGS, "enableWrapperStyle");

    /*
     * <jaxws:enableAsynchronousMapping>xs:boolean
     *      </jaxws:enableAsynchronousMapping>?
     */
    public static QName ENABLE_ASYNC_MAPPING = new QName(NS_JAXWS_BINDINGS, "enableAsyncMapping");

    /*
     * <jaxws:enableAdditionalSOAPHeaderMapping>xs:boolean</jaxws:enableAdditionalSOAPHeaderMapping>?
     */
    public static QName ENABLE_ADDITIONAL_SOAPHEADER_MAPPING = new QName(NS_JAXWS_BINDINGS, "enableAdditionalSOAPHeaderMapping");

    /*
     * <jaxws:enableMIMEContent>xs:boolean</jaxws:enableMIMEContent>?
     */
    public static QName ENABLE_MIME_CONTENT = new QName(NS_JAXWS_BINDINGS, "enableMIMEContent");

    /*
     * <jaxwsc:provider>xs:boolean</jaxws:provider>?
     */
    public static QName PROVIDER = new QName(NS_JAXWS_BINDINGS, "provider");

    /*
     * PortType
     *
     * <jaxws:class name="xs:string">?
     *  <jaxws:javadoc>xs:string</jaxws:javadoc>?
     * </jaxws:class>
     *
     * <jaxws:enableWrapperStyle>
     *  xs:boolean
     * </jaxws:enableWrapperStyle>?
     *
     * <jaxws:enableAsynchronousMapping>
     *  xs:boolean
     * </jaxws:enableAsynchronousMapping>?
     *
     */

    public static QName CLASS = new QName(NS_JAXWS_BINDINGS, "class");

    /*
     * PortType Operation
     *
     * <jaxws:method name="xs:string">?
     *   <jaxws:javadoc>xs:string</jaxws:javadoc>?
     * </jaxws:method>
     *
     * <jaxws:enableWrapperStyle>
     *  xs:boolean
     * </jaxws:enableWrapperStyle>?
     *
     * <jaxws:enableAsyncMapping>
     *  xs:boolean
     * </jaxws:enableAsyncMapping>?
     *
     * <jaxws:parameter part="xs:string"
     *      element="xs:QName"?
     *      name="xs:string"/>*
     */



    public static QName METHOD = new QName(NS_JAXWS_BINDINGS, "method");
    public static QName PARAMETER = new QName(NS_JAXWS_BINDINGS, "parameter");
    public static String PART_ATTR = "part";
    public static String ELEMENT_ATTR = "element";

    /*
     * Binding
     *
     * <jaxws:enableAdditionalSOAPHeaderMapping>
     *  xs:boolean
     * </jaxws:enableAdditionalSOAPHeaderMapping>?
     *
     * <jaxws:enableMIMEContent>
     *  xs:boolean
     * </jaxws:enableMIMEContent>?
     */

    /*
     * BindingOperation
     *
     * <jaxws:enableAdditionalSOAPHeaderMapping>
     *  xs:boolean
     * </jaxws:enableAdditionalSOAPHeaderMapping>?
     *
     * <jaxws:enableMIMEContent>
     *  xs:boolean
     * </jaxws:enableMIMEContent>?
     *
     * <jaxws:parameter part="xs:string"
     *                  element="xs:QName"?
     *                  name="xs:string"/>*
     *
     * <jaxws:exception part="xs:string">*
     *  <jaxws:class name="xs:string">?
     *      <jaxws:javadoc>xs:string</jaxws:javadoc>?
     *  </jaxws:class>
     * </jaxws:exception>
     */

    public static QName EXCEPTION = new QName(NS_JAXWS_BINDINGS, "exception");


    /*
     * jaxb:bindgs QName
     */
    public static QName JAXB_BINDINGS = new QName(NS_JAXB_BINDINGS, "bindings");
    public static String JAXB_BINDING_VERSION = "1.0";
    public static QName XSD_APPINFO = new QName(Constants.NS_XSD, "appinfo");
    public static QName XSD_ANNOTATION = new QName(Constants.NS_XSD, "annotation");
}