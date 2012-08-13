/*
 * Copyright 2004 Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/*
 * Hello_Impl.java
 *
 * Created on July 25, 2003, 10:37 AM
 */

package provider.http_status_code_oneway_607.server;

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
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import javax.annotation.Resource;
import java.io.StringReader;

import org.w3c.dom.Node;

/**
 * Service returns null to indicate oneway, also sets a http status
 * code. Client should make sure it doesn't receive 202
 *
 * @author Jitendra Kotamraju
 */
@WebServiceProvider
@ServiceMode(value=Service.Mode.MESSAGE)
public class HelloImpl implements Provider<SOAPMessage> {

    @Resource
    WebServiceContext wsCtxt;

    public SOAPMessage invoke(SOAPMessage msg) {
        MessageContext msgCtxt = wsCtxt.getMessageContext();        
        msgCtxt.put(MessageContext.HTTP_RESPONSE_CODE, 502);
        return null;
    }
}
