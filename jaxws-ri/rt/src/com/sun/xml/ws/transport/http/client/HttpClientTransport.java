/*
 * $Id: HttpClientTransport.java,v 1.5 2005-06-30 15:10:41 kwalsh Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

/**
 * @author JAX-RPC Development Team
 */
package com.sun.xml.ws.transport.http.client;

import com.sun.xml.messaging.saaj.util.ByteInputStream;
import com.sun.xml.ws.client.ClientTransport;
import com.sun.xml.ws.client.ClientTransportException;
import com.sun.xml.ws.transport.http.server.MessageContextProperties;
import com.sun.xml.ws.encoding.soap.message.SOAPMessageContext;
import com.sun.xml.ws.util.exception.LocalizableExceptionAdapter;
import com.sun.xml.ws.util.localization.Localizable;
import com.sun.xml.ws.util.Base64Util;

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
import java.util.Iterator;


import static javax.xml.ws.BindingProvider.PASSWORD_PROPERTY;
import static javax.xml.ws.BindingProvider.USERNAME_PROPERTY;
import static javax.xml.ws.BindingProvider.SESSION_MAINTAIN_PROPERTY;
import static javax.xml.ws.BindingProvider.SOAPACTION_URI_PROPERTY;
import static com.sun.xml.ws.client.BindingProviderProperties.HTTP_STATUS_CODE;
import static com.sun.xml.ws.client.BindingProviderProperties.HTTP_COOKIE_JAR;
import static com.sun.xml.ws.client.BindingProviderProperties.HOSTNAME_VERIFICATION_PROPERTY;
import static com.sun.xml.ws.client.BindingProviderProperties.REDIRECT_REQUEST_PROPERTY;


/**
 * @author JAX-RPC Development Team
 */
