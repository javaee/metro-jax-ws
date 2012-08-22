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

package com.sun.xml.ws.developer;

import com.sun.istack.NotNull;
import com.sun.xml.ws.addressing.v200408.MemberSubmissionAddressingConstants;
import com.sun.xml.ws.wsdl.parser.WSDLConstants;
import org.w3c.dom.Element;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAnyAttribute;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.XmlValue;
import javax.xml.namespace.QName;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.WebServiceException;
import java.util.List;
import java.util.Map;

/**
 * Data model for Member Submission WS-Addressing specification. This is modeled after the
 * member submission schema at:
 *
 *  http://schemas.xmlsoap.org/ws/2004/08/addressing/
 *
 * @author Kathy Walsh
 * @author Vivek Pandey
 */

@XmlRootElement(name = "EndpointReference", namespace = MemberSubmissionEndpointReference.MSNS)
@XmlType(name = "EndpointReferenceType", namespace = MemberSubmissionEndpointReference.MSNS)
public final class MemberSubmissionEndpointReference extends EndpointReference implements MemberSubmissionAddressingConstants {

    private final static JAXBContext msjc = MemberSubmissionEndpointReference.getMSJaxbContext();

    public MemberSubmissionEndpointReference() {
    }

    /**
     * construct an EPR from infoset representation
     *
     * @param source A source object containing valid XmlInfoset
     *               instance consistent with the Member Submission WS-Addressing
     * @throws javax.xml.ws.WebServiceException
     *                              if the source does not contain a valid W3C WS-Addressing
     *                              EndpointReference.
     * @throws WebServiceException if the <code>null</code> <code>source</code> value is given
     */
    public MemberSubmissionEndpointReference(@NotNull Source source) {

        if (source == null)
            throw new WebServiceException("Source parameter can not be null on constructor");

        try {
            Unmarshaller unmarshaller = MemberSubmissionEndpointReference.msjc.createUnmarshaller();
            MemberSubmissionEndpointReference epr = unmarshaller.unmarshal(source,MemberSubmissionEndpointReference.class).getValue();

            this.addr = epr.addr;
            this.referenceProperties = epr.referenceProperties;
            this.referenceParameters = epr.referenceParameters;
            this.portTypeName = epr.portTypeName;
            this.serviceName = epr.serviceName;
            this.attributes = epr.attributes;
            this.elements = epr.elements;
        } catch (JAXBException e) {
            throw new WebServiceException("Error unmarshalling MemberSubmissionEndpointReference ", e);
        } catch (ClassCastException e) {
            throw new WebServiceException("Source did not contain MemberSubmissionEndpointReference", e);
        }
    }

    public void writeTo(Result result) {
        try {
            Marshaller marshaller = MemberSubmissionEndpointReference.msjc.createMarshaller();
            //marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
            marshaller.marshal(this, result);
        } catch (JAXBException e) {
            throw new WebServiceException("Error marshalling W3CEndpointReference. ", e);
        }
    }

    /**
     * Constructs a Source containing the wsdl from the MemberSubmissionEndpointReference
     *
     * @return Source A source object containing the wsdl in the MemeberSubmissionEndpointReference, if present.
     */
    public Source toWSDLSource() {        
        Element wsdlElement = null;

        for (Element elem : elements) {
            if (elem.getNamespaceURI().equals(WSDLConstants.NS_WSDL) &&
                    elem.getLocalName().equals(WSDLConstants.QNAME_DEFINITIONS.getLocalPart())) {
                wsdlElement = elem;
            }
        }

        return new DOMSource(wsdlElement);
    }


    private static JAXBContext getMSJaxbContext() {
        try {
            return JAXBContext.newInstance(MemberSubmissionEndpointReference.class);
        } catch (JAXBException e) {
            throw new WebServiceException("Error creating JAXBContext for MemberSubmissionEndpointReference. ", e);
        }
    }

    @XmlElement(name = "Address", namespace = MemberSubmissionEndpointReference.MSNS)
    public Address addr;

    @XmlElement(name = "ReferenceProperties", namespace = MemberSubmissionEndpointReference.MSNS)
    public Elements referenceProperties;

    @XmlElement(name = "ReferenceParameters", namespace = MemberSubmissionEndpointReference.MSNS)
    public Elements referenceParameters;

    @XmlElement(name = "PortType", namespace = MemberSubmissionEndpointReference.MSNS)
    public AttributedQName portTypeName;

    @XmlElement(name = "ServiceName", namespace = MemberSubmissionEndpointReference.MSNS)
    public ServiceNameType serviceName;

    @XmlAnyAttribute
    public Map<QName,String> attributes;

    @XmlAnyElement
    public List<Element> elements;

    public static class Address {
        public Address() {
        }

        @XmlValue
        public String uri;
        @XmlAnyAttribute
        public Map<QName, String> attributes;
    }

    public static class Elements {
        public Elements() {}

        @XmlAnyElement
        public List<Element> elements;
    }


    public static class AttributedQName {
        public AttributedQName() {
        }

        @XmlValue
        public QName name;
        @XmlAnyAttribute
        public Map<QName, String> attributes;
    }

    public static class ServiceNameType extends AttributedQName{
        public ServiceNameType() {
        }

        @XmlAttribute(name="PortName")
        public String portName;
    }

    protected static final String MSNS = "http://schemas.xmlsoap.org/ws/2004/08/addressing";
}
