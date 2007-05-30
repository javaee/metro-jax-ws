/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

package asyncprovider.server;

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

    private static final JAXBContext jaxbContext = createJAXBContext();
    private int bodyIndex;

    public javax.xml.bind.JAXBContext getJAXBContext(){
        return jaxbContext;
    }
    
    private static javax.xml.bind.JAXBContext createJAXBContext(){
        try{
            return javax.xml.bind.JAXBContext.newInstance(ObjectFactory.class);
        }catch(javax.xml.bind.JAXBException e){
            throw new WebServiceException(e.getMessage(), e);
        }
    }

    private Source sendSource() {
        System.out.println("**** sendSource ******");

        String[] body  = {
            "<HelloResponse xmlns=\"urn:test:types\"><argument xmlns=\"\">foo</argument><extra xmlns=\"\">bar</extra></HelloResponse>",
            "<ans1:HelloResponse xmlns:ans1=\"urn:test:types\"><argument>foo</argument><extra>bar</extra></ans1:HelloResponse>",
        };
        int i = (++bodyIndex)%body.length;
        return new StreamSource(
            new ByteArrayInputStream(body[i].getBytes()));
    }

    private Hello_Type recvBean(Source source) throws Exception {
        System.out.println("**** recvBean ******");
        return (Hello_Type)jaxbContext.createUnmarshaller().unmarshal(source);
    }

    private Source sendBean() throws Exception {
        System.out.println("**** sendBean ******");
        HelloResponse resp = new HelloResponse();
        resp.setArgument("foo");
        resp.setExtra("bar");
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        jaxbContext.createMarshaller().marshal(resp, bout);
        return new StreamSource(new ByteArrayInputStream(bout.toByteArray()));
    }

    public void invoke(Source source, AsyncProviderCallback<Source> cbak, WebServiceContext ctxt) {
        System.out.println("**** Received in AsyncProvider Impl ******");
		try {
			Hello_Type hello = recvBean(source);
			String arg = hello.getArgument();
			if (arg.equals("sync")) {
				String extra = hello.getExtra();
				if (extra.equals("source")) {
					cbak.send(sendSource());
				} else if (extra.equals("bean")) {
					cbak.send(sendBean());
				} else {
					throw new WebServiceException("Expected extra = (source|bean|fault), Got="+extra);
				}
			} else if (arg.equals("async")) {
				new Thread(new RequestHandler(cbak, hello)).start();
			} else {
				throw new WebServiceException("Expected Argument = (sync|async), Got="+arg);
			}
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
				Thread.sleep(5000);
			} catch(InterruptedException ie) {
				cbak.sendError(new WebServiceException("Interrupted..."));
				return;
			}
			try {
				String extra = hello.getExtra();
				if (extra.equals("source")) {
					cbak.send(sendSource());
				} else if (extra.equals("bean")) {
					cbak.send(sendBean());
				} else {
					cbak.sendError(new WebServiceException("Expected extra = (source|bean|fault), Got="+extra));
				}
			} catch(Exception e) {
				cbak.sendError(new WebServiceException(e));
			}
		}
	}

}
