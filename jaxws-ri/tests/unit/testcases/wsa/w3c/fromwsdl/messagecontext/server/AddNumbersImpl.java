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
package wsa.w3c.fromwsdl.messagecontext.server;

import javax.jws.WebService;
import javax.annotation.Resource;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;

import com.sun.xml.ws.developer.JAXWSProperties;

/**
 * Testcase for issue 629: Access all proeprties in MessageContext and verify WSA headers.
 * @author Rama Pulavarthi
 */

@WebService(serviceName="AddNumbersService", portName="AddNumbersPort",
        endpointInterface = "wsa.w3c.fromwsdl.messagecontext.server.AddNumbersPortType", targetNamespace = "http://example.com/")
public class AddNumbersImpl implements AddNumbersPortType {
    @Resource
    WebServiceContext wsc;
    public int addNumbers( int number1, int number2) throws AddNumbersFault_Exception {
        //Print all the preropteis int he message context.
        MessageContext mc = wsc.getMessageContext();
        for (String key : mc.keySet()) {
            System.out.println("Msg key: " + key);
            System.out.println("Msg value: " + mc.get(key));
        }

        String messageId = (String)mc.get(JAXWSProperties.ADDRESSING_MESSAGEID);
        if(messageId == null) {
            throw new WebServiceException("Expected wsa:MessageID header not present int he request message");
        }
        String action = (String)mc.get(JAXWSProperties.ADDRESSING_ACTION);
        if(action == null || !action.equals("urn:com:example:action")) {
            throw new WebServiceException("Expected wsa:Action header not present in the request message");
        }
                
        return number1 + number2;
    }


}