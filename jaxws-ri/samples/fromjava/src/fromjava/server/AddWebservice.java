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
package fromjava.server;

//import java.net.InetSocketAddress;
//import java.util.concurrent.Executors;
//import com.sun.net.httpserver.HttpContext;
//import com.sun.net.httpserver.HttpServer;

import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.Endpoint;

public class AddWebservice {
    
    public static void main (String[] args) throws Exception {
        Endpoint.publish (
            "http://localhost:8080/jaxws-fromjava/addnumbers",
            new AddNumbersImpl ());
    }
    
//    public static void deployMethod2 () throws Exception {
//        Endpoint endpoint = Endpoint.create(
//            new URI (SOAPBinding.SOAP11HTTP_BINDING),
//            new AddNumbersImpl ());
//        
//        HttpServer server = HttpServer.create (new InetSocketAddress (8080), 5);
//        server.setExecutor (Executors.newFixedThreadPool (5));
//        HttpContext context = server.createContext (
//            "http",
//            "/jaxws-fromjava/addnumbers");
//        
//        endpoint.publish (context);
//        server.start ();
//    }
    
}
