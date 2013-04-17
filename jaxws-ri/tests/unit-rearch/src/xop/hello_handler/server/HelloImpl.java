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

package xop.hello_handler.server;


import com.sun.xml.ws.developer.JAXWSProperties;

import javax.xml.ws.WebServiceException;
import javax.activation.DataHandler;
import javax.annotation.Resource;
import javax.jws.WebService;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import java.awt.*;
import java.util.Map;
import java.util.List;
import java.io.IOException;
import javax.xml.ws.BindingType;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.transform.Source;



@WebService(endpointInterface = "xop.hello_handler.server.Hello")
@BindingType(value=SOAPBinding.SOAP11HTTP_MTOM_BINDING)

public class HelloImpl {
    
    public void detail(Holder<byte[]> photo, Holder<Image> image){
        verifyHandlerExecution();
        if (image.value == null) {
            throw new WebServiceException("Received Image is null");
        }
        MessageContext mc = wsContext.getMessageContext();
        //Mtom attachments are not visible to the applications
//        String conneg = (String)mc.get(JAXWSProperties.CONTENT_NEGOTIATION_PROPERTY);
//        if (conneg == null || conneg != "optimistic" ) {
//            Map<String, DataHandler> attachments = (Map<String, DataHandler>)mc.get(MessageContext.INBOUND_MESSAGE_ATTACHMENTS);
//            if(attachments.size() != 2)
//                throw new WebServiceException("Expected 2 attachments in MessageContext, received: "+attachments.size()+"!");
//        }
    }

    public DataHandler claimForm(DataHandler data) throws IOException {
        verifyHandlerExecution();
//        Source src = null;
//        try {
//            src = (Source) data.getContent();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        if(data.getContentType().equals("image/jpeg")){
            Image image = (Image) data.getContent();
        }else if(data.getContentType().equals("text/xml")){
            Source src = (Source)data.getContent();
        }else{
            throw new WebServiceException("Expected image/jpeg or text/xml contentType, received: "+data.getContentType());
        }
        return data;
    }

    public void echoData(Holder<byte[]> data){
        verifyHandlerExecution();
        if(wsContext != null){
            MessageContext mc = wsContext.getMessageContext();
            mc.put(JAXWSProperties.MTOM_THRESHOLOD_VALUE, 2000);
        }
    }
    
    private void verifyHandlerExecution(){
        MessageContext mc = wsContext.getMessageContext();
        String value = (String)mc.get("MyHandler_Property");
        if(value != "foo"){
            throw new RuntimeException("Server-side Handler not called");
        }        
    }
    @Resource
    private WebServiceContext wsContext;
}
