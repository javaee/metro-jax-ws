/*
 * EndpointImpl.java
 *
 * Created on April 13, 2005, 10:34 AM
 */

package com.sun.xml.ws.transport.http.server;

import com.sun.xml.ws.server.DocInfo;
import com.sun.xml.ws.server.RuntimeEndpointInfo;
import com.sun.xml.ws.server.Tie;
import com.sun.xml.ws.server.WSDLPatcher;
import com.sun.xml.ws.util.localization.LocalizableMessageFactory;
import com.sun.xml.ws.util.localization.Localizer;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import javax.net.http.HttpContext;
import javax.net.http.HttpHandler;
import javax.net.http.HttpServer;
import javax.net.http.HttpTransaction;

import javax.xml.ws.Endpoint;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.Binding;
import java.util.List;
import javax.xml.transform.Source;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.logging.Logger;



/**
 *
 * @author jitu
 */
public class EndpointImpl implements Endpoint {
    
    private Localizer localizer;
    private LocalizableMessageFactory messageFactory;
    private static final Logger logger =
        Logger.getLogger(
            com.sun.xml.ws.util.Constants.LoggingDomain + ".server.http");
    
    private String address;
    private Object impl;
    private List<Handler> chain;
    private HttpContext httpContext;
    private HttpServer httpServer;
    private boolean published;
    private List<Source> metadata;
    private Binding binding;
    
    public EndpointImpl(URI bindingId, Object impl) {
        this.impl = impl;
        // TODO: get Binding from bindingId
        this.binding = null;
        localizer = new Localizer();
        messageFactory =
            new LocalizableMessageFactory("com.sun.xml.ws.resources.jaxrpcservlet");
    }
    
    public Binding getBinding() {
        return binding;
    }

    public Object getImplementor() {
        return impl;
    }

    public void publish(String address) {
        try {
            System.out.println("Publishing at address="+address);
            this.address = address;
            URL url = new URL(address);
            int port = url.getPort();
            if (port == -1) {
                port = url.getDefaultPort();
            }
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 5);
            server.setExecutor(Executors.newFixedThreadPool(5));
            int index = url.getPath().lastIndexOf('/');
            String contextRoot = url.getPath().substring(0, index);
            System.out.println("*** contextRoot ***"+contextRoot);
            HttpContext context = server.createContext(url.getProtocol(), contextRoot);
            publish(context);
            server.start();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void publish(Object serverContext) {
        this.httpContext = (HttpContext)serverContext;
        try {
            publish(httpContext);
        } catch(Exception e) {
            e.printStackTrace();
        }
        published = true;
    }

    public void stop() {
        // TODO: what should be done in unpublish ?
        httpContext.setHandler(null);
        if (httpServer != null) {
            httpServer.removeContext(httpContext);
            httpServer.terminate();
        }
        published = false;
    }

    public boolean isPublished() {
        return false;
    }

    public java.util.List<Source> getMetadata() {
        return metadata;
    }

    public void setMetadata(java.util.List<Source> metadata) {

        this.metadata = metadata;
    }

    public List<Handler> getHandlerChain() {
        return chain;
    }

    public void setHandlerChain(List<Handler> chain) {
        this.chain = chain;
    }

    public void publish (HttpContext context) throws Exception {
        final RuntimeEndpointInfo endpointInfo = new RuntimeEndpointInfo();
        endpointInfo.setImplementor(getImplementor());
        endpointInfo.setUrlPattern("");
        endpointInfo.deploy();
        System.out.println("Doc Metadata="+endpointInfo.getDocMetadata());
        
        
        final Tie tie = new Tie();
        context.setHandler(new HttpHandler() {
            public void handleTransaction(HttpTransaction msg) {
                try {
                    System.out.println("Received HTTP request:"+msg.getRequestURI());
                    if (msg.getRequestMethod().equals("GET")) {
                        InputStream is = msg.getRequestBody();
                        byte[] bytes = readFully(is);
                        System.out.println("Closing input");
                        is.close();
                        writeGetReply(msg, endpointInfo);
                    } else {
                        ServerConnectionImpl con = new ServerConnectionImpl(msg);
                        tie.handle(con, endpointInfo);
                        //con.getOutput().close();
                    }
                    msg.close();
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    
    protected static byte[] readFully(InputStream istream) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int num = 0;

        if (istream != null) {
            while ((num = istream.read(buf)) != -1) {
                bout.write(buf, 0, num);
            }
        }
        byte[] ret = bout.toByteArray();
        return ret;
    }
            
    protected void writeGetReply(HttpTransaction msg, RuntimeEndpointInfo targetEndpoint)
    throws Exception {
     
        String queryString = msg.getRequestURI().getQuery();
        System.out.println("queryString="+queryString);

        String inPath = targetEndpoint.getPath(queryString);
        if (inPath == null) {
            writeNotFoundErrorPage(msg, "Invalid Request="+msg.getRequestURI());
            return;
        }
        DocInfo in = targetEndpoint.getDocMetadata().get(inPath);
        if (in == null) {
            writeNotFoundErrorPage(msg, "Invalid Request="+msg.getRequestURI());
            return;
        }
        msg.getResponseHeaders().addHeader("Content-Type", "text/xml");
        msg.sendResponseHeaders(HttpURLConnection.HTTP_OK,  0);
        OutputStream outputStream = msg.getResponseBody();
        
        List<RuntimeEndpointInfo> endpoints = new ArrayList<RuntimeEndpointInfo>();
        endpoints.add(targetEndpoint);
        WSDLPatcher patcher = new WSDLPatcher(inPath, address,
                targetEndpoint, endpoints, in.getDocContext());
        InputStream is = in.getDoc();
        try {
            patcher.patchDoc(is, outputStream);
        } finally {
            is.close();
            //outputStream.close();
        }
         
    }
    
    protected void writeNotFoundErrorPage(HttpTransaction msg, String message)
    throws Exception {
        
        System.out.println("Wrint the not found");
        msg.getResponseHeaders().addHeader("Content-Type", "text/html");
        msg.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND,  0);
        OutputStream outputStream = msg.getResponseBody();
        PrintWriter out = new PrintWriter(outputStream);
        out.println("<html>");
        out.println("<head><title>");
        out.println(
            localizer.localize(
                messageFactory.getMessage("servlet.html.title")));
        out.println("</title></head>");
        out.println("<body>");
        out.println(
            localizer.localize(
                messageFactory.getMessage("servlet.html.notFound", message)));
        out.println("</body>");
        out.println("</html>");
        out.close();
    }
            
    
}
