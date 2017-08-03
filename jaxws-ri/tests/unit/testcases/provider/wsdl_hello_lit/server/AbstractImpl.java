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

package provider.wsdl_hello_lit.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.ws.WebServiceException;
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
