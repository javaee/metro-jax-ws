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

package com.sun.xml.ws.api.pipe;

import com.sun.xml.ws.api.BindingID;
import com.sun.xml.ws.api.WSBinding;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.server.EndpointAwareCodec;

import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * Encodes a {@link Message} (its XML infoset and attachments) to a sequence of bytes.
 *
 * <p>
 * This interface provides pluggability for different ways of encoding XML infoset,
 * such as plain XML (plus MIME attachments), XOP, and FastInfoset.
 *
 * <p>
 * Transport usually needs a MIME content type of the encoding, so the {@link Codec}
 * interface is designed to return this information. However, for some encoding
 * (such as XOP), the encoding may actually change based on the actual content of
 * {@link Message}, therefore the codec returns the content type as a result of encoding.
 *
 * <p>
 * {@link Codec} does not produce transport-specific information, such as HTTP headers.
 * 
 * <p>
 * {@link Codec} implementations should be thread-safe; a codec instance could be used
 * concurrently in multiple threads. If a codec have to generate or use a per-request
 * state, the codec implementation must store the state in the Packet instead of using an
 * instance variable of the codec implementation.
 *
 * <p>
 * {@link BindingID} determines the {@link Codec}. See {@link BindingID#createEncoder(WSBinding)}.
 *
 * @author Kohsuke Kawaguchi 
 * @author shih-chang.chen@oracle.com
 * 
 * @see EndpointAwareCodec
 */
public interface Codec {

    /**
     * Get the MIME type associated with this Codec.
     * <p>
     * If available the MIME type will represent the media that the codec 
     * encodes and decodes.
     * 
     * The MIME type returned will be the most general representation independent
     * of an instance of this MIME type utilized as a MIME content-type.
     *
     * @return
     *      null if the MIME type can't be determined by the <code>Codec</code>
     *      implementation. Otherwise the MIME type is returned.
     */
    public String getMimeType();
    
    /**
     * If the MIME content-type of the encoding is known statically
     * then this method returns it.
     *
     * <p>
     * Transports often need to write the content type before it writes
     * the message body, and since the encode method returns the content type
     * after the body is written, it requires a buffering.
     *
     * For those {@link Codec}s that always use a constant content type,
     * This method allows a transport to streamline the write operation.
     *
     * @return
     *      null if the content-type can't be determined in short of
     *      encodin the packet. Otherwise content type for this {@link Packet},
     *      such as "application/xml".
     */
    ContentType getStaticContentType(Packet packet);

    /**
     * Encodes an XML infoset portion of the {@link Message}
     * (from &lt;soap:Envelope> to &lt;/soap:Envelope>).
     *
     * <p>
     * Internally, this method is most likely invoke {@link Message#writeTo(XMLStreamWriter)}
     * to turn the message into infoset.
     *
     * @param packet
     * @param out
     *      Must not be null. The caller is responsible for closing the stream,
     *      not the callee.
     *
     * @return
     *      The MIME content type of the encoded message (such as "application/xml").
     *      This information is often ncessary by transport.
     *
     * @throws IOException
     *      if a {@link OutputStream} throws {@link IOException}.
     */
    ContentType encode( Packet packet, OutputStream out ) throws IOException;

    /**
     * The version of {@link #encode(Packet,OutputStream)}
     * that writes to NIO {@link ByteBuffer}.
     *
     * <p>
     * TODO: for the convenience of implementation, write
     * an adapter that wraps {@link WritableByteChannel} to {@link OutputStream}.
     */
    ContentType encode( Packet packet, WritableByteChannel buffer );

    /*
     * The following methods need to be documented and implemented.
     *
     * Such methods will be used by a client side
     * transport pipe that implements the ClientEdgePipe.
     *
    String encode( InputStreamMessage message, OutputStream out ) throws IOException;
    String encode( InputStreamMessage message, WritableByteChannel buffer );
    */

    /**
     * Creates a copy of this {@link Codec}.
     *
     * <p>
     * Since {@link Codec} instance is not re-entrant, the caller
     * who needs to encode two {@link Message}s simultaneously will
     * want to have two {@link Codec} instances. That's what this
     * method produces.
     *
     * <h3>Implentation Note</h3>
     * <p>
     * Note that this method might be invoked by one thread while
     * another thread is executing one of the {@link #encode} methods.
     * <!-- or otherwise you'd always have to maintain one idle copy -->
     * <!-- just so that you can make copies from -->
     * This should be OK because you'll be only copying things that
     * are thread-safe, and creating new ones for thread-unsafe resources,
     * but please let us know if this contract is difficult.
     *
     * @return
     *      always non-null valid {@link Codec} that performs
     *      the encoding work in the same way --- that is, if you
     *      copy an FI codec, you'll get another FI codec.
     *
     *      <p>
     *      Once copied, two {@link Codec}s may be invoked from
     *      two threads concurrently; therefore, they must not share
     *      any state that requires isolation (such as temporary buffer.)
     *
     *      <p>
     *      If the {@link Codec} implementation is already
     *      re-entrant and multi-thread safe to begin with,
     *      then this method may simply return {@code this}.
     */
    Codec copy();

    /**
     * Reads bytes from {@link InputStream} and constructs a {@link Message}.
     *
     * <p>
     * The design encourages lazy decoding of a {@link Message}, where
     * a {@link Message} is returned even before the whole message is parsed,
     * and additional parsing is done as the {@link Message} body is read along.
     * A {@link Codec} is most likely have its own implementation of {@link Message}
     * for this purpose.
     *
     * @param in
     *      the data to be read into a {@link Message}. The transport would have
     *      read any transport-specific header before it passes an {@link InputStream},
     *      and {@link InputStream} is expected to be read until EOS. Never null.
     *
     *      <p>
     *      Some transports, such as SMTP, may 'encode' data into another format
     *      (such as uuencode, base64, etc.) It is the caller's responsibility to
     *      'decode' these transport-level encoding before it passes data into
     *      {@link Codec}.
     *
     * @param contentType
     *      The MIME content type (like "application/xml") of this byte stream.
     *      Thie text includes all the sub-headers of the content-type header. Therefore,
     *      in more complex case, this could be something like
     *      {@code multipart/related; boundary="--=_outer_boundary"; type="multipart/alternative"}.
     *      This parameter must not be null.
     *
     * @param response
     *      The parsed {@link Message} will be set to this {@link Packet}.
     *      {@link Codec} may add additional properties to this {@link Packet}.
     *      On a successful method completion, a {@link Packet} must contain a
     *      {@link Message}.
     *
     * @throws IOException
     *      if {@link InputStream} throws an exception.
     */
    void decode( InputStream in, String contentType, Packet response ) throws IOException;

    /**
     *
     * @see #decode(InputStream, String, Packet)
     */
    void decode( ReadableByteChannel in, String contentType, Packet response );

    /*
     * The following methods need to be documented and implemented.
     *
     * Such methods will be used by a server side
     * transport pipe that can support the invocation of methods on a
     * ServerEdgePipe.
     *
    XMLStreamReaderMessage decode( InputStream in, String contentType ) throws IOException;
    XMLStreamReaderMessage decode( ReadableByteChannel in, String contentType );
    */
}
