/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * Hello_Impl.java
 *
 * Created on July 25, 2003, 10:37 AM
 */

package provider.wsdl_hello_lit_oneway.server;

import java.rmi.Remote;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.util.Iterator;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.Service.Mode;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.MimeHeader;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.WebServiceProvider;

import org.w3c.dom.Node;

/**
 * @author Jitendra Kotamraju
 */
@WebServiceProvider
@ServiceMode(value=Service.Mode.MESSAGE)
public class HelloImpl implements Provider<SOAPMessage> {

    public SOAPMessage invoke(SOAPMessage msg) {
        SOAPBody body;
        try {
            body = msg.getSOAPBody();
        } catch(SOAPException e) {
            throw new WebServiceException(e);
        }
        Node node = body.getFirstChild();
        if (!node.getLocalName().equals("Hello")) {
            throw new WebServiceException("Expecting localName=Hello but got="+node.getLocalName());
        }
        if (!node.getNamespaceURI().equals("urn:test:types")) {
            throw new WebServiceException("Expecting NS=urn:test:types but got="+node.getNamespaceURI());
        }
        MimeHeaders headers = msg.getMimeHeaders();
/*
        Iterator i = headers.getAllHeaders();
        while(i.hasNext()) {
            MimeHeader header = (MimeHeader)i.next();
            System.out.println("name="+header.getName()+" value="+header.getValue());
        }
*/
        String[] action = headers.getHeader("SOAPAction");
        if (action == null || action.length > 1 || !action[0].equals("\"urn:test:hello\"")) {
            throw new WebServiceException("SOAPMessage doesn't contain transport header: SOAPAction");
        }
        String[] ct = headers.getHeader("Content-Type");
        if (ct == null || ct.length > 1 || !ct[0].startsWith("text/xml")) {
            throw new WebServiceException("SOAPMessage doesn't contain transport header: Content-Type");
        }
        return null;
    }
}
