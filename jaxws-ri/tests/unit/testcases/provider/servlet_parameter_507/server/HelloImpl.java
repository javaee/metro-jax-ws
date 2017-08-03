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

package provider.servlet_parameter_507.server;

import javax.annotation.Resource;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.*;
import javax.xml.ws.handler.MessageContext;
import java.io.ByteArrayInputStream;
import javax.servlet.http.HttpServletRequest;

/**
 * Client sends form request in the POST request.
 *
 * @author Jitendra Kotamraju
 */
@WebServiceProvider
@BindingType(value="http://www.w3.org/2004/08/wsdl/http")
public class HelloImpl implements Provider<Source> {

    @Resource
    WebServiceContext ctxt;

    private Source sendSource() {
        String msg = "<S:Envelope xmlns:S='http://schemas.xmlsoap.org/soap/envelope/'><S:Body>"+
            "<HelloResponse xmlns='urn:test:types'><argument xmlns=''>foo</argument><extra xmlns=''>bar</extra></HelloResponse>"+
            "</S:Body></S:Envelope>";
        return new StreamSource(new ByteArrayInputStream(msg.getBytes()));
    }

    public Source invoke(Source source) {
        MessageContext msgCtxt = ctxt.getMessageContext();
        HttpServletRequest request =
          (HttpServletRequest)msgCtxt.get(MessageContext.SERVLET_REQUEST);
        String val = request.getParameter("a");
        if (val == null || !val.equals("b")) {
            throw new WebServiceException("Unexpected parameter value for a. Expected: "+"b"+" Got: "+val);
        }
        return sendSource();
    }
}
