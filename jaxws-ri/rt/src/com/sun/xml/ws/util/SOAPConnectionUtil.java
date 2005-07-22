/*
 * $Id: SOAPConnectionUtil.java,v 1.5 2005-07-22 23:04:30 arungupta Exp $
 */

/*
* Copyright (c) 2005 Sun Microsystems, Inc.
* All rights reserved.
*/

package com.sun.xml.ws.util;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.ws.WebServiceException;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.stream.StreamSource;

import com.sun.xml.messaging.saaj.util.ByteInputStream;
import com.sun.xml.ws.spi.runtime.WSConnection;
import com.sun.xml.ws.spi.runtime.WSConnection.STATUS;
import com.sun.xml.ws.binding.soap.BindingImpl;
import com.sun.pept.ept.MessageInfo;
import com.sun.xml.ws.server.*;

/**
 * @author WS Development Team
 */
public class SOAPConnectionUtil {
    
    public static SOAPMessage getSOAPMessage(WSConnection con, MessageInfo mi, String bindingId) {
        if (con instanceof SOAPConnection) {
            return ((SOAPConnection)con).getSOAPMessage();
        }
        try {
            Map<String, List<String>> headers = con.getHeaders();
            MimeHeaders mh = new MimeHeaders();
            if (headers != null)
                for(Map.Entry<String, List<String>> entry : headers.entrySet()) {
                    String name = entry.getKey();
                    for(String value : entry.getValue()) {
                        mh.addHeader(name, value);
                    }
                }
            RuntimeContext rtCtxt = MessageInfoUtil.getRuntimeContext(mi);
            RuntimeEndpointInfo endpointInfo = rtCtxt.getRuntimeEndpointInfo();
            if (bindingId == null)
                bindingId = ((BindingImpl)endpointInfo.getBinding()).getBindingId();
            SOAPMessage soapMessage =  SOAPUtil.createMessage(mh,
                    con.getInput(), bindingId);
            return soapMessage;
        } catch(Exception e) {
            e.printStackTrace();
            throw new WebServiceException(e);
        }
    }
    
    private static void send(WSConnection con, SOAPMessage soapMessage) {
        if (con instanceof SOAPConnection) {
            ((SOAPConnection)con).sendResponse(soapMessage);
            return;
        }
        try {
            if (soapMessage.saveRequired()) {
                soapMessage.saveChanges();
            }
            Map<String, List<String>> headers = new HashMap<String, List<String>>();
            MimeHeaders mhs = soapMessage.getMimeHeaders();
            Iterator i = mhs.getAllHeaders();
            while(i.hasNext()) {
                MimeHeader mh = (MimeHeader)i.next();
                String name = mh.getName();
                List<String> values = headers.get(name);
                if (values == null) {
                    values = new ArrayList<String>();
                    headers.put(name, values);
                }
                values.add(mh.getValue());
            }
            con.setStatus(STATUS.OK);
            con.setHeaders(headers);
            soapMessage.writeTo(con.getOutput());
            con.closeOutput();
        } catch(Exception e) {
            throw new WebServiceException(e);
        }
    }
    
    public static void sendResponse(WSConnection con, SOAPMessage soapMessage) {
        setStatus(con, STATUS.OK);
        send(con, soapMessage);
    }
    
    public static void sendResponseOneway(WSConnection con) {
         if (con instanceof SOAPConnection) {
            ((SOAPConnection)con).sendResponseOneway();
            return;
        }
        ByteBuffer buf = ByteBuffer.wrap(new byte[0]);
        setStatus(con, STATUS.ONEWAY);
        //con.write(buf);
    }
        
    public static void sendResponseError(WSConnection con) {
        try {
            SOAPMessage message = SOAPUtil.createMessage();
            ByteArrayOutputStream bufferedStream = new ByteArrayOutputStream();
            Writer writer = new OutputStreamWriter(bufferedStream, "UTF-8");
            writer.write(DEFAULT_SERVER_ERROR_ENVELOPE);
            writer.close();
            byte[] data = bufferedStream.toByteArray();
            message.getSOAPPart().setContent(
                new StreamSource(new ByteInputStream(data, data.length)));
            setStatus(con, STATUS.INTERNAL_ERR);
            send(con, message);
        } catch(Exception e) {
            throw new WebServiceException(e);
        }
    }
    
    public static Map<String,List<String>> getHeaders(WSConnection con) {
        return con.getHeaders();
    }
    
    /**
     * sets response headers.
     */
    public static void setHeaders(WSConnection con,
        Map<String,List<String>> headers) {
        con.setHeaders(headers);
    }
    
    public static void setStatus(WSConnection con, STATUS status) {
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

}
