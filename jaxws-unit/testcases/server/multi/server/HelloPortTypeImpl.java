/*
 * Copyright 2004 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package server.multi.server;

import javax.xml.ws.Holder;
import javax.jws.WebService;

/**
 * @author Jitendra Kotamraju
 */
@WebService(endpointInterface="server.multi.server.HelloPortType")
public class HelloPortTypeImpl implements HelloPortType {

    public EchoResponseType echo(EchoType reqBody, EchoType reqHeader,
                                 Echo2Type req2Header) {
        EchoResponseType response = new EchoResponseType();
        response.setRespInfo(reqBody.getReqInfo()
                + reqHeader.getReqInfo() + req2Header.getReqInfo());
        return response;
    }

    public String echo2(String info) {
        return info;
    }

    public void echo3(Holder<java.lang.String> reqInfo) {
    }

    public void echo4(Echo4Type reqBody, Echo4Type reqHeader,
                      String req2Header, Holder<String> respBody,
                      Holder<String> respHeader) {
    }

}
