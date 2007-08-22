/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://glassfish.dev.java.net/public/CDDL+GPL.html
 * or glassfish/bootstrap/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at glassfish/bootstrap/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
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

import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.message.Attachment;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.pipe.ContentType;
import com.sun.xml.ws.message.stream.StreamAttachment;
import com.sun.xml.ws.message.MimeAttachmentSet;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;
import java.util.Map;

/**
 * {@link Codec} that uses MIME/multipart as the base format.
 *
 * @author Jitendra Kotamraju
 */
public final class SwACodec extends MimeCodec {

    public SwACodec(SOAPVersion version, WSBinding binding, Codec rootCodec) {
        super(version, binding);
        this.rootCodec = rootCodec;
    }

    private SwACodec(SwACodec that) {
        super(that);
        this.rootCodec = that.rootCodec.copy();
    }

    @Override
    protected void decode(MimeMultipartParser mpp, Packet packet) throws IOException {
        // TODO: handle attachments correctly
        Attachment root = mpp.getRootPart();
        if (rootCodec instanceof RootOnlyCodec) {
            ((RootOnlyCodec)rootCodec).decode(root.asInputStream(),root.getContentType(),packet, new MimeAttachmentSet(mpp));
        } else {
            rootCodec.decode(root.asInputStream(),root.getContentType(),packet);
            Map<String, Attachment> atts = mpp.getAttachmentParts();
            for(Map.Entry<String, Attachment> att : atts.entrySet()) {
                packet.getMessage().getAttachments().add(att.getValue());
            }
        }
    }

    public ContentType encode(Packet packet, WritableByteChannel buffer) {
        //TODO: not yet implemented
        throw new UnsupportedOperationException();
    }

    public SwACodec copy() {
        return new SwACodec(this);
    }
}
