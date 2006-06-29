/*
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License).  You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the license at
 * https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 * 
 * When distributing Covered Code, include this CDDL
 * Header Notice in each file and include the License file
 * at https://glassfish.dev.java.net/public/CDDLv1.0.html.
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * you own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Copyright 2006 Sun Microsystems Inc. All Rights Reserved
 */
package com.sun.xml.ws.client.dispatch;

import java.util.HashMap;

/**
 * $author: JAXWS Development Team
 */
public class DispatchContext {

    private HashMap dprops = null;

    public DispatchContext() {
        dprops = new HashMap();
    }

    public void setProperty(String name, Object value) {
        dprops.put(name, value);
    }

    public Object getProperty(String name) {
        return dprops.get(name);
    }

    public void removeProperty(String name) {
        dprops.remove(name);
    }

    public void clearProperties() {
        dprops.clear();
    }

    public static final String DISPATCH_MESSAGE =
        "com.sun.xml.ws.rt.client.dispatch.messagetype";
    public static final String DISPATCH_MESSAGE_MODE =
        "com.sun.xml.ws.rt.client.dispatch.mode";
    public static final String DISPATCH_MESSAGE_CLASS =
        "com.sun.xml.ws.rt.client.dispatch.messageclass";

    public enum MessageClass {
        SOURCE ,JAXBOBJECT, SOAPMESSAGE, DATASOURCE
    }

    public enum MessageType {
        JAXB_PAYLOAD,             //SOAP Binding
        SOURCE_PAYLOAD,
        JAXB_MESSAGE,
        SOURCE_MESSAGE ,
        SOAPMESSAGE_MESSAGE,
        //HTTP_DATASOURCE_PAYLOAD,  //HTTP Binding
        HTTP_DATASOURCE_MESSAGE,
        HTTP_SOURCE_MESSAGE, //can be allowed with an HTTP GET method
        HTTP_SOURCE_PAYLOAD,
        HTTP_JAXB_PAYLOAD,
        //HTTP_JAXB_MESSAGE
    }
}
