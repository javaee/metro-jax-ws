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

package com.sun.xml.ws.server;

import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.NextAction;
import com.sun.xml.ws.api.pipe.helper.AbstractTubeImpl;
import com.sun.xml.ws.api.server.SDDocument;
import com.sun.xml.ws.api.server.SDDocumentSource;
import com.sun.xml.ws.api.server.ServiceDefinition;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.util.ByteArrayBuffer;
import com.sun.xml.ws.util.MetadataUtil;
import com.sun.xml.ws.util.pipe.AbstractSchemaValidationTube;
import com.sun.xml.ws.util.xml.MetadataDocument;
import com.sun.xml.ws.fault.SOAPFaultBuilder;
import org.w3c.dom.*;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.ws.WebServiceException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * {@link Tube} that does the schema validation on the server side.
 *
 * @author Jitendra Kotamraju
 */
public class ServerSchemaValidationTube extends AbstractSchemaValidationTube {

    private static final Logger LOGGER = Logger.getLogger(ServerSchemaValidationTube.class.getName());

    //private final ServiceDefinition docs;
    private final Schema schema;
    private final Validator validator;
    
    private final boolean noValidation;
    private final SEIModel seiModel;
    private final WSDLPort wsdlPort;

    public ServerSchemaValidationTube(WSEndpoint endpoint, WSBinding binding,
            SEIModel seiModel, WSDLPort wsdlPort, Tube next) {
        super(binding, next);
        this.seiModel = seiModel;
        this.wsdlPort = wsdlPort;
        //docs = endpoint.getServiceDefinition();
        SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        Source[] sources = getSchemaSources(endpoint.getServiceDefinition());
        for(Source source : sources) {
            LOGGER.fine("Constructing validation Schema from = "+source.getSystemId());
            //printDOM((DOMSource)source);
        }
        if (sources.length != 0) {
            noValidation = false;
            sf.setResourceResolver(new MetadataResolverImpl(endpoint.getServiceDefinition()));
            try {
                schema = sf.newSchema(sources);
            } catch(SAXException e) {
                throw new WebServiceException(e);
            }
            validator = schema.newValidator();
        } else {
            noValidation = true;
            schema = null;
            validator = null;
        }
    }

    /**
     * Constructs list of schema documents as follows:
     *   - all <xsd:schema> fragements from all WSDL documents.
     *   - all schema documents in the application(from WAR etc)
     *
     * @param sd wsdl/schema document
     * @return list of root schema documents
     */
    private Source[] getSchemaSources(ServiceDefinition sd) {
        String primary = sd.getPrimary().getURL().toExternalForm();
        MetadataUtil.MetadataResolver mdresolver = new MetadataResolverImpl(sd);
        Map<String, SDDocument> docs = MetadataUtil.getMetadataClosure(primary, mdresolver, true);

        List<Source> list = new ArrayList<Source>();
        for(Map.Entry<String, SDDocument> entry : docs.entrySet()) {
            SDDocument doc = entry.getValue();
            // Add all xsd:schema fragments from all WSDLs. That should form a closure of schemas.
            if (doc.isWSDL()) {
                Document dom = createDOM(doc);
                // Get xsd:schema node from WSDL's DOM
                addSchemaFragmentSource(dom, doc.getURL().toExternalForm(), list);
            } else if (doc.isSchema()) {
                // If there are multiple schemas with the same targetnamespace,
                // JAXP works only with the first one. Above, all schema fragments may have the same targetnamespace,
                // and that means it will not include all the schemas. Since we have a list of schemas, just add them.
                Document dom = createDOM(doc);
                list.add(new DOMSource(dom, doc.getURL().toExternalForm()));
            }
        }
        //addSchemaSource(list);
        return list.toArray(new Source[list.size()]) ;
    }

    private class MetadataResolverImpl implements MetadataUtil.MetadataResolver, LSResourceResolver {

        Map<String, SDDocument> docs = new HashMap<String, SDDocument>();

        MetadataResolverImpl(ServiceDefinition sd) {
            for(SDDocument doc : sd) {
                docs.put(doc.getURL().toExternalForm(), doc);
            }
        }

        public SDDocument resolveEntity(String systemId) {
            SDDocument sdi = docs.get(systemId);
            if (sdi == null) {
                SDDocumentSource sds;
                try {
                    sds = SDDocumentSource.create(new URL(systemId));
                } catch(MalformedURLException e) {
                    throw new WebServiceException(e);
                }
                sdi = MetadataDocument.create(sds, new QName(""), new QName(""));
                docs.put(systemId, sdi);
            }
            return sdi;
        }

