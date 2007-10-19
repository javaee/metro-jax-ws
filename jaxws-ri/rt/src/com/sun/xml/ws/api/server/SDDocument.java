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

package com.sun.xml.ws.api.server;

import com.sun.istack.Nullable;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.namespace.QName;
import java.io.OutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Set;

/**
 * Represents an individual document that forms a {@link ServiceDefinition}.
 *
 * <pre>
 * TODO:
 *      how does those documents refer to each other?
 *
 * </pre>
 *
 * @author Jitendra Kotamraju
 */
public interface SDDocument {

    /**
     * Gets the root tag name of this document.
     *
     * <p>
     * This can be used to identify a kind of document quickly
     * (such as schema, WSDL, ...)
     *
     * @return
     *      always non-null.
     */
    QName getRootName();

    /**
     * Returns true if this document is WSDL.
     */
    boolean isWSDL();

    /**
     * Returns true if this document is schema.
     */
    boolean isSchema();

    /**
     * returns the referenced documents
     */
    Set<String> getImports();

    /**
     * Gets the system ID of the document where it's taken from. Generated documents
     * use a fake URL that can be used to resolve relative URLs. So donot use this URL
     * for reading or writing.
     */
    URL getURL();

    /**
     * Writes the document to the given {@link OutputStream}.
     *
     * <p>
     * Since {@link ServiceDefinition} doesn't know which endpoint address
     * {@link Adapter} is serving to, (and often it serves multiple URLs
     * simultaneously), this method takes the PortAddressResolver as a parameter,
     * so that it can produce the corret address information in the generated WSDL.
     *
     * @param portAddressResolver
     *      An endpoint address resolver that gives endpoint address for a WSDL
     *      port. Can be null.
     * @param resolver
     *      Used to resolve relative references among documents.
     * @param os
     *      The {@link OutputStream} that receives the generated document.
     *
     * @throws IOException
     *      if there was a failure reported from the {@link OutputStream}.
     */
    void writeTo(@Nullable PortAddressResolver portAddressResolver,
            DocumentAddressResolver resolver, OutputStream os) throws IOException;

    /**
     * Writes the document to the given {@link XMLStreamWriter}.
     *
     * <p>
     * The same as {@link #writeTo(PortAddressResolver,DocumentAddressResolver,OutputStream)} except
     * it writes to an {@link XMLStreamWriter}.
     *
     * <p>
     * The implementation must not call {@link XMLStreamWriter#writeStartDocument()}
     * nor {@link XMLStreamWriter#writeEndDocument()}. Those are the caller's
     * responsibility.
     *
     * @throws XMLStreamException
     *      if the {@link XMLStreamWriter} reports an error.
     */
    void writeTo(PortAddressResolver portAddressResolver,
            DocumentAddressResolver resolver, XMLStreamWriter out) throws XMLStreamException, IOException;

    /**
     * {@link SDDocument} that represents an XML Schema.
     */
    interface Schema extends SDDocument {
        /**
         * Gets the target namepsace of this schema.
         */
        String getTargetNamespace();
    }

    /**
     * {@link SDDocument} that represents a WSDL.
     */
    interface WSDL extends SDDocument {
        /**
         * Gets the target namepsace of this schema.
         */
        String getTargetNamespace();

        /**
         * This WSDL has a portType definition
         * that matches what {@link WSEndpoint} is serving.
         *
         * TODO: does this info needs to be exposed?
         */
        boolean hasPortType();

        /**
         * This WSDL has a service definition
         * that matches the {@link WSEndpoint}.
         *
         * TODO: does this info need to be exposed?
         */
        boolean hasService();
    }
}
