/*
 * Copyright 2004 Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package provider.xmlbinding.server;

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
import javax.xml.soap.*;
import org.w3c.dom.Node;
import javax.xml.transform.dom.DOMSource;
import javax.xml.ws.http.HTTPException;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.BindingType;

/**
 * @author Jitendra Kotamraju
 */
@WebServiceProvider
@BindingType(value="http://www.w3.org/2004/08/wsdl/http")

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
        String begin = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Body>";
        String end = "</soapenv:Body></soapenv:Envelope>";

        String[] body  = {
            "<HelloResponse xmlns=\"urn:test:types\"><argument xmlns=\"\">foo</argument><extra xmlns=\"\">bar</extra></HelloResponse>",
            "<ans1:HelloResponse xmlns:ans1=\"urn:test:types\"><argument>foo</argument><extra>bar</extra></ans1:HelloResponse>"
        };
        int i = (++bodyIndex)%body.length;
        String content = begin+body[i]+end;
System.out.println("SEnding content**** ="+content);
        Source source = new StreamSource(
            new ByteArrayInputStream(content.getBytes()));
        return source;
    }

    private void recvBean(Source source) throws Exception {
        System.out.println("**** recvBean ******");
        Hello_Type hello = (Hello_Type)jaxbContext.createUnmarshaller().unmarshal(source);
        if (!hello.getArgument().equals("foo")) {
            throw new HTTPException(501);
        }
        if (!hello.getExtra().equals("bar")) {
            throw new HTTPException(502);
        }
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
        try {
            System.out.println("**** Received in Provider Impl ******");
            SOAPMessage msg = MessageFactory.newInstance().createMessage();
            msg.getSOAPPart().setContent(source);
            Node node = msg.getSOAPBody().getFirstChild();
            recvBean(new DOMSource(node));
            return sendSource();
        } catch(HTTPException e) {
            throw e;
        } catch(Exception e) {
            throw new HTTPException(503);
        }
    }
}
