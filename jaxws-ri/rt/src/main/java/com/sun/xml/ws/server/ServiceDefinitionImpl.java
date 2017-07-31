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

package com.sun.xml.ws.server;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.server.SDDocument;
import com.sun.xml.ws.api.server.SDDocumentFilter;
import com.sun.xml.ws.api.server.ServiceDefinition;
import com.sun.xml.ws.wsdl.SDDocumentResolver;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * {@link ServiceDefinition} implementation.
 *
 * <p>
 * You construct a {@link ServiceDefinitionImpl} by first constructing
 * a list of {@link SDDocumentImpl}s.
 *
 * @author Kohsuke Kawaguchi
 */
public final class ServiceDefinitionImpl implements ServiceDefinition, SDDocumentResolver {
    private final Collection<SDDocumentImpl> docs;

    private final Map<String,SDDocumentImpl> bySystemId;
    private final @NotNull SDDocumentImpl primaryWsdl;

    /**
     * Set when {@link WSEndpointImpl} is created.
     */
    /*package*/ WSEndpointImpl<?> owner;

    /*package*/ final List<SDDocumentFilter> filters = new ArrayList<SDDocumentFilter>();

    /**
     * @param docs
     *      List of {@link SDDocumentImpl}s to form the description.
     *      There must be at least one entry.
     *      The first document is considered {@link #getPrimary() primary}.
     */
    public ServiceDefinitionImpl(Collection<SDDocumentImpl> docs, @NotNull SDDocumentImpl primaryWsdl) {
        assert docs.contains(primaryWsdl);
        this.docs = docs;
        this.primaryWsdl = primaryWsdl;
        this.bySystemId = new HashMap<String, SDDocumentImpl>();
    }

    private boolean isInitialized = false;
    
    private synchronized void init() {
        if (isInitialized)
            return;
        isInitialized = true;
        
        for (SDDocumentImpl doc : docs) {
            bySystemId.put(doc.getURL().toExternalForm(),doc);
            doc.setFilters(filters);
            doc.setResolver(this);
        }
    }
    
    /**
     * The owner is set when {@link WSEndpointImpl} is created.
     */
    /*package*/ void setOwner(WSEndpointImpl<?> owner) {
        assert owner!=null && this.owner==null;
        this.owner = owner;
    }

    public @NotNull SDDocument getPrimary() {
        return primaryWsdl;
    }

    public void addFilter(SDDocumentFilter filter) {
        filters.add(filter);
    }

    public Iterator<SDDocument> iterator() {
        init();
        return (Iterator)docs.iterator();
    }

    /**
     * Gets the {@link SDDocumentImpl} whose {@link SDDocumentImpl#getURL()}
     * returns the specified value.
     *
     * @return
     *      null if none is found.
     */
    public SDDocument resolve(String systemId) {
        init();
        return bySystemId.get(systemId);
    }
}
