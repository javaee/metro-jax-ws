package bsh;

import java.util.*;
import java.io.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.*;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import javax.xml.ws.Endpoint;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


// freemarker template
public class Deploy3 {

    private static int DEPLOY_PORT = Integer.valueOf(System.getProperty("deployPort")) + 0;
    private static int STOP_PORT = Integer.valueOf(System.getProperty("stopPort")) + 0;

    public static void main(String[] args) throws Throwable {
        Endpoint e = deploy();
        System.out.println("Endpoint [" + e + "] successfully deployed.");
        System.out.println("Deploying endpoint STOPPER to [http://localhost:" + STOP_PORT + "/stop]");
        new EndpointStopper(STOP_PORT, e);
    }

    static javax.xml.ws.Endpoint deploy() throws Exception {
        List<Source> metadata = new ArrayList<Source>();
//        metadata.add(
//            new StreamSource(
//                Deploy3.class.getResourceAsStream("/WEB-INF/wsdl/wsgen.report"),
//                Deploy3.class.getResource("/WEB-INF/wsdl/wsgen.report").toString()
//            )
//        );
        metadata.add(
            new StreamSource(
                Deploy3.class.getResourceAsStream("/WEB-INF/wsdl/EchoInnerService_schema1.xsd"),
                Deploy3.class.getResource("/WEB-INF/wsdl/EchoInnerService_schema1.xsd").toString()
            )
        );
        metadata.add(
            new StreamSource(
                Deploy3.class.getResourceAsStream("/WEB-INF/wsdl/EchoInnerService.wsdl"),
                Deploy3.class.getResource("/WEB-INF/wsdl/EchoInnerService.wsdl").toString()
            )
        );

// properties = {
        // }

        Map properties = new HashMap();

        // testEndpoint.className = fromjava.innerclass.EchoImpl.EchoInner
        Object endpointImpl = fromjava.innerclass.EchoImpl.EchoInner.class.newInstance();

        javax.xml.ws.Endpoint endpoint = javax.xml.ws.Endpoint.create(endpointImpl);
        endpoint.setMetadata(metadata);
        endpoint.setProperties(properties);
        String address = "http://localhost:" + DEPLOY_PORT + "/fromjava.innerclass/EchoImpl.EchoInner";
        System.out.println("Deploying endpoint to [" + address + "]");
        endpoint.publish(address);
        return endpoint;
    }
}


class EndpointStopper {

    EndpointStopper(final int port, final Endpoint endpoint) throws IOException {
        final HttpServer server = HttpServer.create(new InetSocketAddress(port), 5);
        final ExecutorService threads = Executors.newFixedThreadPool(2);
        server.setExecutor(threads);
        server.start();

        HttpContext context = server.createContext("/stop");
        context.setHandler(new HttpHandler() {
            public void handle(HttpExchange msg) throws IOException {
                System.out.println("Shutting down the Endpoint");
                endpoint.stop();
                System.out.println("Endpoint is down");
                msg.sendResponseHeaders(200, 0);
                msg.close();
                server.stop(1);
                threads.shutdown();
            }
        });
    }
}