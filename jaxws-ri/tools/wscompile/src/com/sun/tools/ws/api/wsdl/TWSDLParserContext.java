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

import org.w3c.dom.Element;
import org.xml.sax.Locator;

/**
 * Provides WSDL parsing context. It should be used by the WSDL extension handlers to register their namespaces so that
 * it can be latter used by other extensions to resolve the namespaces.
 *
 * @author Vivek Pandey
 */
public interface TWSDLParserContext {

    /**
     * Pushes the parsing context
     */
    void push();

    /**
     * pops the parsing context
     */
    void pop();

    /**
     * Gives the namespace URI for a given prefix
     *
     * @param prefix non-null prefix
     * @return null of the prefix is not found
     */
    String getNamespaceURI(String prefix);

    /**
     * Gives the prefixes in the current context
     */
    Iterable<String> getPrefixes();

    /**
     * Gives default namespace
     *
     * @return null if there is no default namespace declaration found
     */
    String getDefaultNamespaceURI();

    /**
     * Registers naemespace declarations of a given {@link Element} found in the WSDL
     *
     * @param e {@link Element} whose namespace declarations need to be registered
     */
    void registerNamespaces(Element e);

    /**
     * gives the location information for the given Element.
     */
    Locator getLocation(Element e);

}
