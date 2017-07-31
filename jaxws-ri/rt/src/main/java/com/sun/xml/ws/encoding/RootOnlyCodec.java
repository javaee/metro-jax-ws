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

package com.sun.xml.ws.encoding;

import com.sun.istack.NotNull;
import com.sun.xml.ws.api.message.AttachmentSet;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Codec;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.ReadableByteChannel;

/**
 * {@link Codec} that works only on the root part of the MIME/multipart.
 * It doesn't work on the attachment parts, so it takes {@link AttachmentSet}
 * as an argument and creates a corresponding {@link Message}. This enables
 * attachments to be parsed lazily by wrapping the mimepull parser into an
 * {@link AttachmentSet}
 *
 * @author Jitendra Kotamraju
 */
public interface RootOnlyCodec extends Codec {

    /**
     * Reads root part bytes from {@link InputStream} and constructs a {@link Message}
     * along with the given attachments.
     *
     * @param in root part's data
     *
     * @param contentType root part's MIME content type (like "application/xml")
     *
     * @param packet the new created {@link Message} is set in this packet
     *
     * @param att attachments
     *
     * @throws IOException
     *      if {@link InputStream} throws an exception.
     */
    void decode(@NotNull InputStream in, @NotNull String contentType, @NotNull Packet packet, @NotNull AttachmentSet att)
            throws IOException;

    /**
     *
     * @see #decode(InputStream, String, Packet, AttachmentSet)
     */
    void decode(@NotNull ReadableByteChannel in, @NotNull String contentType, @NotNull Packet packet, @NotNull AttachmentSet att);
}
