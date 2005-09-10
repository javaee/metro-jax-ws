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
package com.sun.xml.ws.client.dispatch;

import java.util.HashMap;

/**
 * $author: JAXWS Development Team
 */
public class DispatchContext {

    HashMap dprops = null;

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

    //public static final int DOMSOURCE = 100;
    //public static final int SAXSOURCE = 200;
    //public static final int STREAMSOURCE = 300;
    //public static final int JAXBOBJECT = 400;
    //public static final int SOAPMESSAGE = 500;

    public enum MessageClass {
        SOURCE ,JAXBOBJECT, SOAPMESSAGE, DATASOURCE
    }

    public enum MessageType {
        JAXB_PAYLOAD, SOURCE_PAYLOAD, JAXB_MESSAGE, SOURCE_MESSAGE , SOAPMESSAGE_MESSAGE, DATASOURCE_PAYLOAD, DATASOURCE_MESSAGE
    }

}
