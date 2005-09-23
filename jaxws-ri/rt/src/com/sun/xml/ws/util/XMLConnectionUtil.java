/*
 * $Id: XMLConnectionUtil.java,v 1.8 2005-09-23 15:18:18 spericas Exp $
 */

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
import com.sun.xml.ws.binding.BindingImpl;
import com.sun.pept.ept.MessageInfo;
import com.sun.xml.ws.encoding.xml.XMLMessage;
import com.sun.xml.ws.server.*;
import java.io.ByteArrayInputStream;
import javax.xml.transform.stream.StreamSource;

import static com.sun.xml.ws.developer.JAXWSProperties.CONTENT_NEGOTIATION_PROPERTY;

/**
 * @author WS Development Team
 */
public class XMLConnectionUtil {
    
    public static XMLMessage getXMLMessage(WSConnection con, MessageInfo mi) {
        try {
            Map<String, List<String>> headers = con.getHeaders();
            MimeHeaders mh = new MimeHeaders();
            if (headers != null) {
                for(Map.Entry<String, List<String>> entry : headers.entrySet()) {
                    String name = entry.getKey();
                    for(String value : entry.getValue()) {
                        mh.addHeader(name, value);
                    }
                }
            }
            return new XMLMessage(mh, con.getInput());
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
        send(con, xmlMessage);
    }
    
    public static void sendResponseOneway(WSConnection con) {
        setStatus(con, WSConnection.ONEWAY);
        con.getOutput();
        con.closeOutput();
    }
        
    public static void sendResponseError(WSConnection con, MessageInfo messageInfo) {
        try {
            StreamSource source = new StreamSource(
                    new ByteArrayInputStream(DEFAULT_SERVER_ERROR.getBytes()));            
            String conneg = (String) messageInfo.getMetaData(CONTENT_NEGOTIATION_PROPERTY);                       
            XMLMessage message = new XMLMessage(source, conneg == "optimistic");
            setStatus(con, WSConnection.INTERNAL_ERR);
            send(con, message);
        } 
        catch(Exception e) {
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
    
    public static void setStatus(WSConnection con, int status) {
        con.setStatus(status);
    }
    
    private final static String DEFAULT_SERVER_ERROR =
        "<?xml version='1.0' encoding='UTF-8'?>"
        + "<err>Internal Server Error</err>";

}
