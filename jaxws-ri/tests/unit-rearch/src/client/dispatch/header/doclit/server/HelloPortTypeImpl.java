/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004-2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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

package client.dispatch.header.doclit.server;

import javax.xml.ws.Holder;

@javax.jws.WebService(endpointInterface="client.dispatch.header.doclit.server.HelloPortType")
public class HelloPortTypeImpl
        implements HelloPortType {
    public EchoResponseType echo(EchoType reqBody,
                                              EchoType reqHeader,
                                              Echo2Type req2Header) {
        EchoResponseType response = new EchoResponseType();
        response.setRespInfo(reqBody.getReqInfo() + reqHeader.getReqInfo() + req2Header.getReqInfo());
        return response;
    }

    public String echo2(String info) {
        return info+"bar";
    }

    public void echo3(javax.xml.ws.Holder<java.lang.String> reqInfo) {
        reqInfo.value += "bar";
    }

    public void echo4(Echo4Type reqBody,
                                     Echo4Type reqHeader,
                                     String req2Header,
                                     Holder<String> respBody,
                                     Holder<String> respHeader) {
                 try {
            String response = reqBody.getExtra() +
                              reqBody.getArgument() +
                              reqHeader.getExtra() +
                              reqHeader.getArgument() +
                              req2Header;

            respBody.value = response;
            respHeader.value = req2Header;
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
    
    public String echo5(EchoType body) {
        return body.getReqInfo();
    }
    
    public String echo6(Holder<String> name, EchoType header, Holder<EchoType> body) {
        System.out.println("Name: "+ name.value);
        System.out.println("Body: "+ body.value.getReqInfo());
        name.value = body.value.getReqInfo();
        body.value.setReqInfo(name.value +"'s Response"); ;
        return name.value + "'s Address: "+ header.getReqInfo();
    }
    
    public NameType echo7(Holder<String> address, Holder<String> personDetails, java.lang.String lastName, java.lang.String firstName) {
        try{
        address.value = "Sun Micro Address";
        personDetails.value = firstName + " "+ lastName;        
        NameType nameType = new NameType();
        nameType.setName("Employee");
        return nameType;
        }catch(Exception e){e.printStackTrace();}
        return null;
    }

}
