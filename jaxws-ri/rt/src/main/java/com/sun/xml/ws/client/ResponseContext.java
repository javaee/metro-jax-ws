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
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
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

package com.sun.xml.ws.client;

import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.message.AttachmentSet;
import com.sun.xml.ws.api.message.Attachment;

import javax.xml.ws.handler.MessageContext;
import javax.activation.DataHandler;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Implements "response context" on top of {@link Packet}.
 *
 * <p>
 * This class creates a read-only {@link Map} view that
 * gets exposed to client applications after an invocation
 * is complete.
 *
 * <p>
 * The design goal of this class is to make it efficient
 * to create a new {@link ResponseContext}, at the expense
 * of making some {@link Map} operations slower. This is
 * justified because the response context is mostly just
 * used to query a few known values, and operations like
 * enumeration isn't likely.
 *
 * <p>
 * Some of the {@link Map} methods requre this class to
 * build the complete {@link Set} of properties, but we
 * try to avoid that as much as possible.
 *
 *
 * <pre>
 * TODO: are we exposing all strongly-typed fields, or
 * do they have appliation/handler scope notion?
 * </pre>
 *
 * @author Kohsuke Kawaguchi
 */
@SuppressWarnings({"SuspiciousMethodCalls"})    // IDE doesn't like me calling Map methods with key typed as Object
public class ResponseContext extends AbstractMap<String,Object> {
    private final Packet packet;

    /**
     * Lazily computed.
     */
    private Set<Map.Entry<String,Object>> entrySet;

    /**
     * @param packet
     *      The {@link Packet} to wrap.
     */
    public ResponseContext(Packet packet) {
        this.packet = packet;
    }

    public boolean containsKey(Object key) {
        if(packet.supports(key))
            return packet.containsKey(key);    // strongly typed

        if(packet.invocationProperties.containsKey(key))
            // if handler-scope, hide it
            return !packet.getHandlerScopePropertyNames(true).contains(key);

        return false;
    }

    public Object get(Object key) {
        if(packet.supports(key))
            return packet.get(key);    // strongly typed

        if(packet.getHandlerScopePropertyNames(true).contains(key))
            return null;            // no such application-scope property

        Object value =  packet.invocationProperties.get(key);

        //add the attachments from the Message to the corresponding attachment property
        if(key.equals(MessageContext.INBOUND_MESSAGE_ATTACHMENTS)){
            Map<String, DataHandler> atts = (Map<String, DataHandler>) value;
            if(atts == null)
                atts = new HashMap<String, DataHandler>();
            AttachmentSet attSet = packet.getMessage().getAttachments();
            for(Attachment att : attSet){
                atts.put(att.getContentId(), att.asDataHandler());
            }
            return atts;
        }
        return value;
    }

    public Object put(String key, Object value) {
        // response context is read-only
        throw new UnsupportedOperationException();
    }

    public Object remove(Object key) {
        // response context is read-only
        throw new UnsupportedOperationException();
    }

    public void putAll(Map<? extends String, ? extends Object> t) {
        // response context is read-only
        throw new UnsupportedOperationException();
    }

    public void clear() {
        // response context is read-only
        throw new UnsupportedOperationException();
    }

    public Set<Entry<String, Object>> entrySet() {
        if(entrySet==null) {
            // this is where the worst case happens. we have to clone the whole properties
            // to get this view.

            // use TreeSet so that toString() sort them nicely. It's easier for apps.
            Map<String,Object> r = new HashMap<String,Object>();

            // export application-scope properties
            r.putAll(packet.invocationProperties);

            // hide handler-scope properties
            r.keySet().removeAll(packet.getHandlerScopePropertyNames(true));

            // and all strongly typed ones
            r.putAll(packet.createMapView());

            entrySet = Collections.unmodifiableSet(r.entrySet());
        }

        return entrySet;
    }

}
