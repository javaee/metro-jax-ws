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

package com.sun.xml.ws.message;

import com.sun.istack.NotNull;
import com.sun.xml.bind.api.Bridge;
import com.sun.xml.bind.api.BridgeContext;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.addressing.AddressingVersion;
import com.sun.xml.ws.api.addressing.WSEndpointReference;
import com.sun.xml.ws.api.message.Header;
import com.sun.xml.ws.api.streaming.XMLStreamReaderFactory;
import com.sun.xml.ws.spi.db.XMLBridge;

import org.xml.sax.helpers.AttributesImpl;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.util.Set;

/**
 * Partial default implementation of {@link Header}.
 *
 * <p>
 * This is meant to be a convenient base class
 * for {@link Header}-derived classes.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class AbstractHeaderImpl implements Header {

    protected AbstractHeaderImpl() {
    }

    /**
     * @deprecated
     */
    public final <T> T readAsJAXB(Bridge<T> bridge, BridgeContext context) throws JAXBException {
        return readAsJAXB(bridge);
    }

    public <T> T readAsJAXB(Unmarshaller unmarshaller) throws JAXBException {
        try {
            return (T)unmarshaller.unmarshal(readHeader());
        } catch (Exception e) {
            throw new JAXBException(e);
        }
    }
    /** @deprecated */
    public <T> T readAsJAXB(Bridge<T> bridge) throws JAXBException {
        try {
            return bridge.unmarshal(readHeader());
        } catch (XMLStreamException e) {
            throw new JAXBException(e);
        }
    }

    public <T> T readAsJAXB(XMLBridge<T> bridge) throws JAXBException {
        try {
            return bridge.unmarshal(readHeader(), null);
        } catch (XMLStreamException e) {
            throw new JAXBException(e);
        }
    }

    /**
     * Default implementation that copies the infoset. Not terribly efficient.
     */
    public WSEndpointReference readAsEPR(AddressingVersion expected) throws XMLStreamException {
        XMLStreamReader xsr = readHeader();
        WSEndpointReference epr = new WSEndpointReference(xsr, expected);
        XMLStreamReaderFactory.recycle(xsr);
        return epr;
    }

    public boolean isIgnorable(@NotNull SOAPVersion soapVersion, @NotNull Set<String> roles) {
        // check mustUnderstand
        String v = getAttribute(soapVersion.nsUri, "mustUnderstand");
        if(v==null || !parseBool(v)) return true;

        if (roles == null) return true;
        
        // now role
        return !roles.contains(getRole(soapVersion));
    }

    public @NotNull String getRole(@NotNull SOAPVersion soapVersion) {
        String v = getAttribute(soapVersion.nsUri, soapVersion.roleAttributeName);
        if(v==null)
            v = soapVersion.implicitRole;
        return v;
    }

    public boolean isRelay() {
        String v = getAttribute(SOAPVersion.SOAP_12.nsUri,"relay");
        if(v==null) return false;   // on SOAP 1.1 message there shouldn't be such an attribute, so this works fine
        return parseBool(v);
    }

    public String getAttribute(QName name) {
        return getAttribute(name.getNamespaceURI(),name.getLocalPart());
    }

    /**
     * Parses a string that looks like {@code xs:boolean} into boolean.
     *
     * This method assumes that the whilespace normalization has already taken place.
     */
    protected final boolean parseBool(String value) {
        if(value.length()==0)
            return false;

        char ch = value.charAt(0);
        return ch=='t' || ch=='1';
    }

    public String getStringContent() {
        try {
            XMLStreamReader xsr = readHeader();
            xsr.nextTag();
            return xsr.getElementText();
        } catch (XMLStreamException e) {
            return null;
        }
    }

    protected static final AttributesImpl EMPTY_ATTS = new AttributesImpl();
}
