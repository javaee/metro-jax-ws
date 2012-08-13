/*
 * Copyright 2004 Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * Hello_Impl.java
 *
 * Created on July 25, 2003, 10:37 AM
 */

package provider.wsdl_hello_lit_context.server;

import java.util.Iterator;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.ws.Service.Mode;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.namespace.QName;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import javax.annotation.Resource;

/**
 * @author Jitendra Kotamraju
 */
@ServiceMode(value=Service.Mode.MESSAGE)
public abstract class ProviderImpl implements Provider<Source> {

    private int combo;
    @Resource
    WebServiceContext wsContextViaBaseField;

    WebServiceContext wsContextViaBaseMethod;
  
    public abstract boolean isInjectionDone();

    @Resource
    public void setBaseContext(WebServiceContext ctxt) {
        this.wsContextViaBaseMethod = ctxt;
    }

    private void printContext() {
        MessageContext ctxt = wsContextViaBaseField.getMessageContext();
/*
        TODO: WSDL_DESCRIPTION's value type is not specified correctly in spec
        InputSource source = (InputSource)ctxt.getProperty(JAXRPCContext.WSDL_DESCRIPTION);
        verifySource(source);
*/
        QName expService = new QName("urn:test", "Hello");
        QName expPort = new QName("urn:test", "HelloPort");
        QName gotService = (QName)ctxt.get(MessageContext.WSDL_SERVICE);
        QName gotPort = (QName)ctxt.get(MessageContext.WSDL_PORT);
        if (!expService.equals(gotService)) {
            System.out.println("WSDL_SERVICE expected="+expService+" Got="+gotService);
            throw new WebServiceException(
                "WSDL_SERVICE expected="+expService+" Got="+gotService);
        }
        if (!expPort.equals(gotPort)) {
            System.out.println("WSDL_PORT expected="+expPort+" Got="+gotPort);
            throw new WebServiceException(
                "WSDL_PORT expected="+expPort+" Got="+gotPort);
        }
        // Get a property from context
        String gotProp = (String)ctxt.get("foo");
        if (!gotProp.equals("bar")) {
            System.out.println("foo property: expected=bar Got="+gotProp);
            throw new WebServiceException(
                "foo property: expected=bar Got="+gotProp);
        }

        // Modify the same property in the context
        ctxt.put("foo", "return-bar");

        // Set a property in the context
        ctxt.put("return-foo", "return-bar");
    }

    public SOAPMessage invoke(SOAPMessage msg) {
        throw new WebServiceException("Wrong method is invoked");
    }

    /* 
     */
    public Source invoke(Source msg) {
    if (!isInjectionDone()) {
        throw new WebServiceException("Injection is not done");
        }
        // Test if we got the correct SOAPMessage that has handler changes
        try {
            MessageFactory fact = MessageFactory.newInstance();
            SOAPMessage soap = fact.createMessage();
            soap.getSOAPPart().setContent(msg);
            SOAPBody body = soap.getSOAPBody();
            Iterator i = body.getChildElements();
            SOAPElement elem = (SOAPElement)i.next();
            QName name = elem.getElementQName();
            QName exp = new QName("urn:test:types", "MyVoidTest");
            if (!exp.equals(name)) {
                throw new WebServiceException("Handler changes aren't reflected");
            }
        } catch(SOAPException e) {
            throw new WebServiceException("Got Incorrect Source");
        }
        
        printContext();
        String content = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Body>  <VoidTestResponse xmlns=\"urn:test:types\"></VoidTestResponse></soapenv:Body></soapenv:Envelope>";
        Source source = new StreamSource(
            new ByteArrayInputStream(content.getBytes()));
        return source;
    }

}
