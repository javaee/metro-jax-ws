/*
 * Copyright 2004 Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
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
