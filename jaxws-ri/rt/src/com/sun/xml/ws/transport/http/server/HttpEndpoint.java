
/**
 * $Id: HttpEndpoint.java,v 1.8 2005-08-31 21:29:57 jitu Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.transport.http.server;

import com.sun.xml.ws.server.DocInfo;
import com.sun.xml.ws.server.RuntimeEndpointInfo;
import com.sun.xml.ws.server.ServerRtException;
import com.sun.xml.ws.server.Tie;
import com.sun.xml.ws.server.WSDLPatcher;
import com.sun.xml.ws.util.localization.LocalizableMessageFactory;
import com.sun.xml.ws.util.localization.Localizer;
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
import com.sun.xml.ws.handler.MessageContextImpl;
import com.sun.xml.ws.server.DocInfo.DOC_TYPE;
import com.sun.xml.ws.spi.runtime.MessageContext;
import com.sun.xml.ws.spi.runtime.WebServiceContext;
import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;
import com.sun.xml.ws.util.xml.XmlUtil;
import com.sun.xml.ws.wsdl.parser.RuntimeWSDLParser;
import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;

import javax.xml.ws.Endpoint;
import java.util.List;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.stream.XMLStreamException;

import org.xml.sax.EntityResolver;




/**
 *
 * @author WS Development Team
 */
public class HttpEndpoint {
    
    private Localizer localizer;
    private LocalizableMessageFactory messageFactory;
    private static final Logger logger =
        Logger.getLogger(
            com.sun.xml.ws.util.Constants.LoggingDomain + ".server.http");
    
    private String address;
    private HttpContext httpContext;
    private HttpServer httpServer;
    private boolean published;
    private RuntimeEndpointInfo endpointInfo;
    
    private static final int MAX_THREADS = 5;
    private static final String GET_METHOD = "GET";
    private static final String POST_METHOD = "POST";
    private static final String HTML_CONTENT_TYPE = "text/html";
    private static final String XML_CONTENT_TYPE = "text/xml";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String J2SE_WSDL_DIR = "META-INF/wsdl";
    
    public HttpEndpoint(RuntimeEndpointInfo rtEndpointInfo) {
        this.endpointInfo = rtEndpointInfo;
        localizer = new Localizer();
        messageFactory =
            new LocalizableMessageFactory("com.sun.xml.ws.resources.httpserver");
    }
    
    // If Service Name is in properties, set it on RuntimeEndpointInfo
    private void setServiceName() {
        Map<String, Object> properties = endpointInfo.getProperties();
        if (properties != null) {
            QName serviceName = (QName)properties.get(Endpoint.WSDL_SERVICE);
            if (serviceName != null) {
                endpointInfo.setServiceName(serviceName);
            }
        }
    }
    
    // If Port Name is in properties, set it on RuntimeEndpointInfo
    private void setPortName() {
        Map<String, Object> properties = endpointInfo.getProperties();
        if (properties != null) {
            QName portName = (QName)properties.get(Endpoint.WSDL_PORT);
            if (portName != null) {
                endpointInfo.setPortName(portName);
            }
        }
    }

