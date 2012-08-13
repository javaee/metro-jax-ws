/*
 * Copyright 2004 Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package server.misc.server;

import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;

@javax.jws.WebService(endpointInterface="server.misc.server.HelloPortType")
public class HelloPortTypeImpl implements HelloPortType {
	private WebServiceContext wsContext;

    public EchoResponseType echo(EchoType reqBody,
                                              EchoType reqHeader,
                                              Echo2Type req2Header) {
        EchoResponseType response = null;
        try {
            ObjectFactory of = new ObjectFactory();
            response = of.createEchoResponseType();
            response.setRespInfo(reqBody.getReqInfo() + reqHeader.getReqInfo() +
                                                      req2Header.getReqInfo());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return response;
    }

    public String echo2(String info) {
		MessageContext msgCtxt = wsContext.getMessageContext();
/*
		if (msgCtxt.get("javax.xml.ws.servlet.context") == null
        	|| msgCtxt.get("javax.xml.ws.servlet.session") == null
        	|| msgCtxt.get("javax.xml.ws.servlet.request") == null
        	|| msgCtxt.get("javax.xml.ws.servlet.response") == null) {
			throw new WebServiceException("MessageContext is not populated.");
		}
*/
        return info;
    }

    public void echo3(Holder<java.lang.String> reqInfo) {
    }

    public void echo4(Echo4Type reqBody,
                      Echo4Type reqHeader,
                      String req2Header,
                      Holder<String> respBody,
                      Holder<String> respHeader) {
    }

    @Resource
    private void setContext(WebServiceContext wsContext) {
		System.out.println("Setting WebServiceContext");
		this.wsContext = wsContext;
    }

    @PostConstruct
    private void onPostConstruct() {
		System.out.println("Called onPostConstruct");
    }

    @PreDestroy
    private void onPreDestroy() {
		System.out.println("Called onPreDestroy");
    }

}
