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

import com.sun.xml.ws.spi.runtime.Invoker;
import com.sun.xml.ws.spi.runtime.MessageContext;
import java.lang.reflect.Method;


/**
 * Utility to manipulate MessageContext properties
 *
 * @author WS Development Team
 */
public class MessageContextUtil {
    
    public static Integer getHttpStatusCode(javax.xml.ws.handler.MessageContext ctxt) {
        return (Integer)ctxt.get(MessageContext.HTTP_RESPONSE_CODE);
    }
    
    public static void setHttpStatusCode(javax.xml.ws.handler.MessageContext ctxt, Integer code) {
        ctxt.put(MessageContext.HTTP_RESPONSE_CODE, code);
    }
}
