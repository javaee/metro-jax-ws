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

package fromwsdl.handler_simple.client;

import org.w3c.dom.Element;

import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.WebServiceException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.namespace.QName;
import java.util.List;
import java.util.Set;

import com.sun.xml.ws.util.DOMUtil;
import com.sun.xml.ws.addressing.W3CAddressingConstants;

/**
 * This handler will be set on the soap 12 binding in the
 * customization file. It's used to test that bindings with multiple
 * ports actually use the correct ports. See bug 6353179 and
 * the HandlerClient tests cases.
 */
public class ReferenceParameterHandler implements SOAPHandler<SOAPMessageContext> {
    public boolean handleMessage(SOAPMessageContext context) {
        Boolean outbound = (Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);
        if(!outbound) {
            return true;
        }

        List<Element> refParams = (List<Element>) context.get(MessageContext.REFERENCE_PARAMETERS);
        System.out.println(refParams.size());
        if (refParams.size() != 2) {
            throw new WebServiceException("Did n't get expected ReferenceParameters");
        }
        try {
            for (Element e : refParams) {
            	XMLStreamWriter writer = XMLOutputFactory.newInstance().createXMLStreamWriter(System.out);
            	writer.writeStartDocument();
                DOMUtil.serializeNode(e, writer);
                if (e.getAttributeNodeNS(W3CAddressingConstants.WSA_NAMESPACE_NAME, "IsReferenceParameter") == null)
                    throw new WebServiceException("isReferenceParameter attribute not present on header");
            	writer.writeEndDocument();
            	writer.close();
            }
        } catch (XMLStreamException el) {
            throw new WebServiceException(el);
        }

        return true;
    }

    /**** empty methods below ****/
    public Set<QName> getHeaders() {
        return null;
    }

    public void close(MessageContext context) {}

    public boolean handleFault(SOAPMessageContext context) {
        return true;
    }

}
