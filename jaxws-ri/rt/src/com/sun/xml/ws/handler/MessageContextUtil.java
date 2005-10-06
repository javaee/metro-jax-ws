/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */
package com.sun.xml.ws.handler;

import java.util.List;
import java.util.Map;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.MessageContext.Scope;
import javax.xml.namespace.QName;


/**
 * Utility to manipulate MessageContext properties
 *
 * @author WS Development Team
 */
public class MessageContextUtil {
    
    public static Integer getHttpStatusCode(MessageContext ctxt) {
        return (Integer)ctxt.get(MessageContext.HTTP_RESPONSE_CODE);
    }
    
    public static void setHttpStatusCode(MessageContext ctxt, Integer code) {
        ctxt.put(MessageContext.HTTP_RESPONSE_CODE, code);
        ctxt.setScope(MessageContext.WSDL_OPERATION, Scope.APPLICATION);
    }
    
    public static void setHttpRequestMethod(MessageContext ctxt, String method) {
        ctxt.put(MessageContext.HTTP_REQUEST_METHOD, method);
        ctxt.setScope(MessageContext.HTTP_REQUEST_METHOD, Scope.APPLICATION);
    }
    
    public static void setHttpRequestHeaders(MessageContext ctxt,
            Map<String, List<String>> headers) {
        ctxt.put(MessageContext.HTTP_REQUEST_HEADERS, headers);
        ctxt.setScope(MessageContext.HTTP_REQUEST_HEADERS, Scope.APPLICATION);
    }
    
    public static void setWsdlOperation(MessageContext ctxt, QName name) {
        ctxt.put(MessageContext.WSDL_OPERATION, name);
        ctxt.setScope(MessageContext.WSDL_OPERATION, Scope.APPLICATION);
    }
}