public class HttpClientTransport
        implements ClientTransport {

    private static String LAST_ENDPOINT = "";
    private static boolean redirect = true;
    private static final int START_REDIRECT_COUNT = 3;
    private static int redirectCount = START_REDIRECT_COUNT;

    public HttpClientTransport() {
        this(null, SOAPBinding.SOAP11HTTP_BINDING);
    }

    public HttpClientTransport(OutputStream logStream, String bindingId) {
        try {
            if(bindingId.equals(SOAPBinding.SOAP12HTTP_BINDING))
                _messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_2_PROTOCOL);
            else
                _messageFactory = MessageFactory.newInstance(SOAPConstants.SOAP_1_1_PROTOCOL);

            //_logStream = logStream;
            _logStream = System.out;
        } catch (Exception e) {
            throw new ClientTransportException("http.client.cannotCreateMessageFactory");
        }
    }

    public void invoke(String endpoint, SOAPMessageContext context)
            throws ClientTransportException {

        if (isOneWayOperation(context)) {
            invokeOneWay(endpoint, context);
            return;
        }

        //using an HttpURLConnection the soap message is sent
        //over the wire
        try {

            HttpURLConnection httpConnection =
                    createHttpConnection(endpoint, context);

            setupContextForInvoke(context);

            CookieJar cookieJar = sendCookieAsNeeded(context, httpConnection);

            moveHeadersFromContextToConnection(context, httpConnection);

            writeMessageToConnection(context, httpConnection);

            boolean isFailure = connectForResponse(httpConnection, context);
            int statusCode = httpConnection.getResponseCode();

            //http URL redirection does not redirect http requests
            //to an https endpoint probably due to a bug in the jdk
            //or by intent - to workaround this if an error code
            //of HTTP_MOVED_TEMP or HTTP_MOVED_PERM is received then
            //the jaxrpc client will reinvoke the original request
            //to the new endpoint - kw bug 4890118
            if (checkForRedirect(statusCode)) {
                redirectRequest(httpConnection, context);
                return;
            }

            MimeHeaders headers = collectResponseMimeHeaders(httpConnection);

            saveCookieAsNeeded(context, httpConnection, cookieJar);

            SOAPMessage response = null;
            //get the response from the HttpURLConnection
            try {
                response = readResponse(httpConnection, isFailure, headers);
            } catch (SOAPException e) {
                if (statusCode == HttpURLConnection.HTTP_NO_CONTENT
                        || (isFailure
                        && statusCode != HttpURLConnection.HTTP_INTERNAL_ERROR)) {
                    throw new ClientTransportException("http.status.code",
                            new Object[]{
                                new Integer(statusCode),
                                httpConnection.getResponseMessage()});
                }
                throw e;
            }
            httpConnection = null;

            logResponseMessage(context, response);

            context.setMessage(response);
            // do not set the failure flag, because stubs cannot rely on it,
            // since transports different from HTTP may not be able to set it
            // context.setFailure(isFailure);

        } catch (ClientTransportException e) {
            // let these through unmodified
            throw e;
        } catch (Exception e) {
            if (e instanceof Localizable) {
                throw new ClientTransportException("http.client.failed",
                        (Localizable) e);
            } else {
                throw new ClientTransportException("http.client.failed",
                        new LocalizableExceptionAdapter(e));
            }
        }
    }

    public void invokeOneWay(String endpoint, SOAPMessageContext context) {

        //one way send of message over the wire
        //no response will be returned
        try {
            HttpURLConnection httpConnection =
                    createHttpConnection(endpoint, context);

            setupContextForInvoke(context);

            moveHeadersFromContextToConnection(context, httpConnection);

            writeMessageToConnection(context, httpConnection);

            forceMessageToBeSent(httpConnection, context);

        } catch (Exception e) {
            if (e instanceof Localizable) {
                throw new ClientTransportException("http.client.failed",
                        (Localizable) e);
            } else {
                throw new ClientTransportException("http.client.failed",
                        new LocalizableExceptionAdapter(e));
            }
        }
    }

    protected void logResponseMessage(SOAPMessageContext context,
                                      SOAPMessage response)
            throws IOException, SOAPException {

        if (_logStream != null) {
            String s = "Response\n";
            _logStream.write(s.getBytes());
            s =
                    "Http Status Code: "
                    + context.get(HTTP_STATUS_CODE)
                    + "\n\n";
            _logStream.write(s.getBytes());
            for (Iterator iter =
                    context.getMessage().getMimeHeaders().getAllHeaders();
                 iter.hasNext();
                    ) {
                MimeHeader header = (MimeHeader) iter.next();
                s = header.getName() + ": " + header.getValue() + "\n";
                _logStream.write(s.getBytes());
            }
            _logStream.flush();
            response.writeTo(_logStream);
            s = "******************\n\n";
            _logStream.write(s.getBytes());
        }
    }

    protected SOAPMessage readResponse(HttpURLConnection httpConnection,
                                       boolean isFailure,
                                       MimeHeaders headers)
            throws IOException, SOAPException {
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

        SOAPMessage response = _messageFactory.createMessage(headers, in);

        contentIn.close();

        return response;
    }

    protected MimeHeaders collectResponseMimeHeaders(HttpURLConnection httpConnection) {
        MimeHeaders headers = new MimeHeaders();
        for (int i = 1; ; ++i) {
            String key = httpConnection.getHeaderFieldKey(i);
            if (key == null) {
                break;
            }
            String value = httpConnection.getHeaderField(i);
            try {
                headers.addHeader(key, value);
            } catch (IllegalArgumentException e) {
                // ignore headers that are illegal in MIME
            }
        }
        return headers;
    }

    protected boolean connectForResponse(HttpURLConnection httpConnection,
                                         SOAPMessageContext context)
            throws IOException {

        httpConnection.connect();
        return checkResponseCode(httpConnection, context);
    }

    protected void forceMessageToBeSent(HttpURLConnection httpConnection,
                                        SOAPMessageContext context)
            throws IOException {

        try {
            httpConnection.connect();
            httpConnection.getInputStream();
            checkResponseCode(httpConnection, context);

        } catch (IOException io) {
        }
    }

    /*
     * Will throw an exception instead of returning 'false' if there is no
     * return message to be processed (i.e., in the case of an UNAUTHORIZED
     * response from the servlet or 404 not found)
     */
    protected boolean checkResponseCode(HttpURLConnection httpConnection,
                                        SOAPMessageContext context)
            throws IOException {
        boolean isFailure = false;
        try {

            int statusCode = httpConnection.getResponseCode();
            context.put(HTTP_STATUS_CODE,
                    Integer.toString(statusCode));
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
            if (httpConnection.getResponseCode()
                    == HttpURLConnection.HTTP_INTERNAL_ERROR) {
                isFailure = true;
            } else {
                throw e;
            }
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

    protected void logRequestMessage(SOAPMessageContext context)
            throws IOException, SOAPException {

        if (_logStream != null) {
            String s = "******************\nRequest\n";
            _logStream.write(s.getBytes());
            for (Iterator iter =
                    context.getMessage().getMimeHeaders().getAllHeaders();
                 iter.hasNext();
                    ) {
                MimeHeader header = (MimeHeader) iter.next();
                s = header.getName() + ": " + header.getValue() + "\n";
                _logStream.write(s.getBytes());
            }
            _logStream.flush();
            context.getMessage().writeTo(_logStream);
            s = "\n";
            _logStream.write(s.getBytes());
            _logStream.flush();
        }
    }

    protected void writeMessageToConnection(SOAPMessageContext context,
                                            HttpURLConnection httpConnection)
            throws IOException, SOAPException {
        OutputStream contentOut = httpConnection.getOutputStream();
        context.getMessage().writeTo(contentOut);
        contentOut.flush();
        contentOut.close();
        logRequestMessage(context);
    }

    protected void moveHeadersFromContextToConnection(SOAPMessageContext context,
                                                      HttpURLConnection httpConnection) {
        for (Iterator iter =
                context.getMessage().getMimeHeaders().getAllHeaders();
             iter.hasNext();
                ) {
            MimeHeader header = (MimeHeader) iter.next();
            httpConnection.setRequestProperty(header.getName(),
                    header.getValue());
        }
    }

    protected CookieJar sendCookieAsNeeded(SOAPMessageContext context,
                                           HttpURLConnection httpConnection) {
        Boolean shouldMaintainSessionProperty =
                (Boolean) context.get(SESSION_MAINTAIN_PROPERTY);
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

    protected void saveCookieAsNeeded(SOAPMessageContext context,
                                      HttpURLConnection httpConnection,
                                      CookieJar cookieJar) {
        if (cookieJar != null) {
            cookieJar.recordAnyCookies(httpConnection);
            context.put(HTTP_COOKIE_JAR,
                    cookieJar);
        }
    }

    protected void setupContextForInvoke(SOAPMessageContext context)
            throws SOAPException, Exception {
        if (context.getMessage().saveRequired()) {
            context.getMessage().saveChanges();
        }
        String soapAction =
                (String) context.get(SOAPACTION_URI_PROPERTY);
        // From SOAP 1.1 spec section 6.1.1 "The header field value of empty string ("") means that
        // the intent of the SOAP message is provided by the HTTP Request-URI. No value means that
        // there is no indication of the intent of the message." Here I provide a mechanism for
        // providing "no value" (PBG):
        //kw null soapaction? made not null-
        if (soapAction == null) {
            context.getMessage().getMimeHeaders().setHeader("SOAPAction",
                    "\"\"");
            // httpConnection.setRequestProperty("SOAPAction", "");
        } else {
            context.getMessage().getMimeHeaders().setHeader("SOAPAction",
                    "\"" + soapAction + "\"");
            // httpConnection.setRequestProperty("SOAPAction", "\"" + soapAction + "\"");
        }
        //set up Basic Authentication mime header
        String credentials = (String) context.get(USERNAME_PROPERTY);
        if (credentials != null) {
            credentials += ":"
                    + (String) context.get(PASSWORD_PROPERTY);
            credentials =
                    Base64Util.encode(credentials.getBytes());
            context.getMessage().getMimeHeaders().setHeader("Authorization",
                    "Basic " + credentials);
        }
    }

    protected HttpURLConnection createHttpConnection(String endpoint,
                                                     SOAPMessageContext context)
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
        // Content type must be xml
        httpConnection.setRequestProperty("Content-Type", "text/xml");
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

    boolean isOneWayOperation(SOAPMessageContext context) {
        //need to modify to check actual setting
        Object oneWayOperation =
                context.get(MessageContextProperties.ONE_WAY_OPERATION);
        if (oneWayOperation != null) {
            return true;
        }
        return false;
    }

    // overide default SSL HttpClientVerifier to always return true
    // effectively overiding Hostname client verification when using SSL
    static class HttpClientVerifier implements HostnameVerifier {
        public boolean verify(String s, SSLSession sslSession) {
            return true;
        }
    }

    private MessageFactory _messageFactory;
    private OutputStream _logStream;
}
