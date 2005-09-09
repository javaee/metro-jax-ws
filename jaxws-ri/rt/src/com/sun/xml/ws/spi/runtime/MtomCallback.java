/**
 * $Id: MtomCallback.java,v 1.1 2005-09-09 22:50:54 vivekp Exp $
 */

/**
 * Copyright 2005 Sun Microsystems, Inc. All rights reserved.
 * SUN PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.sun.xml.ws.spi.runtime;

import javax.activation.DataHandler;

/**
 * MTOM callback class that is passed to InternalSoapEncoder.write() to get notification when
 * mtom attachment is added or the message is xopped.
 */
public interface MtomCallback {
    /**
     * This method provides the content-id that will be set to Content-ID MIME header, the
     * element which will be xop encoded by JAXB.
     *
     * @param contentId
     * @param attachment
     * @param elementTargetNamespace
     * @param elementLocalName
     */
    public void addedMtomAttachment(String contentId, DataHandler attachment, String elementTargetNamespace, String elementLocalName);
}
