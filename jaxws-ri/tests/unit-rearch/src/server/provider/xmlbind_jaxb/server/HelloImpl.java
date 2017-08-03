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

package server.provider.xmlbind_jaxb.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.Provider;
import javax.xml.ws.Service;
import javax.xml.ws.Service.Mode;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.soap.*;
import org.w3c.dom.Node;
import javax.xml.transform.dom.DOMSource;
import javax.activation.DataSource;
import javax.xml.ws.ServiceMode;
import com.sun.xml.messaging.saaj.packaging.mime.internet.MimeBodyPart;
import com.sun.xml.messaging.saaj.packaging.mime.internet.MimeMultipart;
import com.sun.xml.messaging.saaj.packaging.mime.internet.InternetHeaders;
import java.io.*;
import junit.framework.*;
import java.util.*;
import javax.xml.ws.WebServiceProvider;
import javax.xml.transform.sax.SAXSource;
import org.jvnet.fastinfoset.FastInfosetSource;

@WebServiceProvider
@ServiceMode (value=Service.Mode.MESSAGE)
public class HelloImpl implements Provider<Source> {
    
    private static final JAXBContext jaxbContext = createJAXBContext ();
    private int bodyIndex;
    private String fooString;
    
    public javax.xml.bind.JAXBContext getJAXBContext (){
        return jaxbContext;
    }
    
    private static javax.xml.bind.JAXBContext createJAXBContext (){
        try{
            return javax.xml.bind.JAXBContext.newInstance (ObjectFactory.class);
        }catch(javax.xml.bind.JAXBException e){
            throw new WebServiceException (e.getMessage (), e);
        }
    }
    
    private Source sendSource () {
        System.out.println ("**** sendSource ******");
        String begin = "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"><soapenv:Body>";
        String end = "</soapenv:Body></soapenv:Envelope>";
        String body  = "<HelloResponse xmlns=\"urn:test:types\"><argument xmlns=\"\">" + fooString + "</argument><extra xmlns=\"\">bar</extra></HelloResponse>";
        
        String content = body;
        return new StreamSource(new ByteArrayInputStream(content.getBytes()));
    }
    
    private void recvBean (Source source) throws Exception {
        System.out.println ("**** recvBean ******");
        
        Hello hello = (Hello)jaxbContext.createUnmarshaller().unmarshal(source);
//            hello = (Hello)jaxbContext.createUnmarshaller().unmarshal(((StreamSource)source).getInputStream());        
        
        String argument = hello.getArgument();
        if (argument.equals("Dispatch")) {
            fooString = "foo"; // normal case
        } else if (argument.equals("hellofromhandler")) {
            fooString = "hellotohandler"; // handler test case
        } else {
            throw new WebServiceException("hello.getArgument(): expected \"Dispatch\", got \"" + hello.getArgument() + "\"");
        }
        if (!"Test".equals(hello.getExtra())) {
            throw new WebServiceException("hello.getArgument(): expected \"Test\", got \"" + hello.getExtra() + "\"");
        }
    }
    
    public Source invoke(Source source) {
        try {
            recvBean(source);

            return sendSource();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
