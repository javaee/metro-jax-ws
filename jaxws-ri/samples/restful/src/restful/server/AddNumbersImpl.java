/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
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
import javax.servlet.ServletRequest;

@WebServiceProvider
@BindingType(value=HTTPBinding.HTTP_BINDING)
public class AddNumbersImpl implements Provider<Source> {

    @Resource(type=Object.class)
    protected WebServiceContext wsContext;

    public Source invoke(Source source) {
        try {
            MessageContext mc = wsContext.getMessageContext();
            // check for a PATH_INFO request
            String path = (String)mc.get(MessageContext.PATH_INFO);
            if (path != null && path.contains("/num1") &&
                       path.contains("/num2")) {
                return createResultSource(path);
            }
            String query = (String)mc.get(MessageContext.QUERY_STRING);
            System.out.println("Query String = "+query);
//            System.out.println("PathInfo = "+path);
            ServletRequest req = (ServletRequest)mc.get(MessageContext.SERVLET_REQUEST);
            int num1 = Integer.parseInt(req.getParameter("num1"));
            int num2 = Integer.parseInt(req.getParameter("num2"));
            System.out.println("num1: "+num1+" num2: "+num2);
            return createResultSource(num1+num2);
        } catch(Exception e) {
            e.printStackTrace();
            throw new HTTPException(500);
        }
    }
    
    private Source createResultSource(String str) {
        StringTokenizer st = new StringTokenizer(str, "=&/");
        String token = st.nextToken();
        int number1 = Integer.parseInt(st.nextToken());
        st.nextToken();
        int number2 = Integer.parseInt(st.nextToken());
        int sum = number1+number2;
        return createResultSource(sum);
    }
    
    private Source createResultSource(int sum) {
        String body =
            "<ns:addNumbersResponse xmlns:ns=\"http://duke.org\"><ns:return>"
            +sum
            +"</ns:return></ns:addNumbersResponse>";
        Source source = new StreamSource(
            new ByteArrayInputStream(body.getBytes()));
        return source;
    }
    
}
