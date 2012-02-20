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

package fromjava.encoding.serversf.server;

import com.sun.xml.ws.developer.Serialization;

import javax.annotation.Resource;
import javax.jws.*;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.handler.MessageContext;

import java.util.List;
import java.util.Map;

/**
 * @author Jitendra Kotamraju
 */
@WebService(name = "Echo", serviceName = "echoService", targetNamespace = "http://echo.org/")
@Serialization(encoding="UTF-16")
public class EchoImpl {
    @Resource
    WebServiceContext wsc;

    public String echoString(String str) throws Exception1, Fault1, WSDLBarException {
        MessageContext ctxt = wsc.getMessageContext();
        Map<String, List<String>> hdrs = (Map<String, List<String>>)ctxt.get(MessageContext.HTTP_REQUEST_HEADERS);
        
        if (str == null) {
            return null;
        } else if (str.equals("Exception1")) {
            throw new Exception1("my exception1");
        } else if (str.equals("Fault1")) {
            FooException fooException = new FooException();
            fooException.setVarString("foo");
            fooException.setVarInt(33);
            fooException.setVarFloat(44F);
            throw new Fault1("fault1", fooException);
        } else if (str.equals("WSDLBarException")) {
            throw new WSDLBarException("my barException", new Bar(33));
        } else {
            List<String> ctList = hdrs.get("Content-Type");
            if (ctList == null || ctList.size() != 1) {
                throw new WebServiceException("Invalid Content-Type header="+ctList);
            }
            String ct = ctList.get(0).toLowerCase();
            if (!(ct.contains(str.toLowerCase()))) {
                throw new WebServiceException("Invalid Encoding for request. Expected="+str+" Got Content-Type header="+ctList);
            }
        }
        return str;
    }
}
