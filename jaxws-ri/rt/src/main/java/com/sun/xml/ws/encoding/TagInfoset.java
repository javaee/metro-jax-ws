/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
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
     * This is an array of the even length of the form { prefix0, uri0, prefix1, uri1, ... }.
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
        // write start tag.
        if(prefix==null) {
            if(nsUri==null)
                w.writeStartElement(localName);
            else {
                //fix Null prefix. otherwise throws XMLStreamException,
                // if the namespace URI has not been bound to a prefix
                w.writeStartElement("",localName,nsUri);
            }
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
