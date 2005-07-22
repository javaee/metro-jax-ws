/*
 * $Id: HttpClientTransport.java,v 1.10 2005-07-22 00:34:49 arungupta Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.sun.xml.ws.transport.http.client;

import com.sun.pept.ept.EPTFactory;
import com.sun.xml.messaging.saaj.util.ByteInputStream;
import com.sun.xml.ws.client.ClientTransportException;
import com.sun.xml.ws.encoding.soap.message.SOAPMessageContext;
import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;
import com.sun.xml.ws.util.localization.Localizable;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import javax.xml.soap.*;
import javax.xml.ws.soap.SOAPBinding;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


import static javax.xml.ws.BindingProvider.SESSION_MAINTAIN_PROPERTY;
import static javax.xml.ws.BindingProvider.SOAPACTION_URI_PROPERTY;
import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;
import static com.sun.xml.ws.client.BindingProviderProperties.HTTP_STATUS_CODE;
import static com.sun.xml.ws.client.BindingProviderProperties.HTTP_COOKIE_JAR;
import static com.sun.xml.ws.client.BindingProviderProperties.HOSTNAME_VERIFICATION_PROPERTY;
import static com.sun.xml.ws.client.BindingProviderProperties.REDIRECT_REQUEST_PROPERTY;
import static com.sun.xml.ws.client.BindingProviderProperties.BINDING_ID_PROPERTY;
import com.sun.xml.ws.transport.WSConnectionImpl;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * @author WS Development Team
 */
public class HttpClientTransport extends WSConnectionImpl {
    

    private static String LAST_ENDPOINT = "";
    private static boolean redirect = true;
    private static final int START_REDIRECT_COUNT = 3;
    private static int redirectCount = START_REDIRECT_COUNT;
    int statusCode;

    public HttpClientTransport() {
        this(null, SOAPBinding.SOAP11HTTP_BINDING);
    }

