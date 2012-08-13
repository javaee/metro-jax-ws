/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
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

package com.sun.xml.ws.wsdl.writer;

import com.sun.istack.Nullable;
import com.sun.xml.ws.api.server.SDDocument;

/**
 * Resolves relative references among the metadata(WSDL, schema)
 * documents.
 *
 * <p>
 * This interface is implemented by the caller of
 * {@link SDDocument#writeTo} method so that the {@link SDDocument} can
 * correctly produce references to other documents.
 *
 * <h2>Usage Example 1</h2>
 * <p>
 * Say: http://localhost/hello?wsdl has reference to
 * <p>
 *   &lt;xsd:import namespace="urn:test:types" schemaLocation="http://localhost/hello?xsd=1"/>
 *
 * <p>
 * Using this class, it is possible to write A.wsdl to a local filesystem with
 * a local file schema import.
 * <p>
 *   &lt;xsd:import namespace="urn:test:types" schemaLocation="hello.xsd"/>
 *
 * @author Jitendra Kotamraju
 */
public interface DocumentLocationResolver {
    /**
     * Produces a relative reference from one document to another.
     *
     * @param namespaceURI
     *      The namespace urI for the referenced document.
     *      for e.g. wsdl:import/@namespace, xsd:import/@namespace
     * @param systemId
     *      The location value for the referenced document.
     *      for e.g. wsdl:import/@location, xsd:import/@schemaLocation
     * @return
     *      The reference to be put inside {@code current} to refer to
     *      {@code referenced}. This can be a relative URL as well as
     *      an absolute. If null is returned, then the document
     *      will produce a "implicit reference" (for example, &lt;xs:import>
     *      without the @schemaLocation attribute, etc).
     */
    @Nullable String getLocationFor(String namespaceURI, String systemId);
}
