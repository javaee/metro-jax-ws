/*
 * $Id: XMLConnectionUtil.java,v 1.4 2005-08-08 19:13:05 arungupta Exp $
 */

/*
* Copyright (c) 2005 Sun Microsystems, Inc.
* All rights reserved.
*/

package com.sun.xml.ws.util;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.ws.WebServiceException;
import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import com.sun.xml.ws.spi.runtime.WSConnection;
import com.sun.xml.ws.spi.runtime.WSConnection.STATUS;
import com.sun.xml.ws.binding.BindingImpl;
import com.sun.pept.ept.MessageInfo;
import com.sun.xml.ws.encoding.xml.XMLMessage;
import com.sun.xml.ws.server.*;
import java.io.ByteArrayInputStream;
import javax.xml.transform.stream.StreamSource;

/**
 * @author WS Development Team
 */
public class XMLConnectionUtil {
    
    public static XMLMessage getXMLMessage(WSConnection con, MessageInfo mi) {
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
//            RuntimeContext rtCtxt = MessageInfoUtil.getRuntimeContext(mi);
//            RuntimeEndpointInfo endpointInfo = rtCtxt.getRuntimeEndpointInfo();
//            String bindingId = ((BindingImpl)endpointInfo.getBinding()).getBindingId();
            XMLMessage xmlMessage =  new XMLMessage(mh, con.getInput());
            
            return xmlMessage;
        } catch(Exception e) {
            e.printStackTrace();
            throw new WebServiceException(e);
        }
    }
    
    private static void send(WSConnection con, XMLMessage xmlMessage) {
        try {
            Map<String, List<String>> headers = new HashMap<String, List<String>>();
            MimeHeaders mhs = xmlMessage.getMimeHeaders();
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
            con.setHeaders(headers);
            xmlMessage.writeTo(con.getOutput());
            con.closeOutput();
        } catch(Exception e) {
            throw new WebServiceException(e);
        }
    }
    
    public static void sendResponse(WSConnection con, XMLMessage xmlMessage) {
        setStatus(con, xmlMessage.getStatus());
        con.setStatus(xmlMessage.getStatusCode());
        send(con, xmlMessage);
    }
    
    public static void sendResponseOneway(WSConnection con) {
        setStatus(con, STATUS.ONEWAY);
        con.getOutput();
        con.closeOutput();
    }
        
    public static void sendResponseError(WSConnection con) {
        try {
            StreamSource source = new StreamSource(
                    new ByteArrayInputStream(DEFAULT_SERVER_ERROR.getBytes()));
            XMLMessage message = new XMLMessage(source);
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
    
    private final static String DEFAULT_SERVER_ERROR =
        "<?xml version='1.0' encoding='UTF-8'?>"
        + "<err>Internal Server Error</err>";

}
