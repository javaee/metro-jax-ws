/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
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

package com.sun.xml.ws.api.model.wsdl;

import java.util.List;

import javax.xml.namespace.QName;

import org.xml.sax.Locator;

/**
 * Interface that represents WSDL concepts that
 * can have extensions.
 *
 * @author Vivek Pandey
 * @author Kohsuke Kawaguchi
 */
public interface WSDLExtensible extends WSDLObject {
    /**
     * Gets all the {@link WSDLExtension}s
     * added through {@link #addExtension(WSDLExtension)}.
     *
     * @return
     *      never null.
     */
    Iterable<WSDLExtension> getExtensions();

    /**
     * Gets all the extensions that is assignable to the given type.
     *
     * <p>
     * This allows clients to find specific extensions in a type-safe
     * and convenient way.
     *
     * @param type
     *      The type of the extension to obtain. Must not be null.
     *
     * @return
     *      Can be an empty fromjava.collection but never null.
     */
    <T extends WSDLExtension> Iterable<T> getExtensions(Class<T> type);

    /**
     * Gets the extension that is assignable to the given type.
     *
     * <p>
     * This is just a convenient version that does
     *
     * <pre>
     * Iterator itr = getExtensions(type);
     * if(itr.hasNext())  return itr.next();
     * else               return null;
     * </pre>
     *
     * @return
     *      null if the extension was not found.
     */
    <T extends WSDLExtension> T getExtension(Class<T> type);

    /**
     * Adds a new {@link WSDLExtension}
     * to this object.
     *
     * @param extension
     *      must not be null.
     */
    void addExtension(WSDLExtension extension);
    
    /**
     * True if all required WSDL extensions on Port and Binding are understood
     * @return true if all wsdl required extensions on Port and Binding are understood
     */
    public boolean areRequiredExtensionsUnderstood();
    
    /**
     * Marks extension as not understood
     * @param extnEl QName of extension
     * @param locator Locator
     */
    public void addNotUnderstoodExtension(QName extnEl, Locator locator);
    
    /**
     * Lists extensions marked as not understood
     * @return List of not understood extensions
     */
    public List<? extends WSDLExtension> getNotUnderstoodExtensions();
}
