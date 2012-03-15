/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2011 Oracle and/or its affiliates. All rights reserved.
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
package bugs.jaxws1049.server;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * TODO: Write some description here ...
 *
 * @author Miroslav Kos (miroslav.kos at oracle.com)
 */
public class CheckSOAPActionHandler implements SOAPHandler<SOAPMessageContext> {

    public static String[] expectedSOAPActions = {
            "\"http://server.jaxws1049.bugs/EchoImpl/doSomethingRequest\"",
            "\"customSOAPAction\"",
            "\"http://server.jaxws1049.bugs/EchoImpl/doSomethingRequest\"",
            "\"http://server.jaxws1049.bugs/EchoImpl/doSomethingRequest\"",
    };
    int i = 0;

    public boolean handleMessage(SOAPMessageContext context) {

        if (!(Boolean) context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)) {

            Map<String, List<String>> map = (Map<String, List<String>>) context.get(MessageContext.HTTP_REQUEST_HEADERS);
            List<String> soapAction = getHTTPHeader(map, "SOAPAction");
            String soapAction0 = soapAction != null && soapAction.size() > 0 ? soapAction.get(0) : null;
            System.out.println("=======================================================================================");
            System.out.println("soapAction = " + soapAction);
            System.out.println("=======================================================================================");


            if (i < expectedSOAPActions.length) {
                // soapAction tests

                if (!expectedSOAPActions[i].equals(soapAction0)) {
                    throw new IllegalStateException("Received unexpected SOAPAction - received: [" + soapAction0 +
                            "], expectedSOAPActions: [" + expectedSOAPActions[i] + "]");
                }
                i++;

            } else {

                // dispatch test
                List<String> headers;
                headers = getHTTPHeader(map, "X-ExampleHeader2");
                if (headers == null) {
                    throw new IllegalStateException("Missing http header X-ExampleHeader2");
                }
                headers = getHTTPHeader(map, "My-Content-Type");
                if (headers == null) {
                    throw new IllegalStateException("Missing http header My-Content-Type");
                }
            }


        }

        return true;
    }

    private List<String> getHTTPHeader(Map<String, List<String>> headers, String header) {
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            String name = entry.getKey();
            if (name.equalsIgnoreCase(header))
                return entry.getValue();
        }
        return null;
    }

    @Override
    public boolean handleFault(SOAPMessageContext context) {
        return false;
    }

    @Override
    public void close(MessageContext context) {
    }

    @Override
    public Set<QName> getHeaders() {
        return null;
    }
}