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
public abstract class AbstractImpl {

    private static final JAXBContext jaxbContext = createJAXBContext();
    protected int combo;
    private int i;

    private javax.xml.bind.JAXBContext getJAXBContext(){
        return jaxbContext;
    }
    
    private static javax.xml.bind.JAXBContext createJAXBContext(){
        try{
            return javax.xml.bind.JAXBContext.newInstance(ObjectFactory.class);
        }catch(javax.xml.bind.JAXBException e){
            throw new WebServiceException(e.getMessage(), e);
        }
    }

    protected Source sendSource(Hello_Type hello) {
        String arg = hello.getArgument();
        String extra = hello.getExtra();
        String body = ((++i%2) == 0)
            ? "<HelloResponse xmlns='urn:test:types'><argument xmlns=''>"+arg+"</argument><extra xmlns=''>"+extra+"</extra></HelloResponse>"
            : "<ans1:HelloResponse xmlns:ans1='urn:test:types'><argument>"+arg+"</argument><extra>"+extra+"</extra></ans1:HelloResponse>";
        Source source = new StreamSource(new ByteArrayInputStream(body.getBytes()));
        return source;
    }

    protected Source sendFaultSource() {
        String body  = 
            "<soap:Fault xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\"><faultcode>soap:Server</faultcode><faultstring>Server was unable to process request. ---> Not a valid accountnumber.</faultstring><detail /></soap:Fault>";

        Source source = new StreamSource(
            new ByteArrayInputStream(body.getBytes()));
        return source;
    }

    protected Hello_Type recvBean(Source source) throws Exception {
        Hello_Type hello = (Hello_Type)jaxbContext.createUnmarshaller().unmarshal(source);
        return hello;
    }

    protected Source sendBean(Hello_Type req) throws Exception {
        HelloResponse resp = new HelloResponse();
        resp.setArgument(req.getArgument());
        resp.setExtra(req.getExtra());
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        jaxbContext.createMarshaller().marshal(resp, bout);
        return new StreamSource(new ByteArrayInputStream(bout.toByteArray()));
    }

}
