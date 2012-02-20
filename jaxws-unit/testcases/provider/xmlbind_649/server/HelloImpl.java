/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package provider.xmlbind_649.server;

import javax.annotation.Resource;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import javax.xml.ws.*;
import javax.xml.ws.handler.MessageContext;
import java.util.List;
import java.util.Map;

/**
 * Client sends a specific Content-Type application/atom+xml
 *
 * @author Jitendra Kotamraju
 */
@WebServiceProvider(targetNamespace="urn:test", portName="HelloPort", serviceName="Hello")
@BindingType(value="http://www.w3.org/2004/08/wsdl/http")
public class HelloImpl implements Provider<Source> {
    @Resource
    WebServiceContext wsc;

    public Source invoke(Source source) {
        MessageContext ctxt = wsc.getMessageContext();
        String method = (String)ctxt.get(MessageContext.HTTP_REQUEST_METHOD);
        if (!method.equals("PUT")) {
            throw new WebServiceException("HTTP method expected=PUT got="+method);
        }
        Map<String, List<String>> hdrs = (Map<String, List<String>>)ctxt.get(MessageContext.HTTP_REQUEST_HEADERS);
        List<String> ctList = hdrs.get("Content-Type");
        if (ctList == null || ctList.size() != 1) {
            throw new WebServiceException("Invalid Content-Type header="+ctList);
        }
        String got = ctList.get(0);
        if (!got.equals("application/atom+xml")) {
            throw new WebServiceException("Expected=application/atom+xml"+" got="+got);
        }        
        return new SAXSource();
    }
    
}