        public LSInput resolveResource(String type, String namespaceURI, String publicId, final String systemId, final String baseURI) {
            LOGGER.fine("type="+type+ " namespaceURI="+namespaceURI+" publicId="+publicId+" systemId="+systemId+" baseURI="+baseURI);
            try {
                URL base = baseURI == null ? null : new URL(baseURI);
                final URL rel = new URL(base, systemId);
                final SDDocument doc = docs.get(rel.toExternalForm());
                if (doc != null) {
                    return new LSInput() {

                        public Reader getCharacterStream() {
                            return null;
                        }

                        public void setCharacterStream(Reader characterStream) {
                            throw new UnsupportedOperationException();
                        }

                        public InputStream getByteStream() {
                            ByteArrayBuffer bab = new ByteArrayBuffer();
                            try {
                                doc.writeTo(null, resolver, bab);
                            } catch (IOException ioe) {
                                throw new WebServiceException(ioe);
                            }
                            return bab.newInputStream();
                        }

                        public void setByteStream(InputStream byteStream) {
                            throw new UnsupportedOperationException();
                        }

                        public String getStringData() {
                            return null;
                        }

                        public void setStringData(String stringData) {
                            throw new UnsupportedOperationException();
                        }

                        public String getSystemId() {
                            return rel.toExternalForm();
                        }

                        public void setSystemId(String systemId) {
                            throw new UnsupportedOperationException();
                        }

                        public String getPublicId() {
                            return null;
                        }

                        public void setPublicId(String publicId) {
                            throw new UnsupportedOperationException();
                        }

                        public String getBaseURI() {
                            return rel.toExternalForm();
                        }

                        public void setBaseURI(String baseURI) {
                            throw new UnsupportedOperationException();
                        }

                        public String getEncoding() {
                            return null;
                        }

                        public void setEncoding(String encoding) {
                            throw new UnsupportedOperationException();
                        }

                        public boolean getCertifiedText() {
                            return false;
                        }

                        public void setCertifiedText(boolean certifiedText) {
                            throw new UnsupportedOperationException();
                        }
                    };
                }
            } catch(Exception e) {
                LOGGER.log(Level.WARNING, "Exception in LSResourceResolver impl", e);
            }
            LOGGER.fine("Don't know about systemId="+systemId+" baseURI="+baseURI);
            return null;
        }

    }


    protected Validator getValidator() {
        return validator;
    }

    protected boolean isNoValidation() {
        return noValidation;
    }

    @Override
    public NextAction processRequest(Packet request) {
        if (isNoValidation() || !request.getMessage().hasPayload() || request.getMessage().isFault()) {
            return super.processRequest(request);
        }
        try {
            doProcess(request);
        } catch(SAXException se) {
            LOGGER.log(Level.WARNING, "Client Request doesn't pass Service's Schema Validation", se);
            // Client request is invalid. So sending specific fault code
            // Also converting this to fault message so that handlers may get
            // to see the message.
            SOAPVersion soapVersion = binding.getSOAPVersion();
            Message faultMsg = SOAPFaultBuilder.createSOAPFaultMessage(
                    soapVersion, null, se, soapVersion.faultCodeClient);
            return doReturnWith(request.createServerResponse(faultMsg,
                    wsdlPort, seiModel, binding));
        }
        return super.processRequest(request);
    }

    @Override
    public NextAction processResponse(Packet response) {
        if (isNoValidation() || response.getMessage() == null || !response.getMessage().hasPayload() || response.getMessage().isFault()) {
            return super.processResponse(response);
        }
        try {
            doProcess(response);
        } catch(SAXException se) {
            // TODO: Should we convert this to fault Message ??
            throw new WebServiceException(se);
        }
        return super.processResponse(response);
    }

    protected ServerSchemaValidationTube(ServerSchemaValidationTube that, TubeCloner cloner) {
        super(that,cloner);
        //this.docs = that.docs;
        this.schema = that.schema;      // Schema is thread-safe
        this.validator = schema.newValidator();
        this.noValidation = that.noValidation;
        this.seiModel = that.seiModel;
        this.wsdlPort = that.wsdlPort;
    }

    public AbstractTubeImpl copy(TubeCloner cloner) {
        return new ServerSchemaValidationTube(this,cloner);
    }

}
