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

package com.sun.xml.ws.encoding;

import org.xml.sax.helpers.AttributesImpl;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;

import com.sun.xml.ws.message.stream.StreamMessage;
import com.sun.xml.ws.encoding.StreamSOAPCodec;
import com.sun.istack.Nullable;
import com.sun.istack.NotNull;

/**
 * Complete infoset about a start tag.
 *
 * <p>
 * This is used between {@link StreamMessage} and {@link StreamSOAPCodec}
 * to capture the infoset of the s:Envelope, s:Header, and s:Body elements.
 *
 *
 * <h3>Design Note</h3>
 * <p>
 * Since StAX and SAX uses different null vs empty string convention, one has
 * to choose which format we store things. It can go either way, but I'm assuming
 * that we'll be using StAX more in JAX-WS, so things are kept in the StAX style
 * in this class.
 *
 * @author Kohsuke Kawaguchi
 */
public final class TagInfoset {
    /**
     * Namespace declarations on this tag. Read-only.
     *
     * URIs/prefixes can be null (StAX-style)
     */
    public final @NotNull String[] ns;
    /**
     * Attributes on this tag. Read-only.
     */
    public final @NotNull AttributesImpl atts;

    /**
     * Prefix of the start tag in stax-style.
     */
    public final @Nullable String prefix;

    /**
     * Namespace URI of the start tag in stax-style.
     */
    public final @Nullable String nsUri;

    /**
     * Local name of the start tag.
     */
    public final @NotNull String localName;

    /**
     * Lazily computed QName (i.e., "foo:bar")
     */
    private @Nullable String qname;

    public TagInfoset(String nsUri, String localName, String prefix, AttributesImpl atts, String... ns) {
        this.nsUri = nsUri;
        this.prefix = prefix;
        this.localName = localName;
        this.atts = atts;
        this.ns = ns;
    }

    /**
     * Fills a {@link TagInfoset} object by the current element
     * that the reader points to.
     */
    public TagInfoset(XMLStreamReader reader) {
        prefix = reader.getPrefix();
        nsUri = reader.getNamespaceURI();
        localName = reader.getLocalName();

        int nsc = reader.getNamespaceCount();
        if(nsc>0) {
            ns = new String[nsc*2];
            for(int i=0; i<nsc; i++){
                ns[i*2  ] = fixNull(reader.getNamespacePrefix(i));
                ns[i*2+1] = fixNull(reader.getNamespaceURI(i));
            }
        } else {
            ns = EMPTY_ARRAY;
        }

        int ac = reader.getAttributeCount();
        if(ac>0) {
            atts = new AttributesImpl();
            StringBuilder sb = new StringBuilder();
            for(int i=0; i< ac;i++){
                sb.setLength(0);
                String prefix = reader.getAttributePrefix(i);
                String localName = reader.getAttributeLocalName(i);

                String qname;
                if(prefix != null && prefix.length()!=0){
                    sb.append(prefix);
                    sb.append(":");
                    sb.append(localName);
                    qname = sb.toString();
                } else {
                    qname = localName;
                }

                atts.addAttribute(
                    fixNull(reader.getAttributeNamespace(i)),
                    localName,
                    qname,
                    reader.getAttributeType(i),
                    reader.getAttributeValue(i));
            }
        } else {
            atts = EMPTY_ATTRIBUTES;
        }
    }

    /**
     * Writes the start element event.
     */
    public void writeStart(ContentHandler contentHandler) throws SAXException {
        for( int i=0; i<ns.length; i+=2 )
            contentHandler.startPrefixMapping(fixNull(ns[i]),fixNull(ns[i+1]));
        contentHandler.startElement(fixNull(nsUri), localName ,getQName(), atts);
    }

    /**
     * Writes the end element event.
     */
    public void writeEnd(ContentHandler contentHandler) throws SAXException{
        contentHandler.endElement(fixNull(nsUri),localName,getQName());
        for( int i=ns.length-2; i>=0; i-=2 ) {
            contentHandler.endPrefixMapping(fixNull(ns[i]));
        }
    }

    /**
     * Writes the start element event.
     */
    public void writeStart(XMLStreamWriter w) throws XMLStreamException {
        // write start tag. Arrrgggghhh!!
        if(prefix==null) {
            if(nsUri==null)
                w.writeStartElement(localName);
            else
                w.writeStartElement(nsUri,localName);
        } else {
            w.writeStartElement(prefix,localName,nsUri);
        }

        for( int i=0; i<ns.length; i+=2 ) {
            w.writeNamespace(ns[i],ns[i+1]);
        }

        for( int i=0; i<atts.getLength(); i++ ) {
            String nsUri = atts.getURI(i);
            if(nsUri==null || nsUri.length() ==0) {
                w.writeAttribute(atts.getLocalName(i),atts.getValue(i));
            } else {
                String rawName = atts.getQName(i);
                String prefix = rawName.substring(0,rawName.indexOf(':'));
                w.writeAttribute(prefix,nsUri,atts.getLocalName(i),atts.getValue(i));
            }
        }
    }

    private String getQName() {
        if(qname!=null) return qname;

        StringBuilder sb = new StringBuilder();
        if(prefix!=null){
            sb.append(prefix);
            sb.append(':');
            sb.append(localName);
            qname = sb.toString();
        } else {
            qname = localName;
        }
        return qname;
    }
    private static String fixNull(String s) {
        if(s==null) return "";
        else        return s;
    }

    private static final String[] EMPTY_ARRAY = new String[0];
    private static final AttributesImpl EMPTY_ATTRIBUTES = new AttributesImpl();
}
