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
