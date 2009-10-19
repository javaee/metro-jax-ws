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
import com.sun.xml.ws.api.message.Attachment;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.pipe.ContentType;
import com.sun.xml.ws.developer.StreamingAttachmentFeature;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.activation.DataContentHandler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.util.UUID;

/**
 * {@link Codec}s that uses the MIME multipart as the underlying format.
 *
 * <p>
 * When the runtime needs to dynamically choose a {@link Codec}, and
 * when there are more than one {@link Codec}s that use MIME multipart,
 * it is often impossible to determine the right {@link Codec} unless
 * you parse the multipart message to some extent.
 *
 * <p>
 * By having all such {@link Codec}s extending from this class,
 * the "sniffer" can decode a multipart message partially, and then
 * pass the partial parse result to the ultimately-responsible {@link Codec}.
 * This improves the performance.
 *
 * @author Kohsuke Kawaguchi
 */
abstract class MimeCodec implements Codec {
    
    static {
        // DataHandler.writeTo() may search for DCH. So adding some default ones.
        try {
            CommandMap map = CommandMap.getDefaultCommandMap();
            if (map instanceof MailcapCommandMap) {
                MailcapCommandMap mailMap = (MailcapCommandMap) map;
                String hndlrStr = ";;x-java-content-handler=";
                mailMap.addMailcap(
                    "text/xml" + hndlrStr + XmlDataContentHandler.class.getName());
                mailMap.addMailcap(
                    "application/xml" + hndlrStr + XmlDataContentHandler.class.getName());
                mailMap.addMailcap(
                    "image/*" + hndlrStr + ImageDataContentHandler.class.getName());
                mailMap.addMailcap(
                    "text/plain" + hndlrStr + StringDataContentHandler.class.getName());
            }
        } catch (Throwable t) {
            // ignore the exception.
        }
    }

    public static final String MULTIPART_RELATED_MIME_TYPE = "multipart/related";
    
    private String boundary;
    private String messageContentType;
    private boolean hasAttachments;
    protected Codec rootCodec;
    protected final SOAPVersion version;
    protected final WSBinding binding;

    protected MimeCodec(SOAPVersion version, WSBinding binding) {
        this.version = version;
        this.binding = binding;
    }
    
    public String getMimeType() {
        return MULTIPART_RELATED_MIME_TYPE;
    }

    // TODO: preencode String literals to byte[] so that they don't have to
    // go through char[]->byte[] conversion at runtime.
    public ContentType encode(Packet packet, OutputStream out) throws IOException {
        Message msg = packet.getMessage();
        if (msg == null) {
            return null;
        }

        if (hasAttachments) {
            writeln("--"+boundary, out);
            ContentType ct = rootCodec.getStaticContentType(packet);
            String ctStr = (ct != null) ? ct.getContentType() : rootCodec.getMimeType();
            writeln("Content-Type: " + ctStr, out);
            writeln(out);
        }
        ContentType primaryCt = rootCodec.encode(packet, out);

        if (hasAttachments) {
            writeln(out);
            // Encode all the attchments
            for (Attachment att : msg.getAttachments()) {
                writeln("--"+boundary, out);
                //SAAJ's AttachmentPart.getContentId() returns content id already enclosed with
                //angle brackets. For now put angle bracket only if its not there
                String cid = att.getContentId();
                if(cid != null && cid.length() >0 && cid.charAt(0) != '<')
                    cid = '<' + cid + '>';
                writeln("Content-Id:" + cid, out);
                writeln("Content-Type: " + att.getContentType(), out);
                writeln("Content-Transfer-Encoding: binary", out);
                writeln(out);                    // write \r\n
                att.writeTo(out);
                writeln(out);                    // write \r\n
            }
            writeAsAscii("--"+boundary, out);
            writeAsAscii("--", out);
        }
        // TODO not returing correct multipart/related type(no boundary)
        return hasAttachments ? new ContentTypeImpl(messageContentType, packet.soapAction, null) : primaryCt;
    }
    
    public ContentType getStaticContentType(Packet packet) {
        Message msg = packet.getMessage();
        hasAttachments = !msg.getAttachments().isEmpty();

        if (hasAttachments) {
            boundary = "uuid:" + UUID.randomUUID().toString();
            String boundaryParameter = "boundary=\"" + boundary + "\"";
            // TODO use primaryEncoder to get type
            messageContentType =  MULTIPART_RELATED_MIME_TYPE + 
                    "; type=\"" + rootCodec.getMimeType() + "\"; " +
                    boundaryParameter;
            return new ContentTypeImpl(messageContentType, packet.soapAction, null);
        } else {
            return rootCodec.getStaticContentType(packet);
        }
    }

    /**
     * Copy constructor.
     */
    protected MimeCodec(MimeCodec that) {
        this.version = that.version;
        this.binding = that.binding;
    }

    public void decode(InputStream in, String contentType, Packet packet) throws IOException {
        MimeMultipartParser parser = new MimeMultipartParser(in, contentType, binding.getFeature(StreamingAttachmentFeature.class));
        decode(parser,packet);
    }

    public void decode(ReadableByteChannel in, String contentType, Packet packet) {
        throw new UnsupportedOperationException();
    }

    /**
     * Parses a {@link Packet} from a {@link MimeMultipartParser}.
     */
    protected abstract void decode(MimeMultipartParser mpp, Packet packet) throws IOException;

    public abstract MimeCodec copy();


    public static void writeln(String s,OutputStream out) throws IOException {
        writeAsAscii(s,out);
        writeln(out);
    }

    /**
     * Writes a string as ASCII string.
     */
    public static void writeAsAscii(String s,OutputStream out) throws IOException {
        int len = s.length();
        for( int i=0; i<len; i++ )
            out.write((byte)s.charAt(i));
    }

    public static void writeln(OutputStream out) throws IOException {
        out.write('\r');
        out.write('\n');
    }
}
