/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package fromwsdl.header.rpclit.server;

import javax.xml.ws.Holder;
import javax.jws.WebMethod;
import javax.jws.WebResult;
import javax.jws.WebParam;

@javax.jws.WebService(endpointInterface="fromwsdl.header.rpclit.server.HelloPortType")
public class HelloPortTypeImpl
        implements HelloPortType {
    public void hello(HelloType param1, String param2,
                      Holder<String> param3,
                      Holder<String> param4,
                      String param5) {
        return;
    }

    public EchoResponseType echo(EchoType reqBody,
                                 EchoType reqHeader) {
        EchoResponseType response = null;
        try {
            //test rpclit parameter nullability
            if(reqBody.getReqInfo().equals("sendNull"))
                return null;
            ObjectFactory of = new ObjectFactory();
            response = of.createEchoResponseType();
            response.setRespInfo(reqBody.getReqInfo() +
                                 reqHeader.getReqInfo());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return response;
    }
    
    public void echo1(Holder<String> echo1Header) {
        echo1Header.value += " World!";
    }

    public Echo2ResponseType echo2(EchoType reqBody,
                                   EchoType req1Header,
                                   Echo2Type req2Header) {
        Echo2ResponseType response = null;
        try {
            ObjectFactory of = new ObjectFactory();
            response = of.createEcho2ResponseType();
            response.setRespInfo(reqBody.getReqInfo() +
                                 req1Header.getReqInfo() +
                                 req2Header.getReqInfo());
            System.out.println("Set the response object");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return response;
    }

    public int echo3(String echo3Req) {
        return Integer.valueOf(echo3Req);
    }

}
