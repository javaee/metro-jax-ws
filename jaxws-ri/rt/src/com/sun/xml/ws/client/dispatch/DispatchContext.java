/*
 * $Id: DispatchContext.java,v 1.3 2005-08-08 19:13:01 arungupta Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
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
        "com.sun.xml.rpc.rt.client.dispatch.messagetype";
    public static final String DISPATCH_MESSAGE_MODE =
        "com.sun.xml.rpc.rt.client.dispatch.mode";
    public static final String DISPATCH_MESSAGE_CLASS =
        "com.sun.xml.rpc.rt.client.dispatch.messageclass";

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
