/*
 * $Id: InternalEncoder.java,v 1.1 2005-05-23 22:28:42 bbissett Exp $
 *
 * Copyright (c) 2005 Sun Microsystems, Inc.
 * All rights reserved.
 */
package com.sun.xml.ws.encoding.internal;
import com.sun.pept.ept.MessageInfo;

/**
 * Payload is converted from one form to the other
 */
public interface InternalEncoder {
    public void toMessageInfo(Object intMessage, MessageInfo mi);
    public Object toInternalMessage(MessageInfo mi);
}
