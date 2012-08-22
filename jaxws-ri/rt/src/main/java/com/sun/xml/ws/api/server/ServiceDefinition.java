/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package com.sun.xml.ws.api.server;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.model.wsdl.WSDLModel;

/**
 * Root of the unparsed WSDL and other resources referenced from it.
 * This object represents the description of the service
 * that a {@link WSEndpoint} offers.
 *
 * <p>
 * A description consists of a set of {@link SDDocument}, which
 * each represents a single XML document that forms a part of the
 * descriptor (for example, WSDL might refer to separate schema documents,
 * or a WSDL might refer to another WSDL.)
 *
 * <p>
 * {@link ServiceDefinition} and its descendants are immutable
 * read-only objects. Once they are created, they always return
 * the same value.
 *
 * <h2>Expected Usage</h2>
 * <p>
 * This object is intended to be used for serving the descriptors
 * to remote clients (such as by MEX, or other protocol-specific
 * metadata query, such as HTTP GET with "?wsdl" query string.)
 *
 * <p>
 * This object is <b>NOT</b> intended to be used by other
 * internal components to parse them. For that purpose, use
 * {@link WSDLModel} instead.
 *
 * @author Kohsuke Kawaguchi
 */
public interface ServiceDefinition extends Iterable<SDDocument> {
    /**
     * Gets the "primary" {@link SDDocument} that represents a WSDL.
     *
     * <p>
     * This WSDL eventually refers to all the other {@link SDDocument}s.
     *
     * @return
     *      always non-null.
     */
    @NotNull SDDocument getPrimary();

    /**
     * Adds a filter that is called while writing {@link SDDocument}'s infoset. This
     * filter is applied to the all the other reachable {@link SDDocument}s.
     *
     * @param filter that is called while writing the document
     */
    void addFilter(@NotNull SDDocumentFilter filter);
}
