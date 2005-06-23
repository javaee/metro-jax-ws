/*
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved. 
 */
package fromjava.server;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;
import javax.net.http.HttpContext;
import javax.net.http.HttpHandler;
import javax.net.http.HttpServer;
import javax.net.http.HttpTransaction;
import com.sun.xml.ws.server.Tie;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;

import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPMessage;

import javax.xml.ws.soap.SOAPBinding;

import javax.xml.ws.Endpoint;
import javax.xml.ws.EndpointFactory;
import java.net.URI;
import java.util.List;
import java.util.ArrayList;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;

public class AddWebservice {

    public static void main(String[] args) throws Exception {
        EndpointFactory.newInstance().publish(
            "http://localhost:8080/jaxws-fromjava/addnumbers",
            new AddNumbersImpl());
    }

    public static void deployMethod2() throws Exception {
        EndpointFactory factory = EndpointFactory.newInstance();
        Endpoint endpoint = factory.createEndpoint(
                new URI(SOAPBinding.SOAP11HTTP_BINDING),
                new AddNumbersImpl());
                            
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 5);
        server.setExecutor(Executors.newFixedThreadPool(5));
        HttpContext context = server.createContext(
            "http",
            "/jaxws-fromjava/addnumbers");
                                        
        endpoint.publish(context);
        server.start();
    }

}