    // TODO: Consider passing the properyt bag
    public HttpClientTransport(OutputStream logStream, String bindingId) {
        try {
            if(bindingId.equals(SOAPBinding.SOAP12HTTP_BINDING))
                _messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
            else
                _messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);

            _logStream = logStream;
        } catch (Exception e) {
            throw new ClientTransportException("http.client.cannotCreateMessageFactory");
        }
    }

    public HttpClientTransport(OutputStream logStream, Map<String, Object> context) {
        String bindingId = (String)context.get(BINDING_ID_PROPERTY);
        try {
            if(bindingId.equals(SOAPBinding.SOAP12HTTP_BINDING))
                _messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
            else
                _messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);

            endpoint = (String)context.get(ENDPOINT_ADDRESS_PROPERTY);
            this.context = context;
            _logStream = logStream;
        } catch (Exception e) {
            throw new ClientTransportException("http.client.cannotCreateMessageFactory");
        }
    }
    
    /**
     * Prepare the stream for HTTP request
     */
    @Override
    public OutputStream getOutput() {
        try {
            httpConnection = createHttpConnection(endpoint, context);
            // how to incorporate redirect processing: message dispatcher does not seem to tbe right place
            outputStream = httpConnection.getOutputStream();
            
            cookieJar = sendCookieAsNeeded();
            connectForResponse();
        
        } catch (Exception ex) {
            if (ex instanceof Localizable) {
                throw new ClientTransportException("http.client.failed",
                        (Localizable) ex);
            } else {
                throw new ClientTransportException("http.client.failed",
                        new LocalizableExceptionAdapter(ex));
            }
        }
        
        return outputStream;
    }
    
    /**
     * Get the response from HTTP connection and prepare the input stream for response
     */
    @Override
    public InputStream getInput() {
        // response processing
        
        ByteInputStream in;
        try {
            isFailure = checkResponseCode();

            Map<String, List<String>> headers = collectResponseMimeHeaders();

            saveCookieAsNeeded(cookieJar);
            setHeaders(headers);
            
            in = readResponse();
        } catch (IOException e) {
            if (statusCode == HttpURLConnection.HTTP_NO_CONTENT
                    || (isFailure
                    && statusCode != HttpURLConnection.HTTP_INTERNAL_ERROR)) {
                try {
                    throw new ClientTransportException("http.status.code",
                            new Object[]{
                                new Integer(statusCode),
                                httpConnection.getResponseMessage()});
                } catch (IOException ex) {
                    throw new ClientTransportException("http.status.code",
                            new Object[]{
                                new Integer(statusCode),
                                ex});
                }
            }
            throw new ClientTransportException("http.client.failed",
                    e.getMessage());
        }
        httpConnection = null;
        
        return in;
    }
    
    @Override
    public OutputStream getDebug() {
        return _logStream;
    }
    
    public void invoke(String endpoint, SOAPMessageContext context)
            throws ClientTransportException {

//        try {
//            int statusCode = httpConnection.getResponseCode();
//
//            //http URL redirection does not redirect http requests
//            //to an https endpoint probably due to a bug in the jdk
//            //or by intent - to workaround this if an error code
//            //of HTTP_MOVED_TEMP or HTTP_MOVED_PERM is received then
//            //the jaxrpc client will reinvoke the original request
//            //to the new endpoint - kw bug 4890118
//            if (checkForRedirect(statusCode)) {
//                redirectRequest(httpConnection, context);
//                return;
//            }
    }

    protected ByteInputStream readResponse()
            throws IOException {
        ByteInputStream in;
        InputStream contentIn =
                (isFailure
                ? httpConnection.getErrorStream()
                : httpConnection.getInputStream());

        byte[] bytes = readFully(contentIn);
        int length =
                httpConnection.getContentLength() == -1
                ? bytes.length
                : httpConnection.getContentLength();
        in = new ByteInputStream(bytes, length);

        contentIn.close();

        return in;
    }

    protected Map<String, List<String>> collectResponseMimeHeaders() {
        MimeHeaders mimeHeaders = new MimeHeaders();
        for (int i = 1; ; ++i) {
            String key = httpConnection.getHeaderFieldKey(i);
            if (key == null) {
                break;
            }
            String value = httpConnection.getHeaderField(i);
            try {
                mimeHeaders.addHeader(key, value);
            } catch (IllegalArgumentException e) {
                // ignore headers that are illegal in MIME
            }
        }
        
        Map<String, List<String>> headers = new HashMap<String, List<String>>();
        for (Iterator iter = mimeHeaders.getAllHeaders(); iter.hasNext();) {
            MimeHeader header = (MimeHeader)iter.next();
            List<String> h = new ArrayList<String>();
            h.add(header.getValue());
            headers.put (header.getName (), h);
        }
        return headers;
    }

    protected void connectForResponse()
            throws IOException {

        httpConnection.connect();
    }

    /*
     * Will throw an exception instead of returning 'false' if there is no
     * return message to be processed (i.e., in the case of an UNAUTHORIZED
     * response from the servlet or 404 not found)
     */
    protected boolean checkResponseCode()
            throws IOException {
        boolean isFailure = false;
        try {

            statusCode = httpConnection.getResponseCode();
            setStatus (statusCode);
            
            if ((httpConnection.getResponseCode()
                    == HttpURLConnection.HTTP_INTERNAL_ERROR)) {
                isFailure = true;
                //added HTTP_ACCEPT for 1-way operations
            } else if (
                    httpConnection.getResponseCode()
                    == HttpURLConnection.HTTP_UNAUTHORIZED) {

                // no soap message returned, so skip reading message and throw exception
                throw new ClientTransportException("http.client.unauthorized",
                        httpConnection.getResponseMessage());
            } else if (
                    httpConnection.getResponseCode()
                    == HttpURLConnection.HTTP_NOT_FOUND) {

                // no message returned, so skip reading message and throw exception
                throw new ClientTransportException("http.not.found",
                        httpConnection.getResponseMessage());
            } else if (
                    (statusCode == HttpURLConnection.HTTP_MOVED_TEMP) ||
                    (statusCode == HttpURLConnection.HTTP_MOVED_PERM)) {
                isFailure = true;

                if (!redirect || (redirectCount <= 0)) {
                    throw new ClientTransportException("http.status.code",
                            new Object[]{
                                new Integer(statusCode),
                                getStatusMessage(httpConnection)});
                }
            } else if (
                    statusCode < 200 || (statusCode >= 303 && statusCode < 500)) {
                throw new ClientTransportException("http.status.code",
                        new Object[]{
                            new Integer(statusCode),
                            getStatusMessage(httpConnection)});
            } else if (statusCode >= 500) {
                isFailure = true;
            }
        } catch (IOException e) {
            // on JDK1.3.1_01, we end up here, but then getResponseCode() succeeds!
//            if (httpConnection.getResponseCode()
//                    == HttpURLConnection.HTTP_INTERNAL_ERROR) {
//                isFailure = true;
//            } else {
//                throw e;
//            }
        }

        return isFailure;
    }

    protected String getStatusMessage(HttpURLConnection httpConnection)
            throws IOException {
        int statusCode = httpConnection.getResponseCode();
        String message = httpConnection.getResponseMessage();
        if (statusCode == HttpURLConnection.HTTP_CREATED
                || (statusCode >= HttpURLConnection.HTTP_MULT_CHOICE
                && statusCode != HttpURLConnection.HTTP_NOT_MODIFIED
                && statusCode < HttpURLConnection.HTTP_BAD_REQUEST)) {
            String location = httpConnection.getHeaderField("Location");
            if (location != null)
                message += " - Location: " + location;
        }
        return message;
    }

    protected CookieJar sendCookieAsNeeded() {
        String header = (String)context.get(SESSION_MAINTAIN_PROPERTY);
        if (header == null)
            return null;
        
        Boolean shouldMaintainSessionProperty = new Boolean(header);
        boolean shouldMaintainSession =
                (shouldMaintainSessionProperty == null
                ? false
                : shouldMaintainSessionProperty.booleanValue());
        if (shouldMaintainSession) {
            CookieJar cookieJar =
                    (CookieJar) context.get(HTTP_COOKIE_JAR);
            if (cookieJar == null) {
                cookieJar = new CookieJar();
            }
            cookieJar.applyRelevantCookies(httpConnection);
            return cookieJar;
        } else {
            return null;
        }
    }

    protected void saveCookieAsNeeded(CookieJar cookieJar) {
        if (cookieJar != null) {
            cookieJar.recordAnyCookies(httpConnection);
            context.put(HTTP_COOKIE_JAR,
                    cookieJar);
        }
        
        // TODO: where and how this cookieJar is used ?
    }

    protected HttpURLConnection createHttpConnection(String endpoint,
                                                     Map<String, Object> context)
            throws IOException {

        boolean verification = false;
        // does the client want client hostname verification by the service
        String verificationProperty =
                (String) context.get(HOSTNAME_VERIFICATION_PROPERTY);
        if (verificationProperty != null) {
            if (verificationProperty.equalsIgnoreCase("true"))
                verification = true;
        }

        // does the client want request redirection to occur
        String redirectProperty =
                (String) context.get(REDIRECT_REQUEST_PROPERTY);
        if (redirectProperty != null) {
            if (redirectProperty.equalsIgnoreCase("false"))
                redirect = false;
        }

        checkEndpoints(endpoint);

        HttpURLConnection httpConnection = createConnection(endpoint);

        if (!verification) {
            // for https hostname verification  - turn off by default
            if (httpConnection instanceof HttpsURLConnection) {
                ((HttpsURLConnection) httpConnection).setHostnameVerifier(new HttpClientVerifier());
            }
        }

        // allow interaction with the web page - user may have to supply
        // username, password id web page is accessed from web browser
        httpConnection.setAllowUserInteraction(true);
        // enable input, output streams
        httpConnection.setDoOutput(true);
        httpConnection.setDoInput(true);
        // the soap message is always sent as a Http POST
        // HTTP Get is disallowed by BP 1.0
        httpConnection.setRequestMethod("POST");
        
        // set the properties on HttpURLConnection
        for (Map.Entry entry : getHeaders().entrySet()) {
            httpConnection.addRequestProperty ((String)entry.getKey(), ((List<String>)entry.getValue()).get(0));
        }
        
        return httpConnection;
    }

    private java.net.HttpURLConnection createConnection(String endpoint)
            throws IOException {
        return (HttpURLConnection) new URL(endpoint).openConnection();
    }

    private void redirectRequest(HttpURLConnection httpConnection, SOAPMessageContext context) {
        String redirectEndpoint = httpConnection.getHeaderField("Location");
        if (redirectEndpoint != null) {
            httpConnection.disconnect();
            invoke(redirectEndpoint, context);
        } else
            System.out.println("redirection Failed");
    }

    private boolean checkForRedirect(int statusCode) {
        return (((statusCode == 301) || (statusCode == 302)) && redirect && (redirectCount-- > 0));
    }

    private void checkEndpoints(String currentEndpoint) {
        if (!LAST_ENDPOINT.equalsIgnoreCase(currentEndpoint)) {
            redirectCount = START_REDIRECT_COUNT;
            LAST_ENDPOINT = currentEndpoint;
        }
    }

    private byte[] readFully(InputStream istream) throws IOException {
        if (istream == null)
            return new byte[0];
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int num = 0;
        while ((num = istream.read(buf)) != -1) {
            bout.write(buf, 0, num);
        }
        byte[] ret = bout.toByteArray();
        return ret;
    }

    // overide default SSL HttpClientVerifier to always return true
    // effectively overiding Hostname client verification when using SSL
    static class HttpClientVerifier implements HostnameVerifier {
        public boolean verify(String s, SSLSession sslSession) {
            return true;
        }
    }

    private MessageFactory _messageFactory;
    HttpURLConnection httpConnection = null;
    String endpoint = null;
    Map<String, Object> context = null;
    CookieJar cookieJar = null;
    boolean isFailure = false;
    OutputStream _logStream = null;
}
