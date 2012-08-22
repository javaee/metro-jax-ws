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

package com.sun.xml.ws.server;

import com.sun.istack.NotNull;
import com.sun.xml.stream.buffer.MutableXMLStreamBuffer;
import com.sun.xml.stream.buffer.XMLStreamBufferResult;
import com.sun.xml.ws.api.server.SDDocument;
import com.sun.xml.ws.api.server.SDDocumentSource;
import com.sun.xml.ws.wsdl.writer.WSDLResolver;

import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceException;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * WSDLGenerator uses WSDLResolver while creating WSDL artifacts. WSDLResolver
 * is used to control the file names and which artifact to be generated or not.
 *
 * @author Jitendra Kotamraju
 */
final class WSDLGenResolver implements WSDLResolver {
    
    private final List<SDDocumentImpl> docs;
    private final List<SDDocumentSource> newDocs = new ArrayList<SDDocumentSource>();
    private SDDocumentSource concreteWsdlSource;
    
    private SDDocumentImpl abstractWsdl;
    private SDDocumentImpl concreteWsdl;

    /**
     * targetNS -> schema documents.
     */
    private final Map<String, List<SDDocumentImpl>> nsMapping = new HashMap<String,List<SDDocumentImpl>>();

    private final QName serviceName;
    private final QName portTypeName;

    public WSDLGenResolver(@NotNull List<SDDocumentImpl> docs,QName serviceName,QName portTypeName) {
        this.docs = docs;
        this.serviceName = serviceName;
        this.portTypeName = portTypeName;

        for (SDDocumentImpl doc : docs) {
            if(doc.isWSDL()) {
                SDDocument.WSDL wsdl = (SDDocument.WSDL) doc;
                if(wsdl.hasPortType())
                    abstractWsdl = doc;
            }
            if(doc.isSchema()) {
                SDDocument.Schema schema = (SDDocument.Schema) doc;
                List<SDDocumentImpl> sysIds = nsMapping.get(schema.getTargetNamespace());
                if (sysIds == null) {
                    sysIds = new ArrayList<SDDocumentImpl>();
                    nsMapping.put(schema.getTargetNamespace(), sysIds);
                }
                sysIds.add(doc);
            }
        }
    }
    
    /**
     * Generates the concrete WSDL that contains service element.
     *
     * @return Result the generated concrete WSDL
     */
    public Result getWSDL(String filename) {
        URL url = createURL(filename);
        MutableXMLStreamBuffer xsb = new MutableXMLStreamBuffer();
        xsb.setSystemId(url.toExternalForm());
        concreteWsdlSource = SDDocumentSource.create(url,xsb);
        newDocs.add(concreteWsdlSource);
        XMLStreamBufferResult r = new XMLStreamBufferResult(xsb);
        r.setSystemId(filename);
        return r;
    }

    /**
     * At present, it returns file URL scheme eventhough there is no resource
     * in the filesystem.
     *
     * @return URL of the generated document
     *
     */
    private URL createURL(String filename) {
        try {
            return new URL("file:///"+filename);
        } catch (MalformedURLException e) {
            // TODO: I really don't think this is the right way to handle this error,
            // WSDLResolver needs to be documented carefully.
            throw new WebServiceException(e);
        }
    }

    /**
     * Updates filename if the suggested filename need to be changed in
     * wsdl:import. If the metadata already contains abstract wsdl(i.e. a WSDL
     * which has the porttype), then the abstract wsdl shouldn't be generated
     *
     * return null if abstract WSDL need not be generated
     *        Result the abstract WSDL
     */
    public Result getAbstractWSDL(Holder<String> filename) {
        if (abstractWsdl != null) {
            filename.value = abstractWsdl.getURL().toString();
            return null;                // Don't generate abstract WSDL
        }
        URL url = createURL(filename.value);
        MutableXMLStreamBuffer xsb = new MutableXMLStreamBuffer();
        xsb.setSystemId(url.toExternalForm());
        SDDocumentSource abstractWsdlSource = SDDocumentSource.create(url,xsb);
        newDocs.add(abstractWsdlSource);
        XMLStreamBufferResult r = new XMLStreamBufferResult(xsb);
        r.setSystemId(filename.value);
        return r;
    }

    /**
     * Updates filename if the suggested filename need to be changed in
     * xsd:import. If there is already a schema document for the namespace
     * in the metadata, then it is not generated.
     *
     * return null if schema need not be generated
     *        Result the generated schema document
     */
    public Result getSchemaOutput(String namespace, Holder<String> filename) {
        List<SDDocumentImpl> schemas = nsMapping.get(namespace);
        if (schemas != null) {
            if (schemas.size() > 1) {
                throw new ServerRtException("server.rt.err",
                    "More than one schema for the target namespace "+namespace);
            }
            filename.value = schemas.get(0).getURL().toExternalForm();
            return null;            // Don't generate schema
        }

        URL url = createURL(filename.value);
        MutableXMLStreamBuffer xsb = new MutableXMLStreamBuffer();
        xsb.setSystemId(url.toExternalForm());
        SDDocumentSource sd = SDDocumentSource.create(url,xsb);
        newDocs.add(sd);

        XMLStreamBufferResult r = new XMLStreamBufferResult(xsb);
        r.setSystemId(filename.value);
        return r;
    }
    
    /**
     * Converts SDDocumentSource to SDDocumentImpl and updates original docs. It
     * categories the generated documents into WSDL, Schema types.
     *
     * @return the primary WSDL
     *         null if it is not there in the generated documents
     *
     */
    public SDDocumentImpl updateDocs() {
        for (SDDocumentSource doc : newDocs) {
            SDDocumentImpl docImpl = SDDocumentImpl.create(doc,serviceName,portTypeName);
            if (doc == concreteWsdlSource) {
                concreteWsdl = docImpl;
            }
            docs.add(docImpl);
        }
        return concreteWsdl;
    }
    
}
