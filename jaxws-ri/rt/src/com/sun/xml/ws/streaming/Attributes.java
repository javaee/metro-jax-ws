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

package com.sun.xml.ws.streaming;

import javax.xml.namespace.QName;

/**
 * <p> The Attributes interface is essentially a version of the
 * org.xml.sax.Attributes interface modified to use the JAX-WS QName class.</p>
 *
 * <p> Although namespace declarations can appear in the attribute list, the
 * actual values of the local name and URI properties are
 * implementation-specific. </p>
 *
 * <p> Applications that need to iterate through all the attributes can use the
 * {@link #isNamespaceDeclaration} method to identify namespace declarations
 * and skip them. </p>
 *
 * <p> Also, the URI property of an attribute will never be null. The value
 * "" (empty string) is used for the URI of non-qualified attributes. </p>
 *
 * @author WS Development Team
 */
public interface Attributes {

    /**
     * Return the number of attributes in the list.
     *
     */
    public int getLength();

    /**
     * Return true if the attribute at the given index is a namespace
     * declaration.
     *
     * <p> Implementations are encouraged to optimize this method by taking into
     * account their internal representations of attributes. </p>
     *
     */
    public boolean isNamespaceDeclaration(int index);

    /**
     * Look up an attribute's QName by index.
     *
     */
    public QName getName(int index);

    /**
     * Look up an attribute's URI by index.
     *
     */
    public String getURI(int index);

    /**
     * Look up an attribute's local name by index.
     * If attribute is a namespace declaration, result
     * is expected including "xmlns:".
     */
    public String getLocalName(int index);

    /**
     * Look up an attribute's prefix by index.
     *
     */
    public String getPrefix(int index);

    /**
     * Look up an attribute's value by index.
     *
     */
    public String getValue(int index);

    /**
     * Look up the index of an attribute by QName.
     *
     */
    public int getIndex(QName name);

    /**
     * Look up the index of an attribute by URI and local name.
     *
     */
    public int getIndex(String uri, String localName);

    /**
     * Look up the index of an attribute by local name.
     *
     */
    public int getIndex(String localName);

    /**
     * Look up the value of an attribute by QName.
     *
     */
    public String getValue(QName name);

    /**
     * Look up the value of an attribute by URI and local name.
     *
     */
    public String getValue(String uri, String localName);

    /**
     * Look up the value of an attribute by local name.
     *
     */
    public String getValue(String localName);
}
