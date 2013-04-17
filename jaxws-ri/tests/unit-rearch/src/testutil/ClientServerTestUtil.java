/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2004-2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package testutil;


import javax.xml.namespace.QName;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilities for the client-server test clients
 */
public class ClientServerTestUtil {

    /**
     * Clients can pass in the args to main and this class
     * will handle local client transport if needed when
     * setTransport is called.
     */
    public ClientServerTestUtil() {
    }

    /**
     * Convenience method for clients. They can pass in the
     * arguments to the main method and the util class will tell
     * them whether or not to use local client transport instead
     * of http transport.
     *
     * @return Whether or not the command line argument for
     *         local client transport was used
     */
    static public boolean useLocal() {
        return Boolean.getBoolean("uselocal");
    }

    static public boolean getLog(){
        return Boolean.getBoolean("log");
    }
    
    static public boolean uselwhs(){
        return Boolean.getBoolean("uselwhs");
    }

    @Deprecated
    public static void setTransport(Object stub) throws Exception {
        // deprecated. noop.
    }
    
    @Deprecated
    public static void setTransport(Object stub, OutputStream out) throws Exception {
        // deprecated. noop.
    }

    public static HTTPResponseInfo sendPOSTRequest(String address, String message, String ctType) throws Exception {

       return sendPOSTRequest(address,message,ctType,"\"\"");
    }

    public static HTTPResponseInfo sendPOSTRequest(String address, String message, String ctType, String soapAction) throws Exception {

        // convert message
        byte [] requestData = message.getBytes();

        // create connection
        HttpURLConnection conn =
            (HttpURLConnection) new URL(address).openConnection();
        conn.setDoOutput(true);
        conn.setDoInput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("HTTP-Version", "HTTP/1.1");
        conn.setRequestProperty("Content-Type", ctType);
        if(soapAction != null) {
            conn.setRequestProperty("SOAPAction", soapAction);
        }
        conn.setRequestProperty("Content-Length",
            String.valueOf(requestData.length));

        // send request
        OutputStream outputStream = conn.getOutputStream();
        outputStream.write(requestData);
        boolean isFailure = true;
        int responseCode = conn.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            isFailure = false;
        }

        String responseMessage = conn.getResponseMessage();
        String bodyResponse = null;

        InputStream istream =
            !isFailure ? conn.getInputStream() : conn.getErrorStream();
            if (istream != null) {
                BufferedReader reader =
                    new BufferedReader(new InputStreamReader(istream));
                StringBuffer sBuffer = new StringBuffer();
                String line = reader.readLine();
                while (line != null) {
                    sBuffer.append(line);
                    line = reader.readLine();
                }
                bodyResponse = sBuffer.toString();
            }

            conn.disconnect();
            return new HTTPResponseInfo(responseCode,
                responseMessage, bodyResponse);
    }
    /**
     * Used to send a specific message as a POST request to an endpoint.
     *
     * @param address The url to which to send the request
     * @param message The message to send in the POST request
     * @return [change this once you know what you're returning -bobby]
     */
    public static HTTPResponseInfo sendPOSTRequest(String address, String message)
        throws Exception {
        return sendPOSTRequest(address, message, "text/xml");
    } 

    /**
     * Used to send a specific message as a POST request to an endpoint.
     * This version takes a stub object as a convenience. It retrieves
     * the address from the stub and calls sendPOSTRequest(address, message)
     * with that information.
     *
     * @param stub The stub from which to extract the address
     * @param message The message to send in the POST request
     * @return [change this as above -bobby]
     */
    public static HTTPResponseInfo sendPOSTRequest(Object stub, String message) throws Exception {
        
        BindingProvider bp = (BindingProvider) stub;
        String address =
            (String) bp.getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);

        return sendPOSTRequest(address, message);
    }

    public static HTTPResponseInfo sendPOSTRequest(Object stub, String message, String ct) throws Exception {

        BindingProvider bp = (BindingProvider) stub;
        String address =
            (String) bp.getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);

        return sendPOSTRequest(address, message, ct);
    }

    public static SOAPMessage makeSaajRequest(Object stub, InputStream is)
        throws Exception {

        MessageFactory messageFactory = MessageFactory.newInstance();
        SOAPConnectionFactory connectionFactory =
            SOAPConnectionFactory.newInstance();
        MimeHeaders headers = new MimeHeaders();
        headers.addHeader("Content-Type", "text/xml");
        SOAPMessage message = messageFactory.createMessage(headers, is);

        BindingProvider bp = (BindingProvider) stub;
        String address =
            (String) bp.getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
        URL url = new URL(address);
        return connectionFactory.createConnection().call(message, url);
    }

    /**
     * Convenient method to create a service
     *
     * @param serviceClass
     * @return the service
     * @throws Exception
     */
//    public static Service getService(Class serviceClass) throws Exception {
//        return ServiceFactory.newInstance().createService((URL)null,serviceClass);
//    }

    /**
     * Convenient method to create a port using service class, sei class and sei qname.
     *
     * @param serviceClass
     * @param seiClass
     * @param port
     * @return
     * @throws Exception
     */
//    public static Object getPort(Class serviceClass, Class seiClass, QName port) throws Exception {
//        Service service = getService(serviceClass);
//        return getPort(service, seiClass, port);
//    }

    /**
     * Convenient method to create a sei using service, sei class and port qname.
     *
     * @param service
     * @param seiClass
     * @param port
     * @return
     * @throws Exception
     */
    public static Object getPort(Service service, Class seiClass, QName port) throws Exception {
        return service.getPort(port, seiClass);
    }

    /**
     * Method used to add a Handler to a stub or dispatch object.
     */
    public static void addHandlerToBinding(Handler handler,
        BindingProvider bindingProvider) {

        Binding binding = bindingProvider.getBinding();
        List<Handler> handlers = binding.getHandlerChain();
        handlers.add(handler);
        binding.setHandlerChain(handlers);
    }

    /**
     * Method used to clear any handlers from a stub or dispatch object.
     */
    public static void clearHandlers(BindingProvider provider) {
        Binding binding = provider.getBinding();
        binding.setHandlerChain(new ArrayList<Handler>());
    }

    public static String getLocalAddress(QName port) {
        return "local://"+new File(System.getProperty("tempdir")).getAbsolutePath().replace('\\','/')+'?'+port.getLocalPart();
    }

/* This is useless as Stub( along with require pipes) is created first 
   and then Transport is set using ClientServerTestUtil later.
 
    static {
        // enable dumping
        if(getLog()) {
            System.setProperty(StandalonePipeAssembler.class.getName()+".dump","true");
        }
    } */
}
