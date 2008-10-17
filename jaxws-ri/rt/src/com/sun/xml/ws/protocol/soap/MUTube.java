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
package com.sun.xml.ws.protocol.soap;

import com.sun.xml.ws.api.SOAPVersion;
import static com.sun.xml.ws.api.SOAPVersion.SOAP_11;
import static com.sun.xml.ws.api.SOAPVersion.SOAP_12;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.message.HeaderList;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Messages;
import com.sun.xml.ws.api.pipe.Tube;
import com.sun.xml.ws.api.pipe.TubeCloner;
import com.sun.xml.ws.api.pipe.helper.AbstractFilterTubeImpl;
import com.sun.xml.ws.message.DOMHeader;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.soap.SOAPFaultException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author Rama Pulavarthi
 */

abstract class MUTube extends AbstractFilterTubeImpl {

    private static final String MU_FAULT_DETAIL_LOCALPART = "NotUnderstood";
    private final static QName MU_HEADER_DETAIL = new QName(SOAPVersion.SOAP_12.nsUri, MU_FAULT_DETAIL_LOCALPART);
    //TODO: change
    protected static final Logger logger = Logger.getLogger(
            com.sun.xml.ws.util.Constants.LoggingDomain + ".soap.decoder");
    private final static String MUST_UNDERSTAND_FAULT_MESSAGE_STRING =
            "One or more mandatory SOAP header blocks not understood";

    protected final SOAPVersion soapVersion;
    private final AddressingVersion addressingVersion;
    
    protected MUTube(WSBinding binding, Tube next) {
        super(next);
        // MUPipe should n't be used for bindings other than SOAP.
        if (!(binding instanceof SOAPBinding)) {
            throw new WebServiceException(
                    "MUPipe should n't be used for bindings other than SOAP.");
        }
        this.soapVersion = binding.getSOAPVersion();
        this.addressingVersion = binding.getAddressingVersion();
    }

    protected MUTube(MUTube that, TubeCloner cloner) {
        super(that, cloner);
        soapVersion = that.soapVersion;
        addressingVersion = that.addressingVersion;
    }

    /**
     * @param headers      HeaderList that needs MU processing
     * @param roles        Roles configured on the Binding. Required Roles supposed to be assumbed a by a
     *                     SOAP Binding implementation are added.
     * @param knownHeaders Set of headers that this binding understands
     * @return returns the headers that have mustUnderstand attribute and are not understood
     *         by the binding.
     */
    protected final Set<QName> getMisUnderstoodHeaders(HeaderList headers, Set<String> roles, Set<QName> knownHeaders) {
        Set<QName> notUnderstoodHeaders = null;

        understandAddressingHeaders(knownHeaders);

        for (int i = 0; i < headers.size(); i++) {
            if (!headers.isUnderstood(i)) {
                Header header = headers.get(i);
                if (!header.isIgnorable(soapVersion, roles)) {
                    QName qName = new QName(header.getNamespaceURI(), header.getLocalPart());
                    if (! knownHeaders.contains(qName)) {
                        logger.info("Element not understood=" + qName);
                        if (notUnderstoodHeaders == null)
                            notUnderstoodHeaders = new HashSet<QName>();
                        notUnderstoodHeaders.add(qName);
                    }
                }
            }
        }
        return notUnderstoodHeaders;
    }

    /**
     * Understand WS-Addressing headers if WS-Addressing is enabled
     *
     * @param knownHeaders Set of headers that this binding understands
     */
    private void understandAddressingHeaders(Set<QName> knownHeaders) {
        if (addressingVersion != null) {
            knownHeaders.add(addressingVersion.actionTag);
            knownHeaders.add(addressingVersion.faultToTag);
            knownHeaders.add(addressingVersion.fromTag);
            knownHeaders.add(addressingVersion.messageIDTag);
            knownHeaders.add(addressingVersion.relatesToTag);
            knownHeaders.add(addressingVersion.replyToTag);
            knownHeaders.add(addressingVersion.toTag);
        }
    }

    /**
     * @param notUnderstoodHeaders
     * @return SOAPfaultException with SOAPFault representing the MustUnderstand SOAP Fault.
     *         notUnderstoodHeaders are added in the fault detail.
     */
    final SOAPFaultException createMUSOAPFaultException(Set<QName> notUnderstoodHeaders) {
        try {
            SOAPFault fault = createMUSOAPFault();
            setMUFaultString(fault, notUnderstoodHeaders);
            return new SOAPFaultException(fault);
        } catch (SOAPException e) {
            throw new WebServiceException(e);
        }
    }

    /**
     * This should be used only in ServerMUPipe
     *
     * @param notUnderstoodHeaders
     * @return Message representing a SOAPFault
     *         In SOAP 1.1, notUnderstoodHeaders are added in the fault Detail
     *         in SOAP 1.2, notUnderstoodHeaders are added as the SOAP Headers
     */

    final Message createMUSOAPFaultMessage(Set<QName> notUnderstoodHeaders) {
        try {
            SOAPFault fault = createMUSOAPFault();
            if (soapVersion == SOAP_11) {
                setMUFaultString(fault, notUnderstoodHeaders);
            }
            Message muFaultMessage = Messages.create(fault);
            if (soapVersion == SOAP_12) {
                addHeader(muFaultMessage, notUnderstoodHeaders);
            }
            return muFaultMessage;
        } catch (SOAPException e) {
            throw new WebServiceException(e);
        }
    }

    private void setMUFaultString(SOAPFault fault, Set<QName> notUnderstoodHeaders) throws SOAPException {
        fault.setFaultString("MustUnderstand headers:" +
                notUnderstoodHeaders + " are not understood");
    }

    private static void addHeader(Message m, Set<QName> notUnderstoodHeaders) throws SOAPException {
        for (QName qname : notUnderstoodHeaders) {
            SOAPElement soapEl = SOAP_12.saajSoapFactory.createElement(MU_HEADER_DETAIL);
            soapEl.addNamespaceDeclaration("abc", qname.getNamespaceURI());
            soapEl.setAttribute("qname", "abc:" + qname.getLocalPart());
            Header header = new DOMHeader<Element>(soapEl);
            m.getHeaders().add(header);
        }
    }

    private SOAPFault createMUSOAPFault() throws SOAPException {
        return soapVersion.saajSoapFactory.createFault(
                MUST_UNDERSTAND_FAULT_MESSAGE_STRING,
                soapVersion.faultCodeMustUnderstand);
    }
}
