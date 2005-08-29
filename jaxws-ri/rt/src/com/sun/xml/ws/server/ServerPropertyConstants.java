/*
 * $Id: ServerPropertyConstants.java,v 1.2 2005-08-29 19:37:31 kohlert Exp $
 */

/*
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.server;

/**
 * @author Vivek Pandey
 *
 * Defines server side constants
 *
 */
public interface ServerPropertyConstants {
    /*public static final String ATTACHMENT_CONTEXT =
        "com.sun.xml.ws.attachment.AttachmentContext";*/
    public static final String SET_ATTACHMENT_PROPERTY =
        "com.sun.xml.ws.attachment.SetAttachmentContext";
    public static final String GET_ATTACHMENT_PROPERTY =
        "com.sun.xml.ws.attachment.GetAttachmentContext";
}
