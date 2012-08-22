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

package com.sun.xml.ws.message.jaxb;

import com.sun.xml.bind.api.Bridge;
import com.sun.xml.bind.api.JAXBRIContext;
import com.sun.xml.bind.api.TypeReference;
import com.sun.xml.bind.v2.runtime.JAXBContextImpl;
import com.sun.xml.bind.v2.runtime.MarshallerImpl;
import org.w3c.dom.Node;
import org.xml.sax.ContentHandler;

import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Used to adapt {@link Marshaller} into a {@link Bridge}.
 * @deprecated
 * @author Kohsuke Kawaguchi
 */
final class MarshallerBridge extends Bridge {
    public MarshallerBridge(JAXBRIContext context) {
        super((JAXBContextImpl)context);
    }

    public void marshal(Marshaller m, Object object, XMLStreamWriter output) throws JAXBException {
        m.setProperty(Marshaller.JAXB_FRAGMENT,true);
        try {
            m.marshal(object,output);
        } finally {
            m.setProperty(Marshaller.JAXB_FRAGMENT,false);
        }
    }

    public void marshal(Marshaller m, Object object, OutputStream output, NamespaceContext nsContext) throws JAXBException {
        m.setProperty(Marshaller.JAXB_FRAGMENT,true);
        try {
            ((MarshallerImpl)m).marshal(object,output,nsContext);
        } finally {
            m.setProperty(Marshaller.JAXB_FRAGMENT,false);
        }
    }

    public void marshal(Marshaller m, Object object, Node output) throws JAXBException {
        m.setProperty(Marshaller.JAXB_FRAGMENT,true);
        try {
            m.marshal(object,output);
        } finally {
            m.setProperty(Marshaller.JAXB_FRAGMENT,false);
        }
    }

    public void marshal(Marshaller m, Object object, ContentHandler contentHandler) throws JAXBException {
        m.setProperty(Marshaller.JAXB_FRAGMENT,true);
        try {
            m.marshal(object,contentHandler);
        } finally {
            m.setProperty(Marshaller.JAXB_FRAGMENT,false);
        }
    }

    public void marshal(Marshaller m, Object object, Result result) throws JAXBException {
        m.setProperty(Marshaller.JAXB_FRAGMENT,true);
        try {
            m.marshal(object,result);
        } finally {
            m.setProperty(Marshaller.JAXB_FRAGMENT,false);
        }
    }

    public Object unmarshal(Unmarshaller u, XMLStreamReader in) {
        throw new UnsupportedOperationException();
    }

    public Object unmarshal(Unmarshaller u, Source in) {
        throw new UnsupportedOperationException();
    }

    public Object unmarshal(Unmarshaller u, InputStream in) {
        throw new UnsupportedOperationException();
    }

    public Object unmarshal(Unmarshaller u, Node n) {
        throw new UnsupportedOperationException();
    }

    public TypeReference getTypeReference() {
        throw new UnsupportedOperationException();
    }
}
