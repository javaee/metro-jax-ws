/*
 * EndpointImpl.java
 *
 * Created on April 13, 2005, 10:34 AM
 */

package com.sun.xml.ws.transport.http.server;

import com.sun.xml.ws.server.DocInfo;
import com.sun.xml.ws.server.RuntimeEndpointInfo;
import com.sun.xml.ws.server.ServerRtException;
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
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpInteraction;

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
    
    private static final int MAX_THREADS = 5;
    private static final String GET_METHOD = "GET";
    private static final String POST_METHOD = "POST";
    private static final String HTML_CONTENT_TYPE = "text/html";
    private static final String XML_CONTENT_TYPE = "text/xml";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    
    public EndpointImpl(URI bindingId, Object impl) {
        this.impl = impl;
        // TODO: get Binding from bindingId
        this.binding = null;
        localizer = new Localizer();
        messageFactory =
            new LocalizableMessageFactory("com.sun.xml.ws.resources.httpserver");
    }
    
    public Binding getBinding() {
        return binding;
    }

    public Object getImplementor() {
        return impl;
    }

    public void publish(String address) {
        try {
            this.address = address;
            URL url = new URL(address);
            int port = url.getPort();
            if (port == -1) {
                port = url.getDefaultPort();
            }
            InetSocketAddress inetAddress = new InetSocketAddress(port);
            HttpServer server = HttpServer.create(inetAddress, MAX_THREADS);
            server.setExecutor(Executors.newFixedThreadPool(5));
            int index = url.getPath().lastIndexOf('/');
            String contextRoot = url.getPath().substring(0, index);
            System.out.println("*** contextRoot ***"+contextRoot);
            HttpContext context = server.createContext(contextRoot);
            publish(context);
            server.start();
        } catch(Exception e) {
            throw new ServerRtException("publish.err", new Object[] { e } );
        }
    }

    public void publish(Object serverContext) {
        if (!(serverContext instanceof HttpContext)) {
            throw new ServerRtException("not.HttpContext.type");
        }
        this.httpContext = (HttpContext)serverContext;
        try {
            publish(httpContext);
        } catch(Exception e) {
            throw new ServerRtException("publish.err", new Object[] { e } );
        }
        published = true;
    }

    public void stop() {
        // TODO: what should be done in unpublish ?
        httpContext.setHandler(null);
        if (httpServer != null) {
            httpServer.removeContext(httpContext);
            httpServer.terminate(0);
        }
        published = false;
    }

    public boolean isPublished() {
        return published;
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
            public void handleInteraction(HttpInteraction msg) {
                try {
                    System.out.println("Received HTTP request:"+msg.getRequestURI());
                    String method = msg.getRequestMethod();
                    if (method.equals(GET_METHOD)) {
                        InputStream is = msg.getRequestBody();
                        readFully(is);
                        is.close();
                        writeGetReply(msg, endpointInfo);
                    } else if (method.equals(POST_METHOD)) {
                        ServerConnectionImpl con = new ServerConnectionImpl(msg);
                        tie.handle(con, endpointInfo);
                        //con.getOutput().close();
                    } else {
                        logger.warning(
                                localizer.localize(
                                    messageFactory.getMessage(
                                        "unexpected.http.method", method)));
                    }
                } catch(Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        msg.close();
                    } catch(IOException ioe) {
                        ioe.printStackTrace();          // Not much can be done
                    }
                }
            }
        });
    }
    
    /*
     * Consumes the entire input stream
     */
    private static void readFully(InputStream is) throws IOException {
        byte[] buf = new byte[1024];
        if (is != null) {
            while (is.read(buf) != -1);
        }
    }
            
    protected void writeGetReply(HttpInteraction msg, RuntimeEndpointInfo targetEndpoint)
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
        msg.getResponseHeaders().addHeader(CONTENT_TYPE_HEADER, XML_CONTENT_TYPE);
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
    
    /*
     * writes 404 Not found error html page
     */
    private void writeNotFoundErrorPage(HttpInteraction msg, String message)
    throws IOException {
        msg.getResponseHeaders().addHeader(CONTENT_TYPE_HEADER, HTML_CONTENT_TYPE);
        msg.sendResponseHeaders(HttpURLConnection.HTTP_NOT_FOUND,  0);
        OutputStream outputStream = msg.getResponseBody();
        PrintWriter out = new PrintWriter(outputStream);
        out.println("<html><head><title>");
        out.println(
            localizer.localize(
                messageFactory.getMessage("html.title")));
        out.println("</title></head><body>");
        out.println(
            localizer.localize(
                messageFactory.getMessage("html.notFound", message)));
        out.println("</body></html>");
        out.close();
    }
            
    
}
