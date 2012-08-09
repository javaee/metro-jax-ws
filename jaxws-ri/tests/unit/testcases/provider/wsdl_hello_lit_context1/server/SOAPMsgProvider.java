/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */


package provider.wsdl_hello_lit_context1.server;

import javax.annotation.Resource;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.*;
import javax.xml.ws.handler.MessageContext;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author Jitendra Kotamraju 
 */
@WebServiceProvider(targetNamespace="urn:test", portName="HelloMsgPort", serviceName="HelloMsg")
@ServiceMode(value=Service.Mode.MESSAGE)
public class SOAPMsgProvider implements Provider<SOAPMessage> {

    @Resource
    WebServiceContext ctxt;

    public SOAPMessage invoke(SOAPMessage msg) {
        try {
            testMsgContext();

            // keeping white space in the string is intentional
            String content = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Body>  <VoidTestResponse xmlns=\"urn:test:types\"></VoidTestResponse></soapenv:Body></soapenv:Envelope>";
            Source source = new StreamSource(new ByteArrayInputStream(content.getBytes()));
            MessageFactory fact = MessageFactory.newInstance();
            SOAPMessage soap = fact.createMessage();
            soap.getSOAPPart().setContent(source);
            soap.getMimeHeaders().addHeader("foo", "bar");
            return soap;
        } catch(Exception e) {
            throw new WebServiceException(e);
        }
    }

       public void testMsgContext() {
            MessageContext msgCtxt = ctxt.getMessageContext();

            // toString()
            System.out.println("MessageContext="+msgCtxt);

            // Test Map.get()
            String method = (String)msgCtxt.get(MessageContext.HTTP_REQUEST_METHOD);
            if (method == null || !method.equals("POST")) {
                throw new WebServiceException("Expected method=POST but got="+method);
            }
           
            Map<String, List<String>> hdrs = (Map<String, List<String>>)msgCtxt.get(MessageContext.HTTP_RESPONSE_HEADERS);
            msgCtxt.put(MessageContext.HTTP_RESPONSE_HEADERS, hdrs);

            // Test iterator
            Iterator<Map.Entry<String, Object>> i = msgCtxt.entrySet().iterator();
            while(i.hasNext()) {
                i.next();
            }

            // Test keySet() iterator
           for (String s : msgCtxt.keySet()) {
           }

            // Test Map.size()
            int no = msgCtxt.size();

            // Test Map.put()
            msgCtxt.put("key", "value");

            // Test Map.get() for the new addition
            String value = (String)msgCtxt.get("key");
            if (value == null || !value.equals("value")) {
                throw new WebServiceException("Expected=value but got="+value);
            }

            // Test Map.size() for the new addition
            int num = msgCtxt.size();
            if (num != no+1) {
                throw new WebServiceException("Expected no="+(no+1)+" but got="+num);
            }

            // Test Map.remove() for the new addition
            value = (String)msgCtxt.remove("key");
            if (value == null || !value.equals("value")) {
                throw new WebServiceException("Expected=value but got="+value);
            }

            // Test Map.size() for the removal
            num = msgCtxt.size();
            if (num != no) {
                throw new WebServiceException("Expected="+no+" but got="+num);
            }

            // Test iterator.remove()
            msgCtxt.put("key", "value");
            i = msgCtxt.entrySet().iterator();
            while(i.hasNext()) {
                Map.Entry<String, Object> e = i.next();
                if (e.getKey().equals("key")) {
                    i.remove();
                }
            }
            num = msgCtxt.size();
            if (num != no) {
                throw new WebServiceException("Expected="+no+" but got="+num);
            }
            value = (String)msgCtxt.get("key");
            if (value != null) {
                throw new WebServiceException("Expected=null"+" but got="+value);
            }

            // Test Map.putAll()
            Map<String, Object> two = new HashMap<String, Object>();
            two.put("key1", "value1");
            two.put("key2", "value2");
            msgCtxt.putAll(two);
            num = msgCtxt.size();
            if (num != no+2) {
                throw new WebServiceException("Expected="+(no+2)+" but got="+num);
            }
            value = (String)msgCtxt.get("key1");
            if (value == null || !value.equals("value1")) {
                throw new WebServiceException("Expected=value1"+" but got="+value);
            }
            value = (String)msgCtxt.get("key2");
            if (value == null || !value.equals("value2")) {
                throw new WebServiceException("Expected=value2"+" but got="+value);
            }
        }

}
