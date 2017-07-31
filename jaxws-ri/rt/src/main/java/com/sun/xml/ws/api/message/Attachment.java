/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2017 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://oss.oracle.com/licenses/CDDL+GPL-1.1
 * or LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
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
     * http://www.ietf.org/rfc/rfc2392.txt (which is referred by the ws-i attachment profile 
     * http://www.ws-i.org/Profiles/AttachmentsProfile-1.0.html)
     * 
     * content-id    = url-addr-spec
     * url-addr-spec = addr-spec  ; URL encoding of RFC 822 addr-spec
     * cid-url       = "cid" ":" content-id
     * 
     * A "cid" URL is converted to the corresponding Content-ID message header [MIME] by 
     * removing the "cid:" prefix, converting the % encoded character to their equivalent 
     * US-ASCII characters, and enclosing the remaining parts with an angle bracket pair, 
     * "<" and ">".  For  example, "cid:foo4%25foo1@bar.net" corresponds to
     *      Content-ID: <foo4%25foo1@bar.net>
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
