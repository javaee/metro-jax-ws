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

package com.sun.xml.ws.api.message;

import java.util.List;
import java.util.Iterator;
import java.util.Set;

import javax.xml.namespace.QName;

import com.sun.xml.ws.api.WSBinding;

/**
 * Interface representing all the headers of a {@link Message}
 */
public interface MessageHeaders {
    public void understood(Header header);
    public void understood(QName name);
    public void understood(String nsUri, String localName);
    public Header get(String nsUri, String localName, boolean markAsUnderstood);
    public Header get(QName name, boolean markAsUnderstood);
    public Iterator<Header> getHeaders(String nsUri, String localName, final boolean markAsUnderstood);
    /**
     * Get all headers in specified namespace
     * @param nsUri
     * @param markAsUnderstood
     * @return
     */
    public Iterator<Header> getHeaders(String nsUri, final boolean markAsUnderstood);
    public Iterator<Header> getHeaders(QName headerName, final boolean markAsUnderstood);
    public Iterator<Header> getHeaders();    
    public boolean hasHeaders();    
    public boolean add(Header header);
    public Header remove(QName name);
    public Header remove(String nsUri, String localName);
    //DONT public Header remove(Header header);
    public void replace(Header old, Header header);
    
    /**
     * Replaces an existing {@link Header} or adds a new {@link Header}.
     *
     * <p>
     * Order doesn't matter in headers, so this method
     * does not make any guarantee as to where the new header
     * is inserted.
     *
     * @return
     *      always true. Don't use the return value.
     */
    public boolean addOrReplace(Header header);
    
    /**
     * Return a Set of QNames of headers that have been explicitly marked as understood.
     * If none have been marked, this method could return null
     */
    public Set<QName> getUnderstoodHeaders();
    
    /**
     * Returns a Set of QNames of headers that satisfy ALL the following conditions:
     * (a) Have mustUnderstand = true 
     * (b) have NOT been explicitly marked as understood
     * (c) If roles argument is non-null, the header has isIgnorable = false 
     * for the roles argument and SOAP version  
     * (d) If non-null binding is passed in, are NOT understood by the binding
     * (e) If (d) is met, the header is NOT in the knownHeaders list passed in
     * 
     * @param roles
     * @param knownHeaders
     * @param binding
     * @return
     */
    public Set<QName> getNotUnderstoodHeaders(Set<String> roles, Set<QName> knownHeaders, WSBinding binding);
    
    /**
     * True if the header has been explicitly marked understood, false otherwise
     * @param header
     * @return
     */
    public boolean isUnderstood(Header header);
    
    /**
     * True if the header has been explicitly marked understood, false otherwise
     * @param header
     * @return
     */
    public boolean isUnderstood(QName header);
    
    /**
     * True if the header has been explicitly marked understood, false otherwise
     * @param header
     * @return
     */
    public boolean isUnderstood(String nsUri, String header);
    
    /**
     * Returns <code>Header</code> instances in a <code>List</code>.
     * @return <code>List</code> containing <code>Header</code> instances
     */
    public List<Header> asList();
}
