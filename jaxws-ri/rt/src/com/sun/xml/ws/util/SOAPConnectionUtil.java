/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://jwsdp.dev.java.net/CDDLv1.0.html
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://jwsdp.dev.java.net/CDDLv1.0.html  If applicable,
 * add the following below this CDDL HEADER, with the
 * fields enclosed by brackets "[]" replaced with your
 * own identifying information: Portions Copyright [yyyy]
 * [name of copyright owner]
 */

package com.sun.xml.ws.util;

import com.sun.xml.ws.pept.ept.MessageInfo;
import com.sun.xml.messaging.saaj.util.ByteInputStream;
import com.sun.xml.ws.binding.BindingImpl;
import com.sun.xml.ws.server.RuntimeContext;
import com.sun.xml.ws.server.RuntimeEndpointInfo;
import com.sun.xml.ws.spi.runtime.WSConnection;

import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.soap.SOAPBinding;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.util.*;

import static com.sun.xml.ws.developer.JAXWSProperties.CONTENT_NEGOTIATION_PROPERTY;
    
/**
 * @author WS Development Team
 */
public class SOAPConnectionUtil {

    public static SOAPMessage getSOAPMessage(WSConnection con, MessageInfo mi, String bindingId) {
        try {
            Map<String, List<String>> headers = con.getHeaders();
            MimeHeaders mh = new MimeHeaders();
            if (headers != null)
                for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                    String name = entry.getKey();
                    for (String value : entry.getValue()) {
                        mh.addHeader(name, value);
                    }
                }
            RuntimeContext rtCtxt = MessageInfoUtil.getRuntimeContext(mi);
            if (rtCtxt != null) {
                RuntimeEndpointInfo endpointInfo = rtCtxt.getRuntimeEndpointInfo();
                if (bindingId == null)
                    bindingId = ((BindingImpl) endpointInfo.getBinding()).getBindingId();
            }
            SOAPMessage soapMessage = SOAPUtil.createMessage(mh,
                con.getInput(), bindingId);
            return soapMessage;
        } catch (Exception e) {
            e.printStackTrace();
            throw new WebServiceException(e);
        }
    }

    private static void send(WSConnection con, SOAPMessage soapMessage) {
        try {
            if (soapMessage.saveRequired()) {
                soapMessage.saveChanges();
            }
            Map<String, List<String>> headers = new HashMap<String, List<String>>();
            MimeHeaders mhs = soapMessage.getMimeHeaders();
            Iterator i = mhs.getAllHeaders();
            while (i.hasNext()) {
                MimeHeader mh = (MimeHeader) i.next();
                String name = mh.getName();
                List<String> values = headers.get(name);
                if (values == null) {
                    values = new ArrayList<String>();
                    headers.put(name, values);
                }
                values.add(mh.getValue());
            }
            con.setHeaders(headers);
            soapMessage.writeTo(con.getOutput());
            con.closeOutput();
        } catch (Exception e) {
            throw new WebServiceException(e);
        }
    }

    public static void sendResponse(WSConnection con, SOAPMessage soapMessage) {
        //setStatus(con, WSConnection.OK);
        send(con, soapMessage);
    }
    
    public static void sendKnownError(MessageInfo messageInfo, int status) {
        WSConnection con = (WSConnection)messageInfo.getConnection();
        setStatus(con, status);
        con.getOutput();
        con.closeOutput();
    }

    public static void sendResponseOneway(MessageInfo messageInfo) {        
        WSConnection con = (WSConnection)messageInfo.getConnection();
        setStatus(con, WSConnection.ONEWAY);
        con.getOutput();

        // Ensure conneg is completed even if no data is sent back
        if (messageInfo.getMetaData(CONTENT_NEGOTIATION_PROPERTY) == "optimistic") {            
            List<String> acceptList = null;        
            List<String> contentTypeList = null;
            Map<String, List<String>> headers = con.getHeaders();
            
            // Go through the entries because a gets are case sensitive 
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                if (entry.getKey().equalsIgnoreCase("content-type")) {
                    contentTypeList = entry.getValue();
                }
                else if (entry.getKey().equalsIgnoreCase("accept")) {
                    acceptList = entry.getValue();
                }
            }
            
            // If content-type is FI, FI must be listed in the accept header
            assert contentTypeList != null && acceptList != null;
            
            // Use FI MIME type based on Accept header
            contentTypeList.set(0,                 
                FastInfosetUtil.getFastInfosetFromAccept(acceptList));
        }
        
        con.closeOutput();
    }

    public static void sendResponseError(WSConnection con, String bindingId) {
        try {
            SOAPMessage message = SOAPUtil.createMessage(bindingId);
            ByteArrayBuffer bufferedStream = new ByteArrayBuffer();
            Writer writer = new OutputStreamWriter(bufferedStream, "UTF-8");
            if(bindingId.equals(SOAPBinding.SOAP12HTTP_BINDING))
                writer.write(DEFAULT_SERVER_ERROR_SOAP12_ENVELOPE);
            else
                writer.write(DEFAULT_SERVER_ERROR_ENVELOPE);
            writer.close();
            message.getSOAPPart().setContent(new StreamSource(bufferedStream.newInputStream()));
            setStatus(con, WSConnection.INTERNAL_ERR);
            send(con, message);
        } catch (Exception e) {
            throw new WebServiceException(e);
        }
    }

    public static Map<String, List<String>> getHeaders(WSConnection con) {
        return con.getHeaders();
    }

    /**
     * sets response headers.
     */
    public static void setHeaders(WSConnection con,
                                  Map<String, List<String>> headers) {
        con.setHeaders(headers);
    }

    public static void setStatus(WSConnection con, int status) {
        con.setStatus(status);
    }

    private final static String DEFAULT_SERVER_ERROR_ENVELOPE =
        "<?xml version='1.0' encoding='UTF-8'?>"
        + "<env:Envelope xmlns:env=\"http://schemas.xmlsoap.org/soap/envelope/\">"
        + "<env:Body>"
        + "<env:Fault>"
        + "<faultcode>env:Server</faultcode>"
        + "<faultstring>Internal server error</faultstring>"
        + "</env:Fault>"
        + "</env:Body>"
        + "</env:Envelope>";

    private final static String DEFAULT_SERVER_ERROR_SOAP12_ENVELOPE =
        "<?xml version='1.0' encoding='UTF-8'?>"
        + "<env:Envelope xmlns:env=\"http://www.w3.org/2003/05/soap-envelope\">"
        + "<env:Body>"
        + "<env:Fault>"
        + "<env:Code><env:Value>env:Receiver</env:Value></env:Code>"
        + "<env:Reason><env:Text lang=\""+ Locale.getDefault().getLanguage() +"\">"
        + "Internal server error</env:Text></env:Reason>"
        + "</env:Fault>"
        + "</env:Body>"
        + "</env:Envelope>";

}
