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

/**
 * $Id: MyHandler.java,v 1.1 2007-08-11 01:01:58 jitu Exp $
 */
package provider.wsdl_hello_lit_context.server;

import java.io.ByteArrayInputStream;
import java.util.Map;

import javax.xml.ws.LogicalMessage;
import javax.xml.ws.handler.*;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

/**
 * @author Jitendra Kotamraju
 */
public class MyHandler
    implements LogicalHandler<LogicalMessageContext> {
    
    public boolean handleMessage(LogicalMessageContext messageContext) {
    	Boolean bool = (Boolean)messageContext.get(
    			MessageContext.MESSAGE_OUTBOUND_PROPERTY);
    	if (!bool.booleanValue()) {
    		// Do this only for Request
	    	LogicalMessage msg = messageContext.getMessage();
	        String content = "<MyVoidTest xmlns=\"urn:test:types\"></MyVoidTest>";
	        Source source = new StreamSource(
	            new ByteArrayInputStream(content.getBytes()));
	    	msg.setPayload(source);
            messageContext.put("foo", "bar");
            messageContext.setScope("foo", MessageContext.Scope.APPLICATION);
    	} else {
	    	LogicalMessage msg = messageContext.getMessage();
	    	msg.getPayload();
			String value = (String)messageContext.get("foo");
			if (value == null || !value.equals("return-bar")) {
            	throw new IllegalArgumentException(
                	"Got foo property: expected=return-bar Got="+value);
			}
			value = (String)messageContext.get("return-foo");
			if (value == null || !value.equals("return-bar")) {
            	throw new IllegalArgumentException(
                	"Got return-foo property: expected=return-bar Got="+value);
			}
		}
		return true;
    }
    
    public void close(MessageContext messageContext) {
    }    
    
    public boolean handleFault(LogicalMessageContext messageContext) {
		return true;
    }
    
}