    /*
     * Convert metadata sources using identity transform. So that we can
     * reuse the Source object multiple times.
     */
    private void setDocInfo() throws MalformedURLException {
        List<Source> metadata = endpointInfo.getMetadata();
        if (metadata != null) {
            Map<String, DocInfo> newMetadata = new HashMap<String, DocInfo>();
            Transformer transformer = XmlUtil.newTransformer();
            for(Source source: metadata) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try {
                    transformer.transform(source, new StreamResult(baos));
                    baos.close();
                } catch(IOException ioe) {
                    throw new ServerRtException("server.rt.err",
                        new LocalizableExceptionAdapter(ioe));
                } catch (TransformerException te) {
                    throw new ServerRtException("server.rt.err",
                        new LocalizableExceptionAdapter(te));
                }
                String systemId = source.getSystemId();
                URL url = new URL(systemId);
                byte[] buf = baos.toByteArray();
                EndpointDocInfo docInfo = new EndpointDocInfo(url, buf);
                newMetadata.put(systemId, docInfo);
            }
            endpointInfo.setMetadata(newMetadata);
        }
    }
    
    // Fill DocInfo with docuent info : WSDL or Schema, targetNS etc.
    private void fillDocInfo() throws XMLStreamException {
        Map<String, DocInfo> metadata = endpointInfo.getDocMetadata();
        if (metadata != null) {
            Transformer transformer = XmlUtil.newTransformer();
            for(Entry<String, DocInfo> entry: metadata.entrySet()) {
                RuntimeWSDLParser.fillDocInfo(entry.getValue(), 
                    endpointInfo.getServiceName(),
                    endpointInfo.getPortTypeName());
            }
        }
    }
    
    // Finds primary WSDL
    private void findPrimaryWSDL() throws Exception {
        Map<String, DocInfo> metadata = endpointInfo.getDocMetadata();
        if (metadata != null) {
            Transformer transformer = XmlUtil.newTransformer();
            for(Entry<String, DocInfo> entry: metadata.entrySet()) {
                DocInfo docInfo = entry.getValue();
                if (docInfo.getService() != null) {
                    // Donot generate any WSDL or Schema document
                    URL wsdlUrl = new URL(entry.getKey());
                    EntityResolver resolver = new EndpointEntityResolver(metadata);
                    endpointInfo.setWsdlInfo(wsdlUrl, resolver);
                    docInfo.setQueryString("wsdl");
                    break;
                }
            }
        }
    }
    
    /*
     * Fills RuntimeEndpointInfo with ServiceName, and PortName from properties
     */
    public void fillEndpointInfo() throws Exception {
        // set Service Name from properties on RuntimeEndpointInfo
        setServiceName();
        
        // set Port Name from properties on RuntimeEndpointInfo
        setPortName();
        
        // Sets the correct Service Name
        endpointInfo.doServiceNameProcessing();
        
        // Sets the PortType Name
        endpointInfo.doPortTypeNameProcessing();
        
        // Creates DocInfo from metadata and sets it on RuntimeEndpointinfo
        setDocInfo();
        
        // Fill DocInfo with docuent info : WSDL or Schema, targetNS etc.
        fillDocInfo();
        
        //
        findPrimaryWSDL();
        
    }
    
    public void generateWSDLDocs() {
        if (endpointInfo.needWSDLGeneration()) {
            endpointInfo.generateWSDL();
        }
    }
    
    public void publishWSDLDocs() {     
        // Set queryString for the documents
        Map<String, DocInfo> docs = endpointInfo.getDocMetadata();
        Set<Entry<String, DocInfo>> entries = docs.entrySet();
        int wsdlNum = 1;
        int xsdNum = 1;
        for(Entry<String, DocInfo> entry : entries) {
            DocInfo docInfo = (DocInfo)entry.getValue();
            DOC_TYPE docType = docInfo.getDocType();
            String query = docInfo.getQueryString();
            if (query == null) {
                switch(docType) {
                    case WSDL :                   
                        query = "wsdl="+(wsdlNum++);
                        break;
                    case SCHEMA : 
                        query = "xsd="+(xsdNum++);
                        break;
                    case OTHER :
                        logger.warning(docInfo.getUrl()+" is not a WSDL or Schema file.");
                }
                docInfo.setQueryString(query);
            }
        }
        endpointInfo.updateQuery2DocInfo();
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
            throw new ServerRtException("server.rt.err", new LocalizableExceptionAdapter(e) );
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
            throw new ServerRtException("server.rt.err", new Object[] { e } );
        }
        published = true;
    }

    public void stop() {
        httpContext.setHandler(null);
        httpContext.getServer().removeContext(httpContext);
        if (httpServer != null) {
            httpServer.removeContext(httpContext);
            httpServer.terminate(0);
        }
        endpointInfo.endService();
        published = false;
    }

    private void publish (HttpContext context) throws Exception {
        fillEndpointInfo();
        endpointInfo.setUrlPattern("");
        endpointInfo.deploy();
        generateWSDLDocs();
        publishWSDLDocs();
        final WebServiceContext wsContext = new WebServiceContextImpl();
        endpointInfo.setWebServiceContext(wsContext);
        endpointInfo.beginService();
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
                        MessageContext msgCtxt = new MessageContextImpl();
                        wsContext.setMessageContext(msgCtxt);
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
        
        String address =
            "http"
                + "://"
                + msg.getLocalAddress().getHostName()
                + ":"
                + msg.getLocalAddress().getPort()
                + msg.getRequestURI().getPath();
        System.out.println("****** Addres ="+address);
        WSDLPatcher patcher = new WSDLPatcher(in, address,
                targetEndpoint, endpoints);
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
