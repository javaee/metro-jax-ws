/*
 * $Id: ContentNegotiation.java,v 1.1 2005-07-27 13:15:45 spericas Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */

package com.sun.xml.ws.client;

import java.util.Map;

import com.sun.pept.presentation.MessageStruct;

import static com.sun.xml.ws.client.BindingProviderProperties.CONTENT_NEGOTIATION_PROPERTY;

public class ContentNegotiation {
    
    /**
     * Initializes content negotiation property in <code>MessageStruct</code>
     * based on request context and system properties.
     */
    static public void initialize(Map context, MessageStruct messageStruct) {
        String value = (String) context.get(CONTENT_NEGOTIATION_PROPERTY);
        if (value != null) {
            if (value.equals("none") || value.equals("pessimistic")|| value.equals("optimistic")) {
                messageStruct.setMetaData(CONTENT_NEGOTIATION_PROPERTY, value.intern());
            }
            else {
                throw new SenderException("sender.request.illegalValueForContentNegotiation", value);
            }
        }
        else {
            initFromSystemProperties(messageStruct);
        }
    }
    
    /**
     * Initializes content negotiation property in <code>MessageStruct</code>
     * based on system property of the same name.
     */
    static public void initFromSystemProperties(MessageStruct messageStruct)
        throws SenderException 
    {
        String value = System.getProperty(CONTENT_NEGOTIATION_PROPERTY);
        
        if (value == null) {            
            messageStruct.setMetaData(
                CONTENT_NEGOTIATION_PROPERTY, "none");      // FI is off by default
        } 
        else if (value.equals("none") || value.equals("pessimistic") || value.equals("optimistic")) {
            messageStruct.setMetaData(CONTENT_NEGOTIATION_PROPERTY, value.intern());
        } 
        else {
            throw new SenderException("sender.request.illegalValueForContentNegotiation", value);
        }
    }    
}
