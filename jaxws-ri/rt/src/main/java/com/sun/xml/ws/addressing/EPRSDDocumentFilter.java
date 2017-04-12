/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2013 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.addressing;

import com.sun.xml.ws.api.server.*;
import com.sun.xml.ws.api.server.Module;
import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.streaming.XMLStreamReaderFactory;
import com.sun.xml.ws.util.xml.XMLStreamWriterFilter;
import org.jvnet.staxex.util.XMLStreamReaderToXMLStreamWriter;
import com.sun.xml.ws.server.WSEndpointImpl;
import com.sun.xml.ws.wsdl.parser.WSDLConstants;
import com.sun.istack.Nullable;
import com.sun.istack.NotNull;

import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.namespace.NamespaceContext;
import java.io.IOException;
import java.util.List;
import java.util.Collection;
import java.util.Collections;

/**
 * This class acts as a filter for the Extension elements in the wsa:EndpointReference in the wsdl.
 * In addition to filtering the EPR extensions from WSDL, it adds the extensions configured by the JAX-WS runtime
 * specifc to an endpoint.
 *  
 * @author Rama Pulavarthi
 */
public class EPRSDDocumentFilter implements SDDocumentFilter {
    private final WSEndpointImpl<?> endpoint;
    //initialize lazily
    List<BoundEndpoint> beList;
    public EPRSDDocumentFilter(@NotNull WSEndpointImpl<?> endpoint) {
        this.endpoint = endpoint;
    }

    private @Nullable WSEndpointImpl<?> getEndpoint(String serviceName, String portName) {
        if (serviceName == null || portName == null)
            return null;
        if (endpoint.getServiceName().getLocalPart().equals(serviceName) && endpoint.getPortName().getLocalPart().equals(portName))
            return endpoint;

        if(beList == null) {
            //check if it is run in a Java EE Container and get hold of other endpoints in the application
            Module module = endpoint.getContainer().getSPI(Module.class);
            if (module != null) {
                beList = module.getBoundEndpoints();
            } else {
                beList = Collections.<BoundEndpoint>emptyList();
            }
        }

        for (BoundEndpoint be : beList) {
            WSEndpoint wse = be.getEndpoint();
            if (wse.getServiceName().getLocalPart().equals(serviceName) && wse.getPortName().getLocalPart().equals(portName)) {
                return (WSEndpointImpl) wse;
            }
        }

        return null;

    }

