/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
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

package asyncservice.server;

import com.sun.xml.ws.api.server.AsyncProvider;
import com.sun.xml.ws.api.server.AsyncProviderCallback;

import javax.xml.bind.JAXBContext;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceProvider;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@WebServiceProvider(
    wsdlLocation="WEB-INF/wsdl/hello_literal.wsdl",
    targetNamespace="urn:test",
    serviceName="Hello")

public class HelloAsyncImpl implements AsyncProvider<Source> {
    static String body  = "<HelloResponse xmlns=\"urn:test:types\"><argument xmlns=\"\">%s</argument><extra xmlns=\"\">%s</extra></HelloResponse>";
    private static final JAXBContext jaxbContext = createJAXBContext();
    private int bodyIndex;

    /*
    public javax.xml.bind.JAXBContext getJAXBContext(){
        return jaxbContext;
    } */

    private static javax.xml.bind.JAXBContext createJAXBContext(){
        try{
            return javax.xml.bind.JAXBContext.newInstance(ObjectFactory.class);
        }catch(javax.xml.bind.JAXBException e){
            throw new WebServiceException(e.getMessage(), e);
        }
    }

    private Hello_Type recvBean(Source source) throws Exception {
        System.out.println("**** recvBean ******");
        return (Hello_Type)jaxbContext.createUnmarshaller().unmarshal(source);
    }

    private Source sendSource(String arg, String extra) {
        System.out.println("**** sendSource ******");
        String response = String.format(body,arg,extra);
        return new StreamSource(
            new ByteArrayInputStream(response.getBytes()));
    }

    public void invoke(Source source, AsyncProviderCallback<Source> cbak, WebServiceContext ctxt) {
        System.out.println("**** Received in AsyncService Impl ******");
		try {
			Hello_Type hello = recvBean(source);
			String arg = hello.getArgument();
			new Thread(new RequestHandler(cbak, hello)).start();

		} catch(Exception e) {
            throw new WebServiceException("Endpoint failed", e);
		}
    }

	private class RequestHandler implements Runnable {
		final AsyncProviderCallback<Source> cbak;
		final Hello_Type hello;

		public RequestHandler(AsyncProviderCallback<Source> cbak, Hello_Type hello) {
			this.cbak = cbak;
			this.hello = hello;
		}

		public void run() {
			try {
				Thread.sleep(10000);
			} catch(InterruptedException ie) {
				cbak.sendError(new WebServiceException("Interrupted..."));
				return;
			}
			try {
				cbak.send(sendSource(hello.getArgument(),hello.getExtra()));
			} catch(Exception e) {
				cbak.sendError(new WebServiceException(e));
			}
		}
	}

}
