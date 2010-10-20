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

package com.sun.xml.ws.api.message;

import com.sun.xml.bind.api.Bridge;
import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.bind.v2.runtime.MarshallerImpl;
import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.message.DOMHeader;
import com.sun.xml.ws.message.StringHeader;
import com.sun.xml.ws.message.jaxb.JAXBHeader;
import com.sun.xml.ws.message.saaj.SAAJHeader;
import com.sun.xml.ws.message.stream.StreamHeader11;
import com.sun.xml.ws.message.stream.StreamHeader12;
import org.w3c.dom.Element;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Factory methods for various {@link Header} implementations.
 *
 * <p>
 * This class provides various methods to create different
 * flavors of {@link Header} classes that store data
 * in different formats.
 *
 * <p>
 * This is a part of the JAX-WS RI internal API so that
 * {@link Pipe} implementations can reuse the implementations
 * done inside the JAX-WS without having a strong dependency
 * to the actual class.
 *
 * <p>
 * If you find some of the useful convenience methods missing
 * from this class, please talk to us.
 *
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class Headers {
    private Headers() {}

    /**
     * @deprecated
     *      Use {@link #create(JAXBRIContext, Object)} instead.
     */
    public static Header create(SOAPVersion soapVersion, Marshaller m, Object o) {
        return new JAXBHeader(((MarshallerImpl)m).getContext(),o);
    }

    /**
     * Creates a {@link Header} backed a by a JAXB bean.
     */
    public static Header create(JAXBRIContext context, Object o) {
        return new JAXBHeader(context,o);
    }

    /**
     * Creates a {@link Header} backed a by a JAXB bean, with the given tag name.
     *
     * See {@link #create(SOAPVersion, Marshaller, Object)} for the meaning
     * of other parameters.
     *
     * @param tagName
     *      The name of the newly created header. Must not be null.
     * @param o
     *      The JAXB bean that represents the contents of the header. Must not be null.
     */
    public static Header create(SOAPVersion soapVersion, Marshaller m, QName tagName, Object o) {
        return create(soapVersion,m,new JAXBElement(tagName,o.getClass(),o));
    }

    /**
     * Creates a {@link Header} backed a by a JAXB bean.
     */
    public static Header create(Bridge bridge, Object jaxbObject) {
        return new JAXBHeader(bridge, jaxbObject);
    }

    /**
     * Creates a new {@link Header} backed by a SAAJ object.
     */
    public static Header create(SOAPHeaderElement header) {
        return new SAAJHeader(header);
    }

    /**
     * Creates a new {@link Header} backed by an {@link Element}.
     */
    public static Header create( Element node ) {
        return new DOMHeader<Element>(node);
    }

    /**
     * @deprecated
     *      Use {@link #create(Element)}
     */
    public static Header create( SOAPVersion soapVersion, Element node ) {
        return create(node);
    }

    /**
     * Creates a new {@link Header} that reads from {@link XMLStreamReader}.
     *
     * <p>
     * Note that the header implementation will read the entire data
     * into memory anyway, so this might not be as efficient as you might hope.
     */
    public static Header create( SOAPVersion soapVersion, XMLStreamReader reader ) throws XMLStreamException {
        switch(soapVersion) {
        case SOAP_11:
            return new StreamHeader11(reader);
        case SOAP_12:
            return new StreamHeader12(reader);
        default:
            throw new AssertionError();
        }
    }

    /**
     * Creates a new {@link Header} that that has a single text value in it
     * (IOW, of the form &lt;foo>text&lt;/foo>.)
     *
     * @param name QName of the header element
     * @param value text value of the header
     */
    public static Header create(QName name, String value) {
        return new StringHeader(name, value);
    }    
}