    public XMLStreamWriter filter(SDDocument doc, XMLStreamWriter w) throws XMLStreamException, IOException {
        if (!doc.isWSDL()) {
            return w;
        }
        
        return new XMLStreamWriterFilter(w) {
            private boolean eprExtnFilterON = false; //when true, all writer events are filtered out

            private boolean portHasEPR = false;
            private int eprDepth = -1; // -1 -> outside wsa:epr, 0 -> on wsa:epr start/end , > 0 inside wsa:epr

            private String serviceName = null; //non null when inside wsdl:service scope
            private boolean onService = false; //flag to get service name when on wsdl:service element start
            private int serviceDepth = -1;  // -1 -> outside wsdl:service, 0 -> on wsdl:service start/end , > 0 inside wsdl:service

            private String portName = null; //non null when inside wsdl:port scope
            private boolean onPort = false; //flag to get port name when on wsdl:port element start
            private int portDepth = -1; // -1 -> outside wsdl:port, 0 -> on wsdl:port start/end , > 0 inside wsdl:port

            private String portAddress; // when a complete epr is written, endpoint address is used as epr address
            private boolean onPortAddress = false; //flag to get endpoint address when on soap:address element start

            private void handleStartElement(String localName, String namespaceURI) throws XMLStreamException {
                resetOnElementFlags();
                if (serviceDepth >= 0) {
                    serviceDepth++;
                }
                if (portDepth >= 0) {
                    portDepth++;
                }
                if (eprDepth >= 0) {
                    eprDepth++;
                }

                if (namespaceURI.equals(WSDLConstants.QNAME_SERVICE.getNamespaceURI()) && localName.equals(WSDLConstants.QNAME_SERVICE.getLocalPart())) {
                    onService = true;
                    serviceDepth = 0;
                } else if (namespaceURI.equals(WSDLConstants.QNAME_PORT.getNamespaceURI()) && localName.equals(WSDLConstants.QNAME_PORT.getLocalPart())) {
                    if (serviceDepth >= 1) {
                        onPort = true;
                        portDepth = 0;
                    }
                } else if (namespaceURI.equals(W3CAddressingConstants.WSA_NAMESPACE_NAME) && localName.equals("EndpointReference")) {
                    if (serviceDepth >= 1 && portDepth >= 1) {
                        portHasEPR = true;
                        eprDepth = 0;
                    }
                } else if ((namespaceURI.equals(WSDLConstants.NS_SOAP_BINDING_ADDRESS.getNamespaceURI()) || namespaceURI.equals(WSDLConstants.NS_SOAP12_BINDING_ADDRESS.getNamespaceURI()))
                        &&  localName.equals("address") && portDepth ==1) {
                    onPortAddress = true;
                }
                WSEndpoint endpoint = getEndpoint(serviceName,portName);
                //filter epr for only for the port corresponding to this endpoint
                //if (service.getLocalPart().equals(serviceName) && port.getLocalPart().equals(portName)) {
                if ( endpoint != null) {
                    if ((eprDepth == 1) && !namespaceURI.equals(W3CAddressingConstants.WSA_NAMESPACE_NAME)) {
                        //epr extension element
                        eprExtnFilterON = true;

                    }

                    /*
                    if (eprExtnFilterON) {
                        writeEPRExtensions();
                    }
                    */
                }
            }

            private void resetOnElementFlags() {
                if (onService) {
                    onService = false;
                }
                if (onPort) {
                    onPort = false;
                }
                if (onPortAddress) {
                    onPortAddress = false;
                }

            }


            private void writeEPRExtensions(Collection<WSEndpointReference.EPRExtension> eprExtns) throws XMLStreamException {
               if (eprExtns != null) {
                        for (WSEndpointReference.EPRExtension e : eprExtns) {
                            XMLStreamReaderToXMLStreamWriter c = new XMLStreamReaderToXMLStreamWriter();
                            XMLStreamReader r = e.readAsXMLStreamReader();
                            c.bridge(r, writer);
                            XMLStreamReaderFactory.recycle(r);
                        }
                    }
            }

            @Override
            public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
                handleStartElement(localName, namespaceURI);
                if (!eprExtnFilterON) {
                    super.writeStartElement(prefix, localName, namespaceURI);
                }
            }

            @Override
            public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
                handleStartElement(localName, namespaceURI);
                if (!eprExtnFilterON) {
                    super.writeStartElement(namespaceURI, localName);
                }
            }

            @Override
            public void writeStartElement(String localName) throws XMLStreamException {
                if (!eprExtnFilterON) {
                    super.writeStartElement(localName);
                }
            }

            private void handleEndElement() throws XMLStreamException {
                resetOnElementFlags();
                //End of wsdl:port, write complete EPR if not present.
                if (portDepth == 0) {

                    if (!portHasEPR && getEndpoint(serviceName,portName) != null) {

                        //write the complete EPR with address.
                        writer.writeStartElement(AddressingVersion.W3C.getPrefix(),"EndpointReference", AddressingVersion.W3C.nsUri );
                        writer.writeNamespace(AddressingVersion.W3C.getPrefix(), AddressingVersion.W3C.nsUri);
                        writer.writeStartElement(AddressingVersion.W3C.getPrefix(), AddressingVersion.W3C.eprType.address, AddressingVersion.W3C.nsUri);
                        writer.writeCharacters(portAddress);
                        writer.writeEndElement();
                        writeEPRExtensions(getEndpoint(serviceName, portName).getEndpointReferenceExtensions());
                        writer.writeEndElement();

                    }
                }
                //End of wsa:EndpointReference, write EPR extension elements
                if (eprDepth == 0) {
                    if (portHasEPR && getEndpoint(serviceName,portName) != null) {
                        writeEPRExtensions(getEndpoint(serviceName, portName).getEndpointReferenceExtensions());
                    }
                    eprExtnFilterON = false;
                }

                if(serviceDepth >= 0 )  {
                    serviceDepth--;
                }
                if(portDepth >= 0) {
                    portDepth--;
                }
                if(eprDepth >=0) {
                    eprDepth--;
                }

                if (serviceDepth == -1) {
                    serviceName = null;
                }
                if (portDepth == -1) {
                    portHasEPR = false;
                    portAddress = null;
                    portName = null;
                }
            }

