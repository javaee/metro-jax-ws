/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */


package restful.server;

import java.io.ByteArrayInputStream;
import java.util.StringTokenizer;

import javax.annotation.Resource;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Provider;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.WebServiceProvider;
import javax.xml.ws.Service;
import javax.xml.ws.ServiceMode;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.http.HTTPException;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.BindingType;

@WebServiceProvider
@BindingType(value=HTTPBinding.HTTP_BINDING)
public class AddNumbersImpl implements Provider<Source> {

    @Resource(type=Object.class)
    protected WebServiceContext wsContext;

    public Source invoke(Source source) {
        try {
            MessageContext mc = wsContext.getMessageContext();
            String query = (String)mc.get(MessageContext.QUERY_STRING);
            String path = (String)mc.get(MessageContext.PATH_INFO);
            System.out.println("Query String = "+query);
            System.out.println("PathInfo = "+path);
            if (query != null && query.contains("num1=") &&
                query.contains("num2=")) {
                return createSource(query);
            } else if (path != null && path.contains("/num1") &&
                       path.contains("/num2")) {
                return createSource(path);
            } else {
                throw new HTTPException(404);
            }
        } catch(Exception e) {
            e.printStackTrace();
            throw new HTTPException(500);
        }
    }
    
    private Source createSource(String str) {
        StringTokenizer st = new StringTokenizer(str, "=&/");
        String token = st.nextToken();
        int number1 = Integer.parseInt(st.nextToken());
        st.nextToken();
        int number2 = Integer.parseInt(st.nextToken());
        int sum = number1+number2;
        String body =
            "<ns:addNumbersResponse xmlns:ns=\"http://duke.example.org\"><ns:return>"
            +sum
            +"</ns:return></ns:addNumbersResponse>";
        Source source = new StreamSource(
            new ByteArrayInputStream(body.getBytes()));
        return source;
    }
    
}
