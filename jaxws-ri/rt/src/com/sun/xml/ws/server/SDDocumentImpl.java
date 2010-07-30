/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2010 Sun Microsystems, Inc. All rights reserved.
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

package com.sun.xml.ws.server;

import com.sun.xml.ws.api.server.*;
import com.sun.xml.ws.util.RuntimeVersion;
import com.sun.xml.ws.util.xml.MetadataDocument;
import com.sun.xml.ws.wsdl.writer.DocumentLocationResolver;
import com.sun.xml.ws.wsdl.writer.WSDLPatcher;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

/**
 * {@link SDDocument} implmentation.
 *
 * <p>
 * This extends from {@link SDDocumentSource} so that
 * JAX-WS server runtime code can use {@link SDDocument}
 * as {@link SDDocumentSource}.
 *
 * @author Kohsuke Kawaguchi
 * @author Jitendra Kotamraju
 */
public class SDDocumentImpl extends MetadataDocument {

    private static final String VERSION_COMMENT =
        " Published by JAX-WS RI at http://jax-ws.dev.java.net. RI's version is "+RuntimeVersion.VERSION+". ";


    /**
     * Set when {@link ServiceDefinitionImpl} is constructed.
     */
    /*package*/ ServiceDefinitionImpl owner;

    protected SDDocumentImpl(QName rootName, URL url, SDDocumentSource source) {
        super(rootName, url, source);
    }

    protected SDDocumentImpl(QName rootName, URL url, SDDocumentSource source, Set<String> imports) {
        super(rootName, url, source, imports);
    }

    public void writeTo(PortAddressResolver portAddressResolver, DocumentAddressResolver resolver, XMLStreamWriter out) throws XMLStreamException, IOException {
        for (SDDocumentFilter f : owner.filters) {
            out = f.filter(this,out);
        }

        XMLStreamReader xsr = source.read();

        try {
            out.writeComment(VERSION_COMMENT);
            new WSDLPatcher(portAddressResolver, new DocumentAddressResolverImpl(resolver)).bridge(xsr,out);
        } finally {
            xsr.close();
        }
    }

    static final MetadataDocumentFactory<SDDocumentImpl> FACTORY = new MetadataDocumentFactory<SDDocumentImpl>() {


        public SDDocumentImpl createWSDL(QName rootName, URL url, SDDocumentSource source, String targetNamespace, boolean hasPortType,
                        boolean hasService, Set<String> imports, Set<QName> allServices) {
            return new WSDLImpl(rootName, url, source, targetNamespace, hasPortType, hasService, imports, allServices);
        }

        @Override
        public SDDocumentImpl createSchema(QName rootName, URL url, SDDocumentSource source, String targetNamespace, Set<String> imports) {
            return new SchemaImpl(rootName, url, source, targetNamespace, imports);
        }

        @Override
        public SDDocumentImpl createOther(QName rootName, URL url, SDDocumentSource source) {
            return new SDDocumentImpl(rootName, url, source);
        }
    };

    public static SDDocumentImpl create(SDDocumentSource src, QName serviceName, QName portTypeName) {
        return create(FACTORY, src, serviceName, portTypeName);
    }


    private static final class SchemaImpl extends SDDocumentImpl implements Schema {
        private final String targetNamespace;

        public SchemaImpl(QName rootName, URL url, SDDocumentSource source, String targetNamespace,
                          Set<String> imports) {
            super(rootName, url, source, imports);
            this.targetNamespace = targetNamespace;
        }

        public String getTargetNamespace() {
            return targetNamespace;
        }

        public boolean isSchema() {
            return true;
        }
    }

    private static final class WSDLImpl extends SDDocumentImpl implements WSDL {
        private final String targetNamespace;
        private final boolean hasPortType;
        private final boolean hasService;
        private final Set<QName> allServices;

        public WSDLImpl(QName rootName, URL url, SDDocumentSource source, String targetNamespace, boolean hasPortType,
                        boolean hasService, Set<String> imports, Set<QName> allServices) {
            super(rootName, url, source, imports);
            this.targetNamespace = targetNamespace;
            this.hasPortType = hasPortType;
            this.hasService = hasService;
            this.allServices = allServices;
        }

        public String getTargetNamespace() {
            return targetNamespace;
        }

        public boolean hasPortType() {
            return hasPortType;
        }

        public boolean hasService() {
            return hasService;
        }

        public Set<QName> getAllServices() {
            return allServices;
        }

        public boolean isWSDL() {
            return true;
        }
    }

    private class DocumentAddressResolverImpl implements DocumentLocationResolver {
        private DocumentAddressResolver delegate;

        DocumentAddressResolverImpl(DocumentAddressResolver delegate) {
            this.delegate = delegate;
        }

        public String getLocationFor(String namespaceURI, String systemId) {
            try {
                URL ref = new URL(getURL(), systemId);
                SDDocument refDoc = owner.resolveEntity(ref.toExternalForm());
                if (refDoc==null)
                    return systemId;  // not something we know. just leave it as is.

                return delegate.getRelativeAddressFor(SDDocumentImpl.this, refDoc);
            } catch(MalformedURLException mue) {
                return null;
            }
        }
    }

}