            @Override
            public void writeEndElement() throws XMLStreamException {
                handleEndElement();
                if (!eprExtnFilterON) {
                    super.writeEndElement();
                }
            }

            private void handleAttribute(String localName, String value) {
                if (localName.equals("name")) {
                    if (onService) {
                        serviceName = value;
                        onService = false;
                    } else if (onPort) {
                        portName = value;
                        onPort = false;
                    }
                }
                if (localName.equals("location") && onPortAddress) {
                    portAddress = value;
                }


            }

            @Override
            public void writeAttribute(String prefix, String namespaceURI, String localName, String value) throws XMLStreamException {
                handleAttribute(localName, value);
                if (!eprExtnFilterON) {
                    super.writeAttribute(prefix, namespaceURI, localName, value);
                }
            }

            @Override
            public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
                handleAttribute(localName, value);
                if (!eprExtnFilterON) {
                    super.writeAttribute(namespaceURI, localName, value);
                }
            }

            @Override
            public void writeAttribute(String localName, String value) throws XMLStreamException {
                handleAttribute(localName, value);
                if (!eprExtnFilterON) {
                    super.writeAttribute(localName, value);
                }
            }


            @Override
            public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
                if (!eprExtnFilterON) {
                    super.writeEmptyElement(namespaceURI, localName);
                }
            }

            @Override
            public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
                if (!eprExtnFilterON) {
                    super.writeNamespace(prefix, namespaceURI);
                }
            }

            @Override
            public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
                if (!eprExtnFilterON) {
                    super.setNamespaceContext(context);
                }
            }

            @Override
            public void setDefaultNamespace(String uri) throws XMLStreamException {
                if (!eprExtnFilterON) {
                    super.setDefaultNamespace(uri);
                }
            }

            @Override
            public void setPrefix(String prefix, String uri) throws XMLStreamException {
                if (!eprExtnFilterON) {
                    super.setPrefix(prefix, uri);
                }
            }

            @Override
            public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
                if (!eprExtnFilterON) {
                    super.writeProcessingInstruction(target, data);
                }
            }

            @Override
            public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
                if (!eprExtnFilterON) {
                    super.writeEmptyElement(prefix, localName, namespaceURI);
                }
            }

            @Override
            public void writeCData(String data) throws XMLStreamException {
                if (!eprExtnFilterON) {
                    super.writeCData(data);
                }
            }

            @Override
            public void writeCharacters(String text) throws XMLStreamException {
                if (!eprExtnFilterON) {
                    super.writeCharacters(text);
                }
            }

            @Override
            public void writeComment(String data) throws XMLStreamException {
                if (!eprExtnFilterON) {
                    super.writeComment(data);
                }
            }

            @Override
            public void writeDTD(String dtd) throws XMLStreamException {
                if (!eprExtnFilterON) {
                    super.writeDTD(dtd);
                }
            }

            @Override
            public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
                if (!eprExtnFilterON) {
                    super.writeDefaultNamespace(namespaceURI);
                }
            }

            @Override
            public void writeEmptyElement(String localName) throws XMLStreamException {
                if (!eprExtnFilterON) {
                    super.writeEmptyElement(localName);
                }
            }

            @Override
            public void writeEntityRef(String name) throws XMLStreamException {
                if (!eprExtnFilterON) {
                    super.writeEntityRef(name);
                }
            }

            @Override
            public void writeProcessingInstruction(String target) throws XMLStreamException {
                if (!eprExtnFilterON) {
                    super.writeProcessingInstruction(target);
                }
            }


            @Override
            public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
                if (!eprExtnFilterON) {
                    super.writeCharacters(text, start, len);
                }
            }

        };

    }

}
