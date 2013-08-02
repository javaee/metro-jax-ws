/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package provider.wsdl_hello_lit.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.WebServiceProvider;

/**
 * @author Jitendra Kotamraju
 */
@WebServiceProvider(
    wsdlLocation="WEB-INF/wsdl/hello_literal.wsdl",
    targetNamespace="urn:test",
    serviceName="Hello",
    portName="HelloPort")

public class HelloImpl extends AbstractImpl implements Provider<Source> {

    public Source invoke(Source source) {
        try {
            Hello_Type hello = recvBean(source);
            if (!hello.getExtra().startsWith("extra")) {
                throw new WebServiceException("Expected=extra*,got="+hello.getExtra());
            }
            if (hello.getArgument().equals("fault")) {
                return sendFaultSource();
            }
            return (++combo%2 == 0) ? sendSource(hello) : sendBean(hello);
        } catch(Exception e) {
            throw new WebServiceException("Endpoint failed", e);
        }
    }

}
