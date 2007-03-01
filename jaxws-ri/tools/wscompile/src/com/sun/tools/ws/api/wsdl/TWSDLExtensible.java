/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */

package com.sun.tools.ws.api.wsdl;


import javax.xml.namespace.QName;

/**
 * A WSDL element or attribute that can be extended.
 *
 * @author Vivek Pandey
 */
public interface TWSDLExtensible {
    /**
     * Gives the wsdl extensiblity element's name attribute value. It can be null as @name on some of the wsdl
     * extensibility elements are optinal such as wsdl:input
     */
    String getNameValue();

    /**
     * Gives namespace URI of a wsdl extensibility element.
     */
    String getNamespaceURI();

    /**
     * Gives the WSDL element or WSDL extensibility element name
     */
    QName getWSDLElementName();

    /**
     * An {@link TWSDLExtensionHandler} will call this method to add an {@link TWSDLExtension} object
     *
     * @param e non-null extension object
     */
    void addExtension(TWSDLExtension e);

    /**
     * Gives iterator over {@link TWSDLExtension}s
     */
    Iterable<? extends TWSDLExtension> extensions();

    /**
     * Gives the parent of a wsdl extensibility element.
     * <pre>
     * For example,
     *
     *     <wsdl:portType>
     *         <wsdl:operation>
     *     ...
     * Here, the {@link TWSDLExtensible}representing wsdl:operation's parent would be wsdl:portType
     *
     * @return null if the {@link TWSDLExtensible} has no parent, root of wsdl document - wsdl:definition.
     */
    TWSDLExtensible getParent();
}
