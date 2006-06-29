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

package fromjava.server;

//import java.net.InetSocketAddress;
//import java.util.concurrent.Executors;
//import com.sun.net.httpserver.HttpContext;
//import com.sun.net.httpserver.HttpServer;

import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.Endpoint;

public class AddWebservice {
    
    public static void main (String[] args) throws Exception {
        Endpoint endpoint = Endpoint.publish (
            "http://localhost:8080/jaxws-fromjava/addnumbers",
            new AddNumbersImpl ());

        // Stops the endpoint if it receives request http://localhost:9090/stop
        new EndpointStopper(9090, endpoint);
    }
}
