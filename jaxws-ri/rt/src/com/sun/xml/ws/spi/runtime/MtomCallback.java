/**
 * $Id: MtomCallback.java,v 1.2 2005-09-10 19:48:00 kohsuke Exp $
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
