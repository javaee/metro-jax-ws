/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2005-2017 Oracle and/or its affiliates. All rights reserved.
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

package fromwsdl.xmlbind_handler.common;

import java.io.ByteArrayOutputStream;

import javax.xml.namespace.QName;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.http.HTTPException;
import javax.xml.ws.handler.LogicalHandler;
import javax.xml.ws.handler.LogicalMessageContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.LogicalMessage;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Node;

public class TestHandler implements LogicalHandler<LogicalMessageContext> {
    
    public static final int THROW_HTTP_EXCEPTION = -100;
    public static final int THROW_RUNTIME_EXCEPTION = -101;
    public static final int THROW_PROTOCOL_EXCEPTION = -102;
    
    public boolean handleMessage(LogicalMessageContext context) {
        try {
            LogicalMessage message = context.getMessage();
            Source msgSource = message.getPayload();
            Source newMessage = incrementArgument(msgSource);
            message.setPayload(newMessage);
            //verifySource(msgSource); // source already read
            return true;
        } catch (TransformerException e) {
            System.err.println("handler received: " + e);
            throw new HTTPException(502);
        }
    }

    private void verifySource(Source source) throws Exception {
        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer trans = factory.newTransformer();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        trans.transform(source, new StreamResult(baos));
        baos.close();
    }
    
    private Source incrementArgument(Source source)
        throws TransformerException {
        Transformer xFormer =
            TransformerFactory.newInstance().newTransformer();
        xFormer.setOutputProperty("omit-xml-declaration", "yes");
        DOMResult dResult = new DOMResult();
        xFormer.transform(source, dResult);
        Node documentNode = dResult.getNode();
        Node envelopeNode = documentNode.getFirstChild();
        Node requestResponseNode = envelopeNode.getLastChild().getFirstChild();
        Node textNode = requestResponseNode.getFirstChild().getFirstChild();
        int orig = Integer.parseInt(textNode.getNodeValue());
        
        // check for error tests
        if (orig == THROW_HTTP_EXCEPTION) {
            throw new HTTPException(500);
        } else if (orig == THROW_RUNTIME_EXCEPTION) {
            throw new RuntimeException("EXPECTED EXCEPTION");
        } else if (orig == THROW_PROTOCOL_EXCEPTION) {
            throw new ProtocolException("TEST EXCEPTION FROM HANDLER");
        }
        
        textNode.setNodeValue(String.valueOf(++orig));
        return new DOMSource(documentNode);
    }
    
    public boolean handleFault(LogicalMessageContext context) {
        return true;
    }
    
    public void close(MessageContext context) {}
    
}
