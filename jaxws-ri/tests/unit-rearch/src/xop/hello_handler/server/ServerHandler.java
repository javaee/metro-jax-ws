/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004-2017 Oracle and/or its affiliates. All rights reserved.
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

package xop.hello_handler.server;

import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import javax.xml.ws.handler.MessageContext;
import javax.xml.soap.SOAPMessage;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * @author Vivek Pandey
 */
public class ServerHandler implements SOAPHandler<SOAPMessageContext> {
    public Set getHeaders() {
        return null;
    }

    public boolean handleMessage(SOAPMessageContext context) {
        SOAPMessageContext smc = context;
        SOAPMessage message = smc.getMessage();
        //Set a property and verify in ApplicationContext to verify if handler has executed.
        context.put("MyHandler_Property","foo");
        context.setScope("MyHandler_Property",MessageContext.Scope.APPLICATION);
        
        Map<String, List<String>> map = null;
        if(!(Boolean)context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)){
            map = (Map<String,List<String>>)context.get(MessageContext.HTTP_REQUEST_HEADERS);
            if (!verifyContentTypeHttpHeader(map)){
                  throw new RuntimeException("In ServerSOAPHandler:processInboundMessage: FAILED");
            }            
        }

        return true;
    }

    public boolean handleFault(SOAPMessageContext context) {
        return false;
    }

    public void close(MessageContext context) {

    }

    private boolean verifyContentTypeHttpHeader(Map<String,List<String>> m) {
        Map<String,List<String>> map = convertKeysToLowerCase(m);
        List<String> values=map.get("content-type");
	      String sValues = values.toString().toLowerCase();
        if (sValues != null) {
           if ((sValues.indexOf("multipart/related") >= 0) &&
               (sValues.indexOf("text/xml") >= 0) &&
               (sValues.indexOf("application/xop+xml") >= 0) &&
                   (sValues.indexOf("start=") >= 0)) {
                return true;
           } else {
               System.out.println("FAILED: : INVALID HTTP Content-type [\"+sValues+\"]\"");
               return false;
           }
        } else {
            System.out.println("FAILED: the HTTP header Content-Type was not found");
            return false;
        }
    }

    private static Map<String, List<String>> convertKeysToLowerCase(
        Map<String, List<String>> in) {

        Map<String, List<String>> out = new HashMap<String, List<String>>();
        if (in != null) {
            for(Map.Entry<String, List<String>> e : in.entrySet()) {
                if (e.getKey() != null)
                    out.put(e.getKey().toLowerCase(), e.getValue());
                else
                    out.put(e.getKey(), e.getValue());
            }
        }
        return out;
    }


}
