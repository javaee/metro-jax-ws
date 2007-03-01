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
package com.sun.xml.ws.api.message;

import com.sun.istack.NotNull;

import javax.activation.DataHandler;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPException;
import javax.xml.transform.Source;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Attachment.
 */
public interface Attachment {

    /**
     * Content ID of the attachment. Uniquely identifies an attachment.
     *
     * @return
     *      The content ID like "foo-bar-zot@abc.com", without
     *      surrounding '&lt;' and '>' used as the transfer syntax.
     */
    @NotNull String getContentId();

    /**
     * Gets the MIME content-type of this attachment.
     */
    String getContentType();

    /**
     * Gets the attachment as an exact-length byte array.
     */
    byte[] asByteArray();

    /**
     * Gets the attachment as a {@link DataHandler}.
     */
    DataHandler asDataHandler();

    /**
     * Gets the attachment as a {@link Source}.
     * Note that there's no guarantee that the attachment is actually an XML.
     */
    Source asSource();

    /**
     * Obtains this attachment as an {@link InputStream}.
     */
    InputStream asInputStream();

    /**
     * Writes the contents of the attachment into the given stream.
     */
    void writeTo(OutputStream os) throws IOException;

    /**
     * Writes this attachment to the given {@link SOAPMessage}.
     */
    void writeTo(SOAPMessage saaj) throws SOAPException;
}
