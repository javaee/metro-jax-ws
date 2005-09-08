
/**
 * $Id: HttpEndpoint.java,v 1.14 2005-09-08 04:45:37 jitu Exp $
 *
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.transport.http.server;

import com.sun.xml.ws.server.DocInfo;
import com.sun.xml.ws.server.RuntimeEndpointInfo;
import com.sun.xml.ws.server.ServerRtException;
import com.sun.xml.ws.server.Tie;
import com.sun.xml.ws.util.localization.LocalizableMessageFactory;
import com.sun.xml.ws.util.localization.Localizer;
import java.io.IOException;
import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;
import com.sun.xml.ws.spi.runtime.WebServiceContext;
import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;
import com.sun.xml.ws.util.xml.XmlUtil;
import java.io.ByteArrayOutputStream;
import java.net.MalformedURLException;

import javax.xml.ws.Endpoint;
import java.util.List;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;

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
    
    public HttpEndpoint(RuntimeEndpointInfo rtEndpointInfo) {
        this.endpointInfo = rtEndpointInfo;
        endpointInfo.setUrlPattern("");
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
    
    // Finds primary WSDL
    private void findPrimaryWSDL() throws Exception {
        Map<String, DocInfo> metadata = endpointInfo.getDocMetadata();
        if (metadata != null) {
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
        
        // Sets the correct Port Name
        endpointInfo.doPortNameProcessing();
        
        // Sets the PortType Name
        endpointInfo.doPortTypeNameProcessing();
        
        // Creates DocInfo from metadata and sets it on RuntimeEndpointinfo
        setDocInfo();
        
        // Fill DocInfo with docuent info : WSDL or Schema, targetNS etc.
        RuntimeEndpointInfo.fillDocInfo(endpointInfo);
        
        //
        findPrimaryWSDL();
        
    }
    
    public void generateWSDLDocs() {
        if (endpointInfo.needWSDLGeneration()) {
            endpointInfo.generateWSDL();
        }
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
            logger.fine("Endpoint Context Path = "+url.getPath());
            HttpContext context = server.createContext(url.getPath());
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
            ((ExecutorService)httpServer.getExecutor()).shutdown();
            httpServer.stop(0);
        }
        endpointInfo.endService();
        published = false;
    }

    private void publish (HttpContext context) throws Exception {
        fillEndpointInfo();
        endpointInfo.deploy();
        generateWSDLDocs();
        RuntimeEndpointInfo.publishWSDLDocs(endpointInfo);
        System.out.println("Doc Metadata="+endpointInfo.getDocMetadata());
        WebServiceContext wsContext = new WebServiceContextImpl();
        endpointInfo.setWebServiceContext(wsContext);
        endpointInfo.injectContext();
        endpointInfo.beginService();
        Tie tie = new Tie();
        context.setHandler(new WSHttpHandler(tie, endpointInfo));
    }       
    
}
