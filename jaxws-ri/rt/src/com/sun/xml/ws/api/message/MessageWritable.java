package com.sun.xml.ws.api.message;

import java.io.IOException;
import java.io.OutputStream;

import org.jvnet.ws.message.ContentType;

/**
 * A Message implementation may implement this interface as an alternative way to write the
 * message into the OutputStream.
 * 
 * @author shih-chang.chen@oracle.com
 */
public interface MessageWritable {
    
    /**
     * Gets the Content-type of this message.
     * 
     * @return The MIME content type of this message
     */
    ContentType getContentType();

    /**
     * Writes the XML infoset portion of this MessageContext
     * (from &lt;soap:Envelope> to &lt;/soap:Envelope>).
     *
     * @param out
     *      Must not be null. The caller is responsible for closing the stream,
     *      not the callee.
     *
     * @return
     *      The MIME content type of this message (such as "application/xml").
     *      This information is often ncessary by transport.
     *
     * @throws IOException
     *      if a {@link OutputStream} throws {@link IOException}.
     */
    ContentType writeTo( OutputStream out ) throws IOException;
}
