/*
 * Copyright 2004 Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package provider.wsdl_hello_lit_soap12.server;

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
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.BindingType;

/**
 * @author Jitendra Kotamraju
 */

@WebServiceProvider(
    wsdlLocation="WEB-INF/wsdl/hello_literal.wsdl",
    targetNamespace="urn:test",
    serviceName="Hello")
@BindingType(SOAPBinding.SOAP12HTTP_BINDING)
public class HelloImpl implements Provider<Source> {

    private static final JAXBContext jaxbContext = createJAXBContext();
    private int combo;
    private int bodyIndex;

    public javax.xml.bind.JAXBContext getJAXBContext(){
        return jaxbContext;
    }
    
    private static javax.xml.bind.JAXBContext createJAXBContext(){
        try{
            return javax.xml.bind.JAXBContext.newInstance(ObjectFactory.class);
        }catch(javax.xml.bind.JAXBException e){
            throw new WebServiceException(e.getMessage(), e);
        }
    }

    private Source sendSource() {
        System.out.println("**** sendSource ******");

        String[] body  = {
            "<HelloResponse xmlns=\"urn:test:types\"><argument xmlns=\"\">foo</argument><extra xmlns=\"\">bar</extra></HelloResponse>",
            "<ans1:HelloResponse xmlns:ans1=\"urn:test:types\"><argument>foo</argument><extra>bar</extra></ans1:HelloResponse>"
        };
        int i = (++bodyIndex)%body.length;
        Source source = new StreamSource(
            new ByteArrayInputStream(body[i].getBytes()));
        return source;
    }

    private Hello_Type recvBean(Source source) throws Exception {
        System.out.println("**** recvBean ******");
        return (Hello_Type)jaxbContext.createUnmarshaller().unmarshal(source);
    }

    private Source sendBean() throws Exception {
        System.out.println("**** sendBean ******");
        HelloResponse resp = new HelloResponse();
        resp.setArgument("foo");
        resp.setExtra("bar");
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        jaxbContext.createMarshaller().marshal(resp, bout);
        return new StreamSource(new ByteArrayInputStream(bout.toByteArray()));
    }

    public Source invoke(Source source) {
        System.out.println("**** Received in Provider Impl ******");
        try {
            Hello_Type hello = recvBean(source);
            String arg = hello.getArgument();
            if (arg.equals("sendBean")) {
                return sendBean();
            } else if (arg.equals("sendSource")) {
                return sendSource();
            } else if (arg.equals("exp")) {
                throw new WebServiceException("Expected exception");
            } else {
                throw new WebServiceException("Unexpected Argument="+arg);
            }
        } catch(Exception e) {
            e.printStackTrace();
            throw new WebServiceException("Provider endpoint failed", e);
        }
    }
}
