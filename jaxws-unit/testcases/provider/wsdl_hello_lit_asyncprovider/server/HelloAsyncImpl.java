/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package provider.wsdl_hello_lit_asyncprovider.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.ws.WebServiceException;
import com.sun.xml.ws.api.server.AsyncProvider;
import com.sun.xml.ws.api.server.AsyncProviderCallback;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.WebServiceContext;

/**
 * @author Jitendra Kotamraju
 */

@WebServiceProvider(
    wsdlLocation="WEB-INF/wsdl/hello_literal.wsdl",
    targetNamespace="urn:test",
    serviceName="Hello",
    portName="HelloAsyncPort")

public class HelloAsyncImpl extends AbstractImpl implements AsyncProvider<Source> {

    public void invoke(Source source, AsyncProviderCallback cbak, WebServiceContext ctxt) {
        try {
            Hello_Type hello = recvBean(source);
            if (!hello.getExtra().startsWith("extra")) {
                cbak.send(new WebServiceException("Expected=extra*,got="+hello.getExtra()));
                return;
            }
            if (hello.getArgument().equals("fault")) {
                cbak.send(sendFaultSource());
                return;
            }
            cbak.send((++combo%2 == 0) ? sendSource(hello) : sendBean(hello));
        } catch(Exception e) {
            throw new WebServiceException("Endpoint failed", e);
        }
    }

}
