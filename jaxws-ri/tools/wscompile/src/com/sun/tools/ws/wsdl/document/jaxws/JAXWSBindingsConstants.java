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

package com.sun.tools.ws.wsdl.document.jaxws;

import com.sun.tools.ws.wsdl.parser.Constants;

import javax.xml.namespace.QName;

/**
 * @author Vivek Pandey
 *
 */
public interface JAXWSBindingsConstants {

    public static String NS_JAXWS_BINDINGS = "http://java.sun.com/xml/ns/jaxws";
    public static String NS_JAXB_BINDINGS = "http://java.sun.com/xml/ns/jaxb";
    public static String NS_XJC_BINDINGS = "http://java.sun.com/xml/ns/jaxb/xjc";

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
     * PortType WSDLOperation
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
     *      childElementName="xs:QName"?
     *      name="xs:string"/>*
     */



    public static QName METHOD = new QName(NS_JAXWS_BINDINGS, "method");
    public static QName PARAMETER = new QName(NS_JAXWS_BINDINGS, "parameter");
    public static String PART_ATTR = "part";
    public static String ELEMENT_ATTR = "childElementName";

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
     * WSDLBoundOperation
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
    public static String JAXB_BINDING_VERSION = "2.0";
    public static QName XSD_APPINFO = new QName(Constants.NS_XSD, "appinfo");
    public static QName XSD_ANNOTATION = new QName(Constants.NS_XSD, "annotation");
}