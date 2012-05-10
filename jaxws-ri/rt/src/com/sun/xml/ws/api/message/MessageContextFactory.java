/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2012 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.xml.ws.api.message;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.soap.MimeHeader;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Source;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.soap.MTOMFeature;

import org.jvnet.ws.EnvelopeStyle;
import org.jvnet.ws.EnvelopeStyleFeature;
import org.jvnet.ws.message.MessageContext;

import com.sun.xml.ws.api.SOAPVersion;
import com.sun.xml.ws.api.WSFeatureList;
import com.sun.xml.ws.api.pipe.Codec;
import com.sun.xml.ws.api.pipe.Codecs;
import static com.sun.xml.ws.transport.http.HttpAdapter.fixQuotesAroundSoapAction;

/**
 * The MessageContextFactory implements org.jvnet.ws.message.MessageContextFactory as
 * a factory of Packet and public facade of Codec(s).
 * 
 * @author shih-chang.chen@oracle.com
 */
public class MessageContextFactory extends org.jvnet.ws.message.MessageContextFactory {
    
    private WSFeatureList features;
    private Codec soapCodec;
    private Codec xmlCodec;
    private EnvelopeStyleFeature envelopeStyle;
    private EnvelopeStyle.Style singleSoapStyle;
    
    public MessageContextFactory(WebServiceFeature[] wsf) {
        this(new com.sun.xml.ws.binding.WebServiceFeatureList(wsf));
    }
    
    public MessageContextFactory(WSFeatureList wsf) {
        features = wsf;
        envelopeStyle = features.get(EnvelopeStyleFeature.class);
        if (envelopeStyle == null) {//Default to SOAP11
            envelopeStyle = new EnvelopeStyleFeature(new EnvelopeStyle.Style[]{EnvelopeStyle.Style.SOAP11});
            features.mergeFeatures(new WebServiceFeature[]{envelopeStyle}, false);
        }
        for (EnvelopeStyle.Style s : envelopeStyle.getStyles()) {
            if (s.isXML()) {
                if (xmlCodec == null) xmlCodec = Codecs.createXMLCodec(features); 
            } else {
                if (soapCodec == null) soapCodec = Codecs.createSOAPBindingCodec(features);  
                singleSoapStyle = s;
            }
        }
    }

    protected org.jvnet.ws.message.MessageContextFactory newFactory(WebServiceFeature... f) {
        return new com.sun.xml.ws.api.message.MessageContextFactory(f);
    }


    public MessageContext createContext() {
        return packet(null);
    }

    public MessageContext createContext(SOAPMessage soap) {
        throwIfIllegalMessageArgument(soap);
        return packet(Messages.create(soap));
    }

    public MessageContext createContext(Source m, EnvelopeStyle.Style envelopeStyle) {
        throwIfIllegalMessageArgument(m);
        return packet(Messages.create(m, SOAPVersion.from(envelopeStyle)));
    }

    public MessageContext createContext(Source m) {
        throwIfIllegalMessageArgument(m);
        return packet(Messages.create(m, SOAPVersion.from(singleSoapStyle)));
    }
    
    public MessageContext createContext(InputStream in, String contentType) throws IOException {
        throwIfIllegalMessageArgument(in);
        //TODO when do we use xmlCodec?
        Packet p = packet(null);
        soapCodec.decode(in, contentType, p);
        return p;
    }
    
    public MessageContext createContext(InputStream in, MimeHeaders headers) throws IOException {
        String contentType = getHeader(headers, "Content-Type");
        Packet packet = (Packet) createContext(in, contentType);
        packet.acceptableMimeTypes = getHeader(headers, "Accept");
        packet.soapAction = fixQuotesAroundSoapAction(getHeader(headers, "SOAPAction"));
        packet.put(Packet.INBOUND_TRANSPORT_HEADERS, toMap(headers));
        return packet;
    }
    
    static String getHeader(MimeHeaders headers, String name) {
        String[] values = headers.getHeader(name);
        return (values != null && values.length > 0) ? values[0] : null;
    }
   
    static Map<String, List<String>> toMap(MimeHeaders headers) {
        HashMap<String, List<String>> map = new HashMap<String, List<String>>();
        for (Iterator<MimeHeader> i = headers.getAllHeaders(); i.hasNext();) {
            MimeHeader mh = i.next();
            List<String> values = map.get(mh.getName());
            if (values == null) {
                values = new ArrayList<String>();
                map.put(mh.getName(), values);
            }
            values.add(mh.getValue());
        }       
        return map;
    }
    
    public MessageContext createContext(Message m) {
        throwIfIllegalMessageArgument(m);
        return packet(m);
    }
    
    private Packet packet(Message m) {
        final Packet p = new Packet();
        //TODO when do we use xmlCodec?
        p.codec = soapCodec;
        if (m != null) p.setMessage(m);
        MTOMFeature mf = features.get(MTOMFeature.class);
        if (mf != null) {
            p.setMtomFeature(mf);
        }
        return p;
    }  

    private void throwIfIllegalMessageArgument(Object message)
        throws IllegalArgumentException
    {
        if (message == null) {
            throw new IllegalArgumentException("null messages are not allowed.  Consider using MessageContextFactory.createContext()");
        }
    }

    @Deprecated
    public MessageContext doCreate() {
        return packet(null);
    }
    @Deprecated
    public MessageContext doCreate(SOAPMessage m) {
        return createContext(m);
    }
    @Deprecated
    public MessageContext doCreate(Source x, SOAPVersion soapVersion) {
        return packet(Messages.create(x, soapVersion));
    }
}
